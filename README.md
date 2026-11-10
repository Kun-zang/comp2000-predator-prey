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

## Design

* **Entity hierarchy**: `Entity` → `Grass`, `Animal` → `Rabbit`, `Fox`.
* **Generics**: `Grid<T extends Entity>` with `Optional<T>` lookups.
* **Strategy pattern**: `MovementStrategy` (functional interface), concrete strategies are
  lambdas made by `Movements`, combined with `orElse`. Each `Animal` holds one.
* **Observer pattern**: `World` notifies `SimulationListener`s after each step with a
  `Census`. Observers: `WorldPanel`, `PopulationHistory`, `ExtinctionWatcher`,
  `CsvPopulationLogger`.
* **Custom collector**: `Census.collector()`, run on a parallel stream for large grids.
* **Checked exception**: `ConfigurationException` from `SimulationConfig.fromArgs`, caught in `Main`.
