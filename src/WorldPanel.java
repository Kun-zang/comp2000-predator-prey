import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.function.ToIntFunction;
import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

/**
 * Draws the world, a live population graph underneath it, and drives the clock.
 *
 * <p>The panel is an observer of the World: it does not ask the world for counts
 * every time it paints. Instead it registers a listener, and when a step completes
 * it keeps the new census and asks Swing to repaint.</p>
 *
 * <p>Keys: space pauses and resumes; the right arrow advances one step while paused.</p>
 */
public class WorldPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final int STEP_DELAY_MS = 120;
    private static final int GRAPH_HEIGHT = 150;
    private static final int STATUS_HEIGHT = 22;
    private static final Color BACKGROUND = new Color(24, 26, 28);
    private static final Color GRAPH_BACKGROUND = new Color(16, 17, 19);
    private static final Color GRASS_LINE = new Color(70, 150, 80);
    private static final Color RABBIT_LINE = new Color(228, 228, 238);
    private static final Color FOX_LINE = new Color(216, 106, 46);

    private final transient World world;
    private final transient PopulationHistory history;
    private final transient ExtinctionWatcher extinctions;
    private final int cellSize;
    private final Timer clock;
    private transient Census latest;

    public WorldPanel(World world, int cellSize) {
        this.world = world;
        this.cellSize = cellSize;
        this.latest = world.census();
        this.history = new PopulationHistory(world.getColumns() * cellSize / 2);
        this.extinctions = new ExtinctionWatcher();

        world.addListener(history);
        world.addListener(extinctions);
        world.addListener((step, census) -> {
            latest = census;
            repaint();
        });

        setPreferredSize(new Dimension(world.getColumns() * cellSize,
            world.getRows() * cellSize + GRAPH_HEIGHT));
        setBackground(BACKGROUND);

        clock = new Timer(STEP_DELAY_MS, event -> world.step());
        clock.start();
        bindKeys();
    }

    private void bindKeys() {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("SPACE"), "pause");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("RIGHT"), "step");
        getActionMap().put("pause", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent event) {
                if (clock.isRunning()) {
                    clock.stop();
                } else {
                    clock.start();
                }
                repaint();
            }
        });
        getActionMap().put("step", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent event) {
                if (!clock.isRunning()) {
                    world.step();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawCells(g);
        drawStatusLine(g);
        drawGraph((Graphics2D) g, world.getRows() * cellSize);
    }

    private void drawCells(Graphics g) {
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
    }

    /** A one-line readout of the current populations, from the latest census. */
    private void drawStatusLine(Graphics g) {
        String status = "step " + world.getStepCount()
            + "    grass " + latest.grass()
            + "    rabbits " + latest.rabbits()
            + "    foxes " + latest.foxes()
            + (clock.isRunning() ? "" : "    [paused: space to resume, → to step]")
            + "    " + extinctions.summary();
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, getWidth(), STATUS_HEIGHT);
        g.setColor(Color.WHITE);
        g.drawString(status, 8, 16);
    }

    /**
     * Rabbits and foxes share one vertical scale so they can be compared directly.
     * Grass is far more numerous, so it is drawn faintly on its own scale behind them.
     */
    private void drawGraph(Graphics2D g, int top) {
        g.setColor(GRAPH_BACKGROUND);
        g.fillRect(0, top, getWidth(), GRAPH_HEIGHT);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        List<Census> points = history.snapshot();
        int animalScale = Math.max(1, Math.max(history.peak(Census::rabbits), history.peak(Census::foxes)));
        int grassScale = Math.max(1, history.peak(Census::grass));

        plot(g, points, Census::grass, grassScale, top, GRASS_LINE, 1f);
        plot(g, points, Census::rabbits, animalScale, top, RABBIT_LINE, 2f);
        plot(g, points, Census::foxes, animalScale, top, FOX_LINE, 2f);

        g.setColor(Color.LIGHT_GRAY);
        g.drawString("rabbits", 8, top + 16);
        g.setColor(FOX_LINE);
        g.drawString("foxes", 62, top + 16);
        g.setColor(GRASS_LINE);
        g.drawString("grass (own scale)", 104, top + 16);
    }

    private void plot(Graphics2D g, List<Census> points, ToIntFunction<Census> measure,
                      int scale, int top, Color colour, float width) {
        if (points.size() < 2) {
            return;
        }
        double xStep = (double) getWidth() / (history.capacity() - 1);
        int usableHeight = GRAPH_HEIGHT - 28;
        int bottom = top + GRAPH_HEIGHT - 6;
        g.setColor(colour);
        g.setStroke(new BasicStroke(width));
        for (int i = 1; i < points.size(); i++) {
            int x1 = (int) ((i - 1) * xStep);
            int x2 = (int) (i * xStep);
            int y1 = bottom - measure.applyAsInt(points.get(i - 1)) * usableHeight / scale;
            int y2 = bottom - measure.applyAsInt(points.get(i)) * usableHeight / scale;
            g.drawLine(x1, y1, x2, y2);
        }
    }
}
