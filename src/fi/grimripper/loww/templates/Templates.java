package fi.grimripper.loww.templates;

import static fi.grimripper.loww.Direction.*;

import java.util.Arrays;

import fi.grimripper.loww.Direction;

/**
 * Utility class for templates, which also contains instances of basic template implementations.
 * 
 * @author Marko Tuominen
 */
public class Templates {

	/**
	 * A template that only includes the main tile.
	 */
	public static final MovementTemplate SINGLE_TILE_TEMPLATE = new SingleTileTemplate();
	
	/**
	 * A template that has two horizontally adjacent tiles.
	 */
	public static final MovementTemplate HORIZONTAL_TWO_TILE_TEMPLATE =
			new HorizontalTwoTileTemplate();
	
	/**
	 * A template with four squares in a two by two grid.
	 */
	public static final MovementTemplate FOUR_SQUARE_TEMPLATE = new FourSquareTemplate();
	
	/**
	 * A template with a hex and its adjacent neighbors.
	 */
	public static final MovementTemplate HEX_AND_NEIGHBORS_TEMPLATE =
			new HexAndNeighborsTemplate();
	
	/**
	 * A template with a square and its adjacent neighbors.
	 */
	public static final MovementTemplate SQUARE_AND_NEIGHBORS_TEMPLATE =
			new SquareAndNeighborsTemplate();
	
	private static final Direction[] ALL_DIRECTIONS = {
		NORTH, NORTHEAST, NORTHWEST, EAST, WEST, SOUTHEAST, SOUTHWEST, SOUTH };
	
	private static final Direction[] EAST_180 = { NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH };
	private static final Direction[] NORTH_180 = { NORTH, NORTHEAST, NORTHWEST, EAST, WEST };
	private static final Direction[] SOUTH_180 = { EAST, WEST, SOUTHEAST, SOUTHWEST, SOUTH };
	private static final Direction[] WEST_180 = { NORTH, NORTHWEST, WEST, SOUTHWEST, SOUTH };
	
	private static final Direction[] NORTHEAST_90 = { NORTH, NORTHEAST, EAST };
	private static final Direction[] SOUTHEAST_90 = { EAST, SOUTHEAST, SOUTH };
	private static final Direction[] NORTHWEST_90 = { NORTH, NORTHWEST, WEST };
	private static final Direction[] SOUTHWEST_90 = { WEST, SOUTHWEST, SOUTH };
	
	private static Direction[] DIAGONALS = { NORTHEAST, NORTHWEST, SOUTHEAST, SOUTHWEST };
	private static Direction[] NON_VERTICALS = {
		NORTHEAST, NORTHWEST, EAST, WEST, SOUTHEAST, SOUTHWEST };
	private static Direction[] NON_HORIZONTALS = {
		NORTH, NORTHEAST, NORTHWEST, SOUTHEAST, SOUTHWEST, SOUTH };
	
	/**
	 * Selects a facing suitable for the template. If given facing isn't suitable, nearest suitable
	 * direction is selected, preferring directions in clockwise order.
	 * 
	 * @param facing		select template facing nearest to this obstacle facing
	 * @param template		select facing for this template
	 * @return				facing for the template (<code>null</code> if facing parameter was
	 * 						<code>null</code>)
	 */
	public static Direction getTemplateDirection( Direction facing, ObstacleTemplate template ) {
		
		// horizontal and vertical can be unsuitable, but next direction clockwise is diagonal
		if (facing != null && (!template.isHorizontallySymmetric() &&
				!template.isVerticallySymmetric() &&
				(facing.isHorizontal() || facing.isVertical()) ||
				!template.isHorizontallySymmetric() && facing.isVertical() ||
				!template.isVerticallySymmetric() && facing.isHorizontal()))
			return facing.getAdjacentCW();
		
		return facing;
	}
	
	/**
	 * Determines a template's facing direction after it's turned. Result is undefined if old
	 * facing isn't a valid facing for the template.
	 * 
	 * @param newFacing		facing direction after turning (may be invalid for the template)
	 * @param oldFacing		facing direction before turning (must be valid for the template)
	 * @param template		the template
	 * @return				a valid facing direction adjacent to the given new facing
	 * 						(<code>null</code> if either direction parameter was null)
	 */
	public static Direction getTemplateDirection( Direction newFacing, Direction oldFacing,
			ObstacleTemplate template ) {
		if (newFacing == null || oldFacing == null)
			return null;
		
		// template direction must be diagonal if template is symmetric in neither direction
		if (!template.isHorizontallySymmetric() && !template.isVerticallySymmetric())
			return !newFacing.isHorizontal() && !newFacing.isVertical() ? newFacing :
				newFacing.getAdjacentTowards( oldFacing );
		
		// north and south are not allowed for a horizontally asymmetric template
		else if (!template.isHorizontallySymmetric())
			return !newFacing.isVertical() ? newFacing : newFacing.getAdjacentTowards( oldFacing );
		
		// east and west are not allowed for a vertically asymmetric template
		else if (!template.isVerticallySymmetric())
			return !newFacing.isHorizontal() ? newFacing :
				newFacing.getAdjacentTowards( oldFacing );
		
		return newFacing;
	}
	
	/**
	 * Checks if a given facing is suitable for a template.
	 * 
	 * @param template		check if the facing is suitable for this template
	 * @param facing		check if this facing is suitable for the template
	 * @return				the facing is suitable for the template
	 */
	public static boolean isTemplateDirection( ObstacleTemplate template, Direction facing ) {
		return template != null && facing != null &&
				(template.isHorizontallySymmetric() || !facing.isVertical()) &&
				(template.isVerticallySymmetric() || !facing.isHorizontal());
	}
	
	/**
	 * Get possible template directions for a template. A template can't be placed facing a
	 * direction which doesn't identify the tiles that belong to the template.
	 * 
	 * @param template		get possible facing directions for this template
	 * @return				possible directions (copy array)
	 */
	public static Direction[] getTemplateDirections( ObstacleTemplate template ) {
		Direction[] dirs = null;
		
		if (template.isHorizontallySymmetric() && template.isVerticallySymmetric())
			dirs = ALL_DIRECTIONS;
		
		else if (!template.isHorizontallySymmetric() && !template.isVerticallySymmetric())
			dirs = DIAGONALS;
		
		else if (!template.isHorizontallySymmetric())
			dirs = NON_VERTICALS;
		
		else
			dirs = NON_HORIZONTALS;
		
		return Arrays.copyOf( dirs, dirs.length );
	}
	
	/**
	 * Get directions where a template can be moved without turning in place. The returned array
	 * can be empty if facing is <code>null</code> or facing is invalid for the template.
	 * 
	 * @param facing		template's facing
	 * @param template		get possible movement directions for this template
	 * @return				possible movement directions (copy array)
	 */
	public static Direction[] getMoveDirections( Direction facing, MovementTemplate template ) {
		if (facing == null)
			return new Direction[0];
		
		Direction[] dirs = null;
		
		if (template.isHorizontallySymmetric() && template.isVerticallySymmetric())
			dirs = ALL_DIRECTIONS;
		
		else if (!template.isHorizontallySymmetric() && !template.isVerticallySymmetric()) {
			if (facing == NORTHEAST)
				dirs = NORTHEAST_90;
			else if (facing == NORTHWEST)
				dirs = NORTHWEST_90;
			else if (facing == SOUTHEAST)
				dirs = SOUTHEAST_90;
			else if (facing == SOUTHWEST)
				dirs = SOUTHWEST_90;
		}
		
		else if (!template.isHorizontallySymmetric() && !facing.isVertical())
			dirs = facing.isDueEast() ? EAST_180 : WEST_180;
		
		else if (!template.isVerticallySymmetric() && !facing.isHorizontal())
			dirs = facing.isDueNorth() ? NORTH_180 : SOUTH_180;
		
		return dirs == null ? new Direction[0] : Arrays.copyOf( dirs, dirs.length );
	}
}
