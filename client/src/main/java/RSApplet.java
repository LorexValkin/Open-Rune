import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.*;

public class RSApplet extends JPanel implements Runnable, MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, FocusListener, WindowListener {

    // --- Frame creation ---

    final void createClientFrame(int height, int width) {
        myWidth = width;
        myHeight = height;
        gameFrame = new RSFrame(this, myWidth, myHeight);
        gameFrame.addWindowListener(this);
        graphics = getGameComponent().getGraphics();
        fullGameScreen = new RSImageProducer(myWidth, myHeight, getGameComponent());
        startRunnable(this, 1);
    }

    final void initClientFrame(int height, int width) {
        myWidth = width;
        myHeight = height;
        graphics = getGameComponent().getGraphics();
        fullGameScreen = new RSImageProducer(myWidth, myHeight, getGameComponent());
        startRunnable(this, 1);
    }

    /**
     * Rebuilds the game frame for mode switching (fixed/resizable/fullscreen).
     */
    public void rebuildFrame(boolean undecorated, int width, int height, boolean resizable, boolean modeSwitch) {
        myWidth = width;
        myHeight = height;
        if (gameFrame != null) {
            gameFrame.dispose();
        }
        gameFrame = new RSFrame(this, width, height, undecorated, resizable);
        gameFrame.addWindowListener(this);
        graphics = gameFrame.getGraphics();
        getGameComponent().addMouseWheelListener(this);
        getGameComponent().addMouseListener(this);
        getGameComponent().addMouseMotionListener(this);
        getGameComponent().addKeyListener(this);
        getGameComponent().addFocusListener(this);
    }

    // --- Game loop ---

    public void run() {
        getGameComponent().addMouseListener(this);
        getGameComponent().addMouseMotionListener(this);
        getGameComponent().addMouseWheelListener(this);
        getGameComponent().addKeyListener(this);
        getGameComponent().addFocusListener(this);
        getGameComponent().setFocusable(true);
        getGameComponent().requestFocusInWindow();
        getGameComponent().requestFocus();
        if (gameFrame != null)
            gameFrame.addWindowListener(this);
        drawLoadingText(0, "Loading...");
        startUp();
        int i = 0;
        int j = 256;
        int k = 1;
        int i1 = 0;
        int j1 = 0;
        for (int k1 = 0; k1 < 10; k1++)
            aLongArray7[k1] = System.currentTimeMillis();

        while (targetFps >= 0) {
            if (targetFps > 0) {
                targetFps--;
                if (targetFps == 0) {
                    exit();
                    return;
                }
            }
            int i2 = j;
            int j2 = k;
            j = 300;
            k = 1;
            long l1 = System.currentTimeMillis();
            if (aLongArray7[i] == 0L) {
                j = i2;
                k = j2;
            } else if (l1 > aLongArray7[i])
                j = (int) ((long) (2560 * delayTime) / (l1 - aLongArray7[i]));
            if (j < 25)
                j = 25;
            if (j > 256) {
                j = 256;
                k = (int) ((long) delayTime - (l1 - aLongArray7[i]) / 10L);
            }
            if (k > delayTime)
                k = delayTime;
            aLongArray7[i] = l1;
            i = (i + 1) % 10;
            if (k > 1) {
                for (int k2 = 0; k2 < 10; k2++)
                    if (aLongArray7[k2] != 0L)
                        aLongArray7[k2] += k;
            }
            if (k < minDelay)
                k = minDelay;
            try {
                Thread.sleep(k);
            } catch (InterruptedException _ex) {
                j1++;
            }
            for (; i1 < 256; i1 += j) {
                clickMode3 = clickMode1;
                saveClickX = clickX;
                saveClickY = clickY;
                aLong29 = clickTime;
                clickMode1 = 0;
                processGameLoop();
                readIndex = writeIndex;
            }

            i1 &= 0xff;
            if (delayTime > 0)
                fps = (1000 * j) / (delayTime * 256);
            processDrawing();
            if (shouldDebug) {
                System.out.println("ntime:" + l1);
                for (int l2 = 0; l2 < 10; l2++) {
                    int i3 = ((i - l2 - 1) + 20) % 10;
                    System.out.println("otim" + i3 + ":" + aLongArray7[i3]);
                }
                System.out.println("fps:" + fps + " ratio:" + j + " count:" + i1);
                System.out.println("del:" + k + " deltime:" + delayTime + " mindel:" + minDelay);
                System.out.println("intex:" + j1 + " opos:" + i);
                shouldDebug = false;
                j1 = 0;
            }
        }
        if (targetFps == -1)
            exit();
    }

    private void exit() {
        targetFps = -2;
        cleanUpForQuit();
        if (gameFrame != null) {
            try { Thread.sleep(1000L); } catch (Exception _ex) { }
            try { System.exit(0); } catch (Throwable _ex) { }
        }
    }

    final void initDrawingArea(int i) {
        delayTime = 1000 / i;
    }

    public final void start() {
        if (targetFps >= 0)
            targetFps = 0;
    }

    public final void stop() {
        if (targetFps >= 0)
            targetFps = 4000 / delayTime;
    }

    public final void destroy() {
        targetFps = -1;
        try { Thread.sleep(5000L); } catch (Exception _ex) { }
        if (targetFps == -1)
            exit();
    }

    public final void update(Graphics g) {
        if (graphics == null)
            graphics = g;
        shouldClearScreen = true;
        raiseWelcomeScreen();
    }

    public final void paint(Graphics g) {
        if (graphics == null)
            graphics = g;
        shouldClearScreen = true;
        raiseWelcomeScreen();
    }

    // --- Mouse input (inset-aware) ---

    private int getInsetX() {
        if (gameFrame != null) {
            Insets insets = gameFrame.getInsets();
            return insets.left;
        }
        return 0;
    }

    private int getInsetY() {
        if (gameFrame != null) {
            Insets insets = gameFrame.getInsets();
            return insets.top;
        }
        return 0;
    }

    public final void mousePressed(MouseEvent e) {
        int x = e.getX() - getInsetX();
        int y = e.getY() - getInsetY();
        idleTime = 0;
        clickX = x;
        clickY = y;
        clickTime = System.currentTimeMillis();
        getGameComponent().requestFocusInWindow();
        if (javax.swing.SwingUtilities.isRightMouseButton(e)) {
            clickMode1 = 2;
            clickMode2 = 2;
        } else {
            clickMode1 = 1;
            clickMode2 = 1;
        }
    }

    public final void mouseReleased(MouseEvent e) {
        idleTime = 0;
        clickMode2 = 0;
    }

    public final void mouseClicked(MouseEvent e) { }
    public final void mouseEntered(MouseEvent e) { }

    public final void mouseExited(MouseEvent e) {
        idleTime = 0;
        mouseX = -1;
        mouseY = -1;
    }

    public final void mouseDragged(MouseEvent e) {
        int x = e.getX() - getInsetX();
        int y = e.getY() - getInsetY();
        idleTime = 0;
        mouseX = x;
        mouseY = y;
    }

    public final void mouseMoved(MouseEvent e) {
        int x = e.getX() - getInsetX();
        int y = e.getY() - getInsetY();
        idleTime = 0;
        mouseX = x;
        mouseY = y;
    }

    // --- Mouse wheel ---

    public void mouseWheelMoved(MouseWheelEvent e) {
        // Override in client.java for zoom + scroll handling
    }

    // --- Keyboard input ---

    public static int hotKey = 508;

    public final void keyPressed(KeyEvent keyevent) {
        idleTime = 0;
        int i = keyevent.getKeyCode();
        int j = keyevent.getKeyChar();
        // Hotkeys
        if (hotKey == 508) {
            if (i == KeyEvent.VK_ESCAPE) { client.setTab(3); }
            else if (i == KeyEvent.VK_F5) { client.setTab(0); }
            else if (i == KeyEvent.VK_F11) { client.setTab(1); }
            else if (i == KeyEvent.VK_F12) { client.setTab(2); }
            else if (i == KeyEvent.VK_F1) { client.setTab(3); }
            else if (i == KeyEvent.VK_F2) { client.setTab(4); }
            else if (i == KeyEvent.VK_F3) { client.setTab(5); }
            else if (i == KeyEvent.VK_F4) { client.setTab(6); }
            else if (i == KeyEvent.VK_F8) { client.setTab(7); }
            else if (i == KeyEvent.VK_F9) { client.setTab(8); }
            else if (i == KeyEvent.VK_F10) { client.setTab(9); }
        } else {
            if (i == KeyEvent.VK_ESCAPE) { client.setTab(0); }
            else if (i == KeyEvent.VK_F1) { client.setTab(3); }
            else if (i == KeyEvent.VK_F2) { client.setTab(1); }
            else if (i == KeyEvent.VK_F3) { client.setTab(2); }
            else if (i == KeyEvent.VK_F4) { client.setTab(3); }
            else if (i == KeyEvent.VK_F5) { client.setTab(4); }
            else if (i == KeyEvent.VK_F6) { client.setTab(5); }
            else if (i == KeyEvent.VK_F7) { client.setTab(6); }
            else if (i == KeyEvent.VK_F8) { client.setTab(7); }
            else if (i == KeyEvent.VK_F9) { client.setTab(8); }
            else if (i == KeyEvent.VK_F10) { client.setTab(9); }
            else if (i == KeyEvent.VK_F11) { client.setTab(10); }
            else if (i == KeyEvent.VK_F12) { client.setTab(11); }
        }
        if (j < 30) j = 0;
        if (i == 37) j = 1;
        if (i == 39) j = 2;
        if (i == 38) j = 3;
        if (i == 40) j = 4;
        if (i == 17) j = 5;
        if (i == 8) j = 8;
        if (i == 127) j = 8;
        if (i == 9) j = 9;
        if (i == 10) j = 10;
        if (i >= 112 && i <= 123) j = (1008 + i) - 112;
        if (i == 36) j = 1000;
        if (i == 35) j = 1001;
        if (i == 33) j = 1002;
        if (i == 34) j = 1003;
        if (j > 0 && j < 128)
            keyArray[j] = 1;
        if (j > 4) {
            charQueue[writeIndex] = j;
            writeIndex = writeIndex + 1 & 0x7f;
        }
    }

    public final void keyReleased(KeyEvent keyevent) {
        idleTime = 0;
        int i = keyevent.getKeyCode();
        char c = keyevent.getKeyChar();
        if (c < '\036') c = '\0';
        if (i == 37) c = '\001';
        if (i == 39) c = '\002';
        if (i == 38) c = '\003';
        if (i == 40) c = '\004';
        if (i == 17) c = '\005';
        if (i == 8) c = '\b';
        if (i == 127) c = '\b';
        if (i == 9) c = '\t';
        if (i == 10) c = '\n';
        if (c > 0 && c < '\200')
            keyArray[c] = 0;
    }

    public final void keyTyped(KeyEvent keyevent) { }

    final int readChar(int dummy) {
        while (dummy >= 0) {
            for (int j = 1; j > 0; j++) ;
        }
        int k = -1;
        if (writeIndex != readIndex) {
            k = charQueue[readIndex];
            readIndex = readIndex + 1 & 0x7f;
        }
        return k;
    }

    // --- Focus ---

    public final void focusGained(FocusEvent e) {
        awtFocus = true;
        shouldClearScreen = true;
        raiseWelcomeScreen();
    }

    public final void focusLost(FocusEvent e) {
        awtFocus = false;
        for (int i = 0; i < 128; i++)
            keyArray[i] = 0;
    }

    // --- Window events ---

    public final void windowActivated(WindowEvent e) { }
    public final void windowClosed(WindowEvent e) { }
    public final void windowClosing(WindowEvent e) { destroy(); }
    public final void windowDeactivated(WindowEvent e) { }
    public final void windowDeiconified(WindowEvent e) { }
    public final void windowIconified(WindowEvent e) { }
    public final void windowOpened(WindowEvent e) { }

    // --- Overridable hooks ---

    void startUp() { }
    void processGameLoop() { }
    void cleanUpForQuit() { }
    void processDrawing() { }
    void raiseWelcomeScreen() { }

    // --- Utilities ---

    Component getGameComponent() {
        if (gameFrame != null)
            return gameFrame;
        else
            return this;
    }

    public void startRunnable(Runnable runnable, int priority) {
        Thread thread = new Thread(runnable);
        thread.start();
        thread.setPriority(priority);
    }

    void drawLoadingText(int percentage, String text) {
        while (graphics == null) {
            graphics = getGameComponent().getGraphics();
            try { getGameComponent().repaint(); } catch (Exception _ex) { }
            try { Thread.sleep(1000L); } catch (Exception _ex) { }
        }
        Font font = new Font("Helvetica", 1, 13);
        FontMetrics fontmetrics = getGameComponent().getFontMetrics(font);
        Font font1 = new Font("Helvetica", 0, 13);
        getGameComponent().getFontMetrics(font1);
        if (shouldClearScreen) {
            graphics.setColor(Color.black);
            graphics.fillRect(0, 0, myWidth, myHeight);
            shouldClearScreen = false;
        }
        Color color = new Color(140, 17, 17);
        int y = myHeight / 2 - 18;
        graphics.setColor(color);
        graphics.drawRect(myWidth / 2 - 152, y, 304, 34);
        graphics.fillRect(myWidth / 2 - 150, y + 2, percentage * 3, 30);
        graphics.setColor(Color.black);
        graphics.fillRect((myWidth / 2 - 150) + percentage * 3, y + 2, 300 - percentage * 3, 30);
        graphics.setFont(font);
        graphics.setColor(Color.white);
        graphics.drawString(text, (myWidth - fontmetrics.stringWidth(text)) / 2, y + 22);
    }

    // Applet API stubs for Java 21 (JPanel replacement)
    public String getParameter(String name) { return ""; }
    public java.net.URL getCodeBase() { return null; }
    public java.net.URL getDocumentBase() { return null; }
    public void showStatus(String msg) { }

    // --- Constructor ---

    RSApplet() {
        delayTime = 20;
        minDelay = 1;
        aLongArray7 = new long[10];
        shouldDebug = false;
        shouldClearScreen = true;
        awtFocus = true;
        keyArray = new int[128];
        charQueue = new int[128];
    }

    // --- Fields ---

    private int targetFps;
    private int delayTime;
    int minDelay;
    private final long[] aLongArray7;
    int fps;
    boolean shouldDebug;
    int myWidth;
    int myHeight;
    Graphics graphics;
    RSImageProducer fullGameScreen;
    RSFrame gameFrame;
    private boolean shouldClearScreen;
    boolean awtFocus;
    int idleTime;
    int clickMode2;
    public int mouseX;
    public int mouseY;
    private int clickMode1;
    private int clickX;
    private int clickY;
    private long clickTime;
    int clickMode3;
    int saveClickX;
    int saveClickY;
    long aLong29;
    final int[] keyArray;
    private final int[] charQueue;
    private int readIndex;
    private int writeIndex;
    public static int debugFlags;
}