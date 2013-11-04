package fi.grimripper.loww.tiles;

import static fi.grimripper.loww.Direction.EAST;
import static fi.grimripper.loww.Direction.NORTHEAST;
import static fi.grimripper.loww.Direction.NORTHWEST;
import static fi.grimripper.loww.Direction.SOUTHEAST;
import static fi.grimripper.loww.Direction.SOUTHWEST;
import static fi.grimripper.loww.Direction.WEST;
import static java.lang.Math.PI;
import fi.grimripper.loww.ArrayUtilities;
import fi.grimripper.loww.Direction;
import fi.grimripper.loww.Point;

/**
 * A hex grid with pre-created hexes in rows. Even and odd rows can have different numbers of
 * hexes. In order to create a horizontally symmetric grid, either ones should have one less hex
 * than the others. If the difference is greater than one, the longer row length will be reduced to
 * one greater than the shorter row length. This prevents hexes from "dangling" outside the grid.
 * If the rows are of even length, even rows will be half a hex's width to the left of odd rows,
 * because hexes don't fit directly above or below each other.
 * 
 * @author Marko Tuominen
 * @see		Hex
 */
public class FilledRowHexGrid extends TileGrid <Hex> {

	/**
	 * Creates a new filled row hex grid using maximum dimensions for a hex. The hex's width and
	 * height are calculated so that the hex is regular and fits within the given maximum bounds.
	 * All hexes in the grid are the same size.
	 * 
	 * @param maxHexWidth		a hex's maximum width
	 * @param maxHexHeight		a hex's maximum height
	 * @param hexRows			number of rows in the grid
	 * @param evenRowHexes		number of hexes on an even row
	 * @param oddRowHexes		number of hexes on an odd row
	 * @return					a new filled row hex grid with given parameters
	 * @see						#calculateRegularHexDimensions(double, double)
	 */
	public static FilledRowHexGrid createWithHexSize( double maxHexWidth, double maxHexHeight,
			int hexRows, int evenRowHexes, int oddRowHexes ) {
		double[] hexDimensions = calculateRegularHexDimensions( maxHexWidth, maxHexHeight );
		return new FilledRowHexGrid(
				hexDimensions[0], hexDimensions[1], hexRows, evenRowHexes, oddRowHexes );
	}

	/**
	 * Creates a new filled row hex grid using maximum dimensions for the grid. A hex's width and
	 * height are calculated so that the hex is regular and all hexes fit within the given maximum
	 * grid bounds. All hexes in the grid are the same size.
	 * 
	 * @param maxGridWidth		the hex grid's maximum width
	 * @param maxGridHeight		the hex grid's maximum height
	 * @param hexRows			number of rows in the grid
	 * @param evenRowHexes		number of hexes on an even row
	 * @param oddRowHexes		number of hexes on an odd row
	 * @return					a new filled row hex grid with given parameters
	 * @see						#calculateGridHexDimensions(double, double, int, int, int)
	 */
	public static FilledRowHexGrid createWithGridSize( double maxGridWidth, double maxGridHeight,
			int hexRows, int evenRowHexes, int oddRowHexes ) {
		double[] hexDimensions = calculateGridHexDimensions(
				maxGridWidth, maxGridHeight, hexRows, evenRowHexes, oddRowHexes );
		return new FilledRowHexGrid(
				hexDimensions[0], hexDimensions[1], hexRows, evenRowHexes, oddRowHexes );
	}
	
	/**
	 * Calculates dimensions for a regular hex so that it fits inside given bounds. Either the
	 * hex's width or height will be equal to corresponding parameter. The one that allows smaller
	 * regular hexes will be selected. The other dimension will be equal to or less than the
	 * parameter.
	 * 
	 * @param maxWidth			maximum width for a hex
	 * @param maxHeight			maximum height for a hex
	 * @return					dimensions (width first, height second) for a hex so that it's
	 * 							regular and fits inside given dimensions
	 */
	public static double[] calculateRegularHexDimensions( double maxWidth, double maxHeight ) {
		return calculateGridHexDimensions( maxWidth, maxHeight, 1, 1, 1 );
	}
	
	/**
	 * Calculates maximum dimensions for a regular hex so that a hex grid fits inside given bounds.
	 * A grid's size will be determined by width or height, selecting the one which allows smaller
	 * regular hexes. Either the grid's width or height will be equal to the corresponding
	 * parameter. The other dimension will be equal to or less than the corresponding parameter.
	 * 
	 * @param maxWidth			maximum width for a hex grid
	 * @param maxHeight			maximum height for a hex grid
	 * @param hexRows			number of rows in the grid
	 * @param evenRowHexes		number of hexes on even rows, starting from the top row
	 * @param oddRowHexes		number of hexes on odd rows
	 * @return					dimensions (width first, height second) for a hex so that a grid of
	 * 							the given size fits within the parameter dimensions
	 */
	public static double[] calculateGridHexDimensions( double maxWidth, double maxHeight,
			int hexRows, int evenRowHexes, int oddRowHexes ) {

		// limit widths: maximum difference in number of hexes between rows is one
		if (hexRows > 1) {
			evenRowHexes = Math.min( evenRowHexes, oddRowHexes + 1 );
			oddRowHexes = Math.min( oddRowHexes, evenRowHexes + 1 );
		}
		
		double maxHexes = Math.max( evenRowHexes, oddRowHexes );	// calculate with the longer
		
		if (evenRowHexes == oddRowHexes && hexRows > 1)
			maxHexes += 0.5;		// displaced by the width of half a hex
		
    	double maxHexHeight = (double)maxHeight / (0.75 * (hexRows - 1) + 1);
    	double widthForHeight = Math.sqrt( 3.0 ) / 2.0 * maxHexHeight;
    	
    	double maxHexWidth = maxWidth / maxHexes;
    	double heightForWidth = 2.0 / Math.sqrt( 3.0 ) * maxHexWidth;
    	
    	// select smaller dimension to define the map's size
    	if (widthForHeight < maxHexWidth)
    		return new double[] { widthForHeight, maxHexHeight };
    	
    	else
    		return new double[] { maxHexWidth, heightForWidth };
	}

    private int hexRows = 0;
    private int evenRowHexes = 0;
    private int oddRowHexes = 0;

    private Hex[][] hexes = null;
    
	/**
	 * Constructs a new filled row hex grid with given hex and grid dimensions.
	 * 
	 * @param hexWidth			set the width of a hex
	 * @param hexHeight			set the height of a hex
	 * @param hexRows			number of hex rows in the grid
	 * @param evenRowHexes		number of hexes on an even-numbered row (starting on the first row)
	 * @param oddRowHexes		number of hexes on an odd-numbered row (starting on the second row)
	 * @see						#createWithGridSize(double, double, int, int, int)
	 * @see						#createWithHexSize(double, double, int, int, int)
	 */
	private FilledRowHexGrid( double hexWidth, double hexHeight, int hexRows, int evenRowHexes,
			int oddRowHexes ) {
		super( hexWidth, hexHeight );
		
		if (hexRows > 1) {
			evenRowHexes = Math.min( evenRowHexes, oddRowHexes + 1 );
			oddRowHexes = Math.min( oddRowHexes, evenRowHexes + 1 );
		}
		
		this.hexRows = hexRows;
		this.evenRowHexes = evenRowHexes;
		this.oddRowHexes = oddRowHexes;
		
		hexes = new Hex[ hexRows ][];
		
		// calculate coordinates in a way that allows switching number of hexes on even/odd rows
		double evenRowTopX = evenRowHexes < oddRowHexes ? getTileWidth() : getTileWidth() / 2;
		double oddRowTopX = oddRowHexes <= evenRowHexes ? getTileWidth() : getTileWidth() / 2;
		double yShift = 0.75 * getTileHeight();
		
		// create hexes for even rows
		double topY = 0;
		for (int i = 0; i < hexRows; i += 2, topY += yShift * 2) {
			double topX = evenRowTopX;
			hexes[i] = new Hex[ evenRowHexes ];
			
			for (int j = 0; j < hexes[i].length; j++, topX += getTileWidth())
				hexes[i][j] = new Hex( topX, topY, getTileWidth(), getTileHeight(), i, j );
		}
		
		// create hexes for odd rows
		topY = yShift;
		for (int i = 1; i < hexRows; i += 2, topY += yShift * 2) {
			double topX = oddRowTopX;
			hexes[i] = new Hex[ oddRowHexes ];
			
			for (int j = 0; j < hexes[i].length; j++, topX += getTileWidth())
				hexes[i][j] = new Hex( topX, topY, getTileWidth(), getTileHeight(), i, j );
		}
		
		// assign west neighbors
		for (int i = 0; i < hexes.length; i++)
			for (int j = 1; j < hexes[i].length; j++)
				hexes[i][j].setNeighbor( hexes[i][j - 1], WEST );
		
		// assign east neighbors
		for (int i = 0; i < hexes.length; i++)
			for (int j = 0; j < hexes[i].length - 1; j++)
				hexes[i][j].setNeighbor( hexes[i][j + 1], EAST );
		
		// assign northwest neighbors
		int first = oddRowHexes > evenRowHexes ? 1 : 0;
		for (int i = 1; i < hexes.length; i++, first++)
			for (int j = first % 2, k = 0; j < hexes[i].length; j++, k++)
				hexes[i][j].setNeighbor( hexes[i - 1][k], NORTHWEST );
		
		// assign northeast neighbors
		first = oddRowHexes > evenRowHexes ? 0 : 1;
		for (int i = 1; i < hexes.length; i++, first++)
			for (int j = 0, k = first % 2; k < hexes[i - 1].length; j++, k++)
				hexes[i][j].setNeighbor( hexes[i - 1][k], NORTHEAST );
		
		// assign southwest neighbors
		first = oddRowHexes > evenRowHexes ? 0 : 1;
		for (int i = 0; i + 1 < hexes.length; i++, first++)
			for (int j = first % 2, k = 0; j < hexes[i].length; j++, k++)
				hexes[i][j].setNeighbor( hexes[i + 1][k], SOUTHWEST );
		
		// assign southeast neighbors
		first = oddRowHexes > evenRowHexes ? 1 : 0;
		for (int i = 0; i + 1 < hexes.length; i++, first++)
			for (int j = 0, k = first % 2; k < hexes[i + 1].length; j++, k++)
				hexes[i][j].setNeighbor( hexes[i + 1][k], SOUTHEAST );
	}

	@Override
	public int getTotalWidth() {
		return (int)Math.ceil( (evenRowHexes != oddRowHexes ?
			Math.max( evenRowHexes, oddRowHexes ) : evenRowHexes + 0.5) * getTileWidth() );
	}

	@Override
	public int getTotalHeight() {
		return (int)Math.ceil( (0.75 * (hexRows - 1) + 1) * getTileHeight() );
	}

	@Override
	public int getTileCount() {
		return (hexRows / 2 + hexRows % 2) * evenRowHexes + hexRows / 2 * oddRowHexes;
	}

    /**
     * Gets a hex by x- and y-coordinates. Valid coordinates are non-negative. Maximum x- and
     * y-coordinates depend on the size and number of hexes. Also, there will always be areas near
     * the top and bottom of the grid, and to the left and right, where there is no hex.
     */
	@Override
    public Hex getTileAtXY( double x, double y ) {
		if (x < 0 || y < 0)
			return null;
		
		// reduce y for first quarter hex height, then divide by 3/4 of a hex's height (row height)
    	int row = (int)((y - getTileHeight() / 4) / 0.75 / getTileHeight());
    	if (row >= hexes.length)		// coordinates in first 1/4 hex height truncate to zero
    		return null;
    	
    	// row begins at zero or half a hex's width, compensate for this; then divide by hex width
    	int col = (int)((x - hexes[ row ][0].getPoint( NORTHWEST ).getX()) / getTileWidth());
    	if (col >= hexes[ row ].length)
    		return null;
    	
    	// hexes handle areas between top/bottom row hexes and half-hex empty areas on the sides
    	return hexes[ row ][ col ].containsCoords( x, y );
    }
    
	/**
	 * Gets a hex by row and column as coordinates. Valid coordinates are non-negative, row index
	 * must be less than number of rows, and column index must be less than number of hexes on the
	 * row.
	 */
    @Override
	public Hex getTileAtRC( int row, int col ) {
		if (row < 0 || row >= hexRows || col < 0 || col >= hexes[ row ].length)
			return null;
		
		return hexes[ row ][ col ];
	}

	/**
     * Gets hexes in the grid ordered by rows, left to right and top to bottom.
     */
	@Override
    public Hex[][] getTiles() {
    	return ArrayUtilities.copy2D( hexes );
    }

	@Override
	public LineHelper <Hex> createLineHelper( Hex from, Hex to ) {
		return new HexLineHelper( this, from, to );
	}

	/**
	 * Line helper implementation with regular hex geometry.
	 * 
	 * @author Marko Tuominen
	 */
	private class HexLineHelper extends LineHelper <Hex> {

		/**
		 * Constructs a line helper for hexes.
		 * 
		 * @param from		line's starting hex
		 * @param to		line's end hex, or a hex that determines direction
		 */
		private HexLineHelper( TileGrid <Hex> grid, Hex from, Hex to ) {
			super( grid, from, to );
		}

		/**
		 * Three different cases are possible for directions. There are six directions where the
		 * line goes directly from hex to hex and only one direction is necessary. There are also
		 * six directions where it alternates between one and two hexes. These can be handled as
		 * indirect lines, with two directions. Indirect lines get two directions based on the 60
		 * degree sector it passes through. The 120 degree sectors on the left and right have west
		 * and east as primary directions, respectively. Secondary direction is one of the minor
		 * compass directions: northwest, northeast, southwest and southeast. Through the 60 degree
		 * sector upwards, the directions are northwest and northeast, and likewise southwest and
		 * southeast downwards. North and south are not possible directions.
		 */
		@Override
		protected Direction[] determineLineDirections( Hex from, Hex to, double xDiff,
				double yDiff ) {

	    	// use angle to determine possible choices for next hex in the path
			double angle = getAngle( from, to );
			Direction[] dirs = { WEST, NORTHWEST, NORTHEAST, EAST, SOUTHEAST, SOUTHWEST };
			
			for (int i = 0; i < dirs.length; i++) {
				
				// direct hex lines are at multiples of PI/3
				double dAngle = PI - i * PI / 3;
				
				if (angle < dAngle + 0.01 && angle > dAngle - 0.01)
					return new Direction[] { dirs[i], null };		// direct line of hexes

				// alternating hex lines are at straight angles, and +/- PI/6 from them
				double aAngle = (5 - 2 * i) * PI / 6;
				if (angle < aAngle + 0.01 && angle > aAngle - 0.01)
					return new Direction[] { dirs[i], dirs[ (i + 1) % dirs.length ]};
			}
			
	    	double slope = yDiff / xDiff;		// estimate best directions from slope
	    	Direction option1 = null, option2 = null;
	    	
	    	// slope from center to diagonal neighbor's center is abs(sqrt(3))
	    	// abs(slope) < sqrt(3) => line is drawn through the 120 deg segment on either side
	    	if (Math.abs( slope ) < Math.sqrt( 3 )) {
	    		option1 = xDiff > 0 ? EAST : WEST;

	    		if (yDiff > 0)
	    			option2 = (xDiff > 0) ? SOUTHEAST : SOUTHWEST;
	    		else
	    			option2 = (xDiff > 0) ? NORTHEAST : NORTHWEST;
	    	}
	    	
	    	// abs(slope) > sqrt(3) => line is drawn through the 60 deg segment up or down
	    	else if (yDiff > 0) {
	    		option1 = SOUTHWEST;
	    		option2 = SOUTHEAST;
	    	}
	    	
	    	// positive y <=> appr. downwards / negative y <=> appr. upwards
	    	else {
	    		option1 = NORTHEAST;
	    		option2 = NORTHWEST;
	    	}
	    	
	    	return new Direction[] { option1, option2 };
		}

		@Override
		protected Point estimateCenter( Point fromCenter, Direction toDir ) {
			Point newCenter = Point.createPoint( fromCenter );
			
			// for horizontal neighbors, only x differs by width
			if (EAST.equals( toDir ))
				newCenter.setX( newCenter.getX() + getTileWidth() );
			
			else if (WEST.equals( toDir ))
				newCenter.setX( newCenter.getX() - getTileWidth() );
			
			else {		// diagonal neighbors
				double xShift = getTileWidth() * 0.5;		// x-difference is half width
				double yShift = getTileHeight() * 0.75;		// y-difference is 3/4 of height
				
				newCenter.setX( newCenter.getX() + (toDir.isDueWest() ? -xShift : xShift) );
				newCenter.setY( newCenter.getY() + (toDir.isDueNorth() ? -yShift : yShift) );
			}
			
			return newCenter;
		}
	}
}
