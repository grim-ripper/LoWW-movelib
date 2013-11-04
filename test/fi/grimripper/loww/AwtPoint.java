package fi.grimripper.loww;

import java.awt.geom.Point2D;

import fi.grimripper.loww.Point;

/**
 * Uses the AWT <code>Point2D</code> class to implement functionality for the movement library's
 * <code>Point</code> class.
 * 
 * @author Marko Tuominen
 */
public class AwtPoint extends Point {

	static {
		factory = new PointFactory() {
			public Point createPoint( double x, double y ) {
				return new AwtPoint( x, y );
			}			
		};
	}
	
	private Point2D.Double point = null;
	
	private AwtPoint( double x, double y ) {
		point = new Point2D.Double( x, y );
	}

	@Override
	public double getX() {
		return point.getX();
	}

	@Override
	public void setX( double x ) {
		point.x = x;
	}

	@Override
	public double getY() {
		return point.getY();
	}

	@Override
	public void setY( double y ) {
		point.y = y;
	}

	@Override
	public double distance( double x, double y ) {
		return point.distance( x, y );
	}

	@Override
	public boolean equals( Object obj ) {
		return obj instanceof Point && approximateEquals( (Point)obj, 0.001 );
	}

	@Override
	public String toString() {
		return point.toString();
	}
}
