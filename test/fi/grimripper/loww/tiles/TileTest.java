package fi.grimripper.loww.tiles;

import static fi.grimripper.loww.Direction.EAST;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import org.junit.Test;

import fi.grimripper.loww.ArrayUtilities;
import fi.grimripper.loww.Direction;
import fi.grimripper.loww.AdditionalProperties.Properties;
import fi.grimripper.loww.AdditionalProperties.Property;
import fi.grimripper.loww.movement.MotionListener;
import fi.grimripper.loww.test.TestBlock;
import fi.grimripper.loww.test.TestMovementEvent;
import fi.grimripper.loww.test.TestObstacle;
import fi.grimripper.loww.test.TrackingMotionListener;
import fi.grimripper.loww.tiles.Block;
import fi.grimripper.loww.tiles.MovementEvent;
import fi.grimripper.loww.tiles.Obstacle;
import fi.grimripper.loww.tiles.Terrain;
import fi.grimripper.loww.tiles.Tile;

public class TileTest {
	
	@Test
	public void testTile() {
		TrackerTile tile = new TrackerTile( 3, 7 );
		assertEquals( 3, tile.getRow() );
		assertEquals( 7, tile.getColumn() );
	}
	
	@Test
	public void testTerrain() {
		TrackerTile tile = new TrackerTile();
		assertNull( tile.getTerrain() );
		
		Terrain terrain = new Terrain( 0, null );
		tile.setTerrain( terrain );
		assertSame( terrain, tile.getTerrain() );
	}
	
	@Test
	public void testNeighbors() {
		TrackerTile tile = new TrackerTile();
		
		// no neighbors have been set yet, so none are available
		assertEquals( 0, tile.countNeighbors() );
		assertEquals( 0, tile.countNeighbors() );
		assertArrayEquals( new Tile[0], tile.getNeighbors() );
		assertArrayEquals( new Tile[0], tile.getRemoteNeighbors() );
		assertNull( tile.getNeighbor( null ));
		assertArrayEquals( new Tile[0], tile.getAccessibleNeighbors( null ));
		
		// create neighbors, collect them in list and map by direction
		HashMap <Tile, Direction> neighborDirs = new HashMap <>();
		ArrayList <Tile> neighborList = new ArrayList <>();
		
		for (int i = 0; i < Direction.values().length; i++) {
			Tile neighbor = new TrackerTile();
			neighborList.add( neighbor );
			neighborDirs.put( neighbor, Direction.values()[i] );
		}
		
		Collections.shuffle( neighborList );	// add neighbors in random order
		Tile[] neighbors = neighborList.toArray( new Tile[ neighborList.size() ]);
		
		// set neighbors, ensure correct neighbor count and neighbor array
		for (int i = 0; i < neighborList.size(); i++) {
			tile.setNeighbor( neighbors[i], neighborDirs.get( neighbors[i] ));
			assertEquals( i + 1, tile.countNeighbors() );
			assertSame( neighbors[i], tile.getNeighbor( neighborDirs.get( neighbors[i] )));
			assertTrue( tile.isAdjacent( neighbors[i] ));
			
			Tile[] newNeighbors = tile.getNeighbors();
			Arrays.sort( newNeighbors );
			
			Tile[] expected = Arrays.copyOf( neighbors, i + 1 );
			Arrays.sort( expected );
			
			assertArrayEquals( expected, newNeighbors );
		}
		
		// now all previously added neighbors are accessible
		Tile[] accessible = tile.getAccessibleNeighbors( null );
		Arrays.sort( accessible );
		Arrays.sort( neighbors );
		assertArrayEquals( neighbors, accessible );
		
		// null can't be added as remote neighbor
		tile.addRemoteNeighbor( null );
		assertArrayEquals( new Tile[0], tile.getRemoteNeighbors() );
		
		// add remote neighbors and ascertain getters return them correctly
		Tile remoteOne = new TrackerTile();
		Tile remoteTwo = new TrackerTile();
		tile.addRemoteNeighbor( remoteOne );
		
		assertArrayEquals( new Tile[] { remoteOne }, tile.getRemoteNeighbors() );
		tile.addRemoteNeighbor( remoteTwo );
		assertArrayEquals( new Tile[] { remoteOne, remoteTwo }, tile.getRemoteNeighbors() );
		
		// adjacent and remote neighbors are now accessible
		accessible = tile.getAccessibleNeighbors( null );
		for (Tile t : neighbors)
			assertTrue( ArrayUtilities.linearSearch( accessible, t ) >= 0 );
		assertTrue( ArrayUtilities.linearSearch( accessible, remoteOne ) >= 0 );
		assertTrue( ArrayUtilities.linearSearch( accessible, remoteTwo ) >= 0 );

		// neighbors can be removed by settings them as null
		tile.setNeighbor( null, EAST );
		assertNull( tile.getNeighbor( EAST ));
		
		// remove remote neighbors and check that they are gone
		assertSame( remoteOne, tile.removeRemoteNeighbor( remoteOne ));
		assertNull( tile.removeRemoteNeighbor( remoteOne ));
		assertArrayEquals( new Tile[] { remoteTwo }, tile.getRemoteNeighbors() );
		
		assertSame( remoteTwo, tile.removeRemoteNeighbor( remoteTwo ));
		assertArrayEquals( new Tile[0], tile.getRemoteNeighbors() );
		
		// the removed neighbors are no longer accessible
		Tile east = null;
		accessible = tile.getAccessibleNeighbors( null );
		
		for (Tile t : neighbors)
			if (neighborDirs.get( t ) == EAST)
				east = t;
			else
				assertTrue( ArrayUtilities.linearSearch( accessible, t ) >= 0 );

		assertFalse( tile.isAdjacent( east ));
		assertTrue( ArrayUtilities.linearSearch( accessible, east ) < 0 );
		assertTrue( ArrayUtilities.linearSearch( accessible, remoteOne ) < 0 );
		assertTrue( ArrayUtilities.linearSearch( accessible, remoteTwo ) < 0 );
		
		Direction[] noEast = Direction.values();
		noEast[ ArrayUtilities.linearSearch( noEast, EAST )] = noEast[ noEast.length - 1 ];
		noEast = Arrays.copyOf( noEast, noEast.length - 1 );
		
		// without east neighbor, accessible tiles are same without east as with all directions
		Tile[] accessibleWithoutEast = tile.getAccessibleNeighbors( noEast );
		Arrays.sort( accessible );
		Arrays.sort( accessibleWithoutEast );
		assertArrayEquals( accessible, accessibleWithoutEast );
		
		// the same is true with remote neighbors
		tile.addRemoteNeighbor( remoteOne );
		accessible = tile.getAccessibleNeighbors( null );
		assertTrue( ArrayUtilities.linearSearch( accessible, remoteOne ) >= 0 );
		
		accessibleWithoutEast = tile.getAccessibleNeighbors( noEast );
		Arrays.sort( accessible );
		Arrays.sort( accessibleWithoutEast );
		assertArrayEquals( accessible, accessibleWithoutEast );
	}
	
	@Test
	public void testObstacles() {
		TrackerTile tile = new TrackerTile();
		
		Property testProp = new Property();
		Property childProp = new Property( testProp );
		Property prop3 = new Property();
		
		// no obstacles, so none are returned and contain check returns false
		assertNull( tile.getByProperty( testProp, 0 ));
		assertNull( tile.getByProperty( childProp, 3 ));
		assertFalse( tile.containsObstacleWithProperty( testProp ));
		
		// no obstacles, so not occupied (also check that nulls are handled)
		assertFalse( tile.isOccupied() );
		assertNull( tile.getOccupier() );
		assertArrayEquals( new Obstacle[0], tile.getObstacles() );
		assertFalse( tile.containsObstacle( null ));
		assertNull( tile.removeObstacle( null ));
		
		// obstacle motion listener is notified when an obstacle is added to the tile
		TestObstacle noOccupy = new TestObstacle( new Properties( childProp ));
		noOccupy.setOccupying( false );
		TrackingMotionListener tracker = new TrackingMotionListener();
		tile.addMotionListener( tracker );
		tile.addObstacle( noOccupy );

		// still not occupied, and nulls are handled correctly, but contains obstacle
		assertFalse( tile.isOccupied() );
		assertNull( tile.getOccupier() );
		assertFalse( tile.containsObstacle( null ));
		assertTrue( tile.containsObstacle( noOccupy ));
		
		// check that motion listener received correct event for adding obstacle
		assertSame( noOccupy, tracker.getObstacleMovedTo() );
		assertSame( tile, tracker.getMovedToTile() );
		assertNull( tracker.getObstacleMovedFrom() );
		assertNull( tracker.getMovedFromTile() );
		
		TestObstacle occupying = new TestObstacle( new Properties( testProp, prop3 ));
		occupying.setOccupying( true );
		tracker.reset();
		tile.addObstacle( occupying );
		
		// with an occupying obstacle, tile is now occupied
		assertTrue( tile.isOccupied() );
		assertSame( occupying, tile.getOccupier() );
		assertArrayEquals( new Obstacle[] { noOccupy, occupying }, tile.getObstacles() );
		
		// check that motion listener received correct event for adding obstacle
		assertSame( occupying, tracker.getObstacleMovedTo() );
		assertSame( tile, tracker.getMovedToTile() );
		assertNull( tracker.getObstacleMovedFrom() );
		assertNull( tracker.getMovedFromTile() );
		
		// obstacles can be found by their properties
		assertSame( noOccupy, tile.getByProperty( testProp, 0 ));
		assertSame( occupying, tile.getByProperty( testProp, 1 ));
		assertSame( occupying, tile.getByProperty( prop3, 0 ));
		assertTrue( tile.containsObstacleWithProperty( prop3 ));
		
		// two occupying obstacles can't reside in the same tile
		TestObstacle otherOccupying = new TestObstacle( (Properties)null );
		otherOccupying.setOccupying( true );
		tracker.reset();
		tile.addObstacle( otherOccupying );

		// the second occupying obstacle can't be added
		assertTrue( tile.isOccupied() );
		assertSame( occupying, tile.getOccupier() );
		assertArrayEquals( new Obstacle[] { noOccupy, occupying }, tile.getObstacles() );
		
		// motion listener wasn't notified because nothing was added or removed
		assertNull( tracker.getMovedToTile() );
		assertNull( tracker.getMovedFromTile() );
		assertNull( tracker.getObstacleMovedTo() );
		assertNull( tracker.getObstacleMovedFrom() );

		// multiple non-occupying obstacles can be placed in a tile
		TestObstacle otherNoOccupy = new TestObstacle( (Properties)null );
		otherNoOccupy.setOccupying( false );
		tracker.reset();
		tile.addObstacle( otherNoOccupy );
		
		// null isn't a valid obstacle property, but doesn't cause exceptions
		assertNull( tile.getByProperty( null, 0 ));
		
		assertArrayEquals( new Obstacle[] { noOccupy, occupying, otherNoOccupy }, tile.getObstacles() );
		assertSame( otherNoOccupy, tracker.getObstacleMovedTo() );
		assertSame( tile, tracker.getMovedToTile() );
		assertNull( tracker.getObstacleMovedFrom() );
		assertNull( tracker.getMovedFromTile() );
		
		// remove the occupying obstacle, and the tile is no longer occupied
		tracker.reset();
		assertSame( occupying, tile.removeObstacle( occupying ));
		assertNull( tile.removeObstacle( occupying ));		// removing twice isn't a problem
		assertFalse( tile.isOccupied() );
		assertNull( tile.getOccupier() );
		
		// removing the obstacle allows finding another obstacle was after it before
		assertTrue( tile.containsObstacleWithProperty( testProp ));
		assertSame( noOccupy, tile.getByProperty( testProp, 0 ));
		
		// event for removing obstacle, nothing was added
		assertSame( occupying, tracker.getObstacleMovedFrom() );
		assertSame( tile, tracker.getMovedFromTile() );
		assertNull( tracker.getObstacleMovedTo() );
		assertNull( tracker.getMovedToTile() );
		
		// now an occupying obstacle can be added
		tracker.reset();
		tile.addObstacle( otherOccupying );
		assertTrue( tile.isOccupied() );
		assertSame( otherOccupying, tile.getOccupier() );
		
		// check motion listener events
		assertSame( otherOccupying, tracker.getObstacleMovedTo() );
		assertSame( tile, tracker.getMovedToTile() );
		assertNull( tracker.getObstacleMovedFrom() );
		assertNull( tracker.getMovedFromTile() );
		
		// check that obstacles are removed correctly and tile still works after they are gone
		assertSame( otherOccupying, tile.removeObstacle( otherOccupying ));
		assertSame( noOccupy, tile.removeObstacle( noOccupy ));
		assertSame( otherNoOccupy, tile.removeObstacle( otherNoOccupy ));
		assertArrayEquals( new Obstacle[0], tile.getObstacles() );
		
		assertNull( tile.getByProperty( testProp, 0 ));
		assertFalse( tile.containsObstacleWithProperty( testProp ));
	}
	
	@Test
	public void testBlocks() {
		TrackerTile tile = new TrackerTile();
		
		// no blocks have been added yet, and null isn't accepted
		assertArrayEquals( new Block[0], tile.getBlocks() );
		tile.addBlock( null );
		assertArrayEquals( new Block[0], tile.getBlocks() );
		
		// after a block has been added it can be retrieved with getter
		Block testBlock = new TestBlock();
		tile.addBlock( testBlock );
		assertArrayEquals( new Block[] { testBlock }, tile.getBlocks() );
		
		// add another block and test getter again
		Block testBlock2 = new TestBlock();
		tile.addBlock( testBlock2 );
		assertArrayEquals( new Block[] { testBlock, testBlock2 }, tile.getBlocks() );
		
		// a block can be added multiple times
		tile.addBlock( testBlock );
		assertArrayEquals( new Block[] { testBlock, testBlock2, testBlock }, tile.getBlocks() );
		
		// first instance of a block is removed (their order isn't important for their effects)
		assertSame( testBlock, tile.removeBlock( testBlock ));
		assertArrayEquals( new Block[] { testBlock2, testBlock }, tile.getBlocks() );
		
		// second instance can be removed normally regardless of the first
		assertSame( testBlock, tile.removeBlock( testBlock ));
		assertArrayEquals( new Block[] { testBlock2 }, tile.getBlocks() );
		
		// attempting to remove a block that isn't in the tile causes no issues
		assertNull( tile.removeBlock( testBlock ));
		assertSame( testBlock2, tile.removeBlock( testBlock2 ));
		assertArrayEquals( new Block[0], tile.getBlocks() );
	}

	@Test
	public void testMovementEvents() {
		TrackerTile tile = new TrackerTile();
		
		// no movement events have been added yet, and null isn't accepted
		assertArrayEquals( new MovementEvent[0], tile.getMovementEvents() );
		tile.addMovementEvent( null );
		assertArrayEquals( new MovementEvent[0], tile.getMovementEvents() );
		
		// after adding a movement event, it can be retrieved with the getter
		MovementEvent testME = new TestMovementEvent();
		tile.addMovementEvent( testME );
		assertArrayEquals( new MovementEvent[] { testME }, tile.getMovementEvents() );
		
		// multiple movement events can be added
		MovementEvent testME2 = new TestMovementEvent();
		tile.addMovementEvent( testME2 );
		assertArrayEquals( new MovementEvent[] { testME, testME2 }, tile.getMovementEvents() );
		
		// a single movement event can be added multiple times
		tile.addMovementEvent( testME );
		assertArrayEquals( new MovementEvent[] { testME, testME2, testME }, tile.getMovementEvents() );
		
		// first instance of an event is removed (their order isn't important for their effects)
		assertSame( testME, tile.removeMovementEvent( testME ));
		assertArrayEquals( new MovementEvent[] { testME2, testME }, tile.getMovementEvents() );
		
		// second instance can be removed normally despite the first
		assertSame( testME, tile.removeMovementEvent( testME ));
		assertArrayEquals( new MovementEvent[] { testME2 }, tile.getMovementEvents() );
		
		// attempting to remove an event that isn't in the tile causes no issues
		assertNull( tile.removeMovementEvent( testME ));
		assertSame( testME2, tile.removeMovementEvent( testME2 ));
		assertArrayEquals( new MovementEvent[0], tile.getMovementEvents() );	
	}

	@Test
	public void testMotionListeners() {
		TrackerTile tile = new TrackerTile();
		
		// no motion listeners have been added
		assertFalse( tile.hasMotionListener( null ));
		MotionListener testL = new TrackingMotionListener();
		assertFalse( tile.hasMotionListener( testL ));
		assertArrayEquals( new MotionListener[0], tile.getMotionListeners() );
		
		// a null isn't accepted
		tile.addMotionListener( null );
		assertArrayEquals( new MotionListener[0], tile.getMotionListeners() );
		
		// after adding, motion listener is found by check and getter
		tile.addMotionListener( testL );
		assertTrue( tile.hasMotionListener( testL ));
		assertArrayEquals( new MotionListener[] { testL }, tile.getMotionListeners() );
		
		// multiple motion listeners can be added
		MotionListener testL2 = new TrackingMotionListener();
		tile.addMotionListener( testL2 );
		assertTrue( tile.hasMotionListener( testL2 ));
		assertArrayEquals( new MotionListener[] { testL, testL2 }, tile.getMotionListeners() );
		
		// an instance of a motion listener can be added multiple times
		tile.addMotionListener( testL );
		assertArrayEquals( new MotionListener[] { testL, testL2, testL }, tile.getMotionListeners() );
		
		// first instance of a listener is removed (their order isn't important for their effects)
		assertSame( testL, tile.removeMotionListener( testL ));
		assertTrue( tile.hasMotionListener( testL ));
		assertArrayEquals( new MotionListener[] { testL2, testL }, tile.getMotionListeners() );
		
		// second instance can be removed regardless of the first
		assertSame( testL, tile.removeMotionListener( testL ));
		assertFalse( tile.hasMotionListener( testL ));
		assertArrayEquals( new MotionListener[] { testL2 }, tile.getMotionListeners() );
		
		// attempting to remove a listener that isn't in the tile causes no issues
		assertNull( tile.removeMotionListener( testL ));
		assertSame( testL2, tile.removeMotionListener( testL2 ));
		assertFalse( tile.hasMotionListener( testL2 ));
		assertArrayEquals( new MotionListener[0], tile.getMotionListeners() );	
	}
}
