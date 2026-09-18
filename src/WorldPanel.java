import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Draws the world and drives the simulation clock.
 */
public class WorldPanel extends JPanel {
    private static final int STEP_DELAY_MS = 120;
    private static final Color BACKGROUND = new Color(24, 26, 28);

    private final World world;
    private final int cellSize;

    public WorldPanel(World world, int cellSize) {
        this.world = world;
        this.cellSize = cellSize;
        setPreferredSize(new Dimension(world.getColumns() * cellSize, world.getRows() * cellSize));
        setBackground(BACKGROUND);

        Timer clock = new Timer(STEP_DELAY_MS, event -> {
            world.step();
            repaint();
        });
        clock.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int row = 0; row < world.getRows(); row++) {
            for (int column = 0; column < world.getColumns(); column++) {
                final int x = column * cellSize;
                final int y = row * cellSize;
                world.entityAt(new Position(row, column)).ifPresent(entity -> {
                    g.setColor(entity.getColor());
                    g.fillRect(x, y, cellSize, cellSize);
                });
            }
        }
        drawStatusLine(g);
    }

    /** A one-line readout of the current populations. */
    private void drawStatusLine(Graphics g) {
        String status = "step " + world.getStepCount()
            + "    grass " + world.count(Grass.class)
            + "    rabbits " + world.count(Rabbit.class)
            + "    foxes " + world.count(Fox.class);
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, getWidth(), 22);
        g.setColor(Color.WHITE);
        g.drawString(status, 8, 16);
    }
}
