package game;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

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
	int guardX;
	int guardY;
	int left=0;
	int right=0;
	int up=0;
	int down=0;

	Point[] robots;
	Point[] fixedObstacles;
	Point[] fixedTargets;
	Point[] movingGuard;
	Point guardPoint1;
	Point guardPoint2;
	
	int state = 0;

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

	public ControlPanel(int x, int y, int num_robots, int num_targets,int num_obstacles,int guard_x,int guard_y,
		Point guardPoint1, Point guardPoint2, Point[] targets, Point[] movingGuard,String path) {
		this.x = x;
		this.y = y;
		this.num_robots = num_robots;
		this.num_targets = num_targets;
		this.num_obstacles=num_obstacles;
		this.guardX=guard_x;
		this.guardY=guard_y;
		this.robots = new Point[num_robots];
		this.robots_prev = new Point[num_robots];
		this.fixedObstacles = new Point[num_obstacles];
		this.path = path;
		this.fixedTargets=targets;
		this.movingGuard=movingGuard;
		this.guardPoint1=guardPoint1;
		this.guardPoint2=guardPoint2;
	}

	public void init() throws Exception {
		autorun = false;

		for (int i = 0; i < num_robots; i++) {
			robots[i] = new Point();
			robots_prev[i] = new Point();
		}

		// init controller
		executor = new ControllerExecutor(new BasicJitController(), this.path, "EscapingGuard");

		// TODO: initial input values using inputs.put(...)
		for (int i = 0; i < num_targets; i++) {
			inputs.put("T" +Integer.toString(i+1) +"X", Integer.toString(this.fixedTargets[i].getX()));
			inputs.put("T"+ Integer.toString(i+1) +"Y", Integer.toString(this.fixedTargets[i].getY()));
		}

		// set initial robot locations
		// TODO: you may initial other things in a similar way

		this.fixedObstacles[0] = new Point(2, 2);
		this.fixedObstacles[1] = new Point(2, 3);
		this.fixedObstacles[2] = new Point(2, 4);
		this.fixedObstacles[3] = new Point(2, 5);
		this.fixedObstacles[4]= new Point(6, 2);
		this.fixedObstacles[5]= new Point(6, 3);
		
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

		for (int i = 0; i < num_robots; i++) {
			robots_prev[i].setX(Integer.parseInt(sysValues.get("robotX")));
			robots_prev[i].setY(Integer.parseInt(sysValues.get("robotY")));
			robots[i].setX(Integer.parseInt(sysValues.get("robotX")));
			robots[i].setY(Integer.parseInt(sysValues.get("robotY")));
		}
		setUpUI();
	}
	private boolean non_legal(Point p) {
		for(Point obst:fixedObstacles) {
			if(p.equals(obst)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean guardCoversRobot(Point p1,Point p2) {
		for (int i=0;i< num_robots;i++) {
			if(robots[i].getX()>= p1.getX() && robots[i].getX()<=p2.getX() && robots[i].getY()>= p1.getY() 
				&& robots[i].getY()<= p2.getY()) {
				return true;
			}
		}
		return false;
	}

	// handle next turn
	void next() throws Exception {
//		shouldGuardMove = !shouldGuardMove;
		ready_for_next = false;
		state += 1;
		advance_button.setText("...");
		for (int i = 0; i < num_robots; i++) {
			robots_prev[i].setX(robots[i].getX());
			robots_prev[i].setY(robots[i].getY());
		}
		inputs.put("guardPoint1X", Integer.toString(this.guardPoint1.getX()));
		inputs.put("guardPoint1Y", Integer.toString(this.guardPoint1.getY()));
		inputs.put("guardPoint2X" , Integer.toString(this.guardPoint2.getX()));
		inputs.put("guardPoint2Y" , Integer.toString(this.guardPoint2.getY()));

		executor.updateState(inputs);
		
		if(state%3==0) {
			int lRand=0;
			int rRand=0;
			int dRand=0;
			int uRand=0;
			for(int i=0;i<num_robots;i++) {
				if(robots[i].getX()<this.guardPoint1.getX()) {
					lRand=new Random().nextInt(0,this.guardPoint1.getX()+1);
					left=1;
				}
				if(robots[i].getX()>this.guardPoint2.getX()) {
					rRand=new Random().nextInt(0,this.x-this.guardPoint2.getX());
					right=1;
				}
				if(robots[i].getY()>this.guardPoint2.getY()) {
					dRand=new Random().nextInt(0,this.y-this.guardPoint2.getY());
					down=1;
				}
				if(robots[i].getY()<this.guardPoint1.getY()){
					uRand=new Random().nextInt(0,this.guardPoint1.getY()+1);
					up=1;
				}
			}
			Point exGuard1=new Point(this.guardPoint1.getX(),this.guardPoint1.getY());
			Point exGuard2=new Point(this.guardPoint2.getX(),this.guardPoint2.getY());;
			if(left==1) {
				if(down==1) {
					this.guardPoint1.setX(this.guardPoint1.getX()-lRand);
					this.guardPoint1.setY(this.guardPoint1.getY()+dRand);
					this.guardPoint2.setX(this.guardPoint2.getX()-lRand);
					this.guardPoint2.setY(this.guardPoint2.getY()+dRand);
					while(guardCoversRobot(guardPoint1,guardPoint2)) {
						lRand=new Random().nextInt(0,exGuard1.getX()+1);
						dRand=new Random().nextInt(0,this.y-exGuard2.getY());
						this.guardPoint1.setX(exGuard1.getX()-lRand);
						this.guardPoint1.setY(exGuard1.getY()+dRand);
						this.guardPoint2.setX(exGuard2.getX()-lRand);
						this.guardPoint2.setY(exGuard2.getY()+dRand);
						}
					
					}
				else if(up==1) {
					this.guardPoint1.setX(this.guardPoint1.getX()-lRand);
					this.guardPoint1.setY(this.guardPoint1.getY()-uRand);
					this.guardPoint2.setX(this.guardPoint2.getX()-lRand);
					this.guardPoint2.setY(this.guardPoint2.getY()-uRand);
					while(guardCoversRobot(guardPoint1,guardPoint2)) {
						lRand=new Random().nextInt(0,exGuard1.getX()+1);
						uRand=new Random().nextInt(0,exGuard1.getY()+1);
						this.guardPoint1.setX(exGuard1.getX()-lRand);
						this.guardPoint1.setY(exGuard1.getY()-uRand);
						this.guardPoint2.setX(exGuard2.getX()-lRand);
						this.guardPoint2.setY(exGuard2.getY()-uRand);
						}
						
					}
				else {
					this.guardPoint1.setX(this.guardPoint1.getX()-lRand);
					this.guardPoint2.setX(this.guardPoint2.getX()-lRand);
					while(guardCoversRobot(guardPoint1,guardPoint2)) {
						lRand=new Random().nextInt(0,exGuard1.getX()+1);
						this.guardPoint1.setX(exGuard1.getX()-lRand);
						this.guardPoint2.setX(exGuard2.getX()-lRand);
						}
					}
			}
			else if(right==1) {
				if(down==1) {
					this.guardPoint1.setX(this.guardPoint1.getX()+rRand);
					this.guardPoint1.setY(this.guardPoint1.getY()+dRand);
					this.guardPoint2.setX(this.guardPoint2.getX()+rRand);
					this.guardPoint2.setY(this.guardPoint2.getY()+dRand);
					while(guardCoversRobot(guardPoint1,guardPoint2)) {
						rRand=new Random().nextInt(0,this.x-exGuard2.getX());
						dRand=new Random().nextInt(0,this.y-exGuard2.getY());
						this.guardPoint1.setX(exGuard1.getX()+rRand);
						this.guardPoint1.setY(exGuard1.getY()+dRand);
						this.guardPoint2.setX(exGuard2.getX()+rRand);
						this.guardPoint2.setY(exGuard2.getY()+dRand);
						}	
					}
				else if(up==1) {
					this.guardPoint1.setX(this.guardPoint1.getX()+rRand);
					this.guardPoint1.setY(this.guardPoint1.getY()-uRand);
					this.guardPoint2.setX(this.guardPoint2.getX()+rRand);
					this.guardPoint2.setY(this.guardPoint2.getY()-uRand);
					while(guardCoversRobot(guardPoint1,guardPoint2)) {
						rRand=new Random().nextInt(0,this.x-exGuard2.getX());
						uRand=new Random().nextInt(0,exGuard1.getY()+1);
						this.guardPoint1.setX(exGuard1.getX()+rRand);
						this.guardPoint1.setY(exGuard1.getY()-uRand);
						this.guardPoint2.setX(exGuard2.getX()+rRand);
						this.guardPoint2.setY(exGuard2.getY()-uRand);
						}	
					}
				else {
					this.guardPoint1.setX(this.guardPoint1.getX()+rRand);
					this.guardPoint2.setX(this.guardPoint2.getX()+rRand);
					while(guardCoversRobot(guardPoint1,guardPoint2)) {
						rRand=new Random().nextInt(0,this.x-exGuard2.getX());
						this.guardPoint1.setX(exGuard1.getX()+rRand);
						this.guardPoint2.setX(exGuard2.getX()+rRand);
						}	
					}
				}
			else {
				if(down==1) {
					this.guardPoint1.setY(this.guardPoint1.getY()+dRand);
					this.guardPoint2.setY(this.guardPoint2.getY()+dRand);
					while(guardCoversRobot(guardPoint1,guardPoint2)) {
						dRand=new Random().nextInt(0,this.y-exGuard2.getY());
					    this.guardPoint1.setY(exGuard1.getY()+dRand);
					    this.guardPoint2.setY(exGuard2.getY()+dRand);
						}	
						
					}
				else if(up==1) {
					this.guardPoint1.setY(this.guardPoint1.getY()-uRand);
					this.guardPoint2.setY(this.guardPoint2.getY()-uRand);
					while(guardCoversRobot(guardPoint1,guardPoint2)) {
						uRand=new Random().nextInt(0,exGuard1.getY()+1);
						this.guardPoint1.setY(exGuard1.getY()-uRand);
						this.guardPoint2.setY(exGuard2.getY()-uRand);
						}
						
					}
				}
			
			left=0;
			right=0;
			down=0;
			up=0;
			for(int i=0;i<this.guardX;i++) {
				this.movingGuard[i].setX(this.guardPoint1.getX()+i);
				this.movingGuard[i].setY(this.guardPoint1.getY());
			}
			for(int i=(this.guardX*this.guardY)-1;i>=this.guardX;i--) {
				this.movingGuard[i].setX(this.guardPoint2.getX()-(this.guardX*this.guardY-i-1));
				this.movingGuard[i].setY(this.guardPoint2.getY());
			}
			
			
		}
		else {
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
			// Update robot locations
			for (int i = 0; i < num_robots; i++) {
				robots[i].setX(Integer.parseInt(sysValues.get("robotX")));
				robots[i].setY(Integer.parseInt(sysValues.get("robotY")));
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
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);
		board.setVisible(true);
		advance_button.setVisible(true);
		autorun_button.setVisible(true);
		ready_for_next = true;
	}
}
