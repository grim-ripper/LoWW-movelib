package fi.grimripper.loww.movement;

import fi.grimripper.loww.tiles.Obstacle;
import fi.grimripper.loww.tiles.Tile;

/**
 * Interface for observing moving objects on a tile grid. The listener can be attached to a mobile
 * object, and it'll be notified when that object is moved away from a tile, and when it's moved
 * into a tile. The listener can also be attached to a tile. Then it gets notified every time an
 * object is moved into or out of that tile. The listener isn't notified when a mobile object moves
 * through a tile using a movement mode, only when it remains in the tile after its move. The
 * listener is also notified when any obstacle is placed in a tile or removed from it, even if it
 * isn't a mobile object. It's also worth noting that movement modifiers and movement height can
 * prevent movement events from working, but motion listeners aren't affected.
 * 
 * @author Marko Tuominen
 * @see Obstacle
 * @see Tile
 */
public interface MotionListener {

	/**
	 * Notification when an obstacle is placed in a tile, including mobile objects ending a move.
	 * 
	 * @param obstacle		the obstacle that was moved
	 * @param tile			the obstacle was placed into this tile
	 */
	public void objectMovedToTile( Obstacle obstacle, Tile tile );

	/**
	 * Notification when an obstacle is removed from a tile, including mobile objects starting a
	 * move.
	 * 
	 * @param obstacle		the obstacle that was removed
	 * @param tile			the obstacle was moved away from this tile
	 */
	public void objectMovedFromTile( Obstacle obstacle, Tile tile );
}
