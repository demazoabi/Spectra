package game;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;

import tau.smlab.syntech.controller.executor.ControllerExecutor;
import tau.smlab.syntech.games.controller.jits.BasicJitController;

/*
 * Manages the simulation - GUI, controller input/output, board (visualization)
 */

public class ControlPanel {
	// board dimensions
	int x;
	int y;
	// board constants
	final int dim = 130;
	static final int y_offset = 30;

	int num_robots;
	int num_targets;
	int num_obstacles;
	Point[] robots;
	Point[] obstacles;
	Point[] goals;
	boolean[] isTargetBlocked;
	int state = 0;
	boolean engine_problem;
	int[] blockStates;
	Point movingObstacle;
	boolean[] notFirstBlock;
	int randInd=0;
	// holds the robots previous position (for use when animating transitions)
	Point[] robots_prev = new Point[num_robots];

	// Board and GUI elements
	JFrame frame;
	Board board;
	JButton advance_button;
	JButton autorun_button;

	// holds states for the animation
	boolean ready_for_next;
	boolean autorun;

	// The controller and its inputs
	ControllerExecutor executor;
	Map<String, String> inputs = new HashMap<String, String>();

	// The path to the controller files
	String path;

	public ControlPanel(int x, int y, int num_robots, int num_targets,boolean[] blocked,int[] blockStates, Point[] goals,
			Point[] obstacles,Point movingObstacle,String path) {
		this.x = x;
		this.y = y;
		this.num_robots = num_robots;
		this.num_targets = num_targets;
		this.num_obstacles = obstacles.length;
		this.robots = new Point[num_robots];
		this.robots_prev = new Point[num_robots];
		this.obstacles = obstacles;
		this.goals = goals;
		this.engine_problem = false;
		this.path = path;
		this.isTargetBlocked =blocked;
		this.blockStates=blockStates;
		this.movingObstacle=movingObstacle;
		this.notFirstBlock=new boolean[num_targets];
	}
	public void init() throws Exception {
		autorun = false;

		for (int i = 0; i < num_robots; i++) {
			robots[i] = new Point();
			robots_prev[i] = new Point();
		}

		// init controller
		executor = new ControllerExecutor(new BasicJitController(), this.path, "Patrolling");
		
		executor.initState(inputs);

		Map<String, String> sysValues = executor.getCurrOutputs();
		Point rob_point=new Point(Integer.parseInt(sysValues.get("robotX")),Integer.parseInt(sysValues.get("robotY")));
		while(non_legal(rob_point)) {
			executor.updateState(inputs);
			Map<String, String> sysValues2 = executor.getCurrOutputs();
			rob_point=new Point(Integer.parseInt(sysValues2.get("robotX")),Integer.parseInt(sysValues2.get("robotY")));
			if(!(non_legal(rob_point))) {
				sysValues=sysValues2;
			}
		}

		// set initial robot locations
		//TODO: you may initial other things in a similar way
		for (int i = 0; i < num_robots; i++) {
			robots_prev[i].setX(Integer.parseInt(sysValues.get("robotX")));
			robots_prev[i].setY(Integer.parseInt(sysValues.get("robotY")));
			robots[i].setX(Integer.parseInt(sysValues.get("robotX")));
			robots[i].setY(Integer.parseInt(sysValues.get("robotY")));
		}
		setUpUI();
	}
	
	private boolean non_legal(Point p) {
		for(Point obst:obstacles) {
			if(p.equals(obst)) {
				return true;
			}
		}
		return false;
	}

	// handle next turn
	void next() throws Exception {
		ready_for_next = false;
		state += 1;
		advance_button.setText("...");
		for (int i = 0; i < num_robots; i++) {
			robots_prev[i].setX(robots[i].getX());
			robots_prev[i].setY(robots[i].getY());
		}
		for(int j=0;j<this.num_targets;j++) {
			int TBlocked=new Random().nextInt(0,2);
			if(TBlocked==1 && this.notFirstBlock[j]==false) {
				this.notFirstBlock[j]=true;
				this.isTargetBlocked[j]=true;
				int rstate=new Random().nextInt(1,20);
				this.blockStates[j]=rstate;
			}
		}

		//TODO: put here your ramdom env inputs
		for (int i = 0; i < num_targets; i++) {
			inputs.put("goal" +Integer.toString(i+1) +"X", Integer.toString(goals[i].getX()));
			inputs.put("goal"+ Integer.toString(i+1) +"Y", Integer.toString(goals[i].getY()));
			inputs.put("Target"+Integer.toString(i+1)+"Blocked",String.valueOf(isTargetBlocked[i]));
		}
		//put moving obstacle locations
		this.movingObstacle.x=(movingObstacle.getX()+randInd)%this.x;
		inputs.put("movingObstacleX",Integer.toString(movingObstacle.getX()));
		inputs.put("movingObstacleY",Integer.toString(movingObstacle.getY()));
		
		//put engine problem value
		int engineProblemInd=new Random().nextInt(0,this.x);
		if((state%this.x)==engineProblemInd) {
			this.engine_problem=true;
		}
		else {
			this.engine_problem=false;
		}
		inputs.put("engineProblem",String.valueOf(this.engine_problem));
		
		executor.updateState(inputs);
		// Receive updated values from the controller
		Map<String, String> sysValues = executor.getCurrOutputs();
		Point rob_point=new Point(Integer.parseInt(sysValues.get("robotX")),Integer.parseInt(sysValues.get("robotY")));
		while(non_legal(rob_point)) {
			executor.updateState(inputs);
			Map<String, String> sysValues2 = executor.getCurrOutputs();
			rob_point=new Point(Integer.parseInt(sysValues2.get("robotX")),Integer.parseInt(sysValues2.get("robotY")));
			if(!(non_legal(rob_point))) {
				sysValues=sysValues2;
			}
		}
		for(int j=0;j<this.num_targets;j++) {
			if(this.isTargetBlocked[j]==true) {
				if( this.blockStates[j]==0) {
					this.isTargetBlocked[j]=false;
				}
				else {
					this.blockStates[j]-=1;
				}	
		}
		}
		
		// Update robot locations
		for (int i = 0; i < num_robots; i++) {
			robots[i].setX(Integer.parseInt(sysValues.get("robotX")));
			robots[i].setY(Integer.parseInt(sysValues.get("robotY")));
		}
		//moving obstacle random move
		this.randInd=new Random().nextInt(0,3);
		if(randInd==2) {
			if(this.movingObstacle.getX()==0) {
				this.randInd=new Random().nextInt(0,2);
			}
			else {
				this.randInd=-1+this.x;
		}
		}
		if(this.movingObstacle.getX()==this.x && this.randInd==1) {
			int randInd2=new Random().nextInt(0,2);
			if(randInd2==0) {
				this.randInd-=1;
			}
			else {
				this.randInd=-1+this.x;
			}
		}
		
		// Animate transition
		board.animate();
	}
	void setUpUI() throws Exception {
		advance_button = new JButton();
		autorun_button = new JButton();
		frame = new JFrame();
		frame.add(advance_button);
		frame.add(autorun_button);
		board = new Board(this);
		board.init();
		frame.setTitle("Robots");
		frame.setSize(x * dim + 8 + 150, y * dim + y_offset + 8);
		board.setSize(x * dim, y * dim);
		frame.setLayout(null);
		frame.add(board, BorderLayout.CENTER);

		// Handle presses of the "next step" button
		advance_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					if (ready_for_next && !autorun)
						next();
				} catch (Exception e) {
					System.out.println(e);
				}
			}
		});
		advance_button.setBounds(x * dim + 8, 0, 130, 50);
		advance_button.setText("Start");

		// Handle presses of the "autorun/stop autorun" button
		autorun_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					if (autorun) {
						autorun = false;
						autorun_button.setText("Auto run");
					} else if (ready_for_next) {
						autorun = true;
						autorun_button.setText("Stop Auto run");
						next();
					}
				} catch (Exception e) {
					System.out.println(e);
				}
			}
		});
		autorun_button.setBounds(x * dim + 8, 50, 130, 50);
		autorun_button.setText("Auto run");
		frame.setVisible(true);
		board.setVisible(true);
		advance_button.setVisible(true);
		autorun_button.setVisible(true);
		ready_for_next = true;
	}
}
