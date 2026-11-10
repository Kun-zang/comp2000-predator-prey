import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Starts the predator-prey simulation.
 *
 * <p>Settings can be given on the command line as key=value pairs, for example
 * {@code java Main seed=42 rows=50 columns=70 rabbitmove=graze}. See
 * SimulationConfig for the list. With {@code steps=N} the simulation runs N steps
 * without a window and prints the populations as CSV.</p>
 */
public class Main {
    private static final int CELL_SIZE = 16;

    public static void main(String[] args) {
        SimulationConfig config = readSettings(args);
        System.err.println("Seed " + config.getSeed() + " (pass seed=" + config.getSeed()
            + " to repeat this run), rabbits " + config.getRabbitMove()
            + ", foxes " + config.getFoxMove());

        World world = new World(config.getRows(), config.getColumns(), config.getSeed());
        world.populate(config.getGrassChance(), config.getRabbitChance(), config.getFoxChance(),
            config.rabbitMovement(), config.foxMovement());

        if (config.getHeadlessSteps() > 0) {
            runWithoutWindow(world, config.getHeadlessSteps());
        } else {
            SwingUtilities.invokeLater(() -> openWindow(world));
        }
    }

    private static void openWindow(World world) {
        JFrame frame = new JFrame("COMP2000 Predator-Prey Simulation");
        frame.add(new WorldPanel(world, CELL_SIZE));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /** The same World, observed by a CSV logger instead of a panel. */
    private static void runWithoutWindow(World world, int steps) {
        ExtinctionWatcher extinctions = new ExtinctionWatcher();
        world.addListener(new CsvPopulationLogger(System.out));
        world.addListener(extinctions);
        for (int i = 0; i < steps; i++) {
            world.step();
        }
        System.err.println(extinctions.anyExtinct()
            ? extinctions.summary()
            : "Both species survived all " + steps + " steps.");
        world.energyBySpecies().forEach((species, energy) -> System.err.printf(
            "%s: %d alive, energy lowest %.0f, average %.1f, highest %.0f%n",
            species, energy.getCount(), energy.getMin(), energy.getAverage(), energy.getMax()));
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
