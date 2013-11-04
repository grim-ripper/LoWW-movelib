package fi.grimripper.loww.templates;

import static fi.grimripper.loww.tiles.Square.MAX_NEIGHBORS;
import fi.grimripper.loww.Direction;
import fi.grimripper.loww.Point;
import fi.grimripper.loww.tiles.Square;
import fi.grimripper.loww.tiles.Tile;

/**
 * A template that includes a square and all its neighbors, except remote neighbors. It would be
 * suitable for both hexes and squares, except that size and movement indices differ. They are
 * implemented for squares, along with the tile getter. The template is symmetric in both
 * horizontal and vertical directions.
 * 
 * @author Marko Tuominen
 */
public class SquareAndNeighborsTemplate implements MovementTemplate {

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
		return MAX_NEIGHBORS + 1;
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
		Direction[] neighborDirs = Square.getNeighborDirections();
		
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
		// three new squares for horizontal/vertical move, five for diagonal
		int[] indices = new int[ to.isVertical() || to.isHorizontal() ? 3 : 5 ];
		
		// always movement direction and adjacent directions
		indices[ indices.length / 2 ] = to.squareDir() + 1;
		indices[ indices.length / 2 - 1 ] =
				(to.squareDir() + MAX_NEIGHBORS - 1) % MAX_NEIGHBORS + 1;
		indices[ indices.length / 2 + 1 ] = (to.squareDir() + 1) % MAX_NEIGHBORS + 1;
		
		if (to.isHorizontal() || to.isVertical())
			return indices;
		
		// for diagonal movement, also directions that are at distance two
		indices[0] = (to.squareDir() + MAX_NEIGHBORS - 2) % MAX_NEIGHBORS + 1;
		indices[ indices.length - 1 ] = (to.squareDir() + 2) % MAX_NEIGHBORS + 1;
		
		return indices;
	}
}
