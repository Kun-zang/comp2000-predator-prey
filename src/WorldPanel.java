import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;

public class WorldPanel extends JPanel {
    private final int columns;
    private final int rows;
    private final int cellSize;

    public WorldPanel(int columns, int rows, int cellSize) {
        this.columns = columns;
        this.rows = rows;
        this.cellSize = cellSize;
        setPreferredSize(new Dimension(columns * cellSize, rows * cellSize));
        setBackground(new Color(24, 26, 28));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(52, 56, 60));
        for (int col = 0; col <= columns; col++) {
            g.drawLine(col * cellSize, 0, col * cellSize, rows * cellSize);
        }
        for (int row = 0; row <= rows; row++) {
            g.drawLine(0, row * cellSize, columns * cellSize, row * cellSize);
        }
    }
}