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

            JPanel gamePanel = new JPanel();
            gamePanel.setLayout(new BorderLayout());
            gamePanel.add(this);
            gamePanel.setPreferredSize(new Dimension(REGULAR_WIDTH, REGULAR_HEIGHT));

            frame.getContentPane().add(gamePanel, BorderLayout.CENTER);
            frame.pack();
            frame.setVisible(true);
            frame.setResizable(false); // starts in fixed mode
            frame.setLocationRelativeTo(null);

            // Give client.java access to the outer frame for resize
            client.outerFrame = frame;

            // Listen for resize events
            frame.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    if (clientSize == 1) {
                        gamePanel.setPreferredSize(null); // allow free resize
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
     * Returns the current JFrame for external access.
     */
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

    private static void openUpWebSite(String url) {
        Desktop d = Desktop.getDesktop();
        try {
            d.browse(new URI(url));
        } catch (Exception e) {
        }
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