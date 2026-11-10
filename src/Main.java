import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Starts the predator-prey simulation.
 *
 * <p>Settings can be given on the command line as key=value pairs, for example
 * {@code java Main seed=42 rows=50 columns=70}. See SimulationConfig for the list.</p>
 */
public class Main {
    private static final int CELL_SIZE = 16;

    public static void main(String[] args) {
        SimulationConfig config = readSettings(args);
        System.out.println("Seed " + config.getSeed() + " (pass seed=" + config.getSeed()
            + " to repeat this run)");

        World world = new World(config.getRows(), config.getColumns(), config.getSeed());
        world.populate(config.getGrassChance(), config.getRabbitChance(), config.getFoxChance());

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("COMP2000 Predator-Prey Simulation");
            frame.add(new WorldPanel(world, CELL_SIZE));
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    /**
     * The one place a ConfigurationException is caught. A typo in a setting should
     * not stop the simulation from running, so the problem is reported and the
     * defaults are used instead.
     */
    private static SimulationConfig readSettings(String[] args) {
        try {
            return SimulationConfig.fromArgs(args);
        } catch (ConfigurationException e) {
            System.err.println("Could not use the settings given: " + e.getMessage());
            System.err.println("Starting with the default settings instead.");
            return SimulationConfig.defaults();
        }
    }
}
