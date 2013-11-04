package fi.grimripper.loww.tiles;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Vector;

import fi.grimripper.loww.ArrayUtilities;
import fi.grimripper.loww.Direction;
import fi.grimripper.loww.Point;
import fi.grimripper.loww.AdditionalProperties.Properties;
import fi.grimripper.loww.AdditionalProperties.Property;
import fi.grimripper.loww.movement.MotionListener;

/**
 * This is the abstract super class for squares and hexes. Its purpose is to implement their common
 * properties and generalize them in order to enable using the same movement radius algorithms for
 * both squares and hexes. A tile has a specific type of terrain, such as mud or sand. In addition,
 * it can have multiple obstacles such as trees or boulders. Mobile objects are also obstacles.
 * Besides these, tiles can contain movement events, which are executed when a mobile object moves
 * into the tile, and can also stop their movement.
 * <p>
 * Although there can be multiple obstacles, only one of them can occupy the tile. Being occupied
 * means that another occupying obstacle can't be placed in the tile. Any number of obstacles that
 * don't occupy can be placed in a tile, whether or not there's an occupying obstacle in the tile.
 * <p>
 * Tiles can also contain blocks, which affect movement from one tile to another, rather than the
 * properties of the tile itself. Blocks also affect occupation. This is meant for obstacles that
 * cover multiple tiles separated by a block. There are also motion listeners which receive
 * notifications when a mobile object is placed into this tile, or removed.
 * 
 * @author Marko Tuominen
 * @see Block
 * @see MovementEvent
 * @see MotionListener
 * @see Obstacle
 */
public abstract class Tile {

	private int row = 0;
	private int col = 0;
	
	private Tile[] neighbors = null;
	private int neighborCount = -1;					// number of neighbors can vary at map edges

	private Tile[] remoteNeighbors = null;				// accessible but not adjacent tiles

	private Terrain terrain = null;    				// the tile's terrain
    private Vector <Obstacle> obstacles = null;		// obstacles in this tile
    
	private MotionListener[] motionListeners = new MotionListener[0];
	private MovementEvent[] movementEvents = new MovementEvent[0];
	private Block[] blocks = new Block[0];

	/**
	 * Constructs a tile with a specific type for neighbors. All neighbors added to the tile must
	 * be of this type, including remote neighbors.
	 * 
	 * @param tileType			tile type for neighbors
	 * @param row				the tile's row coordinate
	 * @param col				the tile's column coordinate
	 */
	protected <T extends Tile> Tile( Class <T> tileType, int row, int col ) {
		// a neighbor for each compass direction, although can have less
		neighbors = (Tile[])Array.newInstance( tileType, Direction.values().length );
		
		this.row = row;
		this.col = col;
	}
	
	/**
	 * Gets the tile's row coordinate.
	 * 
	 * @return			the tile's row (vertical) coordinate
	 */
	public int getRow() {
		return row;
	}
	
	/**
	 * Gets the tile's column coordinate.
	 * 
	 * @return			the tile's column (horizontal) coordinate
	 */
	public int getColumn() {
		return col;
	}
	
	/**
	 * Gets the tile's width.
	 * 
	 * @return			the tile's width
	 */
	public abstract double getWidth();
	
	/**
	 * Gets the tile's height.
	 * 
	 * @return			the tile's height
	 */
	public abstract double getHeight();
	
	/**
	 * Gets compass directions to the tile's points.
	 * 
	 * @return				directions to the tile's points
	 */
	public abstract Direction[] getPointDirections();
	
	/**
     * Gets one of the tile's points.
     * 
     * @param corner		specifies which corner is requested
     * @return				a <code>Point</code> with corner's coordinates, or <code>null</code>
     * 						for an invalid corner direction
     */
    public abstract Point getPoint( Direction corner );
    
    /**
     * Gets the center point for this tile.
     * 
     * @return				a <code>Point</code> with the tile's center point coordinates
     */
    public abstract Point getCenter();

	/**
	 * Gets an approximation of which tile contains the given coordinates. If they're within this
	 * tile, returns the tile itself. Otherwise returns one of its neighbors, the one closest to
	 * the given coordinates.
	 * 
	 * @param x			the x-coordinate
	 * @param y			the y-coordinate
	 * @return			this tile if the coordinates are inside it, or the closest neighbor (can be
	 * 					<code>null</code> if there isn't a neighbor in the correct direction)
	 */
	public abstract Tile containsCoords( double x, double y );
	
	/**
	 * Gets the direction from this tile to another tile. The other tile doesn't need to be a
	 * neighbor, so simply iterating the neighbors is insufficient. Coordinates are used for
	 * determining the direction. Returns a <code>null</code> if the tiles are of different type.
	 * 
	 * @param tile		get direction to this tile
	 * @return			direction to the tile
	 */
	public Direction getDirection( Tile tile ) {
		return tile.getClass() == this.getClass() ? TileGrid.getDirection( this, tile ) : null;
	}
	
	/**
	 * Checks if the given tile is adjacent to this tile. In other words, if the tile is one of the
	 * neighbors of this tile.
	 * 
	 * @param tile		check if this tile is adjacent
	 * @return			the tiles are adjacent
	 */
    public boolean isAdjacent( Tile tile ) {
    	return ArrayUtilities.linearSearch( neighbors, tile ) >= 0;
    }

	/**
	 * Gets this tile's terrain type.
	 * 
	 * @return			this tile's terrain type
	 */
	public Terrain getTerrain() {
		return terrain;
	}

	/**
	 * Sets this tile's terrain type.
	 * 
	 * @param terrain	set this as the tile's new terrain
	 */
	public void setTerrain( Terrain terrain ) {
		this.terrain = terrain;
	}

	/**
     * Counts this tile's neighbors. Most hexes have six neighbors, and most squares have eight,
     * but those at an edge of a tile grid have less.
     * 
     * @return		number of neighbors
     */
    public int countNeighbors() {

    	if (neighborCount < 0) {		    	// first time, count tiles
    		int count = 0;
    		
    		for (Tile n : neighbors)
    			if (n != null)
    				count++;
    		
    		neighborCount = count;
    	}
    	
    	return neighborCount;
    }

    /**
     * Gets one of the tile's neighbors, identified by a direction. If the tile doesn't have a
     * neighbor to that direction, returns <code>null</code>. This is the case if the requested
     * tile would be outside the tile grid.
     * 
     * @param direction		defines the direction of the requested neighbor
     * @return				the requested neighbor, or <code>null</code>
     */
    public Tile getNeighbor( Direction direction ) {
    	return direction == null ? null : neighbors[ direction.ordinal() ];
    }

	/**
	 * Copies the tile's neighbors into a new array. The array doesn't include <code>null</code>s
	 * for tiles at the edge of the tile grid.
	 * 
	 * @return				neighbors in a new array, without <code>null</code>s
	 */
	public Tile[] getNeighbors() {
		Tile[] copy = Arrays.copyOf( neighbors, neighbors.length );
		int setIndex = 0;
		
		for (Tile n : neighbors)
			if (n != null)
				copy[ setIndex++ ] = n;
	
		return setIndex == copy.length ? copy : Arrays.copyOf( copy, setIndex );
	}

    /**
	 * Gets the tiles that are accessible from this tile even though they're not adjacent.
	 * 
	 * @return				remote neighbors in a new array
	 */
	public Tile[] getRemoteNeighbors() {
		return remoteNeighbors == null ? Arrays.copyOf( neighbors, 0 ):		// same type, but empty
			Arrays.copyOf( remoteNeighbors, remoteNeighbors.length );
	}

	/**
	 * Gets the tiles which are accessible from this tile with direction limitation. Limitations
	 * are used in combination with asymmetric templates, to determine the directions they can move
	 * to without turning in place. Also, remote neighbors are accessible in addition to adjacent
	 * tiles. Returns all neighbors to given directions, ignoring <code>null</code> neighbors, as
	 * well as remote neighbors, in a single array.
	 *
	 * @param directions	get neighbors that are in these directions (all neighbors if
	 * 						<code>null</code>)
	 * @return				accessible neighbors, with direction limitations, and remote neighbors
	 */
	public Tile[] getAccessibleNeighbors( Direction[] directions ) {
		if (directions == null)
			directions = Direction.values();
		
		Tile[] neighbors = new Tile[ directions.length ];
		int counter = 0;
		
		for (Direction d : directions)
			if ((neighbors[ counter ] = getNeighbor( d )) != null)
				counter++;
		
		if (remoteNeighbors == null)
			return counter == neighbors.length ? neighbors : Arrays.copyOf( neighbors, counter );
		
		Tile[] accessible = Arrays.copyOf( neighbors, counter + remoteNeighbors.length );
		System.arraycopy( remoteNeighbors, 0, accessible, counter, remoteNeighbors.length );
		return accessible;
	}

	/**
	 * Removes a tile which is accessible from this tile even though it isn't adjacent.
	 * 
	 * @param neighbor		remove this remote neighbor
	 * @return				the removed remote neighbor, if any
	 */
	public Tile removeRemoteNeighbor( Tile neighbor ) {
		int index = ArrayUtilities.linearSearch( remoteNeighbors, neighbor );
		if (index < 0)
			return null;
		
		remoteNeighbors = ArrayUtilities.removeObject( remoteNeighbors, index );
		return neighbor;
	}
	
	/**
	 * Checks if the tile is occupied.
	 * 
	 * @return		tile is occupied
	 */
	public boolean isOccupied() {
		return getOccupier() != null;
	}

	/**
	 * Gets the obstacle currently occupying this tile.
	 * 
	 * @return		the obstacle which occupies this tile, or <code>null</code>
	 */
	public Obstacle getOccupier() {
		if (obstacles != null)
			for (Obstacle o : obstacles)
				if (o.occupiesTile())
					return o;
		
		return null;
	}

	/**
     * Gets an array of all the obstacles this tile contains.
     * 
     * @return				an array of all obstacles in this tile
     */
	public Obstacle[] getObstacles() {
    	return obstacles == null ? new Obstacle[0] :
    		obstacles.toArray( new Obstacle[ obstacles.size() ]);
    }

    /**
     * Gets an obstacle with a given property, if any.
     * 
     * @param prop		get the first obstacle with this property (including child properties)
     * @param offset	number of matching obstacles that are skipped before returning one (zero
     * 					for the first with the given property)
     * @return			first obstacle with the given property, or <code>null</code> if there
     * 					aren't any
     */
    public Obstacle getByProperty( Property prop, int offset ) {
    	Properties props = null;
    	
    	if (obstacles != null)
    		for (int i = offset; i < obstacles.size(); i++) {
    			props = obstacles.elementAt( i ).getProperties();

    			if (props != null && props.hasProperty( prop ))
    				return obstacles.elementAt( i );
    		}
    	
    	return null;
    }
    
    /**
     * Checks if this tile contains a specific obstacle.
     * 
     * @param obstacle		check if this obstacle resides within this tile
     * @return				this tile contains the specified obstacle
     */
    public boolean containsObstacle( Obstacle obstacle ) {
    	return obstacles != null && obstacles.contains( obstacle );
    }

    /**
     * Checks if this tile contains any obstacles with a particular property.
     * 
     * @param prop		check if there are obstacles with this property in this tile
     * @return			there's at least one obstacle with the given property
     */
    public boolean containsObstacleWithProperty( Property prop ) {
    	if (obstacles != null)
    		for (Obstacle o : obstacles)
    			if (o.getProperties().hasProperty( prop ))
    				return true;
    	
    	return false;
    }
    
    /**
	 * Removes an obstacle from this tile and notifies motion listeners.
	 * 
	 * @param obstacle		remove this obstacle
	 * @return				the obstacle that was removed, or <code>null</code>
	 */
	public Obstacle removeObstacle( Obstacle obstacle ) {
		if (obstacles == null || !obstacles.remove( obstacle ))
			return null;
		
		for (MotionListener ml : getMotionListeners())
			ml.objectMovedFromTile( obstacle, this );
		
		return obstacle;
	}

	/**
	 * Adds a block, which may change movement cost when entering or leaving this tile.
	 * 
	 * @param block			add a block
	 */
	public void addBlock( Block block ) {
		if (block == null)
			return;
		
		blocks = ArrayUtilities.appendObject( blocks, block );
	}

	/**
     * Gets the blocks attached to this tile.
     * 
     * @return		attached blocks
     */
    public Block[] getBlocks() {
    	return Arrays.copyOf( blocks, blocks.length );
    }
    
    /**
     * Removes a block attached to this tile.
     * 
     * @param block			remove this block, if it's present
     * @return				the removed block, if any
     */
    public Block removeBlock( Block block ) {
    	for (int i = 0; i < blocks.length; i++)
    		if (blocks[i].equals( block )) {
    			blocks = ArrayUtilities.removeObject( blocks, i );
    			return block;
    		}
    	
    	return null;
    }
    
    /**
	 * Adds an event that's triggered when a mobile object moves through this tile.
	 * 
	 * @param event		add this movement event
	 */
	public void addMovementEvent( MovementEvent event ) {
		if (event == null)
			return;
		
		movementEvents = ArrayUtilities.appendObject( movementEvents, event );
	}

	/**
     * Gets all movement events attached to this tile in an array.
     * 
     * @return			an array of attached movement events
     */
    public MovementEvent[] getMovementEvents() {
    	return Arrays.copyOf( movementEvents, movementEvents.length );
    }
    
    /**
     * Removes an attached movement event.
     * 
     * @param event		remove this event, if it exists
     * @return			the removed event, or <code>null</code> if it wasn't found
     */
    public MovementEvent removeMovementEvent( MovementEvent event ) {
    	for (int i = 0; i < movementEvents.length; i++)
    		if (movementEvents[i].equals( event )) {
    			movementEvents = ArrayUtilities.removeObject( movementEvents, i );
    			return event;
    		}
    	
    	return null;
    }

	/**
	 * Adds a listener that's notified when an object moves into or out of this tile.
	 * 
	 * @param listener		add this motion listener
	 */
	public void addMotionListener( MotionListener listener ) {
		if (listener == null)
			return;
		
		motionListeners = ArrayUtilities.appendObject( motionListeners, listener );
	}

	/**
	 * Checks if this tile has a specific motion listener.
	 * 
	 * @param listener		check if this listener is attached to the tile
	 * @return				the listener is attached to the tile
	 */
	public boolean hasMotionListener( MotionListener listener ) {
		return ArrayUtilities.linearSearch( motionListeners, listener ) >= 0;
	}
	
	/**
     * Gets all motion listeners attached to this tile in an array.
     * 
     * @return				a new array of attached motion listeners
     */
    public MotionListener[] getMotionListeners() {
    	return Arrays.copyOf( motionListeners, motionListeners.length );
    }
    
	/**
     * Removes an attached motion listener.
     * 
     * @param listener	remove this listener, if it exists
     * @return			the removed listener, or <code>null</code> if it wasn't found
     */
    public MotionListener removeMotionListener( MotionListener listener ) {
    	for (int i = 0; i < motionListeners.length; i++)
    		if (motionListeners[i].equals( listener )) {
    			motionListeners = ArrayUtilities.removeObject( motionListeners, i );
    			return listener;
    		}
    	
    	return null;
    }

	/**
	 * Sets one of the tile's neighbors.
	 * 
	 * @param neighbor					set as one of this tile's neighbors
	 * @param d							set as neighbor to this direction
	 * @throws ArrayStoreException		if tile is not of the type given in the constructor
	 */
	protected void setNeighbor( Tile neighbor, Direction d ) {
		neighbors[ d.ordinal() ] = neighbor;
		neighborCount = -1;
	}

	/**
	 * Adds a tile which is accessible from this tile even though it isn't adjacent.
	 * 
	 * @param neighbor					add this tile as a remote neighbor (<code>null</code>s are
	 * 									ignored)
	 * @throws ArrayStoreException		if tile is not of the type given in the constructor
	 */
	protected void addRemoteNeighbor( Tile neighbor ) {
		if (neighbor != null) {
			if (remoteNeighbors == null)		// ensure correct tile type
				(remoteNeighbors = Arrays.copyOf( neighbors, 1 ))[0] = neighbor;
			
			else
				remoteNeighbors = ArrayUtilities.appendObject( remoteNeighbors, neighbor );
		}
	}

	/**
	 * Adds an obstacle to this tile and notifies motion listeners. If there already is an
	 * occupying obstacle, another occupying obstacle can't be added.
	 * 
	 * @param obstacle			add this obstacle
	 * @see						MotionListener
	 */
	void addObstacle( Obstacle obstacle ) {
		if (obstacle.occupiesTile() && isOccupied())
			return;
		
		if (obstacles == null)
			obstacles = new Vector <Obstacle>();
		
		obstacles.add( obstacle );
		
		for (MotionListener ml : getMotionListeners())
			ml.objectMovedToTile( obstacle, this );
	}
}
