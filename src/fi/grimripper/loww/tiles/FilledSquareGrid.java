package fi.grimripper.loww.tiles;

import static fi.grimripper.loww.Direction.EAST;
import static fi.grimripper.loww.Direction.NORTH;
import static fi.grimripper.loww.Direction.NORTHEAST;
import static fi.grimripper.loww.Direction.NORTHWEST;
import static fi.grimripper.loww.Direction.SOUTH;
import static fi.grimripper.loww.Direction.SOUTHEAST;
import static fi.grimripper.loww.Direction.SOUTHWEST;
import static fi.grimripper.loww.Direction.WEST;
import static java.lang.Math.PI;
import fi.grimripper.loww.ArrayUtilities;
import fi.grimripper.loww.Direction;
import fi.grimripper.loww.Point;

/**
 * A rectangular grid of squares with squares for all positions created in advance.
 * 
 * @author Marko Tuominen
 */
public class FilledSquareGrid extends TileGrid <Square> {

	private int squareRows = 0;
	private int squareColumns = 0;

	private Square[][] squares = null;

	/**
	 * Constructs a new square grid with configurable dimensions.
	 * 
	 * @param squareDimension		a square's width and height (they are equal)
	 * @param rows					number of rows in the grid
	 * @param columns				number of columns in the grid
	 */
	public FilledSquareGrid( int squareDimension, int rows, int columns ) {
		super( squareDimension, squareDimension );

		squareRows = rows;
		squareColumns = columns;
		squares = new Square[ rows ][ columns ];

		for (int i = 0, y = 0; i < rows; i++, y += squareDimension)		// create the squares
			for (int j = 0, x = 0; j < columns; j++, x += squareDimension)
				squares[i][j] = new Square( x, y, squareDimension, i, j );

		for (int i = 1; i < rows; i++)			// connect neighbors to the north
			for (int j = 0; j < columns; j++)
				squares[i][j].setNeighbor( squares[i - 1][j], NORTH );

		for (int i = 0; i + 1 < rows; i++)		// connect neighbors to the south
			for (int j = 0; j < columns; j++)
				squares[i][j].setNeighbor( squares[i + 1][j], SOUTH );

		for (int i = 0; i < rows; i++)			// connect neighbors to the west
			for (int j = 1; j < columns; j++)
				squares[i][j].setNeighbor( squares[i][j - 1], WEST );

		for (int i = 0; i < rows; i++)			// connect neighbors to the east
			for (int j = 0; j + 1 < columns; j++)
				squares[i][j].setNeighbor( squares[i][j + 1], EAST );

		for (int i = 1; i < rows; i++)			// connect neighbors to the northwest
			for (int j = 1; j < columns; j++)
				squares[i][j].setNeighbor( squares[i - 1][j - 1], NORTHWEST );

		for (int i = 1; i < rows; i++)			// connect neighbors to the northeast
			for (int j = 0; j + 1 < columns; j++)
				squares[i][j].setNeighbor( squares[i - 1][j + 1], NORTHEAST );

		for (int i = 0; i + 1 < rows; i++)		// connect neighbors to the southwest
			for (int j = 1; j < columns; j++)
				squares[i][j].setNeighbor( squares[i + 1][j - 1], SOUTHWEST );

		for (int i = 0; i + 1 < rows; i++)		// connect neighbors to the southeast
			for (int j = 0; j + 1 < columns; j++)
				squares[i][j].setNeighbor( squares[i + 1][j + 1], SOUTHEAST );
	}

	@Override
	public int getTotalWidth() {
		return (int)Math.ceil( squareColumns * getTileWidth() );
	}

	@Override
	public int getTotalHeight() {
		return (int)Math.ceil( squareRows * getTileHeight() );
	}

	@Override
	public int getTileCount() {
		return squareRows * squareColumns;
	}

	/**
	 * Gets a square by x- and y-coordinates. Coordinates can't be negative, and they must be less
	 * than the grid's width (x) or height (y).
	 */
	@Override
	public Square getTileAtXY( double x, double y ) {
		if (x < 0 || y < 0 || (x /= getTileWidth()) >= squareColumns ||
				(y /= getTileHeight()) >= squareRows)
			return null;

		return squares[ (int)y ][ (int)x ];
	}

	/**
	 * Gets a square by row and column as coordinates. Valid coordinates are non-negative.
	 * Row coordinate must be less than number of rows, and column coordinate less than number of
	 * columns.
	 */
	@Override
	public Square getTileAtRC( int row, int col ) {
		return row < 0 || row >= squareRows || col < 0 || col >= squareColumns ? null :
			squares[ row ][ col ];
	}

	/**
	 * Gets tiles as an array of rows, each an array of columns.
	 */
	@Override
	public Square[][] getTiles() {
		return ArrayUtilities.copy2D( squares );
	}

	@Override
	public LineHelper <Square> createLineHelper( Square from, Square to ) {
		return new SquareLineHelper( this, from, to );
	}

	/**
	 * Line helper implementation with square geometry.
	 * 
	 * @author Marko Tuominen
	 */
	private class SquareLineHelper extends LineHelper <Square> {

		/**
		 * Constructs a line helper for squares.
		 * 
		 * @param from		line's starting square
		 * @param to		line's end square, or a hex that determines direction
		 */
		private SquareLineHelper( TileGrid <Square> grid, Square start, Square end ) {
			super( grid, start, end );
		}

		/**
		 * There are eight possible sectors, divided by horizontal, vertical and diagonal lines.
		 * Along each of these lines, a series of squares can be selected by getting each square's
		 * neighbor to the line's direction. Otherwise two directions are selected. Each of the
		 * quadrants divided by diagonal lines corresponds to one of the major compass directions:
		 * north, east, south or west.  Each of the quadrants divided by horizontal and vertical
		 * lines corresponds to one of the minor compass directions: northeast, southeast,
		 * southwest or northwest.
		 */
		@Override
		protected Direction[] determineLineDirections( Square from, Square to, double xDiff,
				double yDiff ) {
			
			// use angle to determine direction on a direct path
			double angle = getAngle( from, to );
			Direction[] dirs =
				{ WEST, NORTHWEST, NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST };

			for (int i = 0; i < dirs.length; i++) {

				// direct square lines are at multiples of PI/4 (horizontal, vertical, diagonal)
				double dAngle = PI - i * PI / 4;

				if (angle < dAngle + 0.01 && angle > dAngle - 0.01)
					return new Direction[] { dirs[i], null };		// direct line of squares
			}

			// line doesn't match any of the eight compass directions
			double slope = yDiff / xDiff;		// estimate best directions from slope
			Direction option1 = null, option2 = null;
			
			if (slope > 1 || slope < -1)
				option1 = (yDiff > 0) ? SOUTH : NORTH;
			else
				option1 = (xDiff > 0) ? EAST : WEST;

			if (yDiff > 0)
				option2 = (xDiff > 0) ? SOUTHEAST : SOUTHWEST;
			else
				option2 = (xDiff > 0) ? NORTHEAST : NORTHWEST;
			
			return new Direction[] { option1, option2 };
		}

		@Override
		protected Point estimateCenter( Point fromCenter, Direction toDir ) {
			Point newCenter = Point.createPoint( fromCenter );
			
			// center's x-coordinate can differ by +/- width, north and south not affected
			if (toDir.isDueEast())
				newCenter.setX( newCenter.getX() + getTileWidth() );
			else if (toDir.isDueWest())
				newCenter.setX( newCenter.getX() - getTileWidth() );

			// y-coordinate can differ by +/- height, east and west not affected
			if (toDir.isDueSouth())
				newCenter.setY( newCenter.getY() + getTileHeight() );
			else if (toDir.isDueNorth())
				newCenter.setY( newCenter.getY() - getTileHeight() );

			return newCenter;
		}
	}
}
