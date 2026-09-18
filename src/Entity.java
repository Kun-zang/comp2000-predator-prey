import java.awt.Color;

/**
 * Anything that can occupy a cell of the world.
 */
public abstract class Entity {
    private Position position;
    private boolean alive = true;

    protected Entity(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

    protected void setPosition(Position position) {
        this.position = position;
    }

    public boolean isAlive() {
        return alive;
    }

    protected void die() {
        alive = false;
    }

    /** Take one turn. Each subclass decides what that means. */
    public abstract void act(World world);

    /** The colour this entity is drawn in. */
    public abstract Color getColor();
}
