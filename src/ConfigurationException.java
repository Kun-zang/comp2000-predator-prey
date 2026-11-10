/**
 * Thrown when the settings passed to the simulation cannot be used.
 *
 * <p>This is a checked exception on purpose. A bad setting is not a bug in the
 * program: it is something the person running it typed, and it can always be
 * recovered from by falling back to the defaults. Making it checked forces
 * whoever reads the settings (Main) to decide what to do about it, instead of
 * the simulation crashing halfway through start-up.</p>
 */
public class ConfigurationException extends Exception {
    private static final long serialVersionUID = 1L;

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
