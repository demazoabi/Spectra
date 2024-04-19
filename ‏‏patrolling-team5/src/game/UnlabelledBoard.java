package game;

import java.io.File;

import javax.imageio.ImageIO;

@SuppressWarnings("serial")
public class UnlabelledBoard extends Board {
	public UnlabelledBoard(ControlPanel cp) {
		super(cp);
	}

	@Override
	public void init() throws Exception {
		for (int i = 0; i < cp.num_robots; i++) {
			start_graphics[i] = new Point();
			target_graphics[i] = new Point();
			robots_graphics[i] = new Point();
		}

		obstacle_image = ImageIO.read(new File("img/Obstacle.png"));
		movingObstacle_image=ImageIO.read(new File("img/robot2.png"));

		// Unlabelled: Use the same image for all robots/goals
		for (int i = 0; i < cp.num_robots; i++) {
			base_robot_images[i] = ImageIO.read(new File("img/Robot" + String.valueOf(1) + ".png"));
			robots_images[i] = ImageIO.read(new File("img/Robot" + String.valueOf(1) + ".png"));
			goals_images[i] = ImageIO.read(new File("img/Goal" + String.valueOf(0) + ".png"));
		}
	}
}
