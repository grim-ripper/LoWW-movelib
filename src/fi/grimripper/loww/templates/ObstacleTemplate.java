package fi.grimripper.loww.templates;

import fi.grimripper.loww.Direction;
import fi.grimripper.loww.tiles.Tile;

/**
 * A basic tile template for various obstacles. The template's main operation is to produce the
 * tiles it includes, given a location. A symmetric template will always produce the same tiles for
 * the same main tile. It's also possible to create asymmetric templates, which depend on the
 * direction the obstacle is facing.
 * 
 * @author Marko Tuominen
 */
public interface ObstacleTemplate {

	/**
	 * Checks if the template is horizontally symmetric. A horizontally symmetric template will
	 * cover the same tiles for the same main tile regardless of whether facing is to the east or
	 * to the west. A horizontally asymmetric template needs either east or west facing, and can't
	 * have a vertical facing.
	 * 
	 * @return				template is horizontally symmetric
	 */
	public boolean isHorizontallySymmetric();

	/**
	 * Checks if the template is vertically symmetric. A vertically symmetric template will cover
	 * the same tiles for the same main tile regardless of whether facing is to the north or to the
	 * south. A vertically asymmetric template needs either north or south facing, and can't have a
	 * horizontal facing.
	 * 
	 * @return				template is vertically symmetric
	 */
	public boolean isVerticallySymmetric();
	
	/**
	 * Gets the tiles this template includes. The tile array must be the same size and have tiles
	 * with similar positions in the same indices regardless of location and facing. Therefore must
	 * include <code>null</code>s if the template is partially outside the tile grid. The templates
	 * should always be tested to be within the bounds, to avoid obstacles being placed partially
	 * outside the tile grid.
	 * 
	 * @param mainTile			location where the template is placed
	 * @param facing			facing direction that determines the template's position
	 * @return					tiles included in the template, with given location and facing
	 */
	public Tile[] getTiles( Tile mainTile, Direction facing );
	
	/**
	 * Gets new main tile when the template is turned. After the turn, the template should cover
	 * the same tiles as before. An asymmetric template's main tile may need to be moved so that
	 * it'll continue to occupy the same tiles. For symmetric templates, this method is
	 * unnecessary, and they can simply return the location.
	 * 
	 * @param mainTile			the template's main tile, prior to turning
	 * @param oldFacing			the direction which the template was facing before turning
	 * @param newFacing			the direction which the template faces after turning
	 * @return					new main tile after turning
	 */
	public Tile turnInPlace( Tile mainTile, Direction oldFacing, Direction newFacing );
}
