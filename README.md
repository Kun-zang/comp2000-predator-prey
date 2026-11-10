# comp2000-predator-prey

Grid-based predator-prey ecosystem simulation in Java (Swing). COMP2000 semester project.

Grass grows and spreads, rabbits eat ripe grass, foxes hunt rabbits. Every animal
ages, burns energy each step, breeds when well fed, and dies of hunger or old age.
A live graph under the grid shows the populations rising and falling.

## Running

Uses only the standard JDK (Java 17 or later).

```
javac -d out src/*.java
java -cp out Main
```

Keys: **space** pauses/resumes, **→** steps once while paused.

### Settings

Settings are `key=value` pairs on the command line. Anything left out keeps its default.

| key | default | meaning |
|---|---|---|
| `rows`, `columns` | 30, 40 | grid size |
| `seed` | random | repeat a run exactly by passing its seed |
| `grass`, `rabbits`, `foxes` | 0.35, 0.052, 0.012 | chance each cell starts with that entity |
| `rabbitmove` | `cautious` | `random`, `graze` (head for ripe grass) or `cautious` (flee foxes, else graze) |
| `foxmove` | `stalk` | `random` or `stalk` (head for the nearest visible rabbit) |
| `steps` | 0 | run this many steps with no window and print a CSV of the populations |

Example: `java -cp out Main seed=42 rabbitmove=graze steps=2000 > run.csv`

A bad setting is reported and the defaults are used instead.

### Comparing behaviours

`BehaviourExperiment` runs seeds 1..N without a window for one pairing of behaviours
(runs are spread across CPU cores with a parallel stream):

```
java -cp out BehaviourExperiment cautious stalk 30 2000
java -cp out BehaviourExperiment random stalk 30 2000
```

`RaceConditionDemo` shows the race condition that `Census.collector()` avoids: counting on a
parallel stream with a shared `int` loses counts, while `AtomicInteger`, `synchronized` and the
collector are always right (`java -cp out RaceConditionDemo`).

`ParallelBenchmark` times the census sequentially and in parallel on grids of several sizes,
which is what the census's parallel threshold is based on (`java -cp out ParallelBenchmark`).
Add `sequential` after a `BehaviourExperiment` run to time it without parallelism, e.g.
`java -cp out BehaviourExperiment cautious stalk 30 2000 sequential`.

## Design

* **Entity hierarchy**: `Entity` → `Grass`, `Animal` → `Rabbit`, `Fox`.
* **Generics**: `Grid<T extends Entity>` with `Optional<T>` lookups.
* **Strategy pattern**: `MovementStrategy` (functional interface), concrete strategies are
  lambdas made by `Movements`, combined with `orElse`. Each `Animal` holds one.
* **Observer pattern**: `World` notifies `SimulationListener`s after each step with a
  `Census`. Observers: `WorldPanel`, `PopulationHistory`, `ExtinctionWatcher`,
  `CsvPopulationLogger`.
* **Parallelism**: `Census.collector()` runs on a parallel stream for large grids; `BehaviourExperiment` runs seeds in parallel.
* **Custom collector and groupingBy**: `Census.collector()` and `World.energyBySpecies()` (`groupingBy` + `summarizingDouble`).
* **Exceptions**: checked `ConfigurationException` from `SimulationConfig.fromArgs`, caught in `Main`; unchecked `InvalidPositionException` from `Grid.put`.
