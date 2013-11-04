package fi.grimripper.loww.test;

import fi.grimripper.loww.Direction;
import fi.grimripper.loww.Point;
import fi.grimripper.loww.templates.MovementTemplate;
import fi.grimripper.loww.tiles.Tile;

public class TestMovementTemplate extends TestObstacleTemplate implements MovementTemplate {
	
	public TestMovementTemplate() {
	}

	public TestMovementTemplate( boolean horizontallySymmetric, boolean verticallySymmetric ) {
		super( horizontallySymmetric, verticallySymmetric );
	}

	@Override
	public int getWidth() {
		return 0;
	}

	@Override
	public int getHeight() {
		return 0;
	}

	@Override
	public Point getCenter( Tile location, Direction facing ) {
		return null;
	}

	@Override
	public int[] getMoveIndices( Direction to ) {
		return getSize() == 1 ? new int[] { 0 } : new int[] { 0, 1 };
	}
}
