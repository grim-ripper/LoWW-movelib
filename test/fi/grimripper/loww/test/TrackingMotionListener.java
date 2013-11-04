package fi.grimripper.loww.test;

import fi.grimripper.loww.movement.MotionListener;
import fi.grimripper.loww.tiles.Obstacle;
import fi.grimripper.loww.tiles.Tile;

public class TrackingMotionListener implements MotionListener {

	private Obstacle obstacleMovedTo = null;
	private Obstacle obstacleMovedFrom = null;

	private Tile movedToTile = null;
	private Tile movedFromTile = null;

	@Override
	public void objectMovedToTile( Obstacle obstacle, Tile tile ) {
		obstacleMovedTo = obstacle;
		movedToTile = tile;
	}

	@Override
	public void objectMovedFromTile( Obstacle obstacle, Tile tile ) {
		obstacleMovedFrom = obstacle;
		movedFromTile = tile;
	}

	
	public Obstacle getObstacleMovedTo() {
		return obstacleMovedTo;
	}

	public Tile getMovedToTile() {
		return movedToTile;
	}

	public Obstacle getObstacleMovedFrom() {
		return obstacleMovedFrom;
	}

	public Tile getMovedFromTile() {
		return movedFromTile;
	}

	public void reset() {
		obstacleMovedTo = obstacleMovedFrom = null;
		movedToTile = movedFromTile = null;
	}
}
