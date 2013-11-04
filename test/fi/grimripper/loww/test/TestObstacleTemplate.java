package fi.grimripper.loww.test;

import static fi.grimripper.loww.Direction.EAST;
import static fi.grimripper.loww.Direction.WEST;
import fi.grimripper.loww.Direction;
import fi.grimripper.loww.templates.ObstacleTemplate;
import fi.grimripper.loww.tiles.Tile;

public class TestObstacleTemplate implements ObstacleTemplate {

	private boolean horizontallySymmetric = true;
	private boolean verticallySymmetric = true;
	private boolean getAlternate = false;
	private Tile alternateTile = null;
	private int size = 1;
	
	private Direction facing = null;
	
	public TestObstacleTemplate() {
	}

	public TestObstacleTemplate( boolean horizontallySymmetric, boolean verticallySymmetric ) {
		this.horizontallySymmetric = horizontallySymmetric;
		this.verticallySymmetric = verticallySymmetric;
	}

	@Override
	public boolean isHorizontallySymmetric() {
		return horizontallySymmetric;
	}

	@Override
	public boolean isVerticallySymmetric() {
		return verticallySymmetric;
	}

	public void setHorizontallySymmetric( boolean symmetric ) {
		this.horizontallySymmetric = symmetric;
	}
	
	@Override
	public Tile[] getTiles( Tile mainTile, Direction facing ) {
		return size == 1 ? new Tile[] { mainTile } : new Tile[] { mainTile,
			getAlternate ? turnInPlace( mainTile, facing, facing.getOpposite() ) : alternateTile };
	}

	@Override
	public Tile turnInPlace( Tile location, Direction oldFacing, Direction newFacing ) {
		if (size == 1 || oldFacing.isDueEast() == newFacing.isDueEast())
			return location;
		
		if (getAlternate) {
			if (newFacing.isDueEast())
				facing = EAST;
			else if (newFacing.isDueWest())
				facing = WEST;
			else if (oldFacing.isDueEast())
				facing = EAST;
			else if (oldFacing.isDueWest())
				facing = WEST;
			
			return location.getNeighbor( facing ); 
		}
		
		return alternateTile;
	}

	public int getSize() {
		return size;
	}
	
	public void setSize( int size ) {
		this.size = size;
	}
	
	public Tile getAlternateTile() {
		return alternateTile;
	}
	
	public void setAlternateTile( Tile alternateTile ) {
		this.alternateTile = alternateTile;
	}
	
	public void setGetAlternate( boolean getAlternate ) {
		this.getAlternate = getAlternate;
	}
}