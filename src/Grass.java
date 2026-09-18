import java.awt.Color;

/**
 * Food for rabbits. Does not move. Ripens over time and spreads into empty cells
 * once mature. Only mature grass is worth eating.
 */
public class Grass extends Entity {
    private static final int MATURE_AT = 6;
    private static final double SPREAD_CHANCE = 0.08;

    private int maturity = 0;

    public Grass(Position position) {
        super(position);
    }

    public Grass(Position position, int maturity) {
        super(position);
        this.maturity = maturity;
    }

    public boolean isMature() {
        return maturity >= MATURE_AT;
    }

    @Override
    public void act(World world) {
        if (!isMature()) {
            maturity++;
            return;
        }
        if (world.getRandom().nextDouble() < SPREAD_CHANCE) {
            for (Position candidate : world.neighboursOf(getPosition())) {
                if (world.getEntityAt(candidate) == null) {
                    world.place(new Grass(candidate));
                    return;
                }
            }
        }
    }

    @Override
    public Color getColor() {
        int green = 60 + (maturity * 110 / MATURE_AT);
        return new Color(25, Math.min(green, 175), 40);
    }
}
