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
 * Represents a single square in a tile grid. Each square has up to eight neighbors, including
 * diagonal ones. Minimum number of neighbors for a square in a rectangular grid is three:
 * horizontal, vertical and diagonal. This is a square in a corner of the tile grid. The width and
 * height of a square are equal, and each square has the same dimensions as every other square, but
 * their locations vary.
 * 
 * @author Marko Tuominen
 */
public class Square extends Tile {

	/**
	 * Declares the number of points and sides to a square: four.
	 */
	public static final int SQUARE_SIZE = 4;

	/**
	 * Declares the maximum number of adjacent neighbors a square can have: eight. This includes
	 * two horizontal and two vertical neighbors which share a side with the square, and four
	 * diagonal neighbors which share a point.
	 */
	public static final int MAX_NEIGHBORS = 8;
	
	/**
	 * Gets an array with the four directions of a square's points. They're in clockwise order,
	 * starting from northeast.
	 * 
	 * @return			directions of a square's points
	 */
	public static Direction[] getSquarePoints() {
		return new Direction[] { NORTHEAST, SOUTHEAST, SOUTHWEST, NORTHWEST };
	}

	/**
	 * Gets the directions where a square can have neighbors. Diagonal neighbors are allowed, so
	 * all eight major and minor compass directions are possible.
	 * 
	 * @return			possible directions for a square's neighbors
	 */
	public static Direction[] getNeighborDirections() {
		return Direction.values();
	}
	
	private double[] xpoints, ypoints;
	
	/**
	 * Constructs a new square at a particular location. The square initializes its corner
	 * coordinates based on the constructor parameters. Squares are meant to be created by a square
	 * grid, not directly.
	 * 
	 * @param x				x-coordinate for the square's top-left corner
	 * @param y				y-coordinate for the square's top-left corner	
	 * @param dimension		the square's width and height
	 * @param row			the square's row coordinate
	 * @param col			the square's column coordinate
	 * @see					FilledSquareGrid
	 */
	Square( double x, double y, double dimension, int row, int col ) {
		super( Square.class, row, col );
		
		xpoints = new double[ SQUARE_SIZE ];
		ypoints = new double[ SQUARE_SIZE ];
		
		xpoints[ NORTHWEST.squarePoint() ] = xpoints[ SOUTHWEST.squarePoint() ] = x;
		xpoints[ NORTHEAST.squarePoint() ] = xpoints[ SOUTHEAST.squarePoint() ] = x + dimension;
		
		ypoints[ NORTHWEST.squarePoint() ] = ypoints[ NORTHEAST.squarePoint() ] = y;
		ypoints[ SOUTHWEST.squarePoint() ] = ypoints[ SOUTHEAST.squarePoint() ] = y + dimension;
	}
	
	@Override
	public double getWidth() {
		return xpoints[ NORTHEAST.squarePoint() ] - xpoints[ NORTHWEST.squarePoint() ];
	}

	@Override
	public double getHeight() {
		return ypoints[ SOUTHWEST.squarePoint() ] - ypoints[ NORTHWEST.squarePoint() ];
	}

	@Override
	public Direction[] getPointDirections() {
		return getSquarePoints();
	}

	/**
	 * Gets one of the square's corner points. Squares have points in the four diagonal compass
	 * directions.
	 */
	@Override
	public Point getPoint( Direction corner ) {
		if (corner == null || corner.squarePoint() < 0)
			return null;
		
		return Point.createPoint(
				xpoints[ corner.squarePoint() ], ypoints[ corner.squarePoint() ]);
	}
	
	/**
	 * Gets the center point for this square.
	 */
	@Override
	public Point getCenter() {
		return Point.createPoint(
				(xpoints[ NORTHWEST.squarePoint() ] + xpoints[ NORTHEAST.squarePoint() ]) / 2,
				(ypoints[ NORTHWEST.squarePoint() ] + ypoints[ SOUTHWEST.squarePoint() ]) / 2 );
	}

	@Override
	public Square containsCoords( double x, double y ) {
		if (x < xpoints[ NORTHWEST.squarePoint() ])
			return getNeighbor( y < ypoints[ NORTHWEST.squarePoint() ] ? NORTHWEST :
				y > ypoints[ SOUTHWEST.squarePoint() ] ? SOUTHWEST : WEST );
		
		else if (x > xpoints[ NORTHEAST.squarePoint() ])
			return getNeighbor( y < ypoints[ NORTHEAST.squarePoint() ] ? NORTHEAST :
				y > ypoints[ SOUTHEAST.squarePoint() ] ? SOUTHEAST : EAST );
		
		else if (y < ypoints[ NORTHWEST.squarePoint() ])
			return getNeighbor( NORTH );
		
		else if (y > ypoints[ SOUTHWEST.squarePoint() ])
			return getNeighbor( SOUTH );
		
		else
			return this;
	}

	/**
	 * Gets a neighboring square.
	 */
	@Override
	public Square getNeighbor( Direction direction ) {
		return (Square)super.getNeighbor( direction );
	}

	/**
	 * Gets neighboring squares.
	 */
	@Override
	public Square[] getNeighbors() {
		return (Square[])super.getNeighbors();
	}

	/**
	 * Gets remote neighbor squares.
	 */
	@Override
	public Square[] getRemoteNeighbors() {
		return (Square[])super.getRemoteNeighbors();
	}

	/**
	 * Adds a square which is accessible from this square even though it isn't adjacent.
	 * 
	 * @param neighbor		add this as a remotely accessible square
	 */
	public void addRemoteNeighbor( Square neighbor ) {
		super.addRemoteNeighbor( neighbor );
	}

	/**
	 * Sets a neighbor. All directions are allowed.
	 * 
	 * @param neighbor			set this square as neighbor
	 * @param direction			set neighbor to this direction
	 */
	void setNeighbor( Square neighbor, Direction direction ) {
		super.setNeighbor( neighbor, direction );
	}
}
