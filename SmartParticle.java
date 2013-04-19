import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * SmartParticle extends Particle and adds the additional functionality of
 * displaying two different images, depending on whether or not the particle
 * is colliding with another particle or not. The colliding field is set to 
 * true by Particle's detectCollision method and set to false by ReboundPanel
 * after each individual frame is painted. Then, the SmartParticle displays
 * its collisionImage only for one frame. It may seem weird to rely on another
 * class for setting a Particle back to false collision, but it should be
 * reiterated that a specific application may wish to display the collisionImage
 * for any number of frames after a particle collides, so it becomes the
 * responsibility of that application to control the image that should be
 * displayed.
 * <br><br>
 * Because a colliding boolean was already included in the Particle class in order
 * to prevent collision check redundancy, it was very easy to extend the class
 * to SmartParticle. Only the constructor and the single getImage method needed to
 * be modified.
 * 
 * @author Kevin Roark <ker2143@columbia.edu>
 * @version SE 6
 * @since 2012-11-27
 */
public class SmartParticle extends Particle{
  
  private ImageIcon collisionImage;
  
  /**
   * Constructor calls the super constructor of the Particle class to define
   * the particle's initial location, velocity, and default image. The fourth
   * parameter asked for, cIm, is then used to define the SmartParticle's
   * unique collisionImage field.
   * 
   * @param p initial 2D (x,y) location of particle; given as a Point object
   * @param direction initial 2D movement direction of particle; given in radians
   * @param im the image the particle should display when not colliding
   * @param cIm the image the particle should display when colliding
   */
  public SmartParticle(Point p, double direction, ImageIcon im, ImageIcon cIm) {
    super(p, direction, im);
    collisionImage = cIm;
  }
  
  /**
   * Overriden method from Particle that returns the image that should be used
   * to display the SmartParticle at a given time. If the particle is colliding,
   * it returns the collision image; otherwise it returns the default, 
   * non-collision image.
   * 
   * @return collisionImage if particle is colliding, default image otherwise
   */
  public ImageIcon getImage() {
    return (isColliding())? collisionImage : image;
  }

}
