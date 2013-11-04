package fi.grimripper.loww.tiles;

import fi.grimripper.loww.AdditionalProperties;
import fi.grimripper.loww.Height;
import fi.grimripper.loww.movement.MobileObject;

/**
 * A block affects move costs between tiles, unlike an obstacle, which affects the cost of a single
 * tile. It can also have a cost that depends on the mobile object. Blocks are commonly obstacles,
 * but an interface makes it possible for other things to be blocks as well. A block can be placed
 * in any tile, and it'll be checked when a mobile object tries to move to that hex or leave it.
 * The block receives the tile where the mobile object comes from, the one where it's going,
 * movement height and also the mobile object itself, and can react differently to different
 * directions, mobile objects and heights. A mobile object can move over a block below its movement
 * height without being affected by it.
 * <p>
 * A block can be ignored by movement modifiers in the same manner as an obstacle or a movement
 * event. It may or may not be possible to move into a position partially on one side of a block
 * and partially on the other. The block has another method for checking this. A movement modifier
 * can't override a block which prevents occupation in this kind of position. It can't prevent the
 * block from preventing a move into such a position either, except by ignoring the block entirely.
 * 
 * @author Marko Tuominen
 */
public interface Block extends AdditionalProperties {

	/**
	 * Gets the block's height. Height can differ between tiles. Movement modes always ignore
	 * blocks which are below a mobile object's movement height.
	 * 
	 * @param tile			get height in this tile
	 * @return				the block's height
	 */
	public Height getHeight( Tile tile );

	/**
	 * Move cost change with direction and the moving object as extra parameters.
	 * 
	 * @param from			a mobile object is moving in from this tile
	 * @param to			the mobile object is moving into this tile
	 * @param moving		the mobile object moving through the block
	 * @param height		the mobile object's movement height
	 * @param cost			cost before the block takes effect
	 * @return				cost with the block's effect added
	 */
	public float modifyMoveCost( Tile from, Tile to, MobileObject moving, Height height,
			float cost );
	
	/**
	 * Tests if it's possible to move into a position over the block.
	 * 
	 * @param tile			the tile where the block is
	 * @param moving		the mobile object attempting to move onto the block
	 * @param height		the mobile object's movement/occupy height
	 * @param template		the tiles in the mobile object's position
	 * @return				the mobile object is allowed to move into the given position
	 */
	public boolean allowsTemplate( Tile tile, MobileObject moving, Height height,
			Tile[] template );
}
