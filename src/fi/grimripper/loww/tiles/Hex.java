package fi.grimripper.loww.tiles;

import static fi.grimripper.loww.Direction.EAST;
import static fi.grimripper.loww.Direction.NORTH;
import static fi.grimripper.loww.Direction.NORTHEAST;
import static fi.grimripper.loww.Direction.NORTHWEST;
import static fi.grimripper.loww.Direction.SOUTH;
import static fi.grimripper.loww.Direction.SOUTHEAST;
import static fi.grimripper.loww.Direction.SOUTHWEST;
import static fi.grimripper.loww.Direction.WEST;
import fi.grimripper.loww.Direction;
import fi.grimripper.loww.Point;

/**
 * Represents a single hexagon, or hex, in a tile grid. Each hex has up to six neighbors, at least
 * three even for hexes in the grid's corners. Each hex has the same width and height as every
 * other hex. Their locations, of course, vary. This class implements some calculations, and a
 * drawing algorithm for hexes.
 * 
 * @author Marko Tuominen
 * @see FilledRowHexGrid
 */
public class Hex extends Tile {

	/**
	 * Declares the number of points and sides to a hexagon: six.
	 */
    public static final int HEX_SIZE = 6;

	/**
	 * Gets an array with the six directions of a hex's sides. The directions are in clockwise
	 * order, starting from northeast.
	 * 
	 * @return			directions of a hex's sides
	 */
	public static Direction[] getHexSides() {
		return new Direction[] { NORTHEAST, EAST, SOUTHEAST, SOUTHWEST, WEST, NORTHWEST };
	}

	/**
	 * Gets an array with the six directions of a hex's points. They're in clockwise order,
	 * starting from north.
	 * 
	 * @return			directions of a hex's points
	 */
	public static Direction[] getHexPoints() {
		return new Direction[] { NORTH, NORTHEAST, SOUTHEAST, SOUTH, SOUTHWEST, NORTHWEST };
	}
	
	private double[] xpoints, ypoints;

    /**
     * Constructs a new hex at a particular location. The hex initializes its corner coordinates
     * based on the constructor parameters. The hex will be horizontally and vertically symmetric,
     * but whether or not it's regular depends on the width and height parameters. Hexes are meant
     * to be created by a hex grid, not directly.
     * 
     * @param x				x-coordinate for the hex's top point
     * @param y				y-coordinate for the hex's top point
     * @param width			the hex's width
     * @param height		the hex's height
     * @param row			the hex's row coordinate
     * @param col			the hex's column coordinate
     * @see					FilledRowHexGrid
     */
    Hex( double x, double y, double width, double height, int row, int col ) {
    	super( Hex.class, row, col );
    	
    	xpoints = new double[ HEX_SIZE ];
    	ypoints = new double[ HEX_SIZE ];
    	
    	xpoints[ NORTH.hexPoint() ] = xpoints[ SOUTH.hexPoint() ] = x;
    	xpoints[ NORTHWEST.hexPoint() ] = xpoints[ SOUTHWEST.hexPoint() ] = x - width / 2.0;
    	xpoints[ NORTHEAST.hexPoint() ] = xpoints[ SOUTHEAST.hexPoint() ] = x + width / 2.0;
    	
    	ypoints[ NORTH.hexPoint() ] = y;
    	ypoints[ NORTHWEST.hexPoint() ] = ypoints[ NORTHEAST.hexPoint() ] = y + height * 0.25;
    	ypoints[ SOUTHWEST.hexPoint() ] = ypoints[ SOUTHEAST.hexPoint() ] = y + height * 0.75;
    	ypoints[ SOUTH.hexPoint() ] = y + height;
    }
    
    @Override
    public double getWidth() {
    	return xpoints[ NORTHEAST.hexPoint() ] - xpoints[ NORTHWEST.hexPoint() ];
    }
    
    @Override
    public double getHeight() {
    	return ypoints[ SOUTH.hexPoint() ] - ypoints[ NORTH.hexPoint() ];
    }

    @Override
	public Direction[] getPointDirections() {
    	return getHexPoints();
	}

	/**
     * Gets coordinates for one of this hex's points. Hexes don't have points to the west and east.
     */
    @Override
    public Point getPoint( Direction corner ) {
    	if (corner == null || corner.isHorizontal())
    		return null;
    	
    	return Point.createPoint( xpoints[ corner.hexPoint() ], ypoints[ corner.hexPoint() ]);
    }
    
    /**
     * Gets the center point for this hex.
     */
    @Override
    public Point getCenter() {
    	return Point.createPoint( xpoints[ NORTH.hexPoint() ],
        		(ypoints[ NORTH.hexPoint() ] + ypoints[ SOUTH.hexPoint() ]) / 2 );
    }

    /**
     * Tests if this hex contains a pair of coordinates. A hex doesn't have neighbors to the north
     * or to the south, so returns northeast/southeast if the x-coordinate is the same as this
     * hex's center's, but the coordinates are not in this hex.
     */
	@Override
	public Hex containsCoords( double x, double y ) {
		Point center = getCenter();

		// coordinates on vertical line with center
		if (x == center.getX()) {
			if (y < ypoints[ NORTH.hexPoint() ])
				return getNeighbor( NORTHEAST );
			if (y > ypoints[ SOUTH.hexPoint() ])
				return getNeighbor( SOUTHEAST );
			return this;
		}

		// coordinates are to the left (west) of center
		else if (x < center.getX()) {			
			// use turn direction test to see if the coordinates are within the
			// hex's northwest sector (one sixth of the hex, facing northwest neighbor)
			if (getTurnDir( center.getX(), center.getY(), xpoints[ NORTHWEST.hexPoint() ],
					ypoints[ NORTHWEST.hexPoint() ], x, y ) > 0)

				// then use the turn direction test to see if the coordinates are within
				// the hex or outside (in the hex's northwest neighbor or further away)
				return (getTurnDir( xpoints[ NORTH.hexPoint() ], ypoints[ NORTH.hexPoint() ],
						xpoints[ NORTHWEST.hexPoint() ], ypoints[ NORTHWEST.hexPoint() ],
						x, y ) > 0 ? getNeighbor( NORTHWEST ) : this);

			// do the same test for southwest quadrant, center to southwest point and turning left
			else if (getTurnDir( center.getX(), center.getY(), xpoints[ SOUTHWEST.hexPoint() ],
					ypoints[ SOUTHWEST.hexPoint() ], x, y ) < 0)
				return (getTurnDir( xpoints[ SOUTH.hexPoint() ], ypoints[ SOUTH.hexPoint() ],
						xpoints[ SOUTHWEST.hexPoint() ], ypoints[ SOUTHWEST.hexPoint() ],
						x, y ) < 0 ? getNeighbor( SOUTHWEST ) : this);

			// coords are within west quadrant, testing is easier since the west side is vertical
			else return x < getPoint( NORTHWEST ).getX() ? getNeighbor( WEST ) : this;
		}

		// coordinates are to the right (east) of center
		else {
			// test northeast
			if (getTurnDir(  center.getX(), center.getY(), xpoints[ NORTHEAST.hexPoint() ],
					ypoints[ NORTHEAST.hexPoint() ], x, y ) < 0)
				return (getTurnDir( xpoints[ NORTH.hexPoint() ], ypoints[ NORTH.hexPoint() ],
						xpoints[ NORTHEAST.hexPoint() ], ypoints[ NORTHEAST.hexPoint() ],
						x, y ) < 0 ?
							getNeighbor( NORTHEAST ) : this);

			// test southeast
			else if (getTurnDir(  center.getX(), center.getY(), xpoints[ SOUTHEAST.hexPoint() ],
					ypoints[ SOUTHEAST.hexPoint() ], x, y ) > 0)
				return (getTurnDir( xpoints[ SOUTH.hexPoint() ], ypoints[ SOUTH.hexPoint() ],
						xpoints[ SOUTHEAST.hexPoint() ], ypoints[ SOUTHEAST.hexPoint() ],
						x, y ) > 0 ?
							getNeighbor( SOUTHEAST ) : this);

			// test east
			else return (x > getPoint( NORTHEAST ).getX()) ? getNeighbor( EAST ) : this;
		}
	}

	/**
	 * Gets a neighboring hex.
	 */
	@Override
	public Hex getNeighbor( Direction direction ) {
		return (Hex)super.getNeighbor( direction );
	}

	/**
	 * Gets neighboring hexes.
	 */
	@Override
	public Hex[] getNeighbors() {
		return (Hex[])super.getNeighbors();
	}

	/**
	 * Gets remote neighbor hexes.
	 */
	@Override
	public Hex[] getRemoteNeighbors() {
		return (Hex[])super.getRemoteNeighbors();
	}

	/**
	 * Adds a hex which is accessible from this hex even though it isn't adjacent.
	 * 
	 * @param neighbor			add this as a remote neighbor
	 */
	public void addRemoteNeighbor( Hex neighbor ) {
		super.addRemoteNeighbor( neighbor );
	}

	/**
	 * Sets a neighbor. North and south are illegal directions for neighbors.
	 * 
	 * @param neighbor			neighboring hex
	 * @param direction			direction where the neighbor resides
	 */
	void setNeighbor( Hex neighbor, Direction direction ) {
		if (!direction.isVertical())
			super.setNeighbor( neighbor, direction );
	}

    /**
     * Uses 2D cross product to determine the direction of the turn in point q.
     * 
     * @param px		starting point x-coordinate
     * @param py		starting point y-coordinate
     * @param qx		turning point x-coordinate
     * @param qy		turning point y-coordinate
     * @param rx		ending point x-coordinate
     * @param ry		ending point y-coordinate
     * @return			negative for left turn, positive for right turn, zero for no turn
     */
    private int getTurnDir( double px, double py, double qx, double qy, double rx, double ry ) {
    	
    	// note that y coordinate increases downwards, not up
    	return (int)Math.round( (rx - px) * (py - qy) - (py - ry) * (qx - px) );
    }
}
