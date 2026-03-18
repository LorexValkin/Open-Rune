import java.awt.*;

final class RSFrame extends Frame {

    private static final long serialVersionUID = 1L;
    private final RSApplet rsApplet;

    /**
     * Standard constructor — fixed size window.
     */
    public RSFrame(RSApplet applet, int width, int height) {
        this(applet, width, height, false, false);
    }

    /**
     * Full constructor — supports undecorated and resizable modes.
     */
    public RSFrame(RSApplet applet, int width, int height, boolean undecorated, boolean resizable) {
        rsApplet = applet;
        setTitle("OpenRune");
        setUndecorated(undecorated);
        setBackground(Color.BLACK);
        setVisible(true);
        requestFocus();
        toFront();
        setResizable(resizable);
        setFocusTraversalKeysEnabled(false);

        Insets insets = getInsets();
        setSize(width + insets.left + insets.right, height + insets.top + insets.bottom);
        setLocationRelativeTo(null);
    }

    /**
     * Returns the usable frame width (excluding window decorations).
     */
    public int getFrameWidth() {
        Insets insets = getInsets();
        return getWidth() - (insets.left + insets.right);
    }

    /**
     * Returns the usable frame height (excluding window decorations).
     */
    public int getFrameHeight() {
        Insets insets = getInsets();
        return getHeight() - (insets.top + insets.bottom);
    }

    @Override
    public Graphics getGraphics() {
        Graphics g = super.getGraphics();
        if (g != null) {
            Insets insets = getInsets();
            g.translate(insets.left, insets.top);
        }
        return g;
    }

    @Override
    public void update(Graphics g) {
        rsApplet.update(g);
    }

    @Override
    public void paint(Graphics g) {
        rsApplet.paint(g);
    }
}