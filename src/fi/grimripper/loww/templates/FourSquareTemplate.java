package fi.grimripper.loww.templates;

import static fi.grimripper.loww.Direction.*;
import fi.grimripper.loww.Direction;
import fi.grimripper.loww.Point;
import fi.grimripper.loww.tiles.Tile;

/**
 * A template of four squares. The template consists of a main square, a horizontal neighbor, a
 * vertical neighbor and a diagonal neighbor which is adjacent to all the others. The template
 * isn't suitable for hexes, because they don't have vertical neighbors. Template direction must be
 * diagonal, so that it determines both a horizontal and vertical direction for neighbors. The
 * template tiles are away from the direction, and the diagonal neighbor is in the opposite
 * direction from the main square.
 * 
 * @author Marko Tuominen
 */
public class FourSquareTemplate implements MovementTemplate {

	@Override
	public boolean isHorizontallySymmetric() {
		return false;
	}

	@Override
	public boolean isVerticallySymmetric() {
		return false;
	}

	@Override
	public int getSize() {
		return 4;
	}

	@Override
	public int getWidth() {
		return 2;
	}

	@Override
	public int getHeight() {
		return 2;
	}

	@Override
	public Tile[] getTiles( Tile mainTile, Direction facing ) {
		// the main tile, horizontal, vertical and diagonal neighbors away from facing
		return new Tile[] { mainTile,
			mainTile.getNeighbor( facing.isDueEast() ? WEST : EAST ),
			mainTile.getNeighbor( facing.isDueNorth() ? SOUTH : NORTH ),
			mainTile.getNeighbor( facing.getOpposite() )};
	}

	@Override
	public Tile turnInPlace( Tile mainTile, Direction oldFacing, Direction newFacing ) {
		// horizontal neighbor when turning horizontally
		if (newFacing.isDueNorth() == oldFacing.isDueNorth())
			return newFacing.isDueEast() == oldFacing.isDueEast() ? mainTile :
				mainTile.getNeighbor( newFacing.isDueEast() ? EAST : WEST );
		
		// new facing when turning diagonally, and vertical neighbor when turning vertically
		return mainTile.getNeighbor( newFacing.isDueEast() != oldFacing.isDueEast() ?
			newFacing : newFacing.isDueNorth() ? NORTH : SOUTH );
	}

	@Override
	public Point getCenter( Tile location, Direction facing ) {
		// average x and y of location center and neighbor opposite facing
		Point center = location.getCenter();
		Point backCenter = location.getNeighbor( facing.getOpposite() ).getCenter();
		
		center.setX( (center.getX() + backCenter.getX()) / 2 );
		center.setY( (center.getY() + backCenter.getY()) / 2 );
		
		return center;
	}

	@Override
	public int[] getMoveIndices( Direction to ) {
		if (to.isVertical())
			return new int[] { 0, 1 };		// main tile and horizontal neighbor
		
		if (to.isHorizontal())
			return new int[] { 0, 2 };		// main tile and vertical neighbor
		
		return new int[] { 0, 1, 2 };		// diagonal neighbor never moves to a new tile
	}
}
