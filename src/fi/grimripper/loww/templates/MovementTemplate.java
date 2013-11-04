package fi.grimripper.loww.templates;

import fi.grimripper.loww.Direction;
import fi.grimripper.loww.Point;
import fi.grimripper.loww.tiles.Tile;

/**
 * A template for mobile objects. Since mobile objects are also obstacles, it extends {@link
 * ObstacleTemplate}. This interface adds methods to query the template's dimensions, as well as
 * a method to determine entered tiles when the template's host moves.
 * 
 * @author Marko Tuominen
 */
public interface MovementTemplate extends ObstacleTemplate {

	/**
	 * Gets the number of tiles in this template.
	 * 
	 * @return		number of tiles in the template
	 */
	public int getSize();

	/**
	 * Gets the number of tiles on the template's widest row.
	 * 
	 * @return		number of tiles in widest row
	 */
	public int getWidth();

	/**
	 * Gets the number of tile rows in this template.
	 * 
	 * @return		number of tile rows in this template
	 */
	public int getHeight();

	/**
	 * Gets the template's center point at a specific position.
	 * 
	 * @param location		main tile's location
	 * @param facing		host's facing in the location
	 * @return				template's center point in the specified position
	 */
	public Point getCenter( Tile location, Direction facing );

	/**
	 * Gets indices of entered tiles when a mobile object moves. After a step, the tiles in the
	 * given indices are ones that were not included in the template before the step. This method
	 * assumes that the mobile object is moving to the direction it's facing, so asymmetric
	 * templates don't have extra requirements.
	 * 
	 * @param to		step direction
	 * @return			indices for new tiles
	 */
	public int[] getMoveIndices( Direction to );
}
