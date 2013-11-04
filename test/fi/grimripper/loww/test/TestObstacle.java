package fi.grimripper.loww.test;

import fi.grimripper.loww.Height;
import fi.grimripper.loww.templates.ObstacleTemplate;
import fi.grimripper.loww.tiles.Obstacle;

public class TestObstacle extends Obstacle {

	private boolean blocksLoS = true;
	private boolean blocking = false;
	private boolean occupying = true;

	private float obstacleCost = 0f;

	private int moveHeightCheck = 0;
	
	public TestObstacle( Height height ) {
		this( height, 0f );
	}
	
	public TestObstacle( Height height, ObstacleTemplate template ) {
		super( height, template );
	}
	
	public TestObstacle( Height height, float obstacleCost ) {
		this( height, obstacleCost, new TestObstacleTemplate() );
	}
	
	public TestObstacle( Height height, float obstacleCost, ObstacleTemplate template ) {
		super( height, template );
		this.obstacleCost = obstacleCost;
	}

	public TestObstacle( Height height, float obstacleCost, boolean occupying,
			ObstacleTemplate template ) {
		super( height, template );
		this.obstacleCost = obstacleCost;
		this.occupying = occupying;
	}

	public TestObstacle( Properties properties ) {
		this( Height.LOW, new TestObstacleTemplate(), properties );
	}
	
	public TestObstacle( Height height, ObstacleTemplate template, Properties properties ) {
		super( height, template, properties );
	}

	public void setBlocksLineOfSight( boolean blocking ) {
		blocksLoS = blocking;
	}
	
	@Override
	public boolean blocksLineOfSight( Obstacle obstacle ) {
		return blocksLoS;
	}

	public void setBlocking( boolean blocking ) {
		this.blocking = blocking;
	}

	public int getMoveHeightCheck() {
		return moveHeightCheck;
	}
	
	public void resetMoveHeightCheck() {
		moveHeightCheck = 0;
	}
	
	@Override
	public Height modifyMoveHeight( Height height ) {
		moveHeightCheck++;
		
		if (!occupying)
			return height;
		
		return Height.max( height, getTotalHeight() );
	}

	public float getObstacleCost() {
		return obstacleCost;
	}

	public void setObstacleCost( float obstacleCost ) {
		this.obstacleCost = obstacleCost;
	}
	
	@Override
	public float modifyMoveCost( float cost, int impassable ) {
		if (obstacleCost > 0)
			return cost + obstacleCost;
		
		return blocking ? super.modifyMoveCost( cost, impassable ) : cost;
	}

	public void setOccupying( boolean occupying ) {
		this.occupying = occupying;
	}

	@Override
	public boolean occupiesTile() {
		return occupying;
	}

	@Override
	public String toString() {
		return "Test Obstacle, height " + getHeight();
	}
}
