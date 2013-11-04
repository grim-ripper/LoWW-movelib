package fi.grimripper.loww.templates;

import static fi.grimripper.loww.tiles.Hex.HEX_SIZE;
import fi.grimripper.loww.Direction;
import fi.grimripper.loww.Point;
import fi.grimripper.loww.tiles.Hex;
import fi.grimripper.loww.tiles.Tile;

/**
 * A template that includes a hex and all its neighbors, except remote neighbors. It would be
 * suitable for both hexes and squares, except that size and movement indices differ. In this
 * class, they are implemented for hexes, along with the tile getter. The template is symmetric in
 * both horizontal and vertical directions.
 * 
 * @author Marko Tuominen
 */
public class HexAndNeighborsTemplate implements MovementTemplate {

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
		return HEX_SIZE + 1;
	}

	@Override
	public int getWidth() {
		return 3;
	}

	@Override
	public int getHeight() {
		return 3;
	}

	@Override
	public Tile[] getTiles( Tile mainTile, Direction facing ) {
		Tile[] tiles = new Tile[ getSize() ];
		tiles[0] = mainTile;
		Direction[] neighborDirs = Hex.getHexSides();
		
		for (int i = 1; i < tiles.length; i++)
			tiles[i] = mainTile.getNeighbor( neighborDirs[i - 1] );
		
		return tiles;
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
		// movement indices include the one corresponding to direction, and both adjacent ones
		return new int[] { to.hexDir() + 1, (to.hexDir() + 1) % HEX_SIZE + 1,
			(to.hexDir() + 5) % HEX_SIZE + 1 };
	}
}
