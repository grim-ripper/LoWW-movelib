package fi.grimripper.loww.tiles;

import fi.grimripper.loww.Direction;
import fi.grimripper.loww.Point;
import fi.grimripper.loww.tiles.Obstacle;
import fi.grimripper.loww.tiles.Tile;

public class TrackerTile extends Tile implements Comparable <TrackerTile> {

	private boolean obstacleAdded = false;
	private boolean obstacleRemoved = false;
	
	public TrackerTile() {
		super( Tile.class, 0, 0 );
	}
	
	public TrackerTile( int row, int column ) {
		super( Tile.class, row, column );
	}
	
	@Override
	void addObstacle( Obstacle obstacle ) {
		obstacleAdded = true;
		super.addObstacle( obstacle );
	}

	@Override
	public Obstacle removeObstacle( Obstacle obstacle ) {
		obstacleRemoved = true;
		return super.removeObstacle( obstacle );
	}

	@Override
	public double getWidth() {
		return 0;
	}

	@Override
	public double getHeight() {
		return 0;
	}

	@Override
	public Direction[] getPointDirections() {
		return null;
	}

	@Override
	public Point getPoint( Direction corner ) {
		return null;
	}

	@Override
	public Point getCenter() {
		return null;
	}

	@Override
	public Tile containsCoords( double x, double y ) {
		return null;
	}

	@Override
	public Direction getDirection( Tile tile ) {
		return null;
	}

	@Override
	public int compareTo( TrackerTile o ) {
		return super.hashCode() - o.hashCode();
	}
	
	public boolean obstacleHasBeenAdded() {
		return obstacleAdded;
	}
	
	public boolean obstacleHasBeenRemoved() {
		return obstacleRemoved;
	}
	
	public void clearTrackerState() {
		obstacleAdded = obstacleRemoved = false;
	}
}