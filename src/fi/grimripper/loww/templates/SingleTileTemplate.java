package fi.grimripper.loww.templates;

import fi.grimripper.loww.Direction;
import fi.grimripper.loww.Point;
import fi.grimripper.loww.tiles.Tile;

/**
 * A template that consists of a single tile.
 * 
 * @author Marko Tuominen
 */
public class SingleTileTemplate implements MovementTemplate {

	@Override
	public boolean isHorizontallySymmetric() {
		return true;
	}

	@Override
	public boolean isVerticallySymmetric() {
		return true;
	}

	@Override
	public int getSize() {
		return 1;
	}

	@Override
	public int getWidth() {
		return 1;
	}

	@Override
	public int getHeight() {
		return 1;
	}

	@Override
	public Tile[] getTiles( Tile mainTile, Direction facing ) {
		return new Tile[] { mainTile };
	}

	@Override
	public Tile turnInPlace( Tile mainTile, Direction oldFacing, Direction newFacing ) {
		return mainTile;
	}

	@Override
	public Point getCenter( Tile location, Direction facing ) {
		return location.getCenter();
	}

	@Override
	public int[] getMoveIndices( Direction to ) {
		return new int[] { 0 };
	}
}
