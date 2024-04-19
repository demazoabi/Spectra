package game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

public class MainClass {
	public static void main(String args[]) throws Exception {
		
		//TODO: we provide here some optional code for generating **some** of the 
		// initial env elements, please modify and complete according to your needs
		// You may create more elements and pass them to the control panel.
		
		int x = 5;
		int y = 5;
		int num_robots = 1;
		int num_targets = 3;
		int legal_points_num = 19;
		int num_obstacles = 5;
		boolean[] blocked=new boolean[num_targets];
		int[] blockStates=new int[num_targets];
		Point movingObstacle=new Point();
		
		Point[] obstacles = new Point[num_obstacles];
		obstacles[0] = new Point(1, 1);
		obstacles[1] = new Point(3, 3);
		obstacles[2] = new Point(2, 1);
		obstacles[3] = new Point(0, 3);
		obstacles[4] = new Point(3, 4);
		
		Point[] movingObstacles=new Point[x];
		for (int i=0;i<x;i++) {
			movingObstacles[i] = new Point(i, 2);
		}
		int rind=new Random().nextInt(0,x);
		movingObstacle=movingObstacles[rind];
		
		Point[] legalPoints = new Point[legal_points_num];
		int c=0;
		for(int i=0;i<5;i++) {
			int ind=c;
			for(int j=0;j<5;j++) {
				Point p=new Point(i, j); 
				int legal=0;
				for(Point obst:obstacles) {
					if(p.equals(obst)) {
						legal=1;
						break;
					}
					if(p.equals(new Point(4,0))) {
						legal=1;
						break;
					}
				}
				if(legal==0) {
					c++;
					legalPoints[ind+j]=p;
				}
				else {
					ind=ind-1;
				}
			}
		}
			
		Point[] goals = new Point[num_targets];
		Random rand = new Random();
		int upperbound = 19;
		int first = 0;
		int seconed = 0;
		for (int j = 0; j < num_targets; j++) {
			int index = rand.nextInt(upperbound);
			if (j == 0) {
				first = index;
			}
			else if (j == 1) {
				while (index == first) {
					index = rand.nextInt(upperbound);
				}
				seconed = index;
			}
			else if (j == 2) {
				while (index == first || index == seconed) {
					index = rand.nextInt(upperbound);
				}
			}
			goals[j] = legalPoints[index];
		}

		ControlPanel cp;
		String path = "out/jit";

		System.out.println("Running the system");
		cp = new ControlPanel(x, y, num_robots, num_targets,blocked,blockStates,goals,obstacles,movingObstacle,path);
		cp.init();

	}
}
