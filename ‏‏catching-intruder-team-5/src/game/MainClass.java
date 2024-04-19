package game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

public class MainClass {
	public static void main(String args[]) throws Exception {

		// TODO: we provide here some optional code for generating **some** of the
		// initial env elements, please modify and complete according to your needs
		// You may create more elements and pass them to the control panel.

		int x = 8;
		int y = 8;
		int num_robots = 2;
		int iRand=new Random().nextInt(0,8);
		Point movingIntruder=new Point(iRand,7);
		
		ControlPanel cp;
		String path = "out/jit";

		System.out.println("Running the system");
		cp = new ControlPanel(x, y, num_robots,movingIntruder,path);
		cp.init();

	}
}
