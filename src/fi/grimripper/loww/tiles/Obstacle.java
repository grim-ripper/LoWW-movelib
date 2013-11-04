package fi.grimripper.loww.tiles;

import static fi.grimripper.loww.Height.*;
import fi.grimripper.loww.AdditionalProperties;
import fi.grimripper.loww.Direction;
import fi.grimripper.loww.Height;
import fi.grimripper.loww.templates.ObstacleTemplate;
import fi.grimripper.loww.templates.Templates;

/**
 * Obstacles are items that can be placed on a tile grid. Examples include puddles and rocks, but
 * also mobile objects like armies, and large immobile objects such as cities. An obstacle can be
 * something that simply exists on the tile grid, or it can affect the movement of mobile objects,
 * among other things. An obstacle by default is limited in what kind of effect it can have, but
 * additional functionality can be implemented through movement events, for example. Mobile objects
 * are also obstacles, with additional properties for movement.
 * <p>
 * Each obstacle resides in a tile, although there are also large obstacles which can spread into
 * multiple tiles. An obstacle has a template which determines what tiles the obstacle covers,
 * using the obstacle's location and the direction it faces.
 * <p>
 * Obstacles can prevent moving objects from moving across them, and also alter movement costs in
 * other ways. They also influence movement height. Depending on an obstacle's height, a mobile
 * object can move over it and not be affected by it. Some obstacles cause mobile objects to move
 * on top of them. An obstacle can occupy a tile so that no other occupying obstacles can be placed
 * in the same tile. A larger obstacle occupies all tiles in its template.
 * 
 * @author Marko Tuominen
 * @see MovementEvent
 * @see ObstacleTemplate
 * @see Tile
 */
public abstract class Obstacle implements AdditionalProperties {

	private Height height;
	private Height occupyHeight = null;

	private Properties properties = null;
	
    private ObstacleTemplate template;
	private Tile location = null;
	private Direction facing = null;
	private Direction templateFacing = null;
    
	/**
	 * Sets the obstacle's height and template. Location and facing will initially be
	 * <code>null</code>.
	 * 
	 * @param height		the obstacle's height
	 * @param template		the obstacle's template
	 */
	protected Obstacle( Height height, ObstacleTemplate template ) {
		this.height = height;
		this.template = template;
	}
	
	/**
	 * Sets the obstacle's height, template and additional properties. Location and facing will
	 * initially be <code>null</code>.
	 * 
	 * @param height		the obstacle's height
	 * @param template		the obstacle's template
	 * @param properties	the obstacle's additional properties
	 */
	protected Obstacle( Height height, ObstacleTemplate template, Properties properties ) {
		this.height = height;
		this.properties = properties;
		this.template = template;
	}

	@Override
	public Properties getProperties() {
		return properties;
	}

	/**
	 * Gets the obstacle's height.
	 * 
	 * @return				the obstacle's height
	 */
	public Height getHeight() {
		return height;
	}
    
	/**
	 * Gets the combination of occupy height and obstacle's own height. This is basically the sum
	 * of the two, but limited between the obstacle's own height and very high. A low obstacle will
	 * be low even if it's occupy height is less than flat, for example, while a low obstacle on
	 * top of a very high obstacle will be very high rather than blocking. A blocking obstacle will
	 * be blocking regardless of occupy height.
	 * 
	 * @return			obstacle's height combined with occupy height (<code>null</code> if the
	 * 					obstacle hasn't been placed on the tile grid)
	 * @see				#getHeight()
	 * @see				#getOccupyHeight()
	 */
	public Height getTotalHeight() {
		if (occupyHeight == null)
			return null;
	
		return Height.max( occupyHeight, Height.max( height, Height.min( VERY_HIGH,
				Height.getHeight( height.ordinal() + occupyHeight.ordinal() - FLAT.ordinal() ))));
	}
	
	/**
	 * Gets the height where the obstacle is located.
	 * 
	 * @return		current occupy height
	 */
	public Height getOccupyHeight() {
		return occupyHeight;
	}

	/**
	 * Sets the height where the obstacle is currently located. Occupy height depends on terrain,
	 * and it's also possible for an obstacle to be placed on top of another. Occupy height is not
	 * set automatically.
	 * 
	 * @param occupyHeight		current occupy height
	 */
	public void setOccupyHeight( Height occupyHeight ) {
		this.occupyHeight = occupyHeight;
	}

	/**
	 * Gets the obstacle's location, or main tile.
	 * 
	 * @return			the tile where the obstacle resides (or main tile for large obstacles)
	 */
	public Tile getLocation() {
		return location;
	}

	/**
	 * Sets the obstacle's location. The obstacle removes itself from its previous location, if
	 * applicable, then adds itself to its new location. Removing and adding apply to all tiles
	 * defined by the obstacle's template, using current facing. The update isn't done if the
	 * obstacle doesn't have a facing. The obstacle can't be placed partially outside the tile
	 * grid, but it can be removed completely by setting location as <code>null</code>. The
	 * obstacle's occupy height in the new location is by default set as terrain height. Highest
	 * terrain in the new template determines occupy height.
	 * 
	 * @param location		set the obstacle's location (main tile for its template)
	 */
	public void setLocation( Tile location ) {
		if (templateFacing == null)
			this.location = location;		
		
		else {
			removeFromLocation();
			this.location = location;
			addToLocation();
		}
	}

	/**
     * Gets the direction this obstacle is facing.
     * 
     * @return				the direction this obstacle is facing
     */
    public Direction getFacing() {
    	return facing;
    }
    
    /**
     * Gets the direction which determines template's position relative to obstacle location.
     * 
     * @return				template's facing direction
     */
    public Direction getTemplateFacing() {
    	return templateFacing;
    }
    
	/**
	 * Sets the obstacle's facing. Facing affects the obstacle's template, but otherwise it has no
	 * effect at all. Because template requires facing, the obstacle isn't added to the tile grid
	 * with only location, but needs facing as well. If facing changes, the obstacle will turn in
	 * place. The obstacle's facing and that of its template are separate, because some directions
	 * aren't suitable for determining template tiles.
	 * 
	 * @param facing		set the obstacle to face this direction
	 * @see					Templates#getTemplateDirection(Direction, ObstacleTemplate)
	 */
	public void setFacing( Direction facing ) {
		Direction newTemplateFacing = (facing == null ? null :
			templateFacing == null ? Templates.getTemplateDirection( facing, template ) :
				Templates.getTemplateDirection( facing, templateFacing, template ));
		this.facing = facing;
	
		if (templateFacing == newTemplateFacing)
			return;
		
		removeFromLocation();		// remove from old location

		if (templateFacing != null && facing != null)	// set new location with new facing
			location = template.turnInPlace( location, templateFacing, newTemplateFacing );

		templateFacing = newTemplateFacing;
		addToLocation();			// add to new location with new facing
	}

    /**
     * Sets facing and location at the same time. The obstacle will be removed from its current
     * tiles (if any), then added to the new tiles. In some situations, changing location without
     * changing facing can cause the obstacle's template to be partially outside the tile grid,
     * which is likely to cause problems.
     * 
     * @param facing		new facing
     * @param location		new location
     */
    public void setPosition( Direction facing, Tile location ) {
    	removeFromLocation();			// remove from old position
    	
    	setFacing( facing );			// facing first, isn't placed on the tile grid
    	setLocation( location );		// then location, gets placed on the tile grid
    }
    
	/**
     * Gets the tile template depicting the obstacle's size and shape.
     * 
     * @return				the obstacle's tile template
     */
    public ObstacleTemplate getTemplate() {
    	return template;
    }

	/**
	 * Check if this obstacle prevents normal movement and occupation. Actually tries to modify a
	 * 1.0 movement cost and checks if the result is higher than maximum movement. This test
	 * doesn't have anything to do with this obstacle occupying a tile.
	 * 
	 * @param impassable		the movement cost considered impassable
	 * @return					<code>true</code> if this obstacle prevents moving through the tile
	 * 							and occupying it
	 */
	public final boolean preventsMovement( int impassable ) {
		return modifyMoveCost( 1.0f, impassable ) >= impassable;
	}
	
	/**
	 * Modifies a mobile object's movement cost. By default, an obstacle prevents movement and this
	 * method returns a value that indicates impassability. Subclasses can have varying effects on
	 * movement costs.
	 * 
	 * @param cost				movement cost without the effects of this obstacle
	 * @param impassable		the amount of movement considered impassable
	 * @return					movement cost with the effects of this obstacle
	 */
	public float modifyMoveCost( float cost, int impassable ) {
		return impassable;
	}

	/**
	 * Modifies a mobile object's movement- and occupy height. By default, an obstacle doesn't have
	 * an effect on movement- or occupy height and this method returns the height unchanged.
	 * 
	 * @param height	height without the effects of this obstacle
	 * @return			height with the effects of this obstacle
	 */
	public Height modifyMoveHeight( Height height ) {
		return height;
	}

	/**
	 * Checks if this obstacle occupies the tiles where it resides. A tile can't contain two
	 * occupying obstacles. Mobile objects can only end their moves in a tile which they can
	 * occupy, including other nearby tiles for template. By default obstacles occupy tiles, so
	 * this method returns <code>true</code>.
	 * 
	 * @return		this obstacle occupies tiles where it's placed
	 */
	public boolean occupiesTile() {
		return true;
	}

	/**
	 * Checks if this obstacle blocks the line of sight for another obstacle. Obstacles can be
	 * biased in which obstacles can "see" through them. By default obstacles block line of sight
	 * so this method returns <code>true</code>.
	 * 
	 * @param obstacle		check if line of sight is blocked for this obstacle
	 * @return				the line of sight is blocked
	 */
	public boolean blocksLineOfSight( Obstacle obstacle ) {
		return true;
	}

	/**
	 * Sets the obstacle's height.
	 * 
	 * @param height		set the obstacle's height
	 */
	protected void setHeight( Height height ) {
		this.height = height;
	}

    /**
     * Sets the obstacle's template. Ignores <code>null</code> parameters. If the obstacle has a
     * location and a facing, changing its template will cause an update to the tiles where it
     * resides. When something changes an object's template, it should also ensure the new template
     * fits current location and facing.
     * 
     * @param t				the obstacle's new template
     */
    protected void setTemplate( ObstacleTemplate t ) {
    	if (t != null) {
    		removeFromLocation();			// remove from old tiles
    		template = t;
    		templateFacing = Templates.getTemplateDirection( templateFacing, t );
    		addToLocation();				// add to new tiles
    	}
    }

    /**
     * Adds the obstacle to its current location.
     */
    private void addToLocation() {
    	if (location != null && templateFacing != null)		// if not added, don't update
    		for (Tile t : template.getTiles( location, templateFacing )) {
    			t.addObstacle( this );
    			Height terrainHeight = t.getTerrain().getHeight();

    			if (Height.compareHeights( terrainHeight, occupyHeight ) > 0)	// highest terrain
    				occupyHeight = terrainHeight;
    		}
    }
    
    /**
     * Removes the obstacle from its current location.
     */
    private void removeFromLocation() {
    	if (location != null && templateFacing != null) {		// was placed, remove
			for (Tile t : template.getTiles( location, templateFacing ))
				t.removeObstacle( this );
			
			occupyHeight = null;		// no longer placed
		}
    }
}
