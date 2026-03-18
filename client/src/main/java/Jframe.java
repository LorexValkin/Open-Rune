import sign.signlink;
import java.io.File;
import java.net.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.*;

public class Jframe extends client implements ActionListener {

    private static JFrame frame;
    private static JPanel gamePanel;

    public Jframe(String args[]) {
        super();
        try {
            sign.signlink.startpriv(InetAddress.getByName(server));
            initUI();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void initUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            JPopupMenu.setDefaultLightWeightPopupEnabled(false);
            frame = new JFrame("OpenRune");
            frame.setLayout(new BorderLayout());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            gamePanel = new JPanel();
            gamePanel.setLayout(new BorderLayout());
            gamePanel.add(this);
            gamePanel.setPreferredSize(new Dimension(REGULAR_WIDTH, REGULAR_HEIGHT));

            frame.getContentPane().add(gamePanel, BorderLayout.CENTER);
            frame.pack();
            frame.setVisible(true);
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);

            // Give client.java access to the outer frame for resize
            client.outerFrame = frame;

            // Listen for resize events
            frame.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    if (clientSize != 0 && gamePanel != null) {
                        // Refresh graphics when frame resizes
                        graphics = getGameComponent().getGraphics();
                    }
                }
            });

            this.setFocusable(true);
            this.requestFocusInWindow();
            this.requestFocus();

            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Called by toggleSize to update the JPanel constraints.
     */
    public static void updatePanelSize(int mode, int width, int height) {
        if (gamePanel == null || frame == null) return;

        if (mode == 0) {
            // Fixed: restore preferred size, disable resize
            gamePanel.setPreferredSize(new Dimension(REGULAR_WIDTH, REGULAR_HEIGHT));
            frame.setResizable(false);
            frame.pack();
            frame.setLocationRelativeTo(null);
        } else if (mode == 1) {
            // Resizable: clear preferred size so panel fills frame
            gamePanel.setPreferredSize(null);
            gamePanel.setMinimumSize(new Dimension(765, 503));
            frame.setResizable(true);
            Insets insets = frame.getInsets();
            frame.setSize(width + insets.left + insets.right, height + insets.top + insets.bottom);
            frame.setLocationRelativeTo(null);
            gamePanel.revalidate();
        } else if (mode == 2) {
            // Fullscreen: clear preferred, maximize
            gamePanel.setPreferredSize(null);
            frame.setResizable(false);
            frame.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
            gamePanel.revalidate();
        }
    }

    public static JFrame getFrame() {
        return frame;
    }

    public URL getCodeBase() {
        try {
            return new File(sign.signlink.findcachedir()).toURI().toURL();
        } catch (Exception e) {
            return null;
        }
    }

    public URL getDocumentBase() {
        return getCodeBase();
    }

    public void loadError(String s) {
        System.out.println("loadError: " + s);
    }

    public String getParameter(String key) {
        return "";
    }

    public void actionPerformed(ActionEvent evt) {
        String cmd = evt.getActionCommand();
        try {
            if (cmd != null) {
                if (cmd.equalsIgnoreCase("exit")) {
                    System.exit(0);
                }
            }
        } catch (Exception e) {
        }
    }
}