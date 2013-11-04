package fi.grimripper.loww.movement;

import static fi.grimripper.loww.Direction.*;
import static fi.grimripper.loww.Height.BLOCKING;
import static fi.grimripper.loww.Height.DEEP;
import static fi.grimripper.loww.Height.VERY_HIGH;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import fi.grimripper.loww.Direction;
import fi.grimripper.loww.Height;
import fi.grimripper.loww.StateChangeListener;
import fi.grimripper.loww.templates.MovementTemplate;
import fi.grimripper.loww.templates.Templates;
import fi.grimripper.loww.tiles.Block;
import fi.grimripper.loww.tiles.MovementEvent;
import fi.grimripper.loww.tiles.Obstacle;
import fi.grimripper.loww.tiles.Terrain;
import fi.grimripper.loww.tiles.Tile;

/**
 * The base class for movement modes. Contains the public interface, as well as utilities for
 * subclasses. Utilities include handling of obstacles, events, blocks and movement modifiers, as
 * well as managing movement data. Each movement mode has a base movement value, which determines
 * how many tiles the host can move in case the movement cost for each tile is one.
 * <p>
 * Each tile has a specific terrain type, which determines the base move cost in that tile. Each
 * tile can have any number of obstacles, which may or may not affect movement. An obstacle can
 * make a tile impassable, or affect movement cost in another way.
 * <p>
 * Each tile can also include any number of events, which can prevent a mobile object from entering
 * or leaving a tile. Events are tested during movement radius generation. The movement search
 * can't progress further than an event which prevents movement. When the mobile object moves, tile
 * events get executed, and may block movement even if they previously claimed to allow movement.
 * <p>
 * Blocks can alter move costs between tiles. Their effects can change depending on direction and
 * mobile object, unlike those of an obstacle. Movement modifiers can affect any of the above
 * properties. They can alter the cost of terrain, enable the host to ignore obstacles, events and
 * blocks, as well as modify the final move cost which includes terrain, obstacles and blocks.
 *
 * @author Marko Tuominen
 * @see Block
 * @see MobileObject
 * @see MovementEvent
 * @see Obstacle
 */
public abstract class MovementMode {

	/**
	 * Movement cost which makes a tile impassable by default. Higher cost is also impassable.
	 * The limit cost for impassability can be altered.
	 * 
	 * @see				#getImpassableMoveCost()
	 */
	public static final int IMPASSABLE_MOVE_COST = 100;
	
	// fields used while generating a movement radius
	private MobileObject host = null;
	protected Tile[] initialTiles = null;

	// movement value attached to this mode
	private int movement;

	// mode settings
	private byte disabled = 0;
	
	// movement modifiers and state listeners
	private Vector <MovementModifier> modifiers = null;
	private Vector <StateChangeListener <MovementMode>> listeners = null;
	
	// event buffer lists need indexOf( Object, int ) method
	private Vector <MovementEvent> eventBuffer = new Vector <>();
	private Vector <MovementEvent> executingBuffer = null;
	private Vector <Tile> eventTiles = new Vector <Tile>();
	
	// collections to save movement-related data
	private Map <Terrain, Float> terrainCosts = new HashMap <>();
	private Map <Tile, TileData> tileData = new HashMap <>();
	private Map <Tile, Height[]> occupyHeights = new HashMap <>();

	/**
	 * Creates a new movement mode with host and default movement.
	 * 
	 * @param host				sets the movement mode's host
	 * @param movement			sets default movement
	 */
	protected MovementMode( MobileObject host, int movement ) {
		this.host = host;
		this.movement = movement;
	}
	
	/**
	 * Gets the host mobile object this mode is attached to.
	 * 
	 * @return		the host mobile object
	 */
	public MobileObject getHost() {
		return host;
	}
	
	/**
	 * Gets the movement value associated with this movement mode. Movement mode determines the
	 * default distance a host can move when this movement mode is active.
	 * 
	 * @return					the movement value associated with this movement mode
	 */
	public int getMovement() {
		return movement;
	}
	
	/**
	 * Sets the movement value for this mode. Notifies state change listeners.
	 * 
	 * @param movement			set the movement value associated with this mode
	 */
	public void setMovement( int movement ) {
		this.movement = movement;
		
		if (listeners != null)
			for (StateChangeListener <MovementMode> scl : listeners)
				scl.stateChanged( this );
	}

	/**
	 * Checks if this mode is disabled or not.
	 * 
	 * @return		this mode is disabled
	 */
	public boolean isDisabled() {
		return disabled > 0;
	}
	
	/**
	 * Forces the move costs of different terrain types to be re-calculated. Otherwise they're
	 * saved and only updated when movement modifiers are added or removed.
	 */
	public void clearTerrainCosts() {
		terrainCosts.clear();
	}
	
	/**
	 * Forces the occupy data for tiles to be re-calculated. Otherwise they're saved and only
	 * updated when movement radius is cleared, or when movement modifiers are added or removed.
	 */
	public void clearOccupyData() {
		occupyHeights.clear();
		tileData.clear();
	}
	
	/**
	 * Adds a movement modifier. Notifies state change listeners.
	 * 
	 * @param mod		the new modifier
	 */
	public void addMovementModifier( MovementModifier mod ) {
		if (mod != null) {
			
			if (modifiers == null)
				modifiers = new Vector <MovementModifier>();
			
			modifiers.add( mod );
			clearTerrainCosts();		// re-calculate with new modifier
			clearOccupyData();
			tileData.clear();
			
			if (listeners != null)
				for (StateChangeListener <MovementMode> scl : listeners)
					scl.stateChanged( this );
		}
	}
	
	/**
	 * Gets the movement modifiers attached to this mode.
	 * 
	 * @return			the attached modifiers
	 */
	public MovementModifier[] getMovementModifiers() {
		return modifiers == null ? new MovementModifier[0] :
				modifiers.toArray( new MovementModifier[ modifiers.size() ]);
	}
	
	/**
	 * Checks if a certain modifier has been attached to this mode.
	 * 
	 * @param modifier		check if this modifier has been attached
	 * @return				<code>true</code> if the modifier is attached
	 */
	public boolean hasMovementModifier( MovementModifier modifier ) {
		return modifiers != null && modifiers.contains( modifier );
	}
	
	/**
	 * Removes a movement modifier. Notifies state change listeners.
	 * 
	 * @param mod		the movement modifier to remove
	 * @return			the movement modifier that was removed
	 */
	public MovementModifier removeMovementModifier( MovementModifier mod ) {
		if (mod == null || modifiers == null || !modifiers.remove( mod ))
			return null;
		
		if (listeners != null)
			for (StateChangeListener <MovementMode> scl : listeners)
				scl.stateChanged( this );
			
		clearTerrainCosts();		// re-calculate without the removed modifier
		clearOccupyData();
		tileData.clear();
		return mod;
	}
	
	/**
	 * Adds a state change listener, which is notified when the movement mode's internal state
	 * changes. Changes include disabling, enabling, the adding and removing of movement modifiers,
	 * and changing movement value. State change listeners are not notified when state change
	 * listeners are added.
	 * 
	 * @param listener		the new listener
	 */
	public void addStateChangeListener( StateChangeListener <MovementMode> listener ) {
		if (listeners == null)
			listeners = new Vector <StateChangeListener <MovementMode>>();
		listeners.add( listener );
	}
	
	/**
	 * Gets the state change listeners attached to this mode.
	 * 
	 * @return				attached state change listeners
	 */
	@SuppressWarnings( "unchecked" )
	public StateChangeListener <MovementMode>[] getStateChangeListeners() {
		return listeners == null ? new StateChangeListener[0] :
			listeners.toArray( new StateChangeListener[ listeners.size() ]);
	}
	
	/**
	 * Removes a state change listener. Remaining state change listeners are not notified.
	 * 
	 * @param listener		the listener to remove
	 * @return				the listener that was removed
	 */
	public StateChangeListener <MovementMode> removeStateChangeListener(
			StateChangeListener <MovementMode> listener ) {
		return listeners == null || !listeners.remove( listener ) ? null : listener;
	}

	/**
	 * Calculates the occupy height for a main tile and facing. Terrain determines base occupy
	 * height, which obstacles and movement modifiers can increase. Doesn't perform any checks to
	 * see if it's possible to occupy the given template, except that the template must be fully
	 * within the tile grid in order to calculate the occupy height.
	 * 
	 * @param mainTile		template main tile
	 * @param facing		facing in the tile
	 * @return				occupy height for the given main tile and facing
	 */
	public Height calculateOccupyHeight( Tile mainTile, Direction facing ) {
		if (!Templates.isTemplateDirection( host.getTemplate(), facing ))
			return null;
		return calculateOccupyHeight( host.getTemplate().getTiles( mainTile, facing ));
	}

	/**
	 * Calculates the occupy height for a tile template. Terrain determines base occupy height,
	 * which obstacles and movement modifiers can increase. Doesn't perform any checks to see if
	 * it's possible to occupy the given template, except that height can't be calculated if there
	 * are <code>null</code>s in the template.
	 * 
	 * @param template		template tiles
	 * @return				occupy height for the given template
	 */
	public Height calculateOccupyHeight( Tile... template ) {
		return getTerrainHeight( getMinimumHeight( true, template ), template );
	}
	
	/**
	 * Determines if a mobile object can occupy a tile with a specific facing.
	 * 
	 * @param tile			the tile that is to be occupied
	 * @param facing		the mobile object's facing in the given tile
	 * @return				<code>true</code> if this mode allows the tile to be occupied
	 * @see					#calculateOccupyHeight(Tile, Direction)
	 * @see					#canOccupy(Tile, Direction, Height)
	 */
	public boolean canOccupy( Tile tile, Direction facing ) {
		Height occupyHt = calculateOccupyHeight( tile, facing );
		if (occupyHt == null || occupyHt == BLOCKING)
			return false;		// outside tile grid or otherwise illegal
		
		return canOccupy( tile, facing, occupyHt );
	}
	
	/**
	 * Generates movement radius for the host mobile object.
	 * 
	 * @param totalMove			the mobile object's total movement
	 */
	public abstract void movementRadius( int totalMove );

	/**
	 * Creates a mobile object's movement path to a particular tile. The path array doesn't contain
	 * the mobile object's starting tile (it's location before moving). Instead, the first tile in
	 * the path array is the one where the host moves from the starting tile. After that, the path
	 * progresses from tile to tile until the last tile in the array contains the destination tile,
	 * which was given as a parameter. In case the given tile isn't within the movement radius, the
	 * array is empty. An empty array is also returned for a <code>null</code> destination and if
	 * the current location and destination are the same.
	 * 
	 * @param pathTo		find movement path to this tile
	 * @param facing		facing in the destination tile
	 * @return				the path to the given tile
	 */
	public abstract Tile[] getMovementPath( Tile pathTo, Direction facing );

	/**
	 * Executes a mobile object's movement from its current location to a destination. Starting
	 * from current location, executes movement events for each tile along the path. If movement is
	 * interrupted, places the mobile object in a suitable position along the path. Returns the
	 * path the mobile object moved. If the mobile object is forced to backtrack because of an
	 * interruption, the path ends at the tile the mobile object ends up occupying.
	 * <p>
	 * Regardless of movement direction, the mobile object can always turn in place at its
	 * destination. In case of interruption, facing is towards the next tile on the movement path.
	 * The movement mode should remove the host from its current location before movement and set
	 * the final location and facing after movement.
	 * 
	 * @param destination		the destination tile
	 * @param facing			the host's facing in the destination tile
	 * @return					the path of tiles the mobile object moved through (includes the
	 * 							destination tile, but not the starting tile); empty array if the
	 * 							host failed to move at all, or <code>null</code> if the destination
	 * 							was unreachable in the first place
	 */
	public abstract Tile[] executeMovementPath( Tile destination, Direction facing );

	/**
	 * Adds a movement event to a buffer, which is executed after the mobile object enters a tile.
	 * <p>
	 * This buffer is meant to prevent events from getting executed if enter events stop the mobile
	 * object from entering the tile where the events reside. It's also helpful for repeating a
	 * single event, or getting the event executed only once, even if it's located in multiple
	 * tiles which the mobile object enters at the same time.
	 * <p>
	 * All movement events are executed normally once, and can interrupt movement. The buffered
	 * events are executed after the mobile object enters the tile, so at the second execution they
	 * can't interrupt movement. The events can be ignored by movement height or modifiers on the
	 * first execution, and then won't get a chance to add themselves to the buffer. The buffer can
	 * also be used during movement radius generation for risk calculation.
	 * <p>
	 * In order to get an event executed only once, it can take effect the first time, add itself
	 * to the buffer, and then do nothing once it has been buffered. In order to get the event
	 * executed only if and when the mobile object actually enters the event's tile, the event can
	 * be buffered on the first execution. In order to get an event executed only once and only if
	 * the host enters the tile, it can be added on the first execution, and if executed multiple
	 * times, checked that it isn't already in the buffer.
	 * 
	 * @param event		add event
	 * @param tile		add event to tile (passed to the event when it's executed later)
	 * @see				#getBufferedEvents()
	 * @see				#isEventBuffered(MovementEvent, Tile)
	 * @see				#isEventBufferExecuting()
	 * @see				#removeEventFromBuffer(MovementEvent, Tile)
	 * @see				#getEventBufferRisk(Height)
	 * @see				#executeEventBuffer(Height)
	 */
	public void addEventToBuffer( MovementEvent event, Tile tile ) {
		if (event != null && tile != null && eventBuffer != null) {
			eventBuffer.add( event );
			eventTiles.add( tile );
		}
	}

	/**
	 * Checks if an event has been added to the buffer. The check also works while the events are
	 * being executed. New events can't be added during execution, but the ones that were added
	 * before execution can be checked.
	 * 
	 * @param event		check if event is buffered
	 * @param tile		check if buffered event is in this tile (anywhere if <code>null</code>)
	 * @return			event is buffered
	 * @see				#addEventToBuffer(MovementEvent, Tile)
	 * @see				#isEventBufferExecuting()
	 */
	public boolean isEventBuffered( MovementEvent event, Tile tile ) {
		Vector <MovementEvent> buffer = eventBuffer == null ? executingBuffer : eventBuffer;
		
		if (tile == null)
			return buffer.contains( event );				// just check if event is buffered
		
		int index = buffer.indexOf( event );
		while (index >= 0) {								// look for a specific tile+event pair
			if (eventTiles.elementAt( index ).equals( tile ))
				return true;
			
			index = buffer.indexOf( event, index + 1 );		// negative when not found
		}
		
		return false;
	}
	
	/**
	 * Gets currently buffered movement events, if any.
	 * 
	 * @return			currently buffered movement events (<code>null</code> while executing)
	 * @see				#addEventToBuffer(MovementEvent, Tile)
	 */
	public MovementEvent[] getBufferedEvents() {
		return eventBuffer == null ? null :
			eventBuffer.toArray( new MovementEvent[ eventBuffer.size() ]);
	}
	
	/**
	 * Removes an event from the buffer. Events can't be removed while the buffer is executing.
	 * 
	 * @param event		remove this event
	 * @param tile		remove event associated with this tile (any tile if <code>null</code>)
	 * @return			the removed event, <code>null</code> if nothing was removed
	 * @see				#isEventBufferExecuting()
	 */
	public MovementEvent removeEventFromBuffer( MovementEvent event, Tile tile ) {
		if (eventBuffer == null)
			return null;
		
		if (tile == null) {				// remove first, any tile
			int eventIndex = eventBuffer.indexOf( event );
			
			if (eventIndex < 0)
				return null;
			
			eventBuffer.remove( eventIndex );
			eventTiles.remove( eventIndex );
			return event;
		}
		
		int tileIndex = eventTiles.indexOf( tile );
		while (tileIndex >= 0) {		// remove first with same tile
			if (eventBuffer.elementAt( tileIndex ).equals( event )) {
				eventTiles.remove( tileIndex );
				return eventBuffer.remove( tileIndex );
			}
			
			tileIndex = eventTiles.indexOf( tile, tileIndex + 1 );
		}
		
		return null;		// correct event + tile pair not found
	}
	
	/**
	 * Checks if tile events are being executed from the buffer. Allows enter tile events to take
	 * effect only when the mobile object actually enters a tile, not if other tile events prevent
	 * it from entering. Movement events can't be added to the buffer while it's being executed.
	 * 
	 * @return			the event buffer is being executed
	 * @see				#addEventToBuffer(MovementEvent, Tile)
	 * @see				#isEventBuffered(MovementEvent, Tile)
	 */
	public boolean isEventBufferExecuting() {
		return eventBuffer == null;
	}
	
	/**
	 * Checks if a given tile is one the host occupied before it started moving.
	 * 
	 * @param tile		check if this tile is an initial tile
	 * @return			<code>true</code> for initial tiles
	 */
	public boolean isInitialTile( Tile tile ) {
		if (initialTiles != null)
			for (Tile initial : initialTiles)
				if (initial == tile)
					return true;
		
		return false;
	}

	/**
	 * Clears current movement radius and all associated data.
	 */
	public void clearRadius() {
		initialTiles = null;
		tileData.clear();
		occupyHeights.clear();
		eventBuffer.clear();
		eventTiles.clear();
	}
	
	/**
	 * Gets the movement cost which is treated as impassable. Any values higher than this are also
	 * treated as impassable.
	 * 
	 * @return			minimum move cost which makes a tile impassable
	 */
	protected int getImpassableMoveCost() {
		return IMPASSABLE_MOVE_COST;
	}
	
	/**
	 * Gets move cost of a terrain type with changes from movement modifiers. Terrain costs are
	 * saved, and re-calculated only when movement modifiers are added or removed. The terrain
	 * costs can be explicitly cleared in order to have them calculated again.
	 * 
	 * @param terrain		get cost for this terrain
	 * @return				cost for the given terrain, with modifiers
	 * @see					#clearTerrainCosts()
	 */
	protected float getTerrainCost( Terrain terrain ) {
		if (terrainCosts.containsKey( terrain ))
			return terrainCosts.get( terrain );
		
		float moveCost = terrain.getCost();

		for (MovementModifier mod : getMovementModifiers())
			moveCost = mod.modifyTerrainCost( terrain, moveCost );

		terrainCosts.put( terrain, moveCost );
		return moveCost;
	}
	
	/**
	 * Gets movement data for a tile, creating a new object if necessary. This data is
	 * tile-specific, and a tile can only have one instance of it. The tile data is kept until
	 * movement radius is cleared.
	 * 
	 * @param forTile		get movement data for this tile
	 * @return				the movement data for the tile, possibly a new object
	 * @see					#clearRadius()
	 */
	protected TileData createTileData( Tile forTile ) {
		TileData tileData = this.tileData.get( forTile );
		
		if (tileData == null)
			this.tileData.put( forTile, tileData = new TileData() );
		
		return tileData;
	}
	
	/**
	 * Adds changes from obstacles and modifiers to terrain move cost and saves it. This's the cost
	 * of a single tile, templates must be considered separately. Also, doesn't include costs from
	 * blocks, since they depend on moving direction. Height also affects cost, so cost is saved
	 * separately for different heights. If movement height is higher than the terrain's height,
	 * terrain cost is ignored and a default of one is used. Obstacle costs are then added.
	 * 
	 * @param tile			calculate movement cost at this tile
	 * @param height		movement height, ignores obstacles lower than this
	 * @return				movement cost at the given tile and the given height
	 * @see					MovementModifier#modifyTerrainCost(Terrain, float)
	 * @see					#addObstacleCosts(Tile, Height, float)
	 */
	protected float getAndSaveCost( Tile tile, Height height ) {
		TileData tileData = createTileData( tile );
		
		if (!tileData.isMoveCostSet( height )) {
			Terrain terrain = tile.getTerrain();

			// base move cost by terrain, unless moving higher
			float cost = (terrain.getHeight().compareTo( height ) >= 0 ?
				getTerrainCost( terrain ) : 1.0f);
			
			cost = addObstacleCosts( tile, height, cost );
			tileData.setMoveCost( cost, height );
		}
		
		return tileData.getMoveCost( height );
	}

	/**
	 * Applies obstacles to a tile's move cost, as well as obstacle changes from modifiers.
	 * 
	 * @param tile		the tile whose obstacles are added
	 * @param height	the host's movement height
	 * @param cost		cost without changes from the obstacles
	 * @return			cost with obstacles and related modifiers
	 * @see				MovementModifier#modifyObstacleCost(Tile, Obstacle[], float)
	 */
	protected float addObstacleCosts( Tile tile, Height height, float cost ) {
		Obstacle[] obstacles = tile.getObstacles();
		Obstacle[] notIgnored = new Obstacle[ obstacles.length ];
		int counter = 0;
		
		for (Obstacle o : obstacles)
			if (!getHost().equals( o ) && !isIgnored( o, height )) {
				cost = o.modifyMoveCost( cost, getImpassableMoveCost() );
				notIgnored[ counter++ ] = o;
			}
		
		if (counter < obstacles.length)
			obstacles = Arrays.copyOf( notIgnored, counter );
		
		// modifiers affect obstacle costs
		for (MovementModifier mod : getMovementModifiers())
			cost = mod.modifyObstacleCost( tile, obstacles, cost );
		
		return cost;
	}

	/**
	 * Applies blocks to a cost, as well as block changes from modifiers. Block costs aren't saved.
	 * 
	 * @param from			the tile which the host is leaving
	 * @param to			the tile which the host is entering
	 * @param minHeight		the host's minimum movement height
	 * @param moveHeight	actual movement height with terrain
	 * @param cost			cost without changes from blocks
	 * @return				cost with blocks and related modifiers
	 * @see					Block#modifyMoveCost(Tile, Tile, MobileObject, Height, float)
	 * @see					MovementModifier#modifyBlockCost(Tile, Tile, MobileObject, Height,
	 * 						Block[], float)
	 */
	protected float addBlockCosts( Tile from, Tile to, Height minHeight, Height moveHeight,
			float cost ) {
		Block[] fromBlocks = from.getBlocks();
		Block[] toBlocks = to.getBlocks();
		Block[] notIgnored = new Block[ fromBlocks.length + toBlocks.length ];
		int counter = 0;
		
		for (Block block : fromBlocks)		// effects from blocks in old tile
			if (!isIgnored( block, from, to, minHeight )) {
				cost = block.modifyMoveCost( from, to, host, moveHeight, cost );
				notIgnored[ counter++ ] = block;
			}
		
		for (Block block : toBlocks)		// effects from blocks in new tile
			if (!isIgnored( block, from, to, minHeight )) {
				cost = block.modifyMoveCost( from, to, host, moveHeight, cost );
				notIgnored[ counter++ ] = block;
			}
		
		if (counter < notIgnored.length)
			notIgnored = Arrays.copyOf( notIgnored, counter );
		
		// final changes from modifiers
		for (MovementModifier mod : getMovementModifiers())
			cost = mod.modifyBlockCost( to, from, host, moveHeight, notIgnored, cost );
		
		return cost;
	}

	/**
	 * Checks if blocks prevent movement when a template is partially on one side of the block and
	 * partially on the other. Each block in each template tile needs to be checked. Movement
	 * modifiers can ignore blocks, and blocks below movement height are ignored.
	 * 
	 * @param template		the host's template tiles
	 * @param height		the host's movement height
	 * @return				blocks allow the template to be placed at the given tiles
	 * @see					Block#allowsTemplate(Tile, MobileObject, Height, Tile[])
	 */
	protected boolean blocksAllowTemplate( Tile[] template, Height height ) {
		for (Tile t : template) {
			Block[] blocks = t.getBlocks();
			
			for (Block b : blocks) {		// check all blocks
				// needs to be ignored between the tile where it is and all neighbors in template
				for (int i = 0; i < template.length; i++) {
					if (t == template[i] || !t.isAdjacent( template[i] ) ||		// not neighbors
							isIgnored( b, t, template[i], height ))				// or ignored
						continue;
					
					if (!b.allowsTemplate( t, host, height, template ))
						return false;
				}
			}
		}
		
		return true;
	}

	/**
	 * Gets minimum movement or occupy height for the host mobile object, without terrain.
	 * Obstacles and movement modifiers can increase minimum height, and any terrain, obstacles,
	 * blocks and events below minimum move height will be ignored. Terrain height is not included
	 * here, because being partially on flat terrain, and partially on shallow terrain, for
	 * example, will not allow the mobile object to ignore the shallow terrain's effects. Terrain
	 * height also doesn't allow ignoring obstacles or events. For actual movement and the effects
	 * of events and blocks, terrain height is used. If nothing affects movement height, the
	 * default is deep. For occupy height, obstacles can't be ignored, and movement modifiers may
	 * handle height differently.
	 * 
	 * @param occupy		<code>true</code> for occupy height, <code>false</code> for movement
	 * @param template		get minimum movement height for this template
	 * @return				minimum movement height with obstacles and modifiers (<code>null</code>
	 * 						if template is partially outside the tile grid)
	 */
	protected Height getMinimumHeight( boolean occupy, Tile... template ) {
		Height minHeight = DEEP;
		
		for (Tile t : template) {
			if (t == null)		// can't move outside tile grid
				return null;
			
			// movement modifiers can modify height
			MovementModifier[] modifiers = getMovementModifiers();
			for (MovementModifier mod : modifiers)
				minHeight = Height.max( minHeight, mod.getMovementHeight( t, minHeight, occupy ));
		}
		
		for (Tile t : template)			// obstacles can modify height
			for (Obstacle o : t.getObstacles())
				if (occupy || !isIgnored( o, minHeight ))		// uses height from modifiers
					minHeight = Height.max( minHeight, o.modifyMoveHeight( minHeight ));
		
		return minHeight;
	}

	/**
	 * Gets minimum occupy or movement height for the mobile object, including terrain. The mobile
	 * object can't move below the height of the terrain. This is separate because terrain doesn't
	 * allow ignoring anything below its height, but obstacles and movement modifiers do. Terrain
	 * height or higher should be used for actual movement height.
	 * 
	 * @param minHeight		minimum movement height without terrain
	 * @param template		the host's template tiles
	 * @return				highest terrain height in the tiles, or parameter height if it's higher
	 */
	protected Height getTerrainHeight( Height minHeight, Tile... template ) {
		for (Tile t : template)		// select highest terrain height in template tiles
			if (t == null)
				return null;
			else
				minHeight = Height.max( minHeight, t.getTerrain().getHeight() );
		
		return minHeight;
	}
	
	/**
	 * Checks if a position can be occupied at a given height, including obstacles, blocks and
	 * movement modifiers in the whole template. Saves whether or not the tile can be occupied with
	 * this facing, and uses the saved data on subsequent calls. Tiles which are already occupied
	 * by an obstacle (excluding the host) may not be occupied under any circumstances, and the
	 * same goes for any position where the template is partially outside the tile grid.
	 * <p>
	 * Calculates the tile's move cost, saving it and using saved tile data when possible. An
	 * impassable tile can't be occupied by default, but movement modifiers can allow it. Blocks
	 * must also allow occupation, and they can't be overridden by movement modifiers. Occupy data
	 * for blocks is not saved because they depend on too many variables.
	 * 
	 * @param tile			check if this tile can be occupied
	 * @param facing		facing while occupying
	 * @param height		occupy height
	 * @return				the position can be occupied
	 * @see					#getAndSaveCost(Tile, Height)
	 */
	protected boolean canOccupy( Tile tile, Direction facing, Height height ) {
		if (!Templates.isTemplateDirection( host.getTemplate(), facing ))
			return false;
		
		Tile[] template = host.getTemplate().getTiles( tile, facing );
		
		for (Tile t : template) {
			TileData tileData = createTileData( t );
			
			// tile-specific things, doesn't include blocks
			if (!tileData.isCanOccupySet( height )) {
				boolean canBeOccupied = false;
	
				// no occupation in any situation if already occupied
				if (t.getOccupier() == null || t.getOccupier().equals( host )) {
					if (getAndSaveCost( t, height ) < getImpassableMoveCost())
						canBeOccupied = true;
	
					else {		// not normally occupiable but modifiers can allow
						MovementModifier[] mods = getMovementModifiers();
	
						for (int i = 0; i < mods.length && !canBeOccupied; i++)
							canBeOccupied |= mods[i].allowsOccupation( host, t );
					}
				}
				
				tileData.setCanBeOccupied( canBeOccupied, height );
			}
			
			if (!tileData.canBeOccupied( height ))
				return false;
			
			for (Block block : t.getBlocks())		// blocks must allow occupation
				if (block.getHeight( t ).compareTo( height ) >= 0 &&
						!block.allowsTemplate( t, host, height, template ))
					return false;
		}
		
		return true;
	}

	/**
	 * Checks saved occupy data to determine if a specific position can be occupied.
	 * 
	 * @param tile			occupy this tile
	 * @param facing		facing in the tile
	 * @return				can occupy
	 * @see					#getOccupyHeight(Tile, Direction)
	 */
	protected boolean canBeOccupied( Tile tile, Direction facing ) {
		return getOccupyHeight( tile, facing ) != null;
	}

	/**
	 * Gets previously saved occupy height for a specific tile and facing.
	 * 
	 * @param tile			get occupy height for this tile
	 * @param facing		get occupy height for this facing
	 * @return				occupy height, <code>null</code> if can't occupy or not set, also for
	 * 						facing directions that aren't suitable for the template
	 * @see					#setOccupyHeight(Tile, Direction, Height)
	 */
	protected Height getOccupyHeight( Tile tile, Direction facing ) {
		if (!Templates.isTemplateDirection( host.getTemplate(), facing ))
			return null;
		
		Height[] heights = occupyHeights.get( tile );
		if (heights == null)
			return null;
		
		int index = facing.ordinal() / 2;
		
		// can't occupy if height is blocking (not set) or null (unable to occupy)
		return Height.compareHeights( heights[ index ], VERY_HIGH ) > 0 ? null : heights[ index ];
	}

	/**
	 * Calculates or gets occupy height for a specific tile and facing.  If height is already set,
	 * returns that height. Otherwise calculates occupy height, and checks that the position can be
	 * occupied. If it can't, saves a <code>null</code> height to indicate this.
	 * 
	 * @param tile			set occupy height for this tile
	 * @param facing		set occupy height for this facing
	 * @see					#calculateOccupyHeight(Tile, Direction)
	 * @see					#canOccupy(Tile, Direction, Height)
	 * @see					#setOccupyHeight(Tile, Direction, Height)
	 */
	protected Height calculateOrGetOccupyHeight( Tile tile, Direction facing ) {
		Height occupyHeight = null;
		
		if (occupyHeights.containsKey( tile ) &&
				((occupyHeight = occupyHeights.get( tile )[ facing.ordinal() / 2 ]) == null ||
				occupyHeight.compareTo( BLOCKING ) < 0))
			return occupyHeight;		// occupy height already set for given direction
		
		occupyHeight = calculateOccupyHeight( tile, facing );
		if (occupyHeight != null && !canOccupy( tile, facing, occupyHeight ))
			return null;		// can't occupy
		
		return occupyHeight;
	}
	
	/**
	 * Sets occupy height for a specific tile and facing, as well as symmetric facing directions.
	 * 
	 * @param tile			set occupy height for this tile
	 * @param facing		set occupy height for this facing
	 * @param height		occupy height, <code>null</code> if can't occupy
	 */
	protected void setOccupyHeight( Tile tile, Direction facing, Height height ) {
		MovementTemplate template = host.getTemplate();
		
		// set for only given facing if template has no symmetricity
		if (!template.isHorizontallySymmetric() && !template.isVerticallySymmetric())
			setOccupyHeightForPosition( tile, facing, height );

		// set for all viable directions if template is completely symmetric
		else if (template.isHorizontallySymmetric() && template.isVerticallySymmetric()) {
			setOccupyHeightForPosition( tile, NORTHEAST, height );
			setOccupyHeightForPosition( tile, SOUTHEAST, height );
			setOccupyHeightForPosition( tile, SOUTHWEST, height );
			setOccupyHeightForPosition( tile, NORTHWEST, height );
		}

		// multiple facing directions with same height depending on symmetricities
		else {
			if (template.isHorizontallySymmetric() && facing.isDueNorth() ||
					template.isVerticallySymmetric() && facing.isDueEast())
				setOccupyHeightForPosition( tile, NORTHEAST, height );

			if (template.isHorizontallySymmetric() && facing.isDueSouth() ||
					template.isVerticallySymmetric() && facing.isDueEast())
				setOccupyHeightForPosition( tile, SOUTHEAST, height );

			if (template.isHorizontallySymmetric() && facing.isDueSouth() ||
					template.isVerticallySymmetric() && facing.isDueWest())
				setOccupyHeightForPosition( tile, SOUTHWEST, height );

			if (template.isHorizontallySymmetric() && facing.isDueNorth() ||
					template.isVerticallySymmetric() && facing.isDueWest())
				setOccupyHeightForPosition( tile, NORTHWEST, height );
		}
	}
	
	/**
	 * Sets occupy height for a specific tile and facing.
	 * 
	 * @param tile			set occupy height for this tile
	 * @param facing		set occupy height for this facing
	 * @param height		occupy height, <code>null</code> if can't occupy
	 */
	protected void setOccupyHeightForPosition( Tile tile, Direction facing, Height height ) {
		if (!Templates.isTemplateDirection( host.getTemplate(), facing ))
			return;
		
		Height[] heights = occupyHeights.get( tile );
		
		if (heights == null)		// new array, blocking for unset values
			occupyHeights.put( tile,
					heights = new Height[] { BLOCKING, BLOCKING, BLOCKING, BLOCKING });
		
		int index = facing.ordinal() / 2;
		heights[ index ] = Height.compareHeights( height, VERY_HIGH ) > 0 ? null : height;
	}

	/**
	 * Determines if movement events allow entering and calculates risk. This's a test for a single
	 * tile, not a template. Risks are tested once for each movement event in the tile. In
	 * addition, events that are added to the event buffer can be re-tested later. The buffer
	 * allows events to repeat themselves, but also to prevent the same event from being checked
	 * multiple times for multiple tiles entered at the same time.
	 * 
	 * @param moveTo		execute enter events in this tile
	 * @param minHeight		bypass events beneath this height
	 * @param moveHeight	actual movement height with terrain
	 * @return				negative if events prevent entering, otherwise the events' total risk
	 * @see					MovementModifier#getProtectionAgainstEvent(MovementEvent, Tile)
	 * @see					MovementEvent#canEnterTile(MobileObject, Tile, Height)
	 * @see					MovementEvent#getRisk(MobileObject, Tile, Height)
	 * @see					#addEventToBuffer(MovementEvent, Tile)
	 * @see					#isEventBuffered(MovementEvent, Tile)
	 */
	protected float testEnterEvents( Tile moveTo, Height minHeight, Height moveHeight ) {
		float risk = 0;
		
		for (MovementEvent event : moveTo.getMovementEvents()) {
			int protection = getProtectionAgainst( event, moveTo, minHeight );
			
			if (protection < 0 && !event.canEnterTile( host, moveTo, moveHeight ))
				return -1;					// event prevents entering
			
			else if (protection <= 0)		// on positive, event is ignored and has no effect
				risk += event.getRisk( host, moveTo, moveHeight );
		}
		
		return risk;
	}
	
	/**
	 * Checks if movement events within a template allow the host to leave its current location.
	 * Always returns <code>true</code> if movement events are ignored. Also checks initial tiles.
	 * Events in any of them can't prevent leaving. Sets the given tiles active before checking.
	 * 
	 * @param height		bypass events beneath this height
	 * @param moveFrom		test if the host can move further from this location
	 * @return				events allow the host to leave the given tiles
	 * @see					MovementModifier#getProtectionAgainstEvent(MovementEvent, Tile)
	 * @see					MovementEvent#canLeaveTile(MobileObject, Tile, Height)
	 */
	protected boolean testLeaveEvents( Height height, Tile... moveFrom ) {
		
		for (Tile t : moveFrom)
			for (MovementEvent event : t.getMovementEvents())
				if (getProtectionAgainst( event, t, height ) < 0 &&
						!event.canLeaveTile( host, t, height ))
					return false;	// one event preventing move is enough
		
		return true;
	}

	/**
	 * Executes enter events for a group of tiles during movement. Events below movement height
	 * don't get executed and modifiers can also ignore events.
	 * 
	 * @param minHeight			ignore movement events below this height
	 * @param terrainHeight		actual movement height with terrain
	 * @param eventsAt			execute enter events for these tiles
	 * @return					events allow entering
	 * @see						MovementModifier#getProtectionAgainstEvent(MovementEvent, Tile)
	 * @see						MovementEvent#enteringTile(MobileObject, Tile, Height)
	 */
	protected boolean executeEnterEvents( Height minHeight, Height terrainHeight, Tile[] eventsAt,
			int[] tileIndices ) {
		boolean enters = true;
		
		for (int i : tileIndices)		// enter tile events for each tile identified with index
			for (MovementEvent event : eventsAt[i].getMovementEvents()) {
				int protection = getProtectionAgainst( event, eventsAt[i], minHeight );
				
				if (protection <= 0)
					enters &= event.enteringTile(
							host, eventsAt[i], terrainHeight ) || protection == 0;
			}
	
		return enters;
	}

	/**
	 * Executes leave events for a group of tiles during movement. Events below movement height
	 * don't get executed and modifiers can also ignore events.
	 * 
	 * @param minHeight			ignore movement events below this height
	 * @param terrainHeight		actual movement height with terrain
	 * @param eventsAt			execute leave events for these tiles
	 * @return					events allow leaving
	 * @see						MovementModifier#getProtectionAgainstEvent(MovementEvent, Tile)
	 * @see						MovementEvent#leavingTile(MobileObject, Tile, Height)
	 */
	protected boolean executeLeaveEvents( Height minHeight, Height terrainHeight,
			Tile... eventsAt ) {
		boolean leaves = true;
		
		for (Tile t : eventsAt)		// leave tile events for each tile
			for (MovementEvent event : t.getMovementEvents()) {
				int protection = getProtectionAgainst( event, t, minHeight );
				
				if (protection <= 0)
					leaves &= event.leavingTile( host, t, terrainHeight ) || protection == 0;
			}
		
		return leaves;
	}

	/**
	 * Calculates total risk for all movement events in the event buffer. The buffer is cleared
	 * after the calculation.
	 * 
	 * @param height		the host's movement height
	 * @return				total risk for buffered events
	 * @see					#addEventToBuffer(MovementEvent, Tile)
	 * @see					MovementEvent#getRisk(MobileObject, Tile, Height)
	 */
	protected float getEventBufferRisk( Height height ) {
		float risk = 0f;
		executingBuffer = eventBuffer;
		eventBuffer = null;						// prevents adding events while buffer is executed
		
		for (int i = 0; i < executingBuffer.size(); i++)	// check events in the buffer
			risk += executingBuffer.get( i ).getRisk( host, eventTiles.get( i ), height );
		
		eventTiles.clear();
		executingBuffer.clear();
		eventBuffer = executingBuffer;
		executingBuffer = null;
		
		return risk;
	}
	
	/**
	 * Executes all enter tile events in the event buffer. The buffer is cleared after execution.
	 * The events can't interrupt movement at this point, so there's no return value.
	 * 
	 * @param height		the host's movement height
	 * @see					#addEventToBuffer(MovementEvent, Tile)
	 * @see					MovementEvent#enteringTile(MobileObject, Tile, Height)
	 */
	protected void executeEventBuffer( Height height ) {
		executingBuffer = eventBuffer;
		eventBuffer = null;		// prevents adding events while buffer is executed
		
		for (int i = 0; i < executingBuffer.size(); i++)
			executingBuffer.get( i ).enteringTile( host, eventTiles.get( i ), height );
		
		eventTiles.clear();
		executingBuffer.clear();
		eventBuffer = executingBuffer;
		executingBuffer = null;
	}
	
	/**
	 * Clears all movement events in the event buffer. The event buffer is cleared after executing
	 * the events in it, or calculating risk from them. It should be explicitly cleared when
	 * movement radius generation continues to another tile without calculating risk for the
	 * buffered events, or when the host moves to a different tile without the events being
	 * executed. Otherwise the buffered events will carry on to the next tile.
	 */
	protected void clearEventBuffer() {
		eventBuffer.clear();
		eventTiles.clear();
	}
	
	/**
	 * Checks if an obstacle can be ignored. Movement modifiers allow ignoring obstacles, and they
	 * can also be crossed over at a sufficient movement height.
	 * 
	 * @param obstacle		check if this obstacle can be ignored
	 * @param height		movement height (deep prevents ignoring because of height)
	 * @return				obstacle can be ignored
	 * @see					Obstacle#getTotalHeight()
	 * @see					MovementModifier#ignoresObstacle(Obstacle)
	 */
	protected boolean isIgnored( Obstacle obstacle, Height height ) {
		if (obstacle.getTotalHeight().compareTo( height ) < 0)
			return true;
		
		for (MovementModifier mod : getMovementModifiers())		// ignore by modifiers
			if (mod.ignoresObstacle( obstacle ))
				return true;
		
		return false;
	}

	/**
	 * Checks if a movement event can be ignored completely, or if it can't interrupt movement. It
	 * might not affect movement radius generation or movement, but can still cause a risk and get
	 * executed during movement. Movement modifiers can also make events completely ineffective so
	 * that they can't cause a risk and are not executed.
	 * 
	 * @param event			a movement event
	 * @param tile			the tile where the event is located
	 * @param height		the host's movement height
	 * @return				negative if movement event is unaffected, zero if it can't interrupt
	 * 						movement or positive if it's completely ignored and can't cause a risk
	 * 						or get executed
	 */
	protected int getProtectionAgainst( MovementEvent event, Tile tile, Height height ) {
		if (event.getHeight( tile ).compareTo( height ) < 0)
			return 1;				// moves over the event
		
		// events in initial tiles can't interrupt movement
		int protection = isInitialTile( tile ) ? 0 : -1;
		
		// check if modifiers protect against the event
		for (MovementModifier mod : getMovementModifiers())
			protection = Math.max( mod.getProtectionAgainstEvent( event, tile ), protection );
		
		return protection;
	}
	
	/**
	 * Checks if a block can be ignored. Movement modifiers allow ignoring blocks, and they can
	 * also be crossed over at a sufficient movement height.
	 * 
	 * @param block			check if this block can be ignored
	 * @param from			the tile which the host is leaving
	 * @param to			the tile which the host is entering
	 * @param height		movement height (deep prevents ignoring because of height)
	 * @return				block can be ignored
	 * @see					Block#getHeight(Tile)
	 * @see					MovementModifier#ignoresBlock(Block, Tile, Tile, Height)
	 */
	protected boolean isIgnored( Block block, Tile from, Tile to, Height height ) {
		if (block.getHeight( from ).compareTo( height ) < 0 &&
				block.getHeight( to ).compareTo( height ) < 0)
			return true;
		
		for (MovementModifier mod : getMovementModifiers())		// ignore by modifiers
			if (mod.ignoresBlock( block, from, to, height ))
				return true;
		
		return false;
	}

	/**
	 * Disables or enables this movement mode. The changes caused by disabling a movement mode
	 * actually take place in the host mobile object. Therefore, the host mobile object's disable
	 * method should be used to disable a movement mode. The host will in turn call this method.
	 * Notifies state change listeners if the movement mode's state actually changes.
	 * 
	 * @param disabled			<code>true</code> to disable, or <code>false</code> to enable
	 * @see						MobileObject#setMovementModeDisabled(MovementMode, boolean)
	 */
	void setDisabled( boolean disabled ) {
		this.disabled += disabled ? 1 : -1;
		
		if (listeners != null &&
				(this.disabled == 0 && !disabled || this.disabled == 1 && disabled))
			for (StateChangeListener <MovementMode> scl : listeners)
				scl.stateChanged( this );
	}

	/**
	 * Data object for storing tile-related data during movement radius generation. All data in
	 * this object applies to a single tile, and doesn't include anything that depends on
	 * direction, facing or templates.
	 * 
	 * @author Marko Tuominen
	 */
	protected class TileData {

		private float[] moveCost = null;					// move costs for varying heights
		private short moveCostSet = 0;						// which costs have been set
		private int htOffset = -1;							// used to index cost array
		
		private int canOccupy = 0;							// occupy checks for varying heights
		
	    /**
	     * Gets this tile's movement cost at a certain height. The cost must be calculated and set
	     * beforehand. Cost is impassable if it hasn't been set. Costs can be set separately for
	     * varying heights.
	     * 
	     * @param height	get cost for this height
	     * @return			the move cost for this tile at the given height
	     */
	    public float getMoveCost( Height height ) {
	    	return isMoveCostSet( height ) ? moveCost[ height.ordinal() - htOffset ] :
	    		getImpassableMoveCost();
	    }

	    /**
	     * Checks if movement cost is set at a given height.
	     * 
	     * @param height	check if cost at this height has been set
	     * @return			<code>true</code> if movement cost has been set
	     */
	    public boolean isMoveCostSet( Height height ) {
	    	return (moveCostSet & (1 << height.ordinal())) != 0;
	    }

	    /**
	     * Sets this tile's movement cost at a given height.
	     * 
	     * @param height	set cost at this height
	     * @param cost		set this as the tile's move cost
	     */
	    public void setMoveCost( float cost, Height height ) {
	    	
	    	if (moveCost == null) {		// no costs set yet
	    		moveCost = new float[1];
	    		htOffset = height.ordinal();
	    	}
	    	
	    	// set at different height, no place for this one
	    	else if (height.ordinal() < htOffset ||
	    			height.ordinal() - htOffset >= moveCost.length) {
	    		float[] costs =
	    			new float[ moveCost.length + Math.abs( htOffset - height.ordinal() )];
	    		Arrays.fill( costs, getImpassableMoveCost() );
	    		
	    		int diff = Math.max( 0, htOffset - height.ordinal() );
	    		for (int i = 0; i < moveCost.length; i++)
	    			costs[i + diff] = moveCost[i];
	    		
	    		moveCost = costs;
	    		if (height.ordinal() < htOffset)
	    			htOffset = height.ordinal();
	    	}
	    	
	    	moveCostSet |= 1 << height.ordinal();
	    	moveCost[ height.ordinal() - htOffset ] = cost;
	    }

	    /**
		 * Checks if the mobile object can occupy this tile. Also returns <code>false</code> if
		 * occupy check hasn't been made and the result saved.
		 * 
		 * @param height	can be occupied at this height
		 * @return			<code>true</code> if tile can be occupied
		 * @see				#setCanBeOccupied(boolean, Height)
		 */
		public boolean canBeOccupied( Height height ) {
			return isCanOccupySet( height ) && (canOccupy & (1 << 2 * height.ordinal())) != 0;
		}

		/**
		 * Checks if occupy check has been done and the result has been saved.
		 * 
		 * @param height	occupy check has been done at this height
		 * @return			occupy check has been done
		 * @see				#setCanBeOccupied(boolean, Height)
		 */
		public boolean isCanOccupySet( Height height ) {
			return (canOccupy & (1 << 2 * height.ordinal() + 1)) != 0;
		}

	    /**
	     * Saves knowledge whether or not the host can occupy a tile. This applies only to the
	     * single tile.
	     * 
	     * @param canBeOccupied		whether or not the host can occupy this tile
	     * @param height			saves whether or not the host can occupy at this height
	     */
		public void setCanBeOccupied( boolean canBeOccupied, Height height ) {
			
			int mask = 1 << 2 * height.ordinal();
			canOccupy |= mask << 1;		// mark set
			canOccupy &= ~mask;			// clear existing
			
			if (canBeOccupied)
				canOccupy |= mask;		// mark occupiable
		}
	}
}
