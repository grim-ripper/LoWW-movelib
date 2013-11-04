package fi.grimripper.loww;

/**
 * An abstraction for a 2D point for use with different geometric libraries.
 * 
 * @author Marko Tuominen
 */
public abstract class Point {

	/**
	 * Creates new point objects. Must be initialized before any point objects are created.
	 */
	protected static PointFactory factory = null;
	
	/**
	 * Creates a new point object, copying an existing point.
	 * 
	 * @param point			make a copy of this point
	 * @return				a new point object with the same coordinates as the parameter
	 * @see					#createPoint(double, double)
	 */
	public static Point createPoint( Point point ) {
		return createPoint( point.getX(), point.getY() );
	}
	
	/**
	 * Creates a new point object with x- and y-coordinates.
	 * 
	 * @param x				x-coordinate
	 * @param y				y-coordinate
	 * @return				a new point object with the parameter coordinates
	 * @see					#factory
	 */
	public static Point createPoint( double x, double y ) {
		return factory.createPoint( x, y );
	}
	
	/**
	 * Shortcut method for calculating distance between two points.
	 * 
	 * @param x1			first point's x-coordinate
	 * @param y1			first point's y-coordinate
	 * @param x2			second point's x-coordinate
	 * @param y2			second point's y-coordinate
	 * @return				distance between the points
	 * @see					#createPoint(double, double)
	 * @see					#distance(double, double)
	 */
	public static double distance( double x1, double y1, double x2, double y2 ) {
		return createPoint( x1, y1 ).distance( x2, y2 );
	}
	
	/**
	 * Shortcut method for calculating distance between two points.
	 * 
	 * @param point1		first point
	 * @param point2		second point
	 * @return				distance between the points
	 * @see					#distance(Point)
	 */
	public static double distance( Point point1, Point point2 ) {
		return point1.distance( point2 );
	}
	
	/**
	 * Gets the point's x-coordinate.
	 * 
	 * @return				the x-coordinate
	 */
	public abstract double getX();
	
	/**
	 * Sets the point's x-coordinate.
	 * 
	 * @param x				the x-coordinate
	 */
	public abstract void setX( double x );

	/**
	 * Gets the point's y-coordinate.
	 * 
	 * @return				the y-coordinate
	 */
	public abstract double getY();

	/**
	 * Sets the point's y-coordinate.
	 * 
	 * @param y			the y-coordinate
	 */
	public abstract void setY( double y );
	
	/**
	 * Calculates distance from this point to another.
	 * 
	 * @param x				other point's x-coordinate
	 * @param y				other point's y-coordinate
	 * @return				distance between the points
	 */
	public abstract double distance( double x, double y );
	
	/**
	 * Calculates distance from this point to another.
	 * 
	 * @param point			the other point
	 * @return				distance between the points
	 * @see					#distance(double, double)
	 */
	public double distance( Point point ) {
		return distance( point.getX(), point.getY() );
	}
	
	/**
	 * Compares two points and determines if they're within a given distance of each other.
	 * 
	 * @param point			compare to this point
	 * @param appr			maximum allowed difference in coordinates
	 * @return				this point is within the given distance of the other point
	 */
	public boolean approximateEquals( Point point, double appr ) {
		return getX() < point.getX() + appr && getX() > point.getX() - appr &&
				getY() < point.getY() + appr && getY() > point.getY() - appr;
	}

	/**
	 * Interface for creating point objects.
	 * 
	 * @author Marko Tuominen
	 * @see		Point#factory
	 */
	public static interface PointFactory {
		
		/**
		 * Creates a new point object.
		 * 
		 * @param x			x-coordinate for the new point
		 * @param y			y-coordinate for the new point
		 * @return			new point object with the parameter coordinates
		 */
		public Point createPoint( double x, double y );
	}
}
