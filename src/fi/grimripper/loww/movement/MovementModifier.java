package fi.grimripper.loww.movement;

import fi.grimripper.loww.Height;
import fi.grimripper.loww.tiles.Block;
import fi.grimripper.loww.tiles.MovementEvent;
import fi.grimripper.loww.tiles.Obstacle;
import fi.grimripper.loww.tiles.Terrain;
import fi.grimripper.loww.tiles.Tile;

/**
 * Anything that implements this interface can affect various aspects of movement. It can change
 * the movement cost of a tile, and make impassable terrain passable or vice versa. It can also
 * affect obstacles, movement events and blocks, as well as perform other things during the
 * generation of a movement radius.
 * 
 * @author Marko Tuominen
 * @see MovementMode
 */
public interface MovementModifier {

	/**
	 * Gets movement or occupy height at a certain tile. Movement height is usually based on
	 * terrain and obstacles, but modifiers can increase it. Movement modifiers aren't allowed to
	 * decrease movement height.
	 * 
	 * @param atTile	get movement height at this tile
	 * @param height	movement height without effect from this modifier
	 * @param occupy	<code>true</code> for occupy height, <code>false</code> for movement height
	 * @return			movement height with effect from this modifier
	 */
	public Height getMovementHeight( Tile atTile, Height height, boolean occupy );

	/**
	 * Modifies movement cost for terrain. The <code>moveCost</code> parameter can also be very
	 * large to indicate impassability, and this should be taken into consideration. This method is
	 * executed only once per terrain type. When modifiers are added or removed, the cost is
	 * calculated again.
	 * 
	 * @param terrain		terrain whose cost is modified
	 * @param moveCost		the movement cost before changes from this modifier
	 * @return				the movement cost after changes from this modifier
	 */
	public float modifyTerrainCost( Terrain terrain, float moveCost );
	
	/**
	 * Modifies movement cost for obstacles. The <code>moveCost</code> parameter can also be very
	 * large to indicate impassability, and this should be taken into consideration. This method is
	 * executed once for every tile checked during movement radius generation. Parameter obstacles
	 * don't include ones that were ignored because of movement height or movement modifiers.
	 * 
	 * @param tile			the tile whose cost is modified
	 * @param obstacles		obstacles that were considered for movement cost
	 * @param moveCost		movement cost with terrain and the given obstacles
	 * @return				movement cost after changes from this modifier
	 */
	public float modifyObstacleCost( Tile tile, Obstacle[] obstacles, float moveCost );
	
	/**
	 * Modifies movement cost for blocks. The <code>moveCost</code> parameter can also be very
	 * large to indicate impassability, and this should be taken into consideration. This method is
	 * executed every time the move radius search enters a new tile, since it depends on direction.
	 * Parameter blocks don't include ones that were ignored because of movement height or movement
	 * modifiers.
	 * 
	 * @param to			the tile whose cost is modified
	 * @param from			the tile where the mobile object moves from
	 * @param mobile		the moving mobile object
	 * @param height		the mobile object's movement height at the <code>to</code> tile
	 * @param blocks		blocks that were considered for the move cost
	 * @param moveCost		movement cost with terrain, obstacles and blocks
	 * @return				movement cost after changes from this modifier
	 */
	public float modifyBlockCost( Tile to, Tile from, MobileObject mobile, Height height,
			Block[] blocks, float moveCost );

	/**
	 * Modifies the cost of changing movement height for movement modes that allow it. This is used
	 * only if movement height can be changed by the movement mode itself. The result is the cost
	 * of changing from one height level to another.
	 * 
	 * @param tile			tile where the mobile object is
	 * @param cost			height change cost without this modifier
	 * @param height		current height
	 * @param newHeight		height after change
	 * @return				height change cost with this modifier
	 */
	public float modifyHeightChangeCost( Tile tile, float cost, Height height, Height newHeight );

	/**
	 * Can enable a mobile object to occupy a tile it normally couldn't. This test is separate from
	 * the move cost change, which doesn't affect occupation. This method isn't called for a tile
	 * that can be occupied anyway. It isn't called in cases where a movement modifier may not
	 * allow occupation: the mobile object's template can't be partially outside the tile grid, and
	 * the mobile object may not occupy an already occupied tile.
	 * 
	 * @param occupier		the mobile object
	 * @param occupyTile	check if the modifier allows occupation of this hex
	 * @return				<code>true</code> if this modifier allows occupation
	 */
	public boolean allowsOccupation( MobileObject occupier, Tile occupyTile );
	
	/**
	 * Checks if this modifier allows ignoring an obstacle. The obstacle will not affect movement
	 * height or movement cost.
	 * 
	 * @param obstacle		an obstacle
	 * @return				<code>true</code> if the obstacle should be ignored
	 */
	public boolean ignoresObstacle( Obstacle obstacle );

	/**
	 * Checks if this modifier allows ignoring a block. The block will not affect movement cost,
	 * and can't prevent the mobile object from moving onto it.
	 * 
	 * @param block			a block
	 * @param from			the mobile object is leaving this tile
	 * @param to			the mobile object is entering this tile
	 * @param height		the mobile object is moving at this height
	 * @return				<code>true</code> if the block should be ignored
	 */
	public boolean ignoresBlock( Block block, Tile from, Tile to, Height height );

	/**
	 * Checks if this modifier protects the host against a movement event.
	 * 
	 * @param event		event which might have adverse effects
	 * @param tile		tile where the event is located
	 * @return			negative if the host is affected normally by the movement event, zero if
	 * 					the host's movement can't be interrupted by the event, or positive if host
	 * 					is immune to the event's effects, and the event doesn't even get executed
	 */
	public int getProtectionAgainstEvent( MovementEvent event, Tile tile );
}
