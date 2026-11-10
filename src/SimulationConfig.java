import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * The settings for one run of the simulation, read from command-line arguments
 * written as key=value pairs, for example:
 *
 * <pre>java Main rows=60 columns=80 seed=42 foxes=0.02</pre>
 *
 * Anything not given keeps its default value. Every setting is checked here, once,
 * so the rest of the program can trust the values it is handed.
 */
public final class SimulationConfig {
    private static final List<String> KNOWN_KEYS =
        List.of("rows", "columns", "seed", "grass", "rabbits", "foxes");

    private final int rows;
    private final int columns;
    private final long seed;
    private final double grassChance;
    private final double rabbitChance;
    private final double foxChance;

    private SimulationConfig(int rows, int columns, long seed,
                             double grassChance, double rabbitChance, double foxChance) {
        this.rows = rows;
        this.columns = columns;
        this.seed = seed;
        this.grassChance = grassChance;
        this.rabbitChance = rabbitChance;
        this.foxChance = foxChance;
    }

    /** The settings used when nothing is given, with a fresh random seed. */
    public static SimulationConfig defaults() {
        return new SimulationConfig(30, 40, new Random().nextLong(), 0.35, 0.052, 0.012);
    }

    /**
     * Reads settings from the command line.
     *
     * @throws ConfigurationException if an argument is malformed, unknown, not a
     *         number, or outside its allowed range
     */
    public static SimulationConfig fromArgs(String[] args) throws ConfigurationException {
        Map<String, String> settings = parse(args);
        SimulationConfig fallback = defaults();

        int rows = intSetting(settings, "rows", fallback.rows, 5, 300);
        int columns = intSetting(settings, "columns", fallback.columns, 5, 400);
        long seed = longSetting(settings, "seed", fallback.seed);
        double grass = chanceSetting(settings, "grass", fallback.grassChance);
        double rabbits = chanceSetting(settings, "rabbits", fallback.rabbitChance);
        double foxes = chanceSetting(settings, "foxes", fallback.foxChance);

        if (grass + rabbits + foxes > 1.0) {
            throw new ConfigurationException(
                "grass + rabbits + foxes must not add up to more than 1.0, but they add up to "
                + (grass + rabbits + foxes));
        }
        return new SimulationConfig(rows, columns, seed, grass, rabbits, foxes);
    }

    private static Map<String, String> parse(String[] args) throws ConfigurationException {
        Map<String, String> settings = new HashMap<>();
        for (String argument : args) {
            String[] parts = argument.split("=", 2);
            if (parts.length != 2 || parts[0].isBlank()) {
                throw new ConfigurationException("Expected key=value but got \"" + argument + "\"");
            }
            String key = parts[0].strip().toLowerCase();
            if (!KNOWN_KEYS.contains(key)) {
                throw new ConfigurationException(
                    "Unknown setting \"" + key + "\". Known settings are " + KNOWN_KEYS);
            }
            settings.put(key, parts[1].strip());
        }
        return settings;
    }

    private static int intSetting(Map<String, String> settings, String key, int fallback,
                                  int minimum, int maximum) throws ConfigurationException {
        String raw = settings.get(key);
        if (raw == null) {
            return fallback;
        }
        int value;
        try {
            value = Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            // Translate the library's unchecked exception into our own checked one,
            // keeping the original as the cause so no detail is lost.
            throw new ConfigurationException(key + " must be a whole number, not \"" + raw + "\"", e);
        }
        if (value < minimum || value > maximum) {
            throw new ConfigurationException(
                key + " must be between " + minimum + " and " + maximum + ", not " + value);
        }
        return value;
    }

    private static long longSetting(Map<String, String> settings, String key, long fallback)
            throws ConfigurationException {
        String raw = settings.get(key);
        if (raw == null) {
            return fallback;
        }
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            throw new ConfigurationException(key + " must be a whole number, not \"" + raw + "\"", e);
        }
    }

    private static double chanceSetting(Map<String, String> settings, String key, double fallback)
            throws ConfigurationException {
        String raw = settings.get(key);
        if (raw == null) {
            return fallback;
        }
        double value;
        try {
            value = Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            throw new ConfigurationException(key + " must be a number, not \"" + raw + "\"", e);
        }
        if (value < 0.0 || value > 1.0) {
            throw new ConfigurationException(key + " must be between 0.0 and 1.0, not " + value);
        }
        return value;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public long getSeed() {
        return seed;
    }

    public double getGrassChance() {
        return grassChance;
    }

    public double getRabbitChance() {
        return rabbitChance;
    }

    public double getFoxChance() {
        return foxChance;
    }
}
