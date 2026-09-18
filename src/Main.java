import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Starts the predator-prey simulation.
 */
public class Main {
    private static final int ROWS = 30;
    private static final int COLUMNS = 40;
    private static final int CELL_SIZE = 16;

    public static void main(String[] args) {
        World world = new World(ROWS, COLUMNS);
        world.populate(0.35, 0.052, 0.012);

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("COMP2000 Predator-Prey Simulation");
            frame.add(new WorldPanel(world, CELL_SIZE));
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
