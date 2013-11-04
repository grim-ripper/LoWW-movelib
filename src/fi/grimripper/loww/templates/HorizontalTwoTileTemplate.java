package fi.grimripper.loww.templates;

import static fi.grimripper.loww.Direction.EAST;
import static fi.grimripper.loww.Direction.WEST;
import fi.grimripper.loww.Direction;
import fi.grimripper.loww.Point;
import fi.grimripper.loww.tiles.Tile;

/**
 * A template that consists of two horizontally adjacent tiles. The main tile is towards facing
 * direction, and the secondary tile is away from facing direction, "behind" the main tile. Because
 * the template is for horizontally adjacent tiles, the secondary tile can't be determined if
 * facing is vertical. Otherwise secondary tile will be decided based on whether the facing is
 * closer to east or west. The template is horizontally asymmetric, as the secondary tile is on one
 * side of the main tile and there's nothing on the other side, but vertically symmetric.
 * 
 * @author Marko Tuominen
 */
public class HorizontalTwoTileTemplate implements MovementTemplate {

	@Override
	public boolean isHorizontallySymmetric() {
		return false;
	}

	@Override
	public boolean isVerticallySymmetric() {
		return true;
	}

	@Override
	public int getSize() {
		return 2;
	}

	@Override
	public int getWidth() {
		return 2;
	}

	@Override
	public int getHeight() {
		return 1;
	}

	@Override
	public Tile[] getTiles( Tile mainTile, Direction facing ) {
		return new Tile[] { mainTile, mainTile.getNeighbor( facing.isDueEast() ? WEST : EAST )};
	}

	@Override
	public Tile turnInPlace( Tile mainTile, Direction oldFacing, Direction newFacing ) {
		// when facing changes between east and west, main tile and secondary tile switch places
		return oldFacing.isDueEast() == newFacing.isDueEast() ? mainTile :
			mainTile.getNeighbor( newFacing.isDueEast() ? EAST : WEST );
	}

	@Override
	public Point getCenter( Tile location, Direction facing ) {
		Point center = location.getCenter();
		center.setX( center.getX() + (facing.isDueEast() ? -1 : 1) * location.getWidth() / 2 );
		return center;
	}

	@Override
	public int[] getMoveIndices( Direction to ) {
		// only the main tile moves to a new tile on a horizontal move, while the secondary tile
		// becomes the former main tile
		return to.isHorizontal() ? new int[] { 0 } : new int[] { 0, 1 };
	}
}
