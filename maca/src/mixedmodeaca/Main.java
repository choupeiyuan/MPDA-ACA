/*
 * Entrance of MACA
 */

package mixedmodeaca;

import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author Administrator
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        System.out.println("This application implements Mixed-Mode ACA.");
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    //UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                    //UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                    //UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
                    //UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
                   UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InstantiationException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                } catch (UnsupportedLookAndFeelException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
                //new NewJFrame().setVisible(true);
                NewJFrame f = new NewJFrame();
                //UIManager.LookAndFeelInfo pInfo[] = UIManager.getInstalledLookAndFeels();
                //for ( int i =0; i < pInfo.length ; i++) {
                    //System.out.println(pInfo[i].getClassName());
                //}
                
                f.setExtendedState(JFrame.MAXIMIZED_BOTH);
                f.setVisible(true);
            }
        });
        //NewJFrame f = new NewJFrame();
        //f.setVisible(true);
    }

}
