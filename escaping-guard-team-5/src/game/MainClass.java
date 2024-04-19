package game;

public class MainClass {
	public static void main(String args[]) throws Exception {

		// TODO: we provide here some optional code for generating **some** of the
		// initial env elements, please modify and complete according to your needs
		// You may create more elements and pass them to the control panel.

		int x = 8;
		int y = 8;
		int num_robots = 1;
		int num_targets = 3;
		int num_obstacles=6;
		int guard_x=2;
		int guard_y=2;
		Point[] targets=new Point[num_targets];
		targets[0]= new Point(0,0);
		targets[1]= new Point(0,7);
		targets[2]= new Point(7,7);
		Point guardPoint1=new Point(0,0);
		Point guardPoint2=new Point(1,1);
		Point[] movingGuard=new Point[guard_x*guard_y];
		movingGuard[0]=new Point(0,0);
		movingGuard[1]=new Point(1,0);
		movingGuard[2]=new Point(0,1);
		movingGuard[3]=new Point(1,1);
		
		ControlPanel cp;
		String path = "out/jit";

		System.out.println("Running the system");
		cp = new ControlPanel(x, y, num_robots, num_targets,num_obstacles,guard_x,guard_y,guardPoint1,guardPoint2,targets,movingGuard, path);
		cp.init();

	}
}
