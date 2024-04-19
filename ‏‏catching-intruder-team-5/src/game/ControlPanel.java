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

	Point[] robots;
	Point[] fixedObstacles;
	Point obstaclePoint1;
	Point obstaclePoint2;
	Point movingIntruder;

//	Point elevator;
	int state = 0;

	int stays = 0;

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
	boolean shouldJump;

	// The controller and its inputs
	ControllerExecutor executor;
	Map<String, String> inputs = new HashMap<String, String>();

	// The path to the controller files
	String path;

	public ControlPanel(int x, int y, int num_robots,Point movingIntruder, String path) {
		this.x = x;
		this.y = y;
		this.num_robots = num_robots;
		this.robots = new Point[num_robots];
		this.robots_prev = new Point[num_robots];
		this.fixedObstacles = new Point[4];
		this.path = path;
		this.shouldJump = false;
		this.obstaclePoint1=new Point(2,1);
		this.obstaclePoint2= new Point(5,1);
		this.movingIntruder=movingIntruder;
	}

	public void init() throws Exception {
		autorun = false;

		for (int i = 0; i < num_robots; i++) {
			robots[i] = new Point();
			robots_prev[i] = new Point();
		}

		// init controller
		executor = new ControllerExecutor(new BasicJitController(), this.path, "CatchingIntruder");

		// TODO: initial input values using inputs.put(...)
		inputs.put("obstaclePoint1X", Integer.toString(this.obstaclePoint1.getX()));
		inputs.put("obstaclePoint1Y", Integer.toString(this.obstaclePoint1.getY()));
		inputs.put("obstaclePoint2X", Integer.toString(this.obstaclePoint2.getX()));
		inputs.put("obstaclePoint2Y", Integer.toString(this.obstaclePoint2.getY()));

		// set initial robot locations
		// TODO: you may initial other things in a similar way

		this.fixedObstacles[0] = new Point(2, 1);
		this.fixedObstacles[1] = new Point(3, 1);
		this.fixedObstacles[2] = new Point(4, 1);
		this.fixedObstacles[3] = new Point(5, 1);
		
		
		executor.initState(inputs);

		Map<String, String> sysValues = executor.getCurrOutputs();

		for (int i = 0; i < num_robots; i++) {
			robots_prev[i].setX(Integer.parseInt(sysValues.get("robotX" + Integer.toString(i))));
			robots_prev[i].setY(Integer.parseInt(sysValues.get("robotY" + Integer.toString(i))));
			robots[i].setX(Integer.parseInt(sysValues.get("robotX" + Integer.toString(i))));
			robots[i].setY(Integer.parseInt(sysValues.get("robotY" + Integer.toString(i))));
		}

		setUpUI();

		System.out.println("1");
	}

	boolean isElementOnObstacle(int x, int y) {
		return (x == 2 && y >= 2 && y <= 5);
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

		inputs.put("movingIntruderX", Integer.toString(this.movingIntruder.getX()));
		inputs.put("movingIntruderY", Integer.toString(this.movingIntruder.getY()));
		System.out.println(this.movingIntruder.getY());
		executor.updateState(inputs);
		if(this.stays>0 && this.stays<4) {
			this.stays+=1;
		}
		else {
			this.stays=0;
			int left=1;
			int right=1; 
			int up=1;
			int down=1;
			for (int i = 0; i < num_robots; i++) {
				if(robots[i].getX()==movingIntruder.getX()-1) {
					left=0;
				}
				else if(robots[i].getX()==movingIntruder.getX()+1) {
					right=0;
				}
				else if(robots[i].getY()==movingIntruder.getY()-1) {
					down=0;
				}
				else if(robots[i].getY()==movingIntruder.getY()+1) {
					up=0;
				}
			}
			int rand=new Random().nextInt(0,4);
			if(rand==0) {
				if(left==1) {
					int lrand=new Random().nextInt(0,this.movingIntruder.getX()+1);
					this.movingIntruder.setX(this.movingIntruder.getX()-lrand);
				}
				else {
					this.stays+=1;
			}
			}
				
			if(rand==1) {
				if(right==1) {
					int rrand=new Random().nextInt(0,this.x-this.movingIntruder.getX());
					this.movingIntruder.setX(this.movingIntruder.getX()+rrand);
				}
				else {
					this.stays+=1;
				}
				
			}
			if(rand==2) {
				if(up==1) {
					int urand=new Random().nextInt(0,this.movingIntruder.getY()+1);
				    this.movingIntruder.setY(this.movingIntruder.getY() - urand);
				}
				else {
					this.stays+=1;
				}
			}
			if(rand==3) {
				if(down==1) {
					int drand=new Random().nextInt(0,this.y-this.movingIntruder.getY());
				    this.movingIntruder.setY(this.movingIntruder.getY() + drand);
				}
				else {
					this.stays+=1;
			}
				
				
			}
			
		}

		// Receive updated values from the controller
		Map<String, String> sysValues = executor.getCurrOutputs();

		System.out.println(sysValues);

		// Update robot locations
		for (int i = 0; i < num_robots; i++) {
			robots[i].setX(Integer.parseInt(sysValues.get("robotX" + Integer.toString(i))));
			robots[i].setY(Integer.parseInt(sysValues.get("robotY" + Integer.toString(i))));
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
