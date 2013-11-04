package fi.grimripper.loww.movement;

import static fi.grimripper.loww.Direction.NORTHEAST;
import static fi.grimripper.loww.Direction.NORTHWEST;
import static fi.grimripper.loww.Direction.SOUTHEAST;
import static fi.grimripper.loww.Direction.SOUTHWEST;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import fi.grimripper.loww.Direction;
import fi.grimripper.loww.Height;
import fi.grimripper.loww.templates.MovementTemplate;
import fi.grimripper.loww.templates.Templates;
import fi.grimripper.loww.tiles.Tile;

/**
 * This is the default movement implementation, which progresses outward from a starting position.
 * It uses the default movement costs for terrain and obstacles. A mobile object can move from a
 * tile to any of its neighbors, as long as movement cost, movement events or blocks don't prevent
 * it.
 * 
 * @author Marko Tuominen
 */
public class DefaultMovement extends MovementMode {

	private static Direction[] diagonals = { NORTHEAST, SOUTHEAST, SOUTHWEST, NORTHWEST };
	
	protected float totalMove = -1;
	
	// saves path data for search
	private LinkedList <PathData> progressTiles = new LinkedList <>();
	
	// saves risk paths for search
	private LinkedList <PathData> riskPaths = new LinkedList <>();
	
	// saves search results
	private Map <Tile, ArrayList <PathData>> pathData = new HashMap <>();
	
	/**
	 * Sets the movement mode's host and default movement.
	 * 
	 * @param host			attach movement mode to this host mobile object
	 * @param baseMove		set this movement value for the movement mode
	 */
	public DefaultMovement( MobileObject host, int baseMove ) {
		super( host, baseMove );
	}

	/**
	 * Determines movement radius, starting at the host's current location. The current location
	 * gets total move cost zero, and is set occupiable. The facing directions available in it are
	 * also set. Uses <code>PathData</code> objects to save path data. First clears old movement
	 * radius, then sets the starting tile's occupy height, creates a path data object for it using
	 * occupy height, and calls the <code>beginSearch</code> method to generate successors for the
	 * starting location. After that, removes path data objects for successors from a search list
	 * and calls <code>addProgressTiles</code> for each to generate new successors. Successors with
	 * a risk are stored separately, and iterated in the same way once there are no more safe paths
	 * to explore.
	 * 
	 * @see		PathData
	 * @see		#beginSearch(PathData, int)
	 * @see		#addProgressTiles(PathData)
	 */
	@Override
	public void movementRadius( int totalMove ) {
		clearRadius();
		
		Tile starting = getHost().getLocation();

		// leave events ignored from initial tiles
		initialTiles = getHost().getTemplate().getTiles( starting, getHost().getTemplateFacing() );
		
		// initialize the starting tile
		Height minHeight = getMinimumHeight( true, initialTiles );
		Height occupyHeight =
				calculateOrGetOccupyHeight( starting, getHost().getTemplateFacing() );
		setOccupyHeight( starting, getHost().getTemplateFacing(), occupyHeight );
		PathData pathData = new PathData( starting, null, 0, minHeight,
				occupyHeight, getHost().getFacing(), getHost().getTemplateFacing(), 0 );
		addPathData( pathData );		// adds initial path data to search
		progressTiles.clear();			// ...but it's handled separately so remove it
		
		beginSearch( pathData, totalMove );			// the first step
		
		// search all safe progress tiles first, then risk paths
		@SuppressWarnings( "unchecked" )
		LinkedList <PathData>[] searchPaths = (LinkedList <PathData>[])new LinkedList[] {
			progressTiles, riskPaths };
		
		for (LinkedList <PathData> tileList : searchPaths)
			// continue until there are no more tiles to continue to
			while (!tileList.isEmpty()) {
				
				// don't check if path data has been removed
				if (hasPathData( pathData = tileList.remove() )) {

					// leave events from previous tiles first, before adding progress tiles
					if (testLeaveEvents( pathData.getMoveHeight(),
							getHost().getTemplate().getTiles(
									pathData.getTile(), pathData.getTemplateFacing() )))
						addProgressTiles( pathData );

					else		// can turn in place even if events prevent leaving
						determineSuccessors( pathData );
				}
			}
		
		clearEventBuffer();			// might have events which didn't get executed
	}
	
	/**
	 * Generates path from saved path data. Can be used during movement radius generation, but only
	 * for tiles which have already been processed. Also, the path which is given during movement
	 * radius generation isn't necessarily the shortest since all paths haven't been explored yet.
	 */
	@Override
	public Tile[] getMovementPath( Tile pathTo, Direction facing ) {
		LinkedList <PathData> revPath = getReversedPath( pathTo, facing );
		
		// no path, or no destination
		if (revPath == null)
			return new Tile[0];
		
		return pathToArray( revPath, 0 );
	}

	/**
	 * Makes mobile object move along a path to a destination, executing movement events. The
	 * starting tile's enter tile events and the destination tile's leave tile events aren't
	 * executed. If movement events prevent the mobile object from moving to its destination, finds
	 * a place for the mobile object to occupy. This can be any tile along the movement path up to
	 * and including the last tile the mobile object entered, as well as the starting tile.
	 */
	@Override
	public Tile[] executeMovementPath( Tile destination, Direction facing ) {
		LinkedList <PathData> revPath = getReversedPath( destination, facing );
		if (revPath == null)
			return null;			// can't occupy

		MovementTemplate template = getHost().getTemplate();
		Tile[] oldTemplate =
				template.getTiles( getHost().getLocation(), getHost().getTemplateFacing() );
		Height minHeight = revPath.getLast().getMinHeight();
		Height moveHeight = revPath.getLast().getMoveHeight();
		
		// events in starting location can't interrupt but are executed
		executeLeaveEvents( minHeight, moveHeight, oldTemplate );
		getHost().setLocation( null );
		
		// enter and leave each tile before the destination
		for (int i = revPath.size() - 2; i > 0; i--) {
			PathData currentPD = revPath.get( i );
			Direction newFacing = currentPD.getTemplateFacing();
			
			Tile[] newTemplate = template.getTiles( currentPD.getTile(), newFacing );
			minHeight = currentPD.getMinHeight();
			moveHeight = currentPD.getMoveHeight();
			
			if (!executeEnterEvents(
					minHeight, moveHeight, newTemplate, template.getMoveIndices( newFacing )))
				return movementInterrupted( revPath, i + 1, true );
			
			// events allow entering, so execute buffered events as well
			executeEventBuffer( moveHeight );
			
			if (!executeLeaveEvents( minHeight, moveHeight, newTemplate ))
				return movementInterrupted( revPath, i, false );
			
			oldTemplate = newTemplate;
		}
		
		// finally, enter events for destination tile
		minHeight = revPath.getFirst().getMinHeight();
		moveHeight = revPath.getFirst().getMoveHeight();
		
		Tile[] newTemplate = template.getTiles(
				destination, revPath.getFirst().getTemplateFacing() );
		
		if (!executeEnterEvents( minHeight, moveHeight, newTemplate,
				template.getMoveIndices( revPath.getFirst().getTemplateFacing() )))
			return movementInterrupted( revPath, 1, true );
		
		// events allow entering, so execute buffered events as well
		executeEventBuffer( moveHeight );
		
		getHost().setPosition( facing, destination );
		return pathToArray( revPath, 0 );
	}

	@Override
	public void clearRadius() {
		super.clearRadius();
		pathData.clear();
		totalMove = -1;
		progressTiles.clear();
		riskPaths.clear();
	}

	/**
	 * Finds successive tiles for the host's starting location. By default, sets the movement
	 * mode's total move, and calls {@link #addProgressTiles(PathData)} to add tiles.
	 * 
	 * @param starting		path data for the host's main tile before moving
	 * @param totalMove		the host's total movement
	 */
	protected void beginSearch( PathData starting, int totalMove ) {
		this.totalMove = totalMove;
		addProgressTiles( starting );
	}

	/**
	 * Finds successive movement steps for a position during movement radius generation. These are
	 * tiles with facing directions where the mobile object could move from the given tile. New
	 * path data is added to a list to be handled as the movement radius generation continues.
	 * Tests for entering a tile are performed first. Tiles where the mobile object can't move or
	 * otherwise shouldn't be included in the search are not saved.
	 * <p>
	 * The search gets the tile's neighbors, and calculates movement costs to them. It doesn't
	 * continue to tiles where a path with equal or lower cost already exists. Also, events and
	 * blocks must allow moving, and total move can't be exceeded.
	 * <p>
	 * The possible neighbors for an asymmetric template are limited to one side, and there's an
	 * extra tile where the mobile object's main tile would be after turning in place. Cost to this
	 * tile is zero, and there's no need to check events.
	 * 
	 * @param fromData			contains tile, facing, total cost, movement height etc.
	 */
	protected void addProgressTiles( PathData fromData ) {
		
		MovementTemplate template = getHost().getTemplate();
		Tile[] oldTemplate = template.getTiles( fromData.getTile(), fromData.getTemplateFacing() );
		Tile[] neighbors = determineSuccessors( fromData );
		
		NeighborLoop: for (int i = 0; i < neighbors.length; i++) {	// check neighbors by direction
			clearEventBuffer();
			
			// use direction to adjacent tile, or keep facing if the tiles aren't adjacent
			Direction facing = (neighbors[i].isAdjacent( fromData.getTile() ) ?
				fromData.getTile().getDirection( neighbors[i] ) : fromData.getTemplateFacing());
			Direction templateFacing = Templates.getTemplateDirection(
					facing, fromData.getTemplateFacing(), template );
			Tile[] newTemplate = template.getTiles( neighbors[i], templateFacing );
			
			// get move height at the tile
			Height minHeight = getMinimumHeight( false, newTemplate );
			if (minHeight == null)
				continue;		// outside tile grid

			Height terrainHeight = getTerrainHeight( minHeight, newTemplate );
			if (!blocksAllowTemplate( newTemplate, terrainHeight ))
				continue;		// entry blocked
			
			float moveCost = -1;
			float risk = 0;
			int[] indices = template.getMoveIndices( templateFacing );
			
			// get highest cost for entered tiles and calculate risk
			for (int j : indices) {
				
				float nextRisk = testEnterEvents( newTemplate[j], minHeight, terrainHeight );
				if (nextRisk < 0)			// events prevent entering
					continue NeighborLoop;
				risk += nextRisk;			// total risk from entered tiles
				
				float cost = getAndSaveCost( newTemplate[j], minHeight );
				if (cost >= getImpassableMoveCost())		// tile is impassable
					continue NeighborLoop;
				
				cost = addBlockCosts(
						oldTemplate[j], newTemplate[j], minHeight, terrainHeight, cost );
				if (cost >= getImpassableMoveCost())		// tile is blocked
					continue NeighborLoop;
				
				moveCost = Math.max( moveCost, cost );		// highest cost in entered tiles
			}

			// events allow entering, so check risk for all events buffered for the template
			risk += getEventBufferRisk( terrainHeight );
			
			float totalCost = fromData.getTotalCost() + moveCost;
			risk += fromData.getRisk();
			PathData toData = new PathData( neighbors[i],
					fromData, totalCost, minHeight, terrainHeight, facing, templateFacing, risk );
			
			// already found a better path, or not enough movement
			if (totalCost > totalMove || !shouldKeepPath( toData ))
				continue;
			
			// can move to tile, check occupation and events using occupy height
			Height minOccupyHeight = getMinimumHeight( true, newTemplate );
			Height occupyHeight = calculateOrGetOccupyHeight( neighbors[i], templateFacing );
			if (occupyHeight != null && (!occupyHeight.equals( terrainHeight ) ||
					!minOccupyHeight.equals( minHeight )))
				for (int j = 0; j < newTemplate.length && occupyHeight != null; j++)
					if (testEnterEvents( newTemplate[j], minOccupyHeight, occupyHeight ) < 0)
						occupyHeight = null;		// can't enter because of events
			
			// add tile, but leave risk paths for later
			addPathData( toData );
			setOccupyHeight( neighbors[i], templateFacing, occupyHeight );
		}
	}

	/**
	 * Determines the default successor tiles for a tile. Usually, this means neighbors to all
	 * applicable directions around the tile, but asymmetric templates are handled differently. For
	 * them, only the directions on one side are returned. Instead of the other directions, makes
	 * the host turn in place and adds the main tile after the turn to the search.
	 * <p>
	 * Turning in place doesn't cost any movement, or change the tiles where the host is located.
	 * Therefore, the total cost and risk, movement height, and occupy data in the new main tile's
	 * path data are the same as in the old path data. Only the main tile and facing change.
	 * 
	 * @param pathData		path data for the host's position
	 * @return				neighbors to check
	 * @see					#addTurnInPlaceTile(Direction, PathData)
	 */
	protected Tile[] determineSuccessors( PathData pathData ) {
		if (getHost().getTemplate().isHorizontallySymmetric() &&
				getHost().getTemplate().isVerticallySymmetric())
			return pathData.getTile().getAccessibleNeighbors( null );	// direction doesn't matter
		
		// asymmetric templates need special care
		if (getHost().getTemplate().isHorizontallySymmetric() ||
				getHost().getTemplate().isVerticallySymmetric())
			addTurnInPlaceTile( pathData.getTemplateFacing().getOpposite(), pathData );
		
		else		// without any symmetricity, can turn to three directions
			for (Direction d : diagonals)
				if (d != pathData.getTemplateFacing())
					addTurnInPlaceTile( d, pathData );
		
		return pathData.getTile().getAccessibleNeighbors( Templates.getMoveDirections(
				pathData.getTemplateFacing(), getHost().getTemplate() ));
	}

	/**
	 * Adds a tile for turning in place to the search and sets its occupy properties.
	 * 
	 * @param newFacing		the mobile object turns to this facing
	 * @param pathData		path data for the mobile object's position before turning
	 */
	protected void addTurnInPlaceTile( Direction newFacing, PathData pathData ) {
		Direction templateFacing = Templates.getTemplateDirection( newFacing,
				pathData.getTemplateFacing(), getHost().getTemplate() );
		Tile turnInPlace = getHost().getTemplate().turnInPlace( pathData.getTile(),
				pathData.getTemplateFacing(), templateFacing );
		
		// template is the same, so same height
		PathData toData = new PathData(
				turnInPlace, pathData, pathData.getTotalCost(), pathData.getMinHeight(),
				pathData.getMoveHeight(), newFacing, templateFacing, pathData.getRisk() );
		
		// new path must have either lower cost or lower risk than any old one
		if (shouldKeepPath( toData )) {
			setOccupyHeight( turnInPlace, templateFacing,		// no occupy checks needed
					calculateOrGetOccupyHeight( turnInPlace, templateFacing ));
			
			// add path to tile's data and search (leave risk paths for later)
			addPathData( toData );
		}
	}
	
	/**
	 * Checks if a new path should be kept and added to the search. Keeps the new path if there's
	 * no path data to the given tile. Otherwise checks all existing path data using {@link
	 * #shouldKeepPath(PathData, PathData)}, unless the host's template is asymmetric and the old
	 * and new path data have different facing directions that would require the mobile object to
	 * turn in place. In that case, the old and new path data don't affect each other.
	 * 
	 * @param newPath		new path data with tile, facing etc.
	 * @return				path should be added
	 */
	protected boolean shouldKeepPath( PathData newPath ) {
		PathData[] pathData = getPathData( newPath.getTile() );
		if (pathData.length == 0)
			return true;		// no path data yet
		
		boolean keepPath = true;
		Direction newFacing = newPath.getTemplateFacing();
		
		// compare to all existing paths, those that are not worth exploring should be removed
		for (PathData pd : pathData) {
			if (!getHost().getTemplate().isHorizontallySymmetric() &&
					pd.getTemplateFacing().isDueEast() != newFacing.isDueEast() ||
					!getHost().getTemplate().isVerticallySymmetric() &&
					pd.getTemplateFacing().isDueNorth() != newFacing.isDueNorth())
				continue;	// no compare if asymmetric template and different relevant facing
			
			keepPath &= shouldKeepPath( newPath, pd );
		}
		
		return keepPath;
	}

	/**
	 * Compares new path data to an old path data, and decides if the new path should be kept. If
	 * any existing paths have higher total cost and equal or higher risk, they are removed. If any
	 * existing path has both equal or lower total cost and equal or lower risk, then the new path
	 * isn't needed.
	 * 
	 * @param newPath		the new path
	 * @param oldPath		the old path
	 * @return				new path should be kept
	 */
	protected boolean shouldKeepPath( PathData newPath, PathData oldPath ) {

		if (oldPath.getTotalCost() > newPath.getTotalCost() &&
				oldPath.getRisk() >= newPath.getRisk() ||
				oldPath.getTotalCost() == newPath.getTotalCost() &&
				oldPath.getRisk() > newPath.getRisk())
			removePathData( oldPath );		// new path is better

		// old path is better if it has equal or lower cost and equal or lower risk
		return (oldPath.getTotalCost() > newPath.getTotalCost() ||
				oldPath.getRisk() > newPath.getRisk());
	}

	/**
	 * Selects paths with the lowest risk, and then the one with the lowest cost from those.
	 * 
	 * @param pathData		select the best of these paths
	 * @param facing		template's facing in the tile
	 * @return				the best path
	 */
	protected PathData selectBestPath( PathData[] pathData, Direction facing ) {
		PathData currentPD = null;
		for (PathData pd : pathData)		// check all paths, select the best
			if (!getHost().getTemplate().isHorizontallySymmetric() &&
					pd.getTemplateFacing().isDueEast() != facing.isDueEast())
				continue;					// for asymmetric templates, facing must be the same
		
			else if (currentPD == null ||
					pd.getRisk() < currentPD.getRisk() || pd.getRisk() == currentPD.getRisk() &&
					pd.getTotalCost() < currentPD.getTotalCost())
				currentPD = pd;
		
		return currentPD;
	}

	/**
	 * Adds a path data object for a tile, and also adds it to the search.
	 * 
	 * @param data			add path data
	 */
	protected void addPathData( PathData data ) {
		ArrayList <PathData> pathData = this.pathData.get( data.getTile() );
		if (pathData == null)
			this.pathData.put( data.getTile(), pathData = new ArrayList <PathData>() );
		pathData.add( data );
		
		(data.getRisk() > 0 ? riskPaths : progressTiles).add( data );
	}
	
	/**
	 * Removes a path data object belonging to a tile.
	 * 
	 * @param data			remove path data
	 */
	protected void removePathData( PathData data ) {
		if (pathData.containsKey( data.getTile() ))
			pathData.get( data.getTile() ).remove( data );
	}
	
	/**
	 * Checks if this movement mode has a specific path data object.
	 * 
	 * @param data			check for this path data
	 * @return				movement mode has the path data
	 */
	protected boolean hasPathData( PathData data ) {
		return pathData.containsKey( data.getTile() ) &&
				pathData.get( data.getTile() ).contains( data );
	}
	
	/**
	 * Gets all path data objects for a tile.
	 * 
	 * @param forTile		get path data for this tile
	 * @return				the path data for the tile, if it exists
	 */
	protected PathData[] getPathData( Tile forTile ) {
		ArrayList <PathData> pathData = this.pathData.get( forTile );
		return (pathData == null ? new PathData[0] :
			pathData.toArray( new PathData[ pathData.size() ]));
	}

	/**
	 * Creates a path to a tile, starting backwards from destination.
	 * 
	 * @param destination	create path to this destination
	 * @return				the path in backwards order (destination in first index and starting
	 * 						location in the first), or <code>null</code> if can't occupy
	 * @see					#selectBestPath(PathData[], Direction)
	 */
	protected LinkedList <PathData> getReversedPath( Tile destination, Direction facing ) {
		if (!canBeOccupied( destination, facing ))
			return null;			// can't be occupied, so return null
			
		PathData[] pathData = getPathData( destination );
		PathData currentPD = selectBestPath( pathData, facing );
		
    	// build path in list from end to beginning
		LinkedList <PathData> revPath = new LinkedList <>();
    	while (currentPD != null) {
    		revPath.add( currentPD );
    		currentPD = currentPD.getPath();
    	}
    	
    	return revPath;
	}

	/**
	 * Finds a location where to set a mobile object whose movement was interrupted. If enter
	 * events were executed for a tile, but prevented entry, then leave events for that tile must
	 * be executed first. If the host was interrupted by enter tile events, the search first
	 * re-enters the previous tile. If the host was interrupted by leave events, then they have
	 * already been executed for the last tile, and the backwards search also starts by entering
	 * the previous tile. Then checks if the tile can be occupied, and if necessary, executes leave
	 * events, and continues backwards.
	 * 
	 * @param revPath			the path in backwards order
	 * @param interruptAt		index to the last tile which the mobile object could enter
	 * @param failedEntry		enter events were executed for a tile, but prevented entry, so the
	 * 							host must leave the tile
	 * @return					the path the mobile object moves to its final location (empty if
	 * 							the mobile object re-occupies its starting location)
	 */
	private Tile[] movementInterrupted( LinkedList <PathData> revPath, int interruptAt,
			boolean failedEntry ) {
		MovementTemplate template = getHost().getTemplate();
		PathData current = revPath.get( interruptAt - 1 );
		
		// interruption is the only case where event buffer can escape execution
		clearEventBuffer();
		
		// enter events interrupted -> must leave the tile again
		if (failedEntry)
			executeLeaveEvents( current.getMinHeight(), current.getMoveHeight(),
					template.getTiles( current.getTile(), current.getTemplateFacing() ));
		
		// check tiles in backwards order, last is starting tile which at least can be occupied
		int posIndex = interruptAt - 1;
		while (++posIndex < revPath.size()) {		// always a valid tile somewhere
			current = revPath.get( posIndex );
			Tile[] tiles = template.getTiles( current.getTile(), current.getTemplateFacing() );
			
			executeEnterEvents( current.getMinHeight(), current.getMoveHeight(), tiles,
					template.getMoveIndices( current.getTemplateFacing().getOpposite() ));
			
			// test if the entered tile can be occupied
			Height height = getOccupyHeight( current.getTile(), current.getTemplateFacing() );
			if (height != null)
				break;
			
			// movement events (can't interrupt again)
			executeLeaveEvents( current.getMinHeight(), current.getMoveHeight(), tiles );
		}

		// faces towards tile where would have moved next (next tile's post-move facing)
		getHost().setPosition(
				revPath.get( posIndex - 1 ).getTemplateFacing(), current.getTile() );
		return pathToArray( revPath, posIndex );
	}
	
	/**
	 * Copies path into an array.
	 * 
	 * @param revPath	a list of path data, in backwards order from destination tile, with
	 * 					starting tile in last index
	 * @param last		index of last tile to include
	 * @return			the given path in an array, without the starting tile
	 */
	private Tile[] pathToArray( LinkedList <PathData> revPath, int last ) {
		Tile[] path = new Tile[ revPath.size() - last - 1 ];

		for (int i = 0, j = revPath.size() - 2; i < path.length; i++, j--)
			path[i] = revPath.get( j ).getTile();
		
		return path;
	}
	
	/**
	 * A data object to store movement data relevant to a single tile in a path. Paths can change
	 * by facing if template is asymmetric. The tiles occupied at the destination can also vary
	 * depending on facing.
	 * 
	 * @author Marko Tuominen
	 */
	protected static class PathData {
		
		private Tile tile = null;						// the associated tile
		private PathData path = null;					// shortest path backtrack
		private float totalCost = Float.MAX_VALUE;		// total cost to reach tile
		private Height minHeight = null;				// minimum height without terrain
		private Height moveHeight = null;				// height where unit is moving
		private Direction facing = null;				// mobile object's facing in the tile
		private Direction templateFacing = null;		// template's facing in the tile
		private float risk = 0;						// total risk to reach this tile

		/**
		 * Sets basic path data, without occupation.
		 * 
		 * @param tile				the tile associated with this data object
		 * @param path				path data for previous tile in movement path
		 * @param totalCost			total cost needed to reach the tile
		 * @param minHeight			minimum height without terrain
		 * @param moveHeight		movement height at the tile
		 * @param facing			the mobile object's facing when moving through the tile
		 * @param templateFacing	facing for the mobile object's template in the tile
		 * @param risk				risk to reach the tile
		 */
		public PathData( Tile tile, PathData path, float totalCost, Height minHeight,
				Height moveHeight, Direction facing, Direction templateFacing, float risk ) {
			
			this.tile = tile;
			this.path = path;
			this.totalCost = totalCost;
			this.minHeight = minHeight;
			this.moveHeight = moveHeight;
			this.facing = facing;
			this.templateFacing = templateFacing;
			this.risk = risk;
		}
		
	    /**
		 * Gets the tile which is associated with this data object.
		 * 
		 * @return			associated tile
		 */
		public Tile getTile() {
			return tile;
		}

		/**
		 * Gets the path data for the tile from which this tile is easiest to reach.
		 * 
		 * @return			path data for previous step
		 */
		public PathData getPath() {
			return path;
		}

		/**
		 * Gets the total amount of movement needed to reach this tile.
		 * 
		 * @return			get total movement cost to reach this tile
		 */
		public float getTotalCost() {
			return totalCost;
		}

		/**
		 * Gets minimum movement height without terrain.
		 * 
		 * @return				minimum height
		 */
		public Height getMinHeight() {
			return minHeight;
		}
		
		/**
	     * Gets height where the host is moving at the tile.
	     * 
	     * @return				movement height
	     */
	    public Height getMoveHeight() {
	    	return moveHeight;
	    }

	    /**
	     * Gets the mobile object's facing when moving into the tile.
	     * 
	     * @return			facing at the tile
	     */
	    public Direction getFacing() {
	    	return facing;
	    }
	    
	    /**
	     * Gets facing for the mobile object's template when moving into the tile.
	     * 
	     * @return			facing at the tile
	     */
	    public Direction getTemplateFacing() {
	    	return templateFacing;
	    }
	    
	    /**
		 * Gets the risk for the path up to associated tile.
		 * 
		 * @return				total risk
		 */
		public float getRisk() {
			return risk;
		}

		/**
		 * Sets the previous tile in a mobile object's movement path.
		 * 
		 * @param path			path data for neighboring tile with preferred path
		 */
		public void setPath( PathData path ) {
			this.path = path;
		}

		/**
	     * Sets the total amount of movement needed to reach this tile.
	     * 
	     * @param totalCost		set total movement cost to reach this tile
	     */
	    public void setTotalCost( float totalCost ) {
	    	this.totalCost = totalCost;
	    }

	    /**
	     * Sets minimum movement height without terrain.
	     * 
	     * @param height		minimum height
	     */
	    public void setMinHeight( Height height ) {
	    	minHeight = height;
	    }
	    
	    /**
		 * Sets height where the host is moving at the tile.
		 * 
		 * @param height		movement height
		 */
		public void setMoveHeight( Height height ) {
			moveHeight = height;
		}

		/**
	     * Sets facing in the associated tile.
	     * 
	     * @param facing		facing
	     */
	    public void setFacing( Direction facing ) {
	    	this.facing = facing;
	    }

		/**
	     * Sets the direction which determines template tiles in the associated tile.
	     * 
	     * @param templateFacing		template facing
	     */
	    public void setTemplateFacing( Direction templateFacing ) {
	    	this.templateFacing = templateFacing;
	    }

		/**
		 * Sets the risk for the path up to associated tile.
		 * 
		 * @param risk			total risk
		 */
		public void setRisk( float risk ) {
			this.risk = risk;
		}
	}
}
