package fi.grimripper.loww.tiles;

import fi.grimripper.loww.AdditionalProperties;
import fi.grimripper.loww.Height;
import fi.grimripper.loww.movement.MobileObject;
import fi.grimripper.loww.movement.MovementMode;
import fi.grimripper.loww.movement.MovementModifier;

/**
 * An interface for events triggered when a mobile object moves into or out of a tile. These events
 * are attached to tiles, and triggered when a mobile object moves using a movement mode. They are
 * meant to be used only in combination with movement modes. When a mobile object moves using a
 * movement mode, events are executed in all tiles it passes through.
 * <p>
 * A movement event can stop a moving unit at a tile which it has entered, or prevent it from
 * entering a tile. Each of these is checked beforehand, during movement radius generation. An
 * event doesn't have to reveal in advance if it's going to stop movement. It's possible to make
 * traps which will seem like the mobile object can move across them, but then stop it when it
 * tries to. Movement events can also have other effects, taking place during a move.
 * <p>
 * Each of the methods in this interface takes the associated tile as parameter, so an event can be
 * attached to multiple tiles and doesn't need to remember which ones. The events also get the
 * height where the mobile object moves. For example, when a mobile object travels on the ground,
 * the height is <code>FLAT</code>.
 * <p>
 * Events which are below a mobile object's movement height are ignored. Movement modes which can
 * control the movement height, such as flying, may increase height to avoid events, if possible.
 * Occupy height can differ from movement height, and is used to execute entrance events in the
 * tile to be occupied.
 * 
 * @author Marko Tuominen
 * 
 * @see MobileObject
 * @see MovementMode
 * @see MovementModifier
 * @see Tile
 */
public interface MovementEvent extends AdditionalProperties {

	/**
	 * Gets the movement event's height. Height can differ between tiles. Events below the mobile
	 * object's movement height are ignored. Setting height to <code>BLOCKING</code> prevents the
	 * event from being ignored because of height. Then it won't affect movement height, either,
	 * because a mobile object can't avoid it no matter how far up it moves, and won't even try.
	 * 
	 * @param tile		get height in this tile
	 * @return			the movement event's height
	 */
	public Height getHeight( Tile tile );

	/**
	 * Tests whether or not a mobile object can enter a tile. The test is made when generating the
	 * movement radius, and it affects the paths generated before selecting destination. This
	 * method is used only to see if a mobile object can enter a tile, and it might not get
	 * executed if some other event prevents entry first.
	 * 
	 * @param moving	the mobile object that's moving into the tile
	 * @param tile		the tile where the event resides
	 * @param height	the height at which the mobile object is moving
	 * @return			<code>true</code> if the mobile object can move to the tile, or
	 * 					<code>false</code> to stop it before entering
	 */
	public boolean canEnterTile( MobileObject moving, Tile tile, Height height );

	/**
	 * Tests whether or not a mobile object can continue its move from a tile which it has entered.
	 * The test is made when generating the movement radius, and it affects the paths generated
	 * before selecting destination. This method is used only to see if a mobile object can leave a
	 * tile, and it might not get executed if some other event prevents leaving first.
	 * 
	 * @param moving	the mobile object that's moving away from the tile
	 * @param tile		the tile where the event resides
	 * @param height	the height at which the mobile object is moving
	 * @return			<code>true</code> if the mobile object can continue its movement from the
	 * 					current tile, or <code>false</code> to prevent moving further
	 */
	public boolean canLeaveTile( MobileObject moving, Tile tile, Height height );

	/**
	 * Triggered before a mobile object enters a tile. These events are executed while the mobile
	 * object is moving. They can prevent the mobile object from entering. In this case, movement
	 * is interrupted and the movement mode handles the interruption. Usually, the mobile object
	 * backtracks on its path back to a previous tile it can occupy and stops there.
	 * <p>
	 * These events are executed for all tiles which the mobile object enters during its movement,
	 * unless ignored because of movement height or movement modifiers. Even if executed, they may
	 * fail to block the mobile object's movement depending on movement modifiers. They are also
	 * executed if the mobile object is forced to backtrack, but then they can't stop its movement.
	 * 
	 * @param moving	the mobile object attempting to move into the tile
	 * @param tile		the tile where the event resides
	 * @param height	the height at which the mobile object is moving
	 * @return			<code>true</code> if the mobile object can enter the given tile, or
	 * 					<code>false</code> to prevent it from entering
	 */
	public boolean enteringTile( MobileObject moving, Tile tile, Height height );

	/**
	 * Triggered when a mobile object leaves from a tile. These events are executed while the
	 * mobile object is moving. They can prevent the mobile object from moving further from the
	 * tile. In this case, movement is interrupted and the movement mode handles the interruption.
	 * If the mobile object can't occupy the tile where the interrupting event is, it usually
	 * backtracks on its path back to a previous tile it can occupy and stops there.
	 * <p>
	 * These events are executed for all tiles which the mobile object leaves during its movement,
	 * including its starting tile, unless ignored because of movement height or movement
	 * modifiers. Even if executed, the events can't stop the mobile object from leaving its
	 * starting tile, and can also fail to block it's movement elsewhere depending on movement
	 * modifiers. The events are also executed when the mobile object backtracks, but can't stop
	 * its movement.
	 * 
	 * @param moving	the mobile object about to move away from the tile
	 * @param tile		the tile where the event resides
	 * @param height	the height at which the mobile object is moving
	 * @return			<code>true</code> if the mobile object can move further from the given
	 * 					tile, or <code>false</code> to stop it
	 */
	public boolean leavingTile( MobileObject moving, Tile tile, Height height );
	
	/**
	 * Gets the event's risk for a mobile object. Risk doesn't involve interrupting movement.
	 * There's no predetermined method for estimating risk. It's used by movement modes to
	 * determine when a mobile object should move around a tile. For example, it's preferable to
	 * move through two tiles with risk 0.4 than one with risk 1.0. Risk 0.0 means the event
	 * doesn't cause any extra effects. An event doesn't need to be truthful about its risk. Giving
	 * a wrong risk is necessary for creating traps. The risk isn't checked in tiles which the
	 * mobile object can't enter.
	 * 
	 * @param moving	declare risk for this mobile object
	 * @param tile		declare risk at this tile
	 * @param height	declare risk when moving at this height
	 * @return			risk indicating how dangerous this event is for the given mobile object
	 * 					moving at the given height
	 */
	public float getRisk( MobileObject moving, Tile tile, Height height );
}
