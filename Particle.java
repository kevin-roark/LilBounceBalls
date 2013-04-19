import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Particle represents a colliding object defined by a point in (x,y) space,
 * a size, a velocity, and an image used for display. These components are
 * represented internally via the following: Particle extends Point so that
 * it inherently is defined by a point in space; Particle represents its velocity
 * by its two components (x-vel and y-vel) as doubles; Particle determines its
 * radius and mass based on the width of its characteristic image and a mass
 * constant; and finally Particle represents its characteristic image via an 
 * instance ImageIcon object.
 * <br><br>
 * Explanation of constants: <br>
 * No_MOVEMENT is explained below; value was chosen because it is likely to
 * never be called in another situation <br>
 * MASS_CONSTANT is used to scale the "mass" of a particle to its area. It is such
 * a low value so that particles of a reasonable pixel-size have correspondingly
 * reasonable mass values; a width of 40 pixels should not correspond to 1,256
 * mass units; instead, it corresponds to 1.25 mass units. <br>
 * START_VEL is used so that by default when particles start moving they are
 * moving at a reasonable speed. The frame rate of 22 fps was kept in mind when
 * determining this value because the unit of the velocity is pixels per frame.
 * There is no underlying logic to the magnitude of 7 other than it is what looked
 * nice after some experimentation.
 * 
 * @author Kevin Roark <ker2143@columbia.edu>
 * @version SE 6
 * @since 2012-11-27
 */
public class Particle extends Point {
  
  /**
   * NO_MOVEMENT is used for convenience; when it is used as the angle parameter
   * for setDirection or setVelocity, the particle's velocity goes to zero.
   * Alternatively, setVelocity could be called with a 0 parameter, but this exists
   * so that applications which exclusively change direction can still stop the
   * particle from moving.
   */
  static final double NO_MOVEMENT = 1111111;
  
  private final double MASS_CONSTANT = 0.001;
  private final double START_VEL = 7;
  
  /**
   * image is used to define a Particle's characteristic image for display
   * purposes
   */
  protected ImageIcon image;
  
  private double vX, vY, radius, mass; 
  private boolean colliding;
  
  /**
   * A Particle must be defined by initial point, initial direction, initial velocity,
   * and an image used for display. Constructor calls the super constructor of the Point
   * class to define the particle's initial location, calls Particle's own setVelocity
   * method to define initial velocity based on given direction and default
   * starting velocity, defines particle's display image as the im parameter, and
   * defines its mass based on the width of the given image.
   * 
   * Here, the direction parameter of the setVelocity method is multiplied by -1
   * because the y-axis is inverted in java GUI applications.
   * 
   * @param p initial 2D (x,y) location of particle; given as a Point object
   * @param direction initial 2D movement direction of particle; given in radians
   * @param im the image the particle should use for display purposes
   */
  public Particle(Point p, double direction, ImageIcon im) {
    super(p);
    image = im;
	radius = image.getIconWidth()/2;
    mass = radius*radius*MASS_CONSTANT;
    setVelocity(-direction,START_VEL);
  }
  
  /**
   * A Particle must be defined by initial point, initial direction, initial velocity,
   * and an image used for display. Constructor calls the super constructor of the Point
   * class to define the particle's initial location, calls Particle's own setVelocity
   * method to define initial velocity based on given direction and given velocity, 
   * defines particle's display image as the im parameter, and defines its mass
   * based on the width of the given image.
   * 
   * Here, the direction parameter of the setVelocity method is multiplied by -1
   * because the y-axis is inverted in java GUI applications. Also, although this
   * constructor is not used in the specific application of this assignment, it 
   * was written because it could certainly be useful in the future.
   * 
   * @param p initial 2D (x,y) location of particle; given as a Point object
   * @param direction initial 2D movement direction of particle; given in radians
   * @param v initial magnitude of the particle's velocity; unit of pixels per frame
   * @param im the image the particle should use for display purposes
   */
  public Particle(Point p, double direction, double v, ImageIcon im) {
    super(p);
    image = im;
    radius = image.getIconWidth()/2;
    mass = radius*radius*MASS_CONSTANT;
    setVelocity(-direction,v);
  }
  
  /**
   * Returns true if the two Particles intersect, false otherwise. Determines
   * intersection by comparing the distance between each Particle's center
   * to the sum of their radii. 
   * <br><br>
   * If two particles do intersect, the colliding field of each is set to true;
   * this should make sense. colliding field is not set to false if they do not
   * intersect because the method has no knowledge of whether or not the particles
   * are colliding elsewhere.
   * 
   * @param other Particle that this may be colliding with
   * @return true if particles intersect, false otherwise
   */
  public Boolean intersects (Particle other) {
    double distSQ = distanceSq(other);
    double bothRad = getRadius() + other.getRadius();
    if (distSQ <= bothRad*bothRad) {
      setColliding(true);
      other.setColliding(true);
      return true;
    }
    else
    return false;
  }
  
  /**
   * Sets the colliding field of a Particle. This method is used in this
   * assignment to prevent unnecessary collision checks - in most instances,
   * if a particle is already colliding there is no need to check to see
   * if it collides with other particles.
   * <br><br>
   * Setting colliding to true is done in the intersects method because
   * that is intuitive. Setting colliding to false is a little more tricky
   * because the application using particles has choice over when the particle
   * should stop colliding and start to check for collisions again. Thus it
   * is left to that application to choose when to set colliding to false. For
   * this homework's application, I set every particle's colliding to false at
   * the end of every frame.
   * 
   * @param b true if particle is colliding / has just collided, false otherwise
   */
  public void setColliding(Boolean b) {
    colliding = b;
  }
  
  /**
   * Returns colliding field of a Particle.
   * 
   * @return colliding; should be true if particle is colliding / has just collided,
   * false otherwise.
   */
  public Boolean isColliding() {
    return colliding;
  }
  
  /**
   * After scouring the Internet and tweaking many, many times, the following
   * method for elastic collision between two Particles has been developed:
   * <br>
   * 1. Calculate the angle of collision of the particles and rotate the
   * coordinate system in order to treat the collision as one-dimensional. <br>
   * 2. Account for imperfections inherent to discrete animation and overlap of
   * particles by moving each particle along the collision angle so that they
   * touch rather than intersect. <br>
   * 3. Calculate "x" and "y" velocities for both particles in rotated system. <br>
   * 4. Use the 1D equations for a collision to set "x" velocities for both 
   * particles post collision. <br>
   * 5. Calculate new velocity magnitude, rotate the system back to standard
   * coordinates, and set the velocity of each particle to match the new
   * calculated values.
   * <br><br>
   * Examples and inspiration given on following websites:<br>
   * wikipedia article on elastic collisions<br>
   * http://www.hoomanr.com/Demos/Elastic2/<br>
   * http://www.emanueleferonato.com/2007/08/19/managing-ball-vs-ball-collision-with-flash/<br>
   * http://director-online.com/buildArticle.php?id=532<br>
   * http://spiff.rit.edu/classes/phys311.old/lectures/coll2d/coll2d.html<br>
   * http://stackoverflow.com/questions/345838/ball-to-ball-collision-detection-and-handling
   * @param other Particle that this Particle collides with
   */
  public void elasticCollision(Particle other) {
	  
    // Calculate angle of collision between particles
    double xDist = x - other.x;
    double yDist = y - other.y;
    double colAngle = Math.atan2(yDist,xDist);
    
    // Manually move particles minimum distance necessary so that they do not 
    // overlap. This must be done because collision calculations are done in 
    // discrete time increments, so the exact moment that two particles
    // collide is nearly impossible to capture.
    double bothRad = getRadius() + other.getRadius();
    double dist = Math.sqrt(xDist*xDist + yDist*yDist);
    double overlap = (bothRad-dist) + 1;
    double im1 = 1 / getMass();			// inverse of particle's mass used to
    double im2 = 1 / other.getMass();   // calc how much each particle should move
    double move1 = overlap*(im1 / (im1+im2));  // distance each particle should
    double move2 = -1*overlap*(im2 / (im1+im2)); // move in direction of collision
    int move1X = (int) Math.round(move1*Math.cos(colAngle));
    int move1Y = (int) Math.round(move1*Math.sin(colAngle));
    int move2X = (int) Math.round(move2*Math.cos(colAngle));
    int move2Y = (int) Math.round(move2*Math.sin(colAngle));
    translate(move1X,move1Y);
    other.translate(move2X,move2Y);
	      
    // Calculating the x and y velocities in rotated coordinate system.
    double vX1 = getVelMag()*Math.cos(getDirection()-colAngle);
    double vY1 = getVelMag()*Math.sin(getDirection()-colAngle);
    double vX2 = other.getVelMag()*Math.cos(other.getDirection()-colAngle);
    double vY2 = other.getVelMag()*Math.sin(other.getDirection()-colAngle);
	     
    // Calculating post-collision x velocities in rotated coordinate system.
    // Treated as 1D; Y velocities stay the same (thats the point!)
    double newVX1 = vX1*(getMass()-other.getMass())+2*other.getMass()*vX2;
    newVX1 = newVX1 / (getMass()+other.getMass());
    double newVX2 = vX2*(other.getMass()-getMass())+2*getMass()*vX1;
    newVX2 = newVX2 / (getMass()+other.getMass());
    
    // Calculating magnitude of post-collision velocities
    double newVelMag1 = Math.sqrt(newVX1*newVX1 + vY1*vY1);
    double newVelMag2 = Math.sqrt(newVX2*newVX2 + vY2*vY2);
    
    // Calculating direction of post-collision movement
    // after rotating back to standard coordinate system.
    double newDir1 = Math.atan2(vY1, newVX1) + colAngle;
    double newDir2 = Math.atan2(vY2, newVX2) + colAngle;
	
    // Defining post-collision direction and velocity for each particle
    setVelocity(newDir1,newVelMag1);
    other.setVelocity(newDir2,newVelMag2);
  }
  
  /**
   * Defines new direction for particle velocity without changing magnitude
   * of velocity. Simply calls the setVelocity method using the angle parameter
   * and the current velocity.
   * 
   * @param angle new direction of particle's velocity; to be given in radians
   */
  public void setDirection(double angle) {
    setVelocity(angle,getVelMag());
  }
  
  /**
   * Used to define velocity of particle in polar coordinates. Polar coordinates
   * are used because they seem a more intuitive way of visualizing velocity.
   * <br><br>
   * The method of course abstracts the polar coordinates into vx and vy
   * components for updating the particle fields. The NO_MOVEMENT field is used
   * so that ReboundPanel and similar applications can call setDirection() and
   * set velocity to 0 conveniently.
   * 
   * @param angle new direction of particle's velocity; to be given in radians
   * @param velMag new magnitude of particle's velocity; unit of pixels per frame
   */
  public void setVelocity(double angle, double velMag) {
    if (Math.abs(angle)==Particle.NO_MOVEMENT) {
      vX = 0;
      vY = 0;
    }
    else {
      vX = Math.cos(angle)*velMag;
      vY = Math.sin(angle)*velMag;
    }
  }
  
  /**
   * Sets the y-component of the particle's velocity.
   * 
   * @param v new y-velocity of particle.
   */
  public void setVx(double v) {
    vX = v;
  }
  
  /**
   * Returns the x-component of the particle's velocity.
   * 
   * @return x-velocity as a double
   */
  public double getVx() {
    return vX;
  }
  
  /**
   * Sets the y-component of the particle's velocity.
   * 
   * @param v new y-velocity of particle.
   */
  public void setVy(double v) {
    vY = v;
  }
  
  /**
   * Returns the y-component of the particle's velocity.
   * 
   * @return y-velocity as a double
   */
  public double getVy() {
    return vY;
  }
  
  /**
   * Returns the magnitude of the particle's velocity.
   * 
   * @return length of particle's velocity vector as a double
   */
  public double getVelMag() {
    return Math.sqrt(vX*vX + vY*vY);
  }
  
  /**
   * Returns the direction of the particle's velocity in Radians; range from -pi to pi.
   * 
   * @return Radian value of particle's movement direction as a double
   */
  public double getDirection() {
    return Math.atan2(vY,vX);
  }
  
  /**
   * Overloaded method assumes that when user does not specify the amount to
   * move, that the particle should move according to one time-step of its
   * velocity. Very convenient for animation.
   */
  public void translate() {
    translate((int)Math.round(vX),(int)Math.round(vY));
  }
  
  /**
   * Returns the image used for graphical display of the particle.
   * 
   * @return ImageIcon containing image of particle
   */
  public ImageIcon getImage() {
    return image;
  }
  
  /**
   * Returns the radius of the given particle. Determined by width of image
   * used to display particle.
   * 
   * @return radius of particle as a double
   */
  public double getRadius() {
    return radius;
  }
  
  /**
   * Returns mass of particle. Mass is calculated proportionally based on the
   * area of the particle.
   * 
   * @return proportional mass value of particle as a double
   */
  public double getMass() {
    return mass;
  }
  
}
