package fi.grimripper.loww;

import static java.lang.Math.PI;
import fi.grimripper.loww.tiles.Hex;
import fi.grimripper.loww.tiles.Square;

/**
 * Depicts the eight compass directions, and contains some operations for utilizing them with
 * hexes and squares.
 * 
 * @author Marko Tuominen
 * @see Hex
 * @see Square
 */
public enum Direction {
	
	NORTH		(-1, 0, PI / 2, 0, -1),
	NORTHEAST	(0, 1, PI / 3, 1, 0),
	EAST		(1, -1, 0, 2, -1),
	SOUTHEAST	(2, 2, -PI / 3, 3, 1),
	SOUTH		(-1, 3, -PI / 2, 4, -1),
	SOUTHWEST	(3, 4, -PI * 2 / 3, 5, 2),
	WEST		(4, -1, PI, 6, -1),
	NORTHWEST	(5, 5, PI * 2 / 3, 7, 3);
	
	private static final Direction[] VALUES = values();
	
	private int hexDirection;
	private int hexPoint;
	private double hexAngle;
	private int squareDirection;
	private int squarePoint;
	
	private Direction( int hexDir, int hexPoint, double angle, int squareDir, int squarePoint ) {
		hexDirection = hexDir;
		this.hexPoint = hexPoint;
		hexAngle = angle;
		squareDirection = squareDir;
		this.squarePoint = squarePoint;
	}
	
	/**
	 * Gets an identifier for a hex's neighbor.
	 * 
	 * @return		index identifier for neighbor hex
	 */
	public int hexDir() {
		return hexDirection;
	}
	
	/**
	 * Gets an identifier for a hex's point.
	 * 
	 * @return		index identifier for hex points
	 */
	public int hexPoint() {
		return hexPoint;
	}
	
	/**
	 * Gets angle, related to hex sides.
	 * 
	 * @return		hex side angle
	 */
	public double hexAngle() {
		return hexAngle;
	}
	
	/**
	 * Gets an identifier for a square's neighbor.
	 * 
	 * @return		index identifier for neighbor square
	 */
	public int squareDir() {
		return squareDirection;
	}
	
	/**
	 * Gets an identifier for a square's point.
	 * 
	 * @return		index identifier for square points
	 */
	public int squarePoint() {
		return squarePoint;
	}
	
	/**
	 * Gets the opposite of this direction.
	 * 
	 * @return			the opposite direction
	 */
	public Direction getOpposite() {
		return VALUES[ (ordinal() + 4) % 8 ];
	}
	
	/**
	 * Takes a selection of directions and picks the one closest to this one. If two of the
	 * directions are equal, it is unspecified which is picked.
	 * 
	 * @param sel	selection of directions to pick from (not <code>null</code> and not an empty
	 * 				array, and no <code>null</code>s in the array)
	 * @return		the closest direction in the selection
	 */
	public Direction getClosest( Direction[] sel ) {
		int minDist = 8, minIndex = -1;
		
		for (int i = 0; i < sel.length; i++) {
			int ccwDist = Math.abs( ordinal() - sel[i].ordinal() );
			int distance = Math.min( ccwDist, 8 - ccwDist );
			
			if (distance < minDist) {
				minIndex = i;
				minDist = distance;
			}
		}
		
		return sel[ minIndex ];
	}

	/**
	 * Gets the adjacent direction clockwise.
	 * 
	 * @return			clockwise adjacent direction
	 */
	public Direction getAdjacentCW() {
		return VALUES[ (ordinal() + 1) % VALUES.length ];
	}
	
	/**
	 * Gets the adjacent direction counter-clockwise.
	 * 
	 * @return			counter-clockwise adjacent direction
	 */
	public Direction getAdjacentCCW() {
		return VALUES[ (ordinal() + VALUES.length - 1) % VALUES.length ];
	}
	
	/**
	 * Returns adjacent direction closer to another direction. If the parameter is same as this
	 * direction, next direction clockwise is returned. Similarly, if the parameter is the opposite
	 * direction, next direction counter-clockwise is returned.
	 * 
	 * @param to		get adjacent direction closer to this direction
	 * @return			adjacent direction that is closer to given direction than this one
	 */
	public Direction getAdjacentTowards( Direction to ) {
		return (to.ordinal() - ordinal() + VALUES.length) % VALUES.length < VALUES.length / 2 ?
			getAdjacentCW() : getAdjacentCCW();
	}
	
	/**
	 * Checks if this direction is generally towards east. Includes northeast, east and southeast.
	 * 
	 * @return		<code>true</code> if this direction is towards east
	 */
	public boolean isDueEast() {
		return ordinal() > 0 && ordinal() < 4;
	}

	/**
	 * Checks if this direction is generally towards north. Includes northwest, north and
	 * northeast.
	 * 
	 * @return		<code>true</code> if the direction is towards north
	 */
	public boolean isDueNorth() {
		return ordinal() < 2 || ordinal() == 7;
	}

	/**
	 * Checks if this direction is generally towards south. Includes southeast, south and
	 * southwest.
	 * 
	 * @return		<code>true</code> if the direction is towards south
	 */
	public boolean isDueSouth() {
		return ordinal() > 2 && ordinal() < 6;
	}
	
	/**
	 * Checks if this direction is generally towards west. Includes northwest, west and southwest.
	 * 
	 * @return		<code>true</code> if the direction is towards west
	 */
	public boolean isDueWest() {
		return ordinal() > 4;
	}
	
	/**
	 * Checks if this direction is along the horizontal axis of a compass. Includes west and east.
	 * 
	 * @return		<code>true</code> if the direction is horizontal in compass
	 */
	public boolean isHorizontal() {
		return equals( WEST ) || equals( EAST );
	}
	
	/**
	 * Checks if this direction is along the vertical axis of a compass. Includes north and south.
	 * 
	 * @return		<code>true</code> if the direction is vertical in compass
	 */
	public boolean isVertical() {
		return equals( NORTH ) || equals( SOUTH );
	}
}
