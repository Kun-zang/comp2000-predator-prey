import java.io.PrintStream;

/**
 * An observer that writes one line of comma-separated values per step, so a run
 * can be opened in a spreadsheet and graphed.
 */
public class CsvPopulationLogger implements SimulationListener {
    private final PrintStream out;

    public CsvPopulationLogger(PrintStream out) {
        this.out = out;
        out.println("step,grass,ripe_grass,rabbits,foxes,mean_rabbit_energy,mean_fox_energy");
    }

    @Override
    public void stepCompleted(int step, Census census) {
        out.printf("%d,%d,%d,%d,%d,%.2f,%.2f%n", step, census.grass(), census.ripeGrass(),
            census.rabbits(), census.foxes(), census.meanRabbitEnergy(), census.meanFoxEnergy());
    }
}
