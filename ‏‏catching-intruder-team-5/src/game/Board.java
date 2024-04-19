package game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Timer;

@SuppressWarnings("serial")
// Responsible for visualization/animation of transitions
public class Board extends JPanel {
	int targets_counter = 0;
	int last_target = -1;
	int animation_steps = 0;
	Timer timer;
	ControlPanel cp;

	// The robots current, start, and target locations for a single transition
	// - used for animation
	Point[] robots_graphics;
	Point[] start_graphics;
	Point[] fixedObstacle_graphics;
	Point intruder_graphic;

	BufferedImage buffer;
	BufferedImage[] robots_images;
	BufferedImage[] base_robot_images;

	BufferedImage origin_image;
	BufferedImage fixedObstacle_image;
	BufferedImage intruder_image;

	public Board(ControlPanel cp) {
		super();
		this.cp = cp;
		robots_graphics = new Point[cp.num_robots];
		start_graphics = new Point[cp.num_robots];
//		target_graphics = new Point[cp.num_robots];
//		targets_graphics = new Point[cp.num_targets];
		fixedObstacle_graphics = new Point[4];
		intruder_graphic=new Point();

		robots_images = new BufferedImage[cp.num_robots];
		base_robot_images = new BufferedImage[cp.num_robots];
//		target_images = new BufferedImage[cp.num_targets];

	}

	public void init() throws Exception {
		for (int i = 0; i < cp.num_robots; i++) {
			start_graphics[i] = new Point();
			robots_graphics[i] = new Point();
		}

		// TODO: Here is a loading of some relevant photos, you may rename the variables
		// according to your needs.

		// Load images for different elements
		for (int i = 0; i < cp.num_robots; i++) {
			base_robot_images[i] = ImageIO.read(new File("img/Robot" + String.valueOf(i) + ".png"));
			robots_images[i] = ImageIO.read(new File("img/Robot" + String.valueOf(i) + ".png"));
		}

		fixedObstacle_image = ImageIO.read(new File("img/fixedObstacle.png"));
		intruder_image = ImageIO.read(new File("img/intruder.png"));

		fixedObstacle_graphics[0] = new Point(2 * cp.dim, 1 * cp.dim);
		fixedObstacle_graphics[1] = new Point(3 * cp.dim, 1 * cp.dim);
		fixedObstacle_graphics[2] = new Point(4 * cp.dim, 1 * cp.dim);
		fixedObstacle_graphics[3] = new Point(5 * cp.dim, 1 * cp.dim);
		
		intruder_graphic = new Point(cp.movingIntruder.getX() * cp.dim, cp.movingIntruder.getY() * cp.dim);
	}

	// Animate a transition (from robots_prev to robots)
	public void animate() throws Exception {

		for (int i = 0; i < cp.num_robots; i++) {
			robots_graphics[i].setX(cp.robots_prev[i].getX() * cp.dim);
			robots_graphics[i].setY(cp.robots_prev[i].getY() * cp.dim);
			start_graphics[i].setX(cp.robots_prev[i].getX() * cp.dim);
			start_graphics[i].setY(cp.robots_prev[i].getY() * cp.dim);
		}

		// TODO: updtae the location of other elements in a similar way
		intruder_graphic.setX(cp.movingIntruder.getX()*cp.dim);
		intruder_graphic.setY(cp.movingIntruder.getY()*cp.dim);

		// Each tick of the timer advances the animation
		timer = new Timer(16, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int num_steps = 20;
				if (animation_steps > num_steps)
				// Animation ended
				{
					timer.stop();
					animation_steps = 0;
					cp.ready_for_next = true;
					if (cp.autorun) {
						try {
							cp.next();
						} catch (Exception ex) {
							System.out.println(ex);
						}
					} else {
						cp.advance_button.setText("Next step");
					}
					return;
				}

				animation_steps++;
				// Redraw
				updateBuffer();
				repaint();
			}
		});

		timer.start();
	}

	@Override
	public void invalidate() {
		buffer = null;
		// updateBuffer();
		super.invalidate();
	}

	// Use buffering for smooth animations
	protected void updateBuffer() {
		if (getWidth() > 0 && getHeight() > 0) {

			if (buffer == null) {

				buffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);

			}

			Graphics2D g2d = buffer.createGraphics();
			g2d.clearRect(0, 0, cp.x * cp.dim + 8, cp.y * cp.dim + 8);
			g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
					RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
			g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

			int row;
			int col;
			// Draw background
			for (row = 0; row < cp.y; row++) {
				for (col = 0; col < cp.x; col++) {
					if ((row % 2) == (col % 2))
						g2d.setColor(Color.WHITE);
					else
						g2d.setColor(Color.WHITE);

					g2d.fillRect(col * cp.dim, row * cp.dim, cp.dim, cp.dim);
				}
			}

			g2d.setColor(Color.WHITE);

			// Draw robots
			for (int i = 0; i < cp.num_robots; i++) {
				g2d.drawImage(robots_images[i], robots_graphics[i].getX(), robots_graphics[i].getY(), null);
			}

			for (int i = 0; i < 4; i++) {
				g2d.drawImage(fixedObstacle_image, fixedObstacle_graphics[i].getX(), fixedObstacle_graphics[i].getY(),
						null);
			}

			// TODO: You can draw here other objects as well.
			g2d.drawImage(intruder_image,intruder_graphic.getX(),intruder_graphic.getY(), null);
			// draw origin
			g2d.drawImage(origin_image, 0, 0, null);
		}

	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		if (buffer != null) {
			g2d.drawImage(buffer, 0, 0, this);
		}
	}

}
