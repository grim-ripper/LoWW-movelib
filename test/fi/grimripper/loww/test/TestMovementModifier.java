package fi.grimripper.loww.test;

import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.HashMap;

import fi.grimripper.loww.Height;
import fi.grimripper.loww.movement.MobileObject;
import fi.grimripper.loww.movement.MovementModifier;
import fi.grimripper.loww.tiles.Block;
import fi.grimripper.loww.tiles.MovementEvent;
import fi.grimripper.loww.tiles.Obstacle;
import fi.grimripper.loww.tiles.Terrain;
import fi.grimripper.loww.tiles.Tile;

public class TestMovementModifier implements MovementModifier {

	private Height occupyHeight = null;
	private HashMap <Tile, Height> tileHeights = new HashMap <>();
	private int heightCheckCount = 0;
	
	private float terrainCost = -1f;
	private ArrayList <Terrain> checkedTerrain = new ArrayList <>();

	private boolean allowsOccupation = false;
	private boolean allowsOccupationCheck = false;
	
	private Obstacle ignoredObstacle = null;
	private boolean ignoreObstacleCheck = false;
	
	private float obstacleCost = 0f;
	private Obstacle[] obstaclesForCost = null;
	
	private Block ignoredBlock = null;
	private boolean ignoreBlockCheck = false;
	
	private float blockCost = 0f;
	private Block[] blocksForCost = null;
	
	private HashMap <MovementEvent, Integer> eventProtections = new HashMap <>();
	
	public TestMovementModifier() {}
	
	public TestMovementModifier( Height occupyHeight ) {
		this.occupyHeight = occupyHeight;
	}
	
	public TestMovementModifier( float terrainCost ) {
		this.terrainCost = terrainCost;
	}
	
	public TestMovementModifier( boolean allowsOccupying ) {
		this.allowsOccupation = allowsOccupying;
	}

	public TestMovementModifier( Obstacle ignoredObstacle ) {
		this.ignoredObstacle = ignoredObstacle;
	}
	
	public TestMovementModifier( Obstacle ignoredObstacle, float obtacleCost ) {
		this.ignoredObstacle = ignoredObstacle;
		this.obstacleCost = obtacleCost;
		this.terrainCost = 0f;
	}
	
	public TestMovementModifier( Block ignoredBlock, float blockCost ) {
		this.ignoredBlock = ignoredBlock;
		this.blockCost = blockCost;
	}
	
	public void setMovementHeight( Tile tile, Height height ) {
		tileHeights.put( tile, height );
	}
	
	public int getHeightCheckCount() {
		return heightCheckCount;
	}
	
	public void resetHeightCheckCount() {
		heightCheckCount = 0;
	}
	
	@Override
	public Height getMovementHeight( Tile atTile, Height height, boolean occupy ) {
		heightCheckCount++;
		
		if (occupy && occupyHeight != null)
			return occupyHeight;
		
		else if (tileHeights.containsKey( atTile ))
			return tileHeights.get( atTile );
		
		else
			return height;
	}

	public float getTerrainCost() {
		return terrainCost;
	}

	@Override
	public float modifyTerrainCost( Terrain terrain, float moveCost ) {
		if (terrain instanceof OnceOnlyTerrain) {
			assertFalse( checkedTerrain.contains( terrain ));
			checkedTerrain.add( terrain );
			return terrainCost;
		}
		
		return terrainCost >= 0f ? terrainCost : moveCost;
	}

	public Obstacle[] getObstaclesForCost() {
		return obstaclesForCost;
	}
	
	public void setObstaclesForCost( Obstacle[] obstaclesForCost ) {
		this.obstaclesForCost = obstaclesForCost;
	}

	public float getObstacleCost() {
		return obstacleCost;
	}

	@Override
	public float modifyObstacleCost( Tile tile, Obstacle[] obstacles, float moveCost ) {
		obstaclesForCost = obstacles;
		return moveCost + obstacleCost;
	}

	public float getBlockCost() {
		return blockCost;
	}
	
	public Block[] getBlocksForCost() {
		return blocksForCost;
	}
	
	public void resetBlocksForCost() {
		blocksForCost = null;
	}
	
	@Override
	public float modifyBlockCost( Tile to, Tile from, MobileObject mobile, Height height,
			Block[] blocks, float moveCost ) {
		blocksForCost = blocks;
		return moveCost + blockCost;
	}

	@Override
	public float modifyHeightChangeCost( Tile tile, float cost, Height height, Height newHeight ) {
		return cost;
	}
	
	public void setAllowsOccupation( boolean allowsOccupation ) {
		this.allowsOccupation = allowsOccupation;
	}

	public boolean getAllowsOccupationCheck() {
		return allowsOccupationCheck;
	}
	
	public void resetAllowsOccupationCheck() {
		allowsOccupationCheck = false;
	}
	
	@Override
	public boolean allowsOccupation( MobileObject occupier, Tile occupyTile ) {
		allowsOccupationCheck = true;
		return allowsOccupation;
	}

	public boolean ignoreObstacleChecked() {
		return ignoreObstacleCheck;
	}

	@Override
	public boolean ignoresObstacle( Obstacle obstacle ) {
		ignoreObstacleCheck = true;
		return obstacle == ignoredObstacle;
	}

	public boolean getIgnoreBlockCheck() {
		return ignoreBlockCheck;
	}
	
	public void resetIgnoreBlockCheck() {
		ignoreBlockCheck = false;
	}
	
	@Override
	public boolean ignoresBlock( Block block, Tile from, Tile to, Height height ) {
		ignoreBlockCheck = true;
		return block == ignoredBlock;
	}

	public void addEventProtection( MovementEvent event, int protection ) {
		eventProtections.put( event, protection );
	}
	
	public void removeEventProtection( MovementEvent event ) {
		eventProtections.remove( event );
	}
	
	public void clearEventProtections() {
		eventProtections.clear();
	}
	
	@Override
	public int getProtectionAgainstEvent( MovementEvent event, Tile tile ) {
		return eventProtections.containsKey( event ) ? eventProtections.get( event ) : -1;
	}
}
