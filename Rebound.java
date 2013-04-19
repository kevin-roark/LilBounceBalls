import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Rebound sets up a JFrame for the ReboundPanel collision class to take
 * place in. It performs the JFrame idioms, adding a new ReboundPanel to
 * its content pane. Essentially, this is merely a tester class.
 * <br><br>
 * Code originally adopted from Lewis/Lofton Textbook
 * 
 * @author Kevin Roark <ker2143@columbia.edu>
 * @version SE 6
 * @since 2012-11-27
 */
public class Rebound {

  /**
   * Main method initializes a JFrame for the particle simulation to occur
   * in, then performs the necessary JFrame idioms to set the close operation,
   * set the size, set the visibility, and most importantly to add an
   * instance of the ParticlePanel class to the content pane.
   */
  public static void main(String[] args) {
    JFrame frame = new JFrame("Rebound");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    frame.getContentPane().add(new ReboundPanel());
    frame.pack();
    frame.setMinimumSize(frame.getSize());
    frame.setVisible(true);
  }
}
