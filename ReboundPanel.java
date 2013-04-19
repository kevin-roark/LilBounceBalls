import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * ReboundPanel extends JPanel and functions as a container in which Particles
 * collide with one another and the walls. The user can add new Particles to the
 * panel by clicking the mouse at a specific location. Further, the user can
 * specify the initial direction of the particle's movement and the particle's
 * type by pressing keys on the keyboard. A Timer is used to animate the
 * particles. A delay of 45 ms (corresponding to about 22 frames per second)
 * is chosen because it is near the maximum delay I could choose without visual
 * "jerkiness"; I wanted to find the maximum delay to minimize the amount of
 * computation.
 * <br>
 * Pressing any key in the WERSDFXCV 3x3 block of keys specifies that the next
 * particle added should not be smart, and the specific key pressed specifies
 * its initial direction. W corresponds to Northwest, E to North, R to Northeast,
 * S to West, and so on. D means the particle has no initial velocity or direction.
 * Likewise, Pressing any key in the UIOJKLM,. 3x3 block of keys specifies
 * that the next particle should be smart, and the specific key specifies
 * its direction. Like before, I is Northwest, O North, and so on.
 * <br><br>
 * A maximum of 250 particles can be added to the panel. This is probably overkill
 * because 250 particles cannot even fit on the screen, but the user should ideally
 * never reach this limit and see an error. An array of Particles is used to store
 * each particle bouncing in the panel for convenience
 * <br><br>
 * Information about the next particle to be added is displayed to the user for ease
 * of use. Strings are used to store this information. Also the field nextDirection
 * double stores the direction as a radian value for the next particle to 
 * be added. The String nextParticleType is also used in the mouse listener to
 * determine whether the added particle should be smart or not. It seemed simple to
 * use the one field for two uses.
 * <br><br>
 * Evaluation of elastic collisions: <br>
 * After thorough experimentation, it seems that the application is finally able
 * to detect all interparticle collisions. The key step in achieving this was
 * accounting for particle and wall overlap; before doing that the particles
 * would get stuck within one another and within the walls of the panel. Even
 * when a ton of particles are added on to one another with no intial velocity,
 * the program automatically moves them apart. Then when a single ball with
 * any velocity is sent towards the clump it breaks the clump apart. Its pretty
 * cool to see, check the screenshots! <br>
 * Then, the collisions seem to remain fairly realistic no matter how many
 * particles are added to the panel, but after about 100 particles the panel becomes
 * so packed that the whole thing is to clumpy and frantic to look realistic or cool.
 * <br>
 * Documentation for design decisions and collision calculations are given later and
 * in the Particle class.
 * <br><br>
 * Code originally adopted from Lewis/Lofton Textbook
 * 
 * @author Kevin Roark <ker2143@columbia.edu>
 * @version SE 6
 * @since 2012-11-27
 */
public class ReboundPanel extends JPanel {
  
  private final int WIDTH = 800; // 800 seemed a nice "medium" size
  private final int HEIGHT = 800; // want container to be square
  private final int DELAY = 45;  // 45 ms corresponds to about 22 frames per second
  private final int MAX_PARTICLES = 250; 
  
  private Particle[] pList;
  private ImageIcon pImage, spImage1, spImage2;
  private Timer timer;
  private int numParticles;
  private double nextDirection;
  private String selectedDirection, nextParticleType;
  
  /**
   * Constructor adds key and mouse listeners to the panel, initializes the
   * Timer with a listener, initializes list of Particles, initializes images
   * to be used for both types of particles, initializes integer and String fields
   * to default values, and performs necessary JPanel and Timer idioms so that
   * they are used properly. In doing all of this, it prepares the panel to bounce
   * some particles around ad nasuem!
   */
  public ReboundPanel() {
    addKeyListener(new DirectionListener());
    addMouseListener(new ClickListener());
    timer = new Timer(DELAY, new CollisionListener());
    
    pList = new Particle[MAX_PARTICLES];
    
    pImage = new ImageIcon("henry.gif");
    spImage1 = new ImageIcon("dylan.gif");
    spImage2 = new ImageIcon("angry_dylan.gif");

    numParticles = 0;
    nextDirection = 0;
    selectedDirection = "East";
    nextParticleType = "Regular";
    
    setPreferredSize(new Dimension(WIDTH,HEIGHT));
    setBackground(Color.black);
    setForeground(Color.white);
    timer.start();
    setFocusable(true);
  }
  
  /**
   * Method called when Panel is initialized and at every call of
   * repaint. After calling super.paintComponent(), the method
   * loops through the list of particles to be painted in the panel,
   * calculates locations for painting, paints them using ImageIcon's
   * paintIcon method, draws strings conveying next particle selection
   * information to user, and (very importantly) sets the colliding
   * field of each particle to false.
   * <br><br>
   * Each particle is set to not colliding after the painting of every
   * frame so that for the next frame new checks for particle collisions
   * can occur. In this application, particles are set to colliding on a
   * frame-by-frame basis to prevent unnecessary checks, but if they are
   * not reset to false than the entire routine would fail. Another trick
   * in this method is calculating the points for display of imageIcons;
   * the paintIcon method for imageIcons assumes the given point is the
   * top-left corner of the image. Because Particles are represented by
   * their center point, to paint them the top-left corner is calculated.
   * 
   * @param page the Graphics component of the JPanel
   */
  public void paintComponent(Graphics page) {
    super.paintComponent(page);
    int xLeft, yTop;
    for(int i = 0; i < numParticles; i++) {
      xLeft = (int) (pList[i].x-pList[i].getRadius());
      yTop = (int) (pList[i].y-pList[i].getRadius());
      pList[i].getImage().paintIcon(this, page, xLeft, yTop);
    }
    
    page.drawString("Count: " + numParticles,5,15);
    page.drawString("Direction set to: " + selectedDirection,5,30);
    page.drawString("Selected particle type: " + nextParticleType,5,45);
    
    for (int i=0; i < numParticles; i++)
      pList[i].setColliding(false);
  }
  
  
  /**
   * CollisionListener implements ActionListener and functions to respond to
   * every tick of the Timer. Specific documentation for how the class works
   * is provided below before each method, but broadly the class is responsible
   * for animating the frame and managing collisions between particles.
   */
  private class CollisionListener implements ActionListener {
    
    /**
     * Implemented from ActionListener. At every frame, the Timer produces an
     * event and actionPerformed is called. The method goes through every
     * Particle in the panel, first checking if the particle hits the walls of
     * the frame, then checking if the particle intersects with any other
     * particle. Finally, each particle is translated by one time-step of its
     * velocity, and the frame is repainted. Performing this repaint at every
     * timer event is what produces the animation of this design.
     * 
     * @event ActionEvent produced at every tick of the Timer
     */
    public void actionPerformed(ActionEvent event) {
      for (int i=0; i < numParticles; i++) {    	
    	detectWallCollision(i);
        detectInterParticleCollisions(i);
        pList[i].translate();
      }
      
      repaint();
    }
    
    /**
     * Private method used to check each particle for collisions with every
     * subsequent particle. To prevent redundancy, particles only check for
     * collisions with other particles located after them in the array of
     * Particles (because if p1 checked with p2, p2 needs not to check with p1),
     * and once a particle has detected collision with one particle, it stops
     * checking for collision with another particle. In theory, a single particle
     * could collide with two other particles at the exact same moment, but in
     * practice this doesn't seem to be an issue, and only accounting for a single
     * collision for each particle for frame reduces computation a good bit.
     * Particle's methods for detecting collisions and responding to them are called
     * for simplicity.
     * 
     * @param i index of pList containing the particle that should check collisions
     * with every subsequent particle
     */
    private void detectInterParticleCollisions(int i) {  
      for (int c = i+1; c < numParticles; c++) {
    	if(!pList[i].isColliding() && pList[i].intersects(pList[c]))
    	  pList[i].elasticCollision(pList[c]);
      }
    }
    
    /**
     * Private method called to check if a particle runs into the walls of the
     * containing panel. If it does, its x or y velocity is appropriately
     * reversed. Then, because the time frames are discrete, the particle could overlap
     * with the wall, and continually reverse its direction, getting stuck.
     * This method places the balls completely within the panel whenever they
     * overlap in order to prevent sticking.
     * <br>
     * Wall collisions deliberately do not set particles to colliding. Only collisions
     * with other particles count as "colliding" for the particles. This is done
     * so that interparticle collisions are not ignored because of wall collisions,
     * and so that smart particles do not change images when they hit a wall; in this
     * design it seems more natural for smart particles to change image when they hit
     * other particles only.
     * 
     * @param i index of pList containing Particle to put in frame
     */
    private void detectWallCollision(int i) {
      int x = pList[i].x;
      int y = pList[i].y;
      int r = (int) Math.round(pList[i].getRadius());
      if (x<=r) {		    // left wall
        pList[i].setLocation(r+1,y);
        pList[i].setVx(-1*pList[i].getVx());
      }
      else if (x>=(WIDTH-r)) {	    // right wall
        pList[i].setLocation(WIDTH-r-1,y);
        pList[i].setVx(-1*pList[i].getVx());
      }
  	  
      x = pList[i].x; 		   // update x to current x location
  	  
      if (y<=r) {                  // top wall
        pList[i].setLocation(x,r+1);
        pList[i].setVy(-1*pList[i].getVy());
      }
      else if (y>=(HEIGHT-r)) {    // bottom wall
        pList[i].setLocation(x,HEIGHT-r-1);
        pList[i].setVy(-1*pList[i].getVy());
      }
    
    }
  }
  
  /**
   * FaceListener extends MouseAdapter and is used in this design to listen to
   * and respond to every user press of the mouse. Specifics for how clicks are
   * responded to are documented in the mousePressed method below. Broadly, the
   * class creates a new particle based on user and class defined fields, and
   * adds that particle to the list of current particles in the pane.
   */
  private class ClickListener extends MouseAdapter {

    /**
     * Method overrides one from MouseAdapter. Event is generated whenever the
     * user clicks the mouse. A click adds a new particle to the panel. The
     * point of the click becomes the center of the Particle, then the
     * nextParticleType and nextDirection fields are used to construct either a
     * new Particle or a new SmartParticle. This particle is then added to the
     * program's list of particles in the frame.
     * 
     * @param event MouseEvent generated when user clicks the mouse in the panel;
     * where the click occurs becomes the center of the next ball to be added
     */
    public void mousePressed(MouseEvent event) {
       Particle nextParticle;
	  
         if (nextParticleType.equals("Regular"))
           nextParticle = new Particle(event.getPoint(),nextDirection,pImage);
         else
           nextParticle = new SmartParticle(event.getPoint(),nextDirection,spImage1,spImage2);
	  
         pList[numParticles] = nextParticle;
         numParticles++;
    }
  }
  
  /**
   * DirListener extends KeyAdapter and is used in this design to listen to and
   * respond to any time the user presses a key on the keyboard. Specifics for 
   * how it responds to specific keys are documented for the keyPressed method
   * below. Broadly, a user presses keys to select the next type of particle to
   * be added when the mouse is clicked, and its initial direction.
   */
  private class DirectionListener extends KeyAdapter {
    
    /**
     * Method overrides the one in KeyAdapter. It is called whenever the user
     * presses a key on the keyboard. 18 keys in total are listened for, each
     * updating the selected direction of movement for the next particle to be
     * added to panel and whether or not that particle is "smart." A switch-case
     * statement is used because there are so many unique real-world constraints
     * on what a key can be that it makes the most sense to read.
     * <br><br>
     * As specified in the class header, the switch case sets nextparticle fields.
     * Pressing any key in the WERSDFXCV 3x3 block of keys specifies that the next
     * particle added should not be smart, and the specific key pressed specifies
     * its initial direction. W corresponds to Northwest, E to North, R to Northeast,
     * S to West, and so on. D means the particle has no initial velocity or direction.
     * Likewise, Pressing any key in the UIOJKLM,. 3x3 block of keys specifies
     * that the next particle should be smart, and the specific key specifies
     * its direction. Like before, I is Northwest, O North, and so on. The
     * directions are represented mathematically as angles in radians--this is intuitive.
     */
    public void keyPressed(KeyEvent event) {
      switch(event.getKeyCode()) {
        case KeyEvent.VK_W:
          nextDirection = 0.75*Math.PI;
          selectedDirection = "Northwest";
          nextParticleType = "Regular";
          break;
        case KeyEvent.VK_E:
          nextDirection = 0.5*Math.PI;
          selectedDirection = "North";
          nextParticleType = "Regular";
          break;
        case KeyEvent.VK_R:
          nextDirection = 0.25*Math.PI;
          selectedDirection = "Northeast";
          nextParticleType = "Regular";
          break;
        case KeyEvent.VK_S:
          nextDirection = Math.PI;
          selectedDirection = "West";
          nextParticleType = "Regular";
          break;
        case KeyEvent.VK_D:
          nextDirection=Particle.NO_MOVEMENT;
          selectedDirection = "None";
          nextParticleType = "Regular";
          break;
        case KeyEvent.VK_F:
          nextDirection = 0;
          selectedDirection = "East";
          nextParticleType = "Regular";
          break;
        case KeyEvent.VK_X:
          nextDirection = 1.25*Math.PI;
          selectedDirection = "Southwest";
          nextParticleType = "Regular";
          break;
        case KeyEvent.VK_C:
          nextDirection = 1.5*Math.PI;
          selectedDirection = "South";
          nextParticleType = "Regular";
          break;
        case KeyEvent.VK_V:
          nextDirection = 1.75*Math.PI;
          selectedDirection = "Southeast";
          nextParticleType = "Regular";
          break;
        case KeyEvent.VK_U:
          nextDirection = 0.75*Math.PI;
          selectedDirection = "Northwest";
          nextParticleType = "Smart";
          break; 
        case KeyEvent.VK_I:
          nextDirection = 0.5*Math.PI;
          selectedDirection = "North";
          nextParticleType = "Smart";
          break;
        case KeyEvent.VK_O:
          nextDirection = 0.25*Math.PI;
          selectedDirection = "Northeast";
          nextParticleType = "Smart";
          break;
        case KeyEvent.VK_J:
          nextDirection = Math.PI;
          selectedDirection = "West";
          nextParticleType = "Smart";
          break;
        case KeyEvent.VK_K:
          nextDirection = Particle.NO_MOVEMENT;
          selectedDirection = "None";
          nextParticleType = "Smart";
          break;
        case KeyEvent.VK_L:
          nextDirection = 0;
          selectedDirection = "East";
          nextParticleType = "Smart";
          break;
        case KeyEvent.VK_M:
          nextDirection = 1.25*Math.PI;
          selectedDirection = "Southwest";
          nextParticleType = "Smart";
          break;
        case KeyEvent.VK_COMMA:
          nextDirection = 1.5*Math.PI;
          selectedDirection = "South";
          nextParticleType = "Smart";
          break;
        case KeyEvent.VK_PERIOD:
          nextDirection = 1.75*Math.PI;
          selectedDirection = "Southeast";
          nextParticleType = "Smart";
          break;
      }
    }
  }
  
}
