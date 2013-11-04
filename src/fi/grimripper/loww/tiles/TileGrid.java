package fi.grimripper.loww.tiles;

import static fi.grimripper.loww.Direction.EAST;
import static fi.grimripper.loww.Direction.NORTH;
import static fi.grimripper.loww.Direction.NORTHEAST;
import static fi.grimripper.loww.Direction.NORTHWEST;
import static fi.grimripper.loww.Direction.SOUTH;
import static fi.grimripper.loww.Direction.SOUTHEAST;
import static fi.grimripper.loww.Direction.SOUTHWEST;
import static fi.grimripper.loww.Direction.WEST;
import static fi.grimripper.loww.Height.BLOCKING;
import static java.lang.Math.PI;

import java.lang.reflect.Array;
import java.util.ArrayList;

import fi.grimripper.loww.Direction;
import fi.grimripper.loww.Height;
import fi.grimripper.loww.Point;

/**
 * Abstract base class for a grid of tiles that are of equal shape and size.
 * 
 * @author Marko Tuominen
 * @param <T>			type of tile in the grid
 */
public abstract class TileGrid <T extends Tile> {

	/**
	 * Determines the direction from one tile to another. If the two tiles are given in reversed
	 * order, then the resulting direction is the opposite. In case the tiles are same, returns
	 * <code>null</code>.
	 * 
	 * @param from		find direction from this tile to the other one
	 * @param to		find direction from the other tile to this one
	 */
	public static Direction getDirection( Tile from, Tile to ) {
		Point tile1center = from.getCenter();
		Point tile2center = to.getCenter();

		if (tile1center.getX() == tile2center.getX())
			return (tile1center.getY() == tile2center.getY() ? null :
				tile1center.getY() > tile2center.getY() ? NORTH : SOUTH);
		
		if (tile1center.getY() == tile2center.getY())
			return (tile1center.getX() > tile2center.getX() ? WEST : EAST);

		double x = tile2center.getX() - tile1center.getX();
		double y = tile2center.getY() - tile1center.getY();

		// determine direction by angle
		double angle = Math.abs( Math.atan( y / x ));	// different x-coordinates, x != 0
		if (angle < PI / 8)
			return (x > 0 ? EAST : WEST);

		angle = Math.abs( angle - PI / 2 );
		if (angle < PI / 8)
			return (y > 0 ? SOUTH : NORTH);
		else if (y < 0)
			return (x > 0 ? NORTHEAST : NORTHWEST);
		else
			return (x > 0 ? SOUTHEAST : SOUTHWEST);
	}

	/**
	 * Determines the angle from one tile to another. The angle is reminiscent of the unit circle:
	 * angles go from east (zero) to west (pi), positive angles to the north, and negative angles
	 * to the south. Angles to the main compass directions are:
	 * <ul>
	 * <li>north = pi/2</li>
	 * <li>south = -pi/2</li>
	 * <li>west = pi</li>
	 * <li>east = 0</li>
	 * </ul>
	 * 
	 * @param from		get angle from this tile to another
	 * @param to		get angle from another tile to this one
	 * @return			the angle from the first tile to the second
	 */
	public static double getAngle( Tile from, Tile to ) {
		Point c1 = from.getCenter(), c2 = to.getCenter();

		if (c1.getX() == c2.getX())
			return c1.getY() > c2.getY() ? PI / 2 : -PI / 2;	// north & south

			double x = c2.getX() - c1.getX(), y = c2.getY() - c1.getY();
			double angle = Math.atan( y / x );

			if (c1.getX() > c2.getX())		// west (n:s)
				return c1.getY() >= c2.getY() ? PI - angle : -PI - angle;

				else							// east
					return -angle;
	}

	/**
	 * Determines if a line intersects the bounds of a tile.
	 * 
	 * @param tile		determine if the line intersects this tile's bounds
	 * @param p1		the line's start point
	 * @param p2		the line's end point
	 * @return			the line intersects the tile
	 * @see				#intersects(Tile, double, double, double, double)
	 */
	public static boolean intersects( Tile tile, Point p1, Point p2 ) {
		return intersects( tile, p1.getX(), p1.getY(), p2.getX(), p2.getY() );
	}
	
	/**
	 * Determines if a line intersects the bounds of a tile. Doesn't check if one of the tile's
	 * edges is along the line. The given coordinates must specify a line. A point isn't considered
	 * to intersect anything, even if it lies on an edge of the tile. Also can't correctly handle a
	 * tile which has adjacent points with the same x and y coordinates (zero-length tile edge).
	 * 
	 * @param tile		determine if the line intersects this tile's bounds
	 * @param x1		x-coordinate for the line's start point
	 * @param y1		y-coordinate for the line's start point
	 * @param x2		x-coordinate for the line's end point
	 * @param y2		y-coordinate for the line's end point
	 * @return			the line intersects the tile
	 */
	public static boolean intersects( Tile tile, double x1, double y1, double x2, double y2 ) {
		Direction[] pointDirs = tile.getPointDirections();

		double a1 = y2 - y1;
		double b1 = x1 - x2;
		double c1 = a1 * x1 + b1 * y1;

		for (int i = 0; i < pointDirs.length; i++) {
			Point first = tile.getPoint( pointDirs[i] );
			Point second = tile.getPoint( pointDirs[ (i + 1) % pointDirs.length ]);

			double a2 = second.getY() - first.getY();
			double b2 = first.getX() - second.getX();
			double det = a1 * b2 - a2 * b1;

			if (det != 0) {		// calculate intersection point for the two lines
				double c2 = a2 * first.getX() + b2 * first.getY();
				double x = (b2 * c1 - b1 * c2) / det;
				double y = (a1 * c2 - a2 * c1) / det;

				// must be within the tile edge, not on a parallel line outside the tile
				if (Math.min( first.getX(), second.getX() ) < x + 0.001 &&
						Math.max( first.getX(), second.getX() ) > x - 0.001 &&
						Math.min( first.getY(), second.getY() ) < y + 0.001 &&
						Math.max( first.getY(), second.getY() ) > y - 0.001)
					return true;
			}
		}

		return false;
	}

	/**
	 * Generates a path between two tiles, going neighbor to neighbor. The path is as straight as
	 * possible in the tile grid. A path from a tile to itself is an array with only the single
	 * tile. Otherwise, there's at least two tiles (when the path's ends are neighbors).
	 * <p>
	 * The path can also progress through the target tile and straight ahead until the tile grid's
	 * edge. In this case, the second parameter doesn't determine the path's end, but only the
	 * path's direction. The last tile in the path is the one farthest away from the starting tile
	 * among the tiles that intersect the direct line.
	 * <p>
	 * At each step, there are at most two alternatives for progress. The better one is selected,
	 * unless they're equal. In that case, they're both added to the path. The path effectively
	 * widens to two tiles for a step.
	 * 
	 * @param helper		a line helper with data about the direct line and tiles along it
	 * @param hitEdge		<code>true</code> to create a path all the way to a tile grid edge
	 * @return				the straightest possible tile path to the specified destination, or to
	 * 						a tile grid edge in the specified direction
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Tile> T[] getDirectPath( LineHelper <T> helper, boolean hitEdge ) {

		if (helper.getSource() == helper.getTarget()) {
			T[] retArr = (T[])Array.newInstance( helper.getSource().getClass(), 1 );
			retArr[0] = helper.getSource();
			return retArr;
		}

		ArrayList <T> path = new ArrayList <>();
		path.add( helper.getSource() );

		while ((!hitEdge && !helper.targetReached()) || (hitEdge && !helper.pathEnds())) {
			helper.nextTiles();
			T current = helper.getBetterTile( true );

			path.add( current );
			if (helper.tilesAreEqual() && (current = helper.getCurrentTile( false )) != null)
				path.add( current );
		}

		return path.toArray( (T[])Array.newInstance( helper.getSource().getClass(), path.size() ));
	}

	/**
	 * Finds out whether or not there's a line of sight between two tiles. Line of sight is
	 * determined for an obstacle, using its total height with occupy height, to a tile, using its
	 * terrain height.
	 * 
	 * @param helper	a line helper with data for the direct line and tiles along the line
	 * @param losFor	check line of sight for this obstacle
	 * @return			there are no obstacles blocking line of sight
	 * @see				#hasLineOfSight(LineHelper, Obstacle, Obstacle)
	 */
	public static boolean hasLineOfSight( LineHelper <?> helper, Obstacle losFor ) {
		return hasLineOfSight( helper, losFor, null );
	}

	/**
	 * Finds out whether or not there's a line of sight for an obstacle to another obstacle. Line
	 * of sight is along the most direct path between two tiles. Obstacles on that line can affect
	 * line of sight. If any obstacle blocks visibility, then there's no line of sight. The
	 * obstacle for which the line of sight is generated may influence the outcome, because
	 * obstacles can be biased about which other obstacles' line of sight they block. In addition
	 * to whether an obstacle blocks line of sight, its height determines if it's possible for it
	 * to block the line of sight. It is possible to see over obstacles that are low enough, even
	 * if those obstacles are opaque.
	 * <p>
	 * The seven height categories deep, shallow, flat, low, high, very high and blocking are used
	 * for height comparisons. An obstacle whose height is blocking can always block line of sight,
	 * and there's no viewing over it. Otherwise, the generic check of what is looked over uses the
	 * ratio of obstacle's height above lower of viewer and target height and the difference
	 * between target and viewer height in comparison to distance between the lower of viewer and
	 * target and the obstacle. If the height ratio is equal to or less than the distance
	 * normalized by full distance between viewer and target, it's possible to see over the
	 * obstacle.
	 * <p>
	 * From the generic check, if an obstacle is lower than either the viewer or the target, and
	 * at most as high as the other of them, it can always be looked over. The height ratio is at
	 * most zero. Likewise, if an obstacle's height is the same as the higher of viewer and target
	 * height, it can't be looked over because height ratio is at least one.
	 * <p>
	 * The direct line is determined by a line helper. Since a helper can't be reset, a new one
	 * should be used. Otherwise behavior is undefined. The viewer obstacle doesn't actually need
	 * to reside in the helper's starting tile, but it needs to be placed so that its occupy height
	 * can be used. The same applies to the target obstacle and the helper's target tile. Both of
	 * these obstacles are ignored along the line, in case they cover multiple tiles. An obstacle
	 * can't block its own line of sight, or another's line of sight to itself. The line helper's
	 * starting and target tiles don't affect the line of sight.
	 * 
	 * @param helper		a line helper with data for direct line and tiles along the line
	 * @param losFor		check line of sight for this obstacle
	 * @param losTo			check line of sight to this obstacle (if <code>null</code>, terrain
	 * 						height in the helper's target tile is used instead)
	 * @return				there are no obstacles blocking line of sight
	 */
	public static <T extends Tile> boolean hasLineOfSight( LineHelper <T> helper, Obstacle losFor,
			Obstacle losTo ) {
		if (helper.targetReached())
			return true;			// has line of sight to self

		Height targetHt = losTo != null ? losTo.getTotalHeight() :
			helper.getTarget().getTerrain().getHeight();
		double totalDist = helper.getLineLength();
		T primary = null, secondary = null;
		helper.nextTiles();
		
		while (!helper.targetReached()) {		// don't check first and last hexes

			// test preferred tile, or both if line goes through both
			if (blocksLineOfSight( primary = helper.getCurrentTile( true ), losFor, losTo,
					targetHt, helper.getDistanceToSource( primary ) / totalDist ) ||
					(secondary = helper.getBetterTile( false )) != null &&
					blocksLineOfSight( secondary, losFor, losTo, targetHt,
							helper.getDistanceToSource( secondary ) / totalDist ))
				return false;

			helper.nextTiles();
		}

		return true;
	}
	
	/**
	 * Tests if obstacles block line of sight for an obstacle.
	 * 
	 * @param tile			test if any obstacles in this tile block line of sight
	 * @param losFor		test if line of sight is blocked for this obstacle
	 * @param losTo			test if line of sight is blocked to this obstacle (can be
	 * 						<code>null</code>)
	 * @param toHt			test if line of sight to this height in the target tile is blocked
	 * @param dist			distance from starting tile to parameter tile, normalized to the range
	 * 						0..1 by total distance from starting tile to target tile
	 * @return				obstacles in the tile block line of sight
	 * @see					#hasLineOfSight(LineHelper, Obstacle, Obstacle)
	 */
	private static boolean blocksLineOfSight( Tile tile, Obstacle losFor, Obstacle losTo,
			Height toHt, double dist ) {
		Obstacle[] obstacles = tile.getObstacles();
		Height fromHt = losFor.getTotalHeight();
		
		for (Obstacle o : obstacles)
			if (o != losFor && o != losTo && o.blocksLineOfSight( losFor ) && canBlockLineOfSight(
					o.getTotalHeight(), fromHt, toHt, dist ))
				return true;	// one visibility block is enough
		
		return false;
	}
	
	/**
	 * Tests if an obstacle is high enough that it can't be looked over for a line of sight.
	 * Doesn't check if the obstacle blocks line of sight, and is unnecessary if the obstacle
	 * doesn't block the line of sight.
	 * 
	 * @param obstHeight		the obstacle's height
	 * @param fromHeight		check line of sight for an obstacle of this height
	 * @param toHeight			check line of sight to an obstacle of this height
	 * @param distance			distance from viewer to obstacle, normalized to the range 0..1 by
	 * 							total distance between viewer and target
	 * @return					obstacle is too high to be looked over, and can block line of sight
	 * @see						#hasLineOfSight(LineHelper, Obstacle, Obstacle)
	 */
	private static boolean canBlockLineOfSight( Height obstHeight, Height fromHeight,
			Height toHeight, double distance ) {
		if (obstHeight == BLOCKING || obstHeight.compareTo( fromHeight ) >= 0 &&
				obstHeight.compareTo( toHeight ) >= 0)
			return true;
		
		else if (obstHeight.compareTo( fromHeight ) <= 0 && obstHeight.compareTo( toHeight ) <= 0)
			return false;
		
		double heightRatio = (double)(obstHeight.ordinal() - Math.min( fromHeight.ordinal(),
				toHeight.ordinal() )) / Math.abs( fromHeight.ordinal() - toHeight.ordinal() );
		return (fromHeight.compareTo( toHeight ) < 0 ? distance :
			1 - distance) < heightRatio + 0.001;
	}
	
	private double tileWidth = 0;
	private double tileHeight = 0;

	/**
	 * Constructs a new tile grid, with given width and height for the tiles.
	 * 
	 * @param width				set the width of a tile
	 * @param height			set the height of a tile
	 */
	protected TileGrid( double width, double height ) {
		tileWidth = width;
		tileHeight = height;
	}

	/**
	 * Gets the width of a tile. All tiles in a grid have the same width.
	 * 
	 * @return			the width of a tile
	 */
	public double getTileWidth() {
		return tileWidth;
	}

	/**
	 * Gets the height of a tile. All tiles in a grid have the same height.
	 * 
	 * @return			the height of a tile
	 */
	public double getTileHeight() {
		return tileHeight;
	}

	/**
	 * Gets the tile grid's total width.
	 * 
	 * @return			total width for the tiles
	 */
	public abstract int getTotalWidth();

	/**
	 * Gets the tile grid's total height.
	 * 
	 * @return			total height for the tiles
	 */
	public abstract int getTotalHeight();

	/**
	 * Returns the number of tiles in the grid.
	 * 
	 * @return			number of tiles in the grid
	 */
	public abstract int getTileCount();

	/**
	 * Gets all of the tiles in the grid. Ordering is unspecified.
	 * 
	 * @return		the tiles in the grid
	 */
	public abstract T[][] getTiles();

	/**
	 * Gets a tile at a specific location by x- and y-coordinates. There can be some inaccuracy
	 * regarding the coordinates of a tile. An implementation can then select a tile as is most
	 * convenient. The coordinates can also be outside the grid, or a tile location can be empty.
	 * Then <code>null</code> is returned. Negative values can also be allowed, depending on
	 * implementation.
	 * 
	 * @param x			the tile's x-coordinate
	 * @param y			the tile's y-coordinate
	 * @return			tile at the given location (can be <code>null</code>)
	 */
	public abstract T getTileAtXY( double x, double y );

	/**
	 * Gets a tile at a specific location by row and column. The meaning of rows and columns can
	 * vary depending on the tile grid's implementation, but any coordinates will correspond to a
	 * specific tile. The coordinates can be outside the grid, or a tile location can be empty. Then
	 * <code>null</code> is returned. Negative values can also be allowed, depending on
	 * implementation.
	 * 
	 * @param row		the tile's row
	 * @param col		the tile's column
	 * @return			tile at the given location (can be <code>null</code>)
	 */
	public abstract T getTileAtRC( int row, int col );

	/**
	 * Gets a tile at a specific point.
	 * 
	 * @param point		a point inside the tile
	 * @return			tile at the point (can be <code>null</code>)
	 * @see				#getTileAtXY(double, double)
	 */
	public T getTileAtPoint( Point point ) {
		return point == null ? null : getTileAtXY( point.getX(), point.getY() );
	}
	
	/**
	 * Creates a line helper for this grid.
	 * 
	 * @param from		draw a line from this tile
	 * @param to		draw a line to this tile
	 * @return			a new line helper
	 * @see				LineHelper
	 */
	public abstract LineHelper <T> createLineHelper( T from, T to );
	
	/**
	 * Contains parameters and methods for selecting tiles along a direct line. Calculates the
	 * parameters of a direct line from starting tile to destination tile. Based on the line's
	 * parameters, selects the two closest directions from those associated with a tile's sides.
	 * <p>
	 * On a direct line of tiles, the helper needs to pick only one direction, and one tile per
	 * step. If the line doesn't go directly along a straight line of tiles, two tiles are needed
	 * per step. When there are two, the better option can be selected by calculating the distance
	 * from the tile's center to the actual direct line.
	 * 
	 * @author Marko Tuominen
	 */
	public abstract static class LineHelper <T extends Tile> {

		private boolean equalTiles = false;

		private double a = -1.0, b = -1.0, c = -1.0;
		private double normLen = -1.0;

		private Point source = null, target = null;
		private Direction option1 = null, option2 = null;

		private Point next1 = null, next2 = null;
		private Point current1 = null, current2 = null;
		private Point previous1 = null, previous2 = null;
		private Point better = null, worse = null;

		private TileGrid <T> grid = null;
		
		/**
		 * Constructs a line helper and sets parameters for handling the line.
		 * 
		 * @param from			the line's starting tile
		 * @param to			the tile that determines the line's direction
		 */
		protected LineHelper( TileGrid <T> grid, T from, T to ) {
			this.grid = grid;
			source = from.getCenter();
			target = to.getCenter();

			// properties used for calculations involving the line between the points
			a = source.getY() - target.getY();				// x multiplier (-delta Y)
			b = target.getX() - source.getX();				// y multiplier (delta X)
			c = source.getX() * target.getY() - target.getX() * source.getY();	// constant

			normLen = Math.hypot( a, b );					// distance between the two points

			if (from != to) {
				Direction[] dirs = determineLineDirections( from, to, b, -a );
				option1 = dirs[0];
				option2 = dirs[1];
			}
			
			better = current1 = source;
			if (option1 != null)
				next1 = estimateCenter( source, option1 );
			if (option2 != null)
				next2 = estimateCenter( source, option2 );
		}

		/**
		 * Selects next pair of tiles along the line. The secondary tile can be <code>null</code>
		 * after the selection when the line is near a grid edge. Also selects the better option of
		 * the next two tiles, if appropriate. The other tile is also prepared for retrieval
		 * through {@link #getBetterTile(boolean)}. In most cases, there are two options. Then the
		 * distance of each option's center to the line is calculated. The preferred tile is the
		 * one with the shorter distance to the line. In a case where there's only one tile that
		 * the line passes through, it is selected and the other tile will be <code>null</code>. In
		 * some cases, the two tiles can be equal. If the preferred tile is an imaginary one
		 * outside the tile grid, the preferred tile will be <code>null</code>. The other tile can
		 * in this case be something other than a <code>null</code>.
		 */
		public void nextTiles() {
			equalTiles = false;
			better = worse = null;

			if (option2 == null) {		// direct lines, no secondary tile
				previous1 = current1;
				better = current1 = next1;
				next1 = estimateCenter( current1, option1 );
				
				return;
			}

			// indirect lines ->
			previous1 = current1;
			previous2 = current2;

			current1 = next1;
			current2 = next2;

			// distances from current tile center points to the line
			double dist1 = getDistanceToLine( current1 );
			double dist2 = getDistanceToLine( current2 );

			if (dist1 > dist2 - 0.001 && dist1 < dist2 + 0.001)
				equalTiles = true;			// tiles are equidistant from line

			else if (dist1 > dist2) {		// update preferred tile
				Point point = current1;
				current1 = current2;
				current2 = point;
			}

			// compare tiles on either side of the line to decide if direction should change
			Point nextAlt1 = estimateCenter( current1, option1 );
			Point nextAlt2 = estimateCenter( current2, option2 );
			
			if (nextAlt1.approximateEquals( nextAlt2, 0.001 )) {
				nextAlt1 = estimateCenter( current2, option1 );
				nextAlt2 = estimateCenter( current1, option2 );
			}
			
			// distances from next tile center points to the line
			dist1 = getDistanceToLine( nextAlt1 );
			dist2 = getDistanceToLine( nextAlt2 );

			if (dist1 > dist2 + 0.001) {		// update preferred direction
				Direction dir = option1;
				option1 = option2;
				option2 = dir;
			}

			next1 = estimateCenter( current1, option1 );
			next2 = estimateCenter( current2, option1 );

			// switch primary and secondary tile if primary tile is outside the grid
			// at least one of the tiles is on the grid, or the helper has gone over grid edge
			if (getCurrentTile( true ) == null) {
				Point point = current1;
				current1 = current2;
				current2 = point;
			}
			
			better = current1;
			
			T secondary = null;
			
			// if tiles are equal, the second tile can't be off the line
			// doesn't handle tiles with an edge along the line, but then the tiles should be equal
			if (equalTiles || (secondary = grid.getTileAtPoint( current2 )) != null &&
					intersects( secondary, source, target ))
				worse = current2;		// null if doesn't intersect
		}

		/**
		 * Checks if the current primary tile is the target.
		 * 
		 * @return		the target tile is on the current step
		 */
		public boolean targetReached() {
			return target.approximateEquals( current1, 0.001 );
		}

		/**
		 * Checks if the line has reached a tile grid edge and can proceed no further.
		 * 
		 * @return		a tile grid edge has been reached
		 */
		public boolean pathEnds() {
			T next1 = getNextTile( true );
			T next2 = getNextTile( false );
			
			// quick check: line intersects if distance to center is at most half shorter dimension
			double shorter = Math.min( grid.getTileWidth(), grid.getTileHeight() ) / 2;
			if (next1 != null && getDistanceToLine( next1 ) < shorter + 0.001 ||
					next2 != null && getDistanceToLine( next2 ) < shorter + 0.001)
				return false;
			
			// ends when next tiles are outside the grid, or tile on the grid doesn't intersect
			return next1 == null || !intersects( next1, source, target );
		}

		/**
		 * Calculates distance between a tile's center and the line.
		 * 
		 * @param tile				calculate distance to the center of this tile
		 * @return					distance to the point (negative for <code>null</code>)
		 */
		public double getDistanceToLine( T tile ) {
			return getDistanceToLine( tile.getCenter() );
		}

		/**
		 * Calculates distance between a point and the line.
		 * 
		 * @param point			calculate distance to this point
		 * @return				distance to the point (negative for <code>null</code>)
		 */
		public double getDistanceToLine( Point point ) {
			return Math.abs( a * point.getX() + b * point.getY() + c ) / normLen;
		}

		/**
		 * Gets the distance from a tile's center point to the starting tile's center point.
		 * 
		 * @param tile			get distance to starting tile from this tile
		 * @return				distance between the tiles
		 */
		public double getDistanceToSource( T tile ) {
			return getDistanceToSource( tile.getCenter() );
		}
		
		/**
		 * Gets the distance from a point to the starting tile's center point.
		 * 
		 * @param point			get distance to starting tile's center from this point
		 * @return				distance between the points
		 */
		public double getDistanceToSource( Point point ) {
			return point.distance( source );
		}
		
		/**
		 * Gets the distance from starting tile's center to target tile's center.
		 * 
		 * @return				the line's length
		 */
		public double getLineLength() {
			return source.distance( target );
		}
		
		/**
		 * Gets the line's starting tile, which can be the same as the destination.
		 * 
		 * @return				start tile (can't be <code>null</code>)
		 */
		public T getSource() {
			return grid.getTileAtPoint( source );
		}

		/**
		 * Gets the line's destination tile, which can be the same as the starting tile.
		 * 
		 * @return				end tile (can't be <code>null</code>)
		 */
		public T getTarget() {
			return grid.getTileAtPoint( target );
		}

		/**
		 * Gets one of the current tiles.
		 * 
		 * @param primary		<code>true</code> for primary, <code>false</code> for secondary
		 * @return				one of the current tiles (can be <code>null</code> at the edge of
		 * 						the tile grid, or if line goes along a straight line of tiles)
		 */
		public T getCurrentTile( boolean primary ) {
			return grid.getTileAtPoint( primary ? current1 : current2 );
		}

		/**
		 * Gets one of the previous tiles.
		 * 
		 * @param primary		<code>true</code> for primary, <code>false</code> for secondary
		 * @return				one of the previous tiles (can be <code>null</code> at the
		 * 						beginning of the line or at the edge of the tile grid, or if line
		 * 						goes along a straight line of tiles)
		 */
		public T getPreviousTile( boolean primary ) {
			return grid.getTileAtPoint( primary ? previous1 : previous2 );
		}

		/**
		 * Gets one of the next tiles. Preference between these hasn't been determined, and either
		 * one can be completely off the line.
		 * 
		 * @param primary		<code>true</code> for primary, <code>false</code> for secondary
		 * @return				one of the next tiles (can be <code>null</code> at the edge of the
		 * 						tile grid, or if line goes along a straight line of tiles)
		 */
		public T getNextTile( boolean primary ) {
			return grid.getTileAtPoint( primary ? next1 : next2 );
		}

		/**
		 * Gets the better of the two tiles on the current step based on closeness to the line.
		 * The worse option can be <code>null</code>, in case the tile is one that doesn't
		 * intersect the line, or an imaginary one outside the tile grid, or the line goes along a
		 * straight line of tiles. The primary tile can also be <code>null</code> if an imaginary
		 * tile outside the grid is closer to the line than an alternative that's on the grid.
		 * 
		 * @param better		<code>true</code> for better tile, <code>false</code> for the other
		 * @return				the tile closer to the line, or the tile further away from it
		 */
		public T getBetterTile( boolean better ) {
			return grid.getTileAtPoint( better ? this.better : worse );
		}

		/**
		 * Checks if the current two tiles have equal distance to the line.
		 * 
		 * @return				current two tiles have equal distance to the line
		 */
		public boolean tilesAreEqual() {
			return equalTiles;
		}

		/**
		 * Determines directions for default line handling. There are normally two directions, one
		 * of which is selected on each step. If the line goes along a straight line of tiles, then
		 * only one direction is necessary.
		 * 
		 * @param from		the line's starting tile
		 * @param to		the line's destination tile
		 * @param xDiff		difference between the tiles' x-coordinates (positive if target is to
		 * 					the east)
		 * @param yDiff		difference between the tiles' y-coordinates (positive if target is to
		 * 					the south)
		 * @return			primary and secondary direction for selecting tiles (secondary can be
		 * 					<code>null</code> for a line along a straight line of tiles)
		 */
		protected abstract Direction[] determineLineDirections( T from, T to, double xDiff,
				double yDiff );

		/**
		 * Estimates the center point of a tile based on a neighbor's center.
		 * 
		 * @param fromCenter		estimate center relative to this center
		 * @param toDir				estimate center of neighbor to this direction
		 * @return					the estimated center point
		 */
		protected abstract Point estimateCenter( Point fromCenter, Direction toDir );

		/**
		 * Gets the width of a tile in the current grid.
		 * 
		 * @return					tile width
		 */
		protected double getTileWidth() {
			return grid.getTileWidth();
		}
		
		/**
		 * Gets the height of a tile in the current grid.
		 * 
		 * @return					tile height
		 */
		protected double getTileHeight() {
			return grid.getTileHeight();
		}
	}
}
