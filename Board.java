import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.event.MouseInputListener;

public class Board extends JComponent implements MouseInputListener, ComponentListener {
	private static final long serialVersionUID = 1L;
	private Point[][] points;
	private int size = 10;
	public int editType = 0;

	public int iterations = 0;
	public int cars = 0;

	public static boolean periodic = false;
	public static float spawn = 0.3f;
	public static float disappear = 1f;
	public static int lanes = 3;

	public Board(int length, int height) {
		addMouseListener(this);
		addComponentListener(this);
		addMouseMotionListener(this);
		setBackground(Color.WHITE);
		setOpaque(true);
	}

	public void iteration() {
		for (int y = 0; y < lanes; ++y) {
			if (!periodic && Math.random() < spawn)
				points[0][y].empty = false;

			if (!periodic && Math.random() < spawn)
				points[points.length - 1][lanes + 1 + y].empty = false;
		}

		for (int y = 0; y < lanes; ++y)
			for (int x = 0; x < points.length; ++x) {
				points[x][y].changeLanes();
				points[points.length - 1 - x][y + lanes + 1].changeLanes();
			}

		for (int y = 0; y < lanes; ++y)
			for (int x = 0; x < points.length; ++x) {
				points[x][y].updateVelocity();
				points[points.length - 1 - x][y + lanes + 1].updateVelocity();
			}

		for (int y = 0; y < lanes; ++y)
			for (int x = 0; x < points.length - 1; ++x) {
				if (!points[x][y].empty)
					points[x][y].move();

				if (!points[points.length - 1 - x][y + lanes + 1].empty)
					points[points.length - 1 - x][y + lanes + 1].move();
			}

		for (int y = 0; y < lanes; ++y) {
			if (!periodic && Math.random() < disappear) {
				if (!points[points.length - 1][y].empty) {
					cars++;
					points[points.length - 1][y].clear();
				}
			} else if (!points[points.length - 1][y].empty)
				points[points.length - 1][y].move();

			if (!periodic && Math.random() < disappear) {
				if (!points[0][y + lanes + 1].empty) {
					cars++;
					points[0][y + lanes + 1].clear();
				}
			} else if (!points[points.length - 1][y + lanes + 1].empty)
				points[0][y + lanes + 1].move();
		}

		if (cars > 0)
			iterations++;

		float average = (float) cars / iterations;
		System.out.println("Iterations: " + iterations + " Cars: " + cars + " Average: " + average);

		this.repaint();
	}

	public void clear() {
		for (int x = 0; x < points.length; ++x)
			for (int y = 0; y < points[x].length; ++y) {
				points[x][y].clear();
			}
		this.repaint();
	}

	private void initialize(int length, int height) {
		points = new Point[length][height];

		for (int x = 0; x < points.length; ++x)
			for (int y = 0; y < points[x].length; ++y)
				points[x][y] = new Point();

		for (int x = 0; x < points.length; ++x)
			for (int y = 0; y < lanes; ++y) {
				points[x][y].next = points[(x + 1) % points.length][y];
				points[x][y].prev = points[(x + points.length - 1) % points.length][y];

				points[x][y + lanes + 1].prev = points[(x + 1) % points.length][y + lanes + 1];
				points[x][y + lanes + 1].next = points[(x + points.length - 1) % points.length][y + lanes + 1];

				if (y > 0) {
					points[x][y].down = points[x][y - 1];
					points[x][y + lanes + 1].up = points[x][y + lanes];
				}

				if (y < lanes - 1) {
					points[x][y].up = points[x][y + 1];
					points[x][y + lanes + 1].down = points[x][y + lanes + 2];
				}

			}
	}

	protected void paintComponent(Graphics g) {
		if (isOpaque()) {
			g.setColor(getBackground());
			g.fillRect(0, 0, this.getWidth(), this.getHeight());
		}
		g.setColor(Color.GRAY);
		drawNetting(g, size);
	}

	private void drawNetting(Graphics g, int gridSpace) {
		Insets insets = getInsets();
		int firstX = insets.left;
		int firstY = insets.top;
		int lastX = this.getWidth() - insets.right;
		int lastY = this.getHeight() - insets.bottom;

		int x = firstX;
		while (x < lastX) {
			g.drawLine(x, firstY, x, lastY);
			x += gridSpace;
		}

		int y = firstY;
		while (y < lastY) {
			g.drawLine(firstX, y, lastX, y);
			y += gridSpace;
		}

		for (x = 0; x < points.length; ++x) {
			for (y = 0; y < points[x].length; ++y) {
				// if(points[x][y].type==0){
				// float change = points[x][y].getPressure();
				// if (change > 0.5) {
				// change = 0.5f;
				// }
				// if (change < -0.5f) {
				// change = -0.5f;
				// }
				// float a = 0.5f + change;
				if (points[x][y].empty)
					g.setColor(new Color(1f, 1f, 1f));
				else {
					float red = 0.3f * (float) points[x][y].lastSwitch();
					if (red > 1f)
						red = 1f;

					g.setColor(new Color(1 - red, 0, 0));
				}

				// }
				/*
				 * else if (points[x][y].type==1){
				 * g.setColor(new Color(1.0f, 0.0f, 0.0f, 0.7f));
				 * }
				 * else if (points[x][y].type==2){
				 * g.setColor(new Color(0.0f, 1.0f, 0.0f, 0.7f));
				 * }
				 */
				g.fillRect((x * size) + 1, (y * size) + 1, (size - 1), (size - 1));
			}
		}

	}

	public void mouseClicked(MouseEvent e) {
		int x = e.getX() / size;
		int y = e.getY() / size;
		if ((x < points.length) && (x >= 0) && (y < points[x].length) && (y >= 0)) {
			// if (editType == 0) {
			points[x][y].clicked();
			// } else {
			// points[x][y].type= editType;
			// }
			this.repaint();
		}
	}

	public void componentResized(ComponentEvent e) {
		int dlugosc = (this.getWidth() / size) + 1;
		int wysokosc = (this.getHeight() / size) + 1;
		initialize(dlugosc, wysokosc);
	}

	public void mouseDragged(MouseEvent e) {
		int x = e.getX() / size;
		int y = e.getY() / size;
		if ((x < points.length) && (x > 0) && (y < points[x].length) && (y > 0)) {
			// if (editType == 0) {
			points[x][y].clicked();
			// } else {
			// points[x][y].type= editType;
			// }
			this.repaint();
		}
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void componentShown(ComponentEvent e) {
	}

	public void componentMoved(ComponentEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
	}

	public void componentHidden(ComponentEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

}
