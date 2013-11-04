package fi.grimripper.loww.tiles;

import static fi.grimripper.loww.Height.FLAT;
import fi.grimripper.loww.AdditionalProperties;
import fi.grimripper.loww.Height;

/**
 * Terrain for tiles: has a movement cost and height. Can also have miscellaneous properties that
 * affect how movement modes and movement modifiers treat the terrain. There's some support for
 * terrain that has special effects. The terrain object is notified when it's set to a tile and
 * can add movement events and such at that time. The terrain object is also notified when removed
 * from a tile.
 * 
 * @author Marko Tuominen
 */
public class Terrain implements AdditionalProperties {

	private float moveCost;
	private Height height = FLAT;
	
	private Properties props = null;
	
	/**
	 * Constructor for basic terrain types, sets movement cost and default height flat.
	 * 
	 * @param cost			set movement cost
	 */
	public Terrain( float cost ) {
		this.moveCost = cost;
	}
	
	/**
	 * Constructor for basic terrain types, sets movement cost and height.
	 * 
	 * @param cost			set movement cost
	 * @param height		set terrain height
	 */
	public Terrain( float cost, Height height ) {
		moveCost = cost;
		this.height = height;
	}
	
	/**
	 * Constructor for basic terrain types, sets movement cost, height and extra properties.
	 * 
	 * @param cost			set movement cost
	 * @param height		set terrain height
	 * @param props			additional properties for the terrain
	 */
	public Terrain( float cost, Height height, Properties props ) {
		moveCost = cost;
		this.height = height;
		this.props = props;
	}
	
	@Override
	public Properties getProperties() {
		return props;
	}

	/**
	 * Gets the terrain's unmodified movement cost. Movement modifiers can alter the cost.
	 * 
	 * @return		base movement cost of this terrain
	 */
	public float getCost() {
		return moveCost;
	}
	
	/**
	 * Gets the terrain's height.
	 * 
	 * @return		base height of this terrain without obstacles
	 */
	public Height getHeight() {
		return height;
	}
	
	/**
	 * Initializes special effects when the terrain is set to a tile. By default there are no such
	 * effects and this method does nothing.
	 * 
	 * @param tile		attach any events, listeners etc. to this tile
	 */
	public void setToTile( Tile tile ) {
		// do nothing
	}
	
	/**
	 * Removes special effects from a tile when the terrain is removed. By default there are no
	 * such effects and this method does nothing.
	 * 
	 * @param tile		remove any events, listeners etc. from this tile
	 */
	public void removeFromTile( Tile tile ) {
		// do nothing
	}
}
