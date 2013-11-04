package fi.grimripper.loww.movement;

import static fi.grimripper.loww.Direction.EAST;
import static fi.grimripper.loww.Direction.NORTH;
import static fi.grimripper.loww.Direction.NORTHEAST;
import static fi.grimripper.loww.Direction.NORTHWEST;
import static fi.grimripper.loww.Direction.SOUTH;
import static fi.grimripper.loww.Direction.SOUTHEAST;
import static fi.grimripper.loww.Direction.SOUTHWEST;
import static fi.grimripper.loww.Direction.WEST;
import static fi.grimripper.loww.Height.BLOCKING;
import static fi.grimripper.loww.Height.FLAT;
import static fi.grimripper.loww.Height.HIGH;
import static fi.grimripper.loww.Height.LOW;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import fi.grimripper.loww.Direction;
import fi.grimripper.loww.Height;
import fi.grimripper.loww.Point;
import fi.grimripper.loww.movement.DefaultMovement;
import fi.grimripper.loww.movement.MobileObject;
import fi.grimripper.loww.movement.MovementMode;
import fi.grimripper.loww.movement.DefaultMovement.PathData;
import fi.grimripper.loww.templates.FourSquareTemplate;
import fi.grimripper.loww.templates.HexAndNeighborsTemplate;
import fi.grimripper.loww.templates.HorizontalTwoTileTemplate;
import fi.grimripper.loww.templates.MovementTemplate;
import fi.grimripper.loww.templates.SingleTileTemplate;
import fi.grimripper.loww.templates.SquareAndNeighborsTemplate;
import fi.grimripper.loww.templates.Templates;
import fi.grimripper.loww.test.TestBlock;
import fi.grimripper.loww.test.TestMobileObject;
import fi.grimripper.loww.test.TestMovementEvent;
import fi.grimripper.loww.test.TestMovementModifier;
import fi.grimripper.loww.test.TestMovementTemplate;
import fi.grimripper.loww.test.TestObstacle;
import fi.grimripper.loww.tiles.Block;
import fi.grimripper.loww.tiles.FilledRowHexGrid;
import fi.grimripper.loww.tiles.FilledSquareGrid;
import fi.grimripper.loww.tiles.Hex;
import fi.grimripper.loww.tiles.MovementEvent;
import fi.grimripper.loww.tiles.Square;
import fi.grimripper.loww.tiles.Terrain;
import fi.grimripper.loww.tiles.Tile;
import fi.grimripper.loww.tiles.TileGrid;
import fi.grimripper.loww.tiles.TileGrid.LineHelper;

public class DefaultMovementTest {

	@BeforeClass
	public static void initializePoints() {
		try {
			Class.forName( "fi.grimripper.loww.AwtPoint" );
		} catch (ClassNotFoundException cnfx) {
			cnfx.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void testPathData() {
		FilledSquareGrid sgrid = new FilledSquareGrid( 0, 1, 2 );
		FilledRowHexGrid hgrid = FilledRowHexGrid.createWithHexSize( 0, 0, 1, 2, 0 );
		testPathData( sgrid.getTileAtRC( 0, 0 ), sgrid.getTileAtRC( 0, 1 ));
		testPathData( hgrid.getTileAtRC( 0, 0 ), hgrid.getTileAtRC( 0, 1 ));
		testPathData( sgrid.getTileAtRC( 0, 0 ), hgrid.getTileAtRC( 0, 0 ));
		
		DefaultMovement move = new DefaultMovement( null, 0 );
		testPathDataStorage( move, sgrid.getTileAtRC( 0, 0 ), sgrid.getTileAtRC( 0, 1 ));
		testPathDataStorage( move, hgrid.getTileAtRC( 0, 0 ), hgrid.getTileAtRC( 0, 1 ));
		testPathDataStorage( move, sgrid.getTileAtRC( 0, 0 ), hgrid.getTileAtRC( 0, 0 ));
	}
	
	@Test (expected = NullPointerException.class)
	public void testPathDataNullPointerException1() {
		new DefaultMovement( null, 0 ).addPathData( null );
	}
	
	@Test (expected = NullPointerException.class)
	public void testPathDataNullPointerException2() {
		new DefaultMovement( null, 0 ).removePathData( null );
	}

	@Test (expected = NullPointerException.class)
	public void testPathDataNullPointerException3() {
		new DefaultMovement( null, 0 ).hasPathData( null );
	}
	
	@Test
	public void testHeuristics() {
		TestMovementTemplate template = new TestMovementTemplate();
		MobileObject mob = new TestMobileObject( LOW, template );
		DefaultMovement move = new DefaultMovement( mob, 0 );
		
		PathData cost0risk0 = new PathData( null, null, 0, null, null, null, EAST, 0 );
		PathData cost0risk1 = new PathData( null, null, 0, null, null, null, EAST, 1 );
		PathData cost1risk0 = new PathData( null, null, 1, null, null, null, EAST, 0 );
		PathData cost1risk1 = new PathData( null, null, 1, null, null, null, EAST, 1 );
		
		// same cost and higher risk -> unnecessary
		move.addPathData( cost1risk0 );
		assertFalse( move.shouldKeepPath( cost1risk1, cost1risk0 ));
		assertTrue( move.hasPathData( cost1risk0 ));
		
		// same risk and lower cost -> replace old data
		assertTrue( move.shouldKeepPath( cost0risk0, cost1risk0 ));
		assertFalse( move.hasPathData( cost1risk0 ));
		
		// same risk and higher cost -> unnecessary
		move.addPathData( cost0risk1 );
		assertFalse( move.shouldKeepPath( cost1risk1, cost0risk1 ));
		assertTrue( move.hasPathData( cost0risk1 ));
		
		// same cost and lower risk -> replace old data
		assertTrue( move.shouldKeepPath( cost0risk0, cost0risk1 ));
		assertFalse( move.hasPathData( cost0risk1 ));
		
		// either risk or cost is higher and the other lower -> keep both
		assertTrue( move.shouldKeepPath( cost0risk1, cost1risk0 ));
		assertTrue( move.shouldKeepPath( cost1risk0, cost0risk1 ));
		
		// higher risk or cost and the other is same -> unnecessary
		move.addPathData( cost0risk0 );
		assertFalse( move.shouldKeepPath( cost1risk1, cost0risk0 ));
		assertFalse( move.shouldKeepPath( cost1risk0, cost0risk0 ));
		assertFalse( move.shouldKeepPath( cost0risk1, cost0risk0 ));
		assertFalse( move.shouldKeepPath( cost0risk0, cost0risk0 ));
		assertTrue( move.hasPathData( cost0risk0 ));
		
		// no path data saved, test method that compares to existing data
		move.removePathData( cost0risk0 );
		assertTrue( move.shouldKeepPath( cost0risk0 ));
		assertTrue( move.shouldKeepPath( cost0risk1 ));
		assertTrue( move.shouldKeepPath( cost1risk0 ));
		assertTrue( move.shouldKeepPath( cost1risk1 ));
		
		// test removing existing data for a better one (lower risk, not added)
		move.addPathData( cost1risk1 );
		assertTrue( move.shouldKeepPath( cost1risk0 ));
		assertFalse( move.hasPathData( cost1risk1 ));
		assertFalse( move.hasPathData( cost1risk0 ));

		// test removing existing data for a better one (lower cost, not added)
		move.addPathData( cost1risk1 );
		assertTrue( move.shouldKeepPath( cost0risk1 ));
		assertFalse( move.hasPathData( cost1risk1 ));
		assertFalse( move.hasPathData( cost0risk1 ));
		
		// replace multiple existing data with a better one
		move.addPathData( cost1risk1 );
		move.addPathData( cost0risk1 );
		move.addPathData( cost1risk0 );
		assertTrue( move.shouldKeepPath( cost0risk0 ));
		assertFalse( move.hasPathData( cost1risk1 ));
		assertFalse( move.hasPathData( cost0risk1 ));
		assertFalse( move.hasPathData( cost1risk0 ));
		assertFalse( move.hasPathData( cost0risk0 ));
		
		// remove one data, but a better one already exists (lower risk)
		move.addPathData( cost1risk1 );
		move.addPathData( cost0risk0 );
		assertFalse( move.shouldKeepPath( cost1risk0 ));
		assertFalse( move.hasPathData( cost1risk1 ));
		assertFalse( move.hasPathData( cost1risk0 ));
		assertTrue( move.hasPathData( cost0risk0 ));

		// remove one data, but a better one already exists (lower cost)
		move.addPathData( cost1risk1 );
		assertFalse( move.shouldKeepPath( cost0risk1 ));
		assertFalse( move.hasPathData( cost1risk1 ));
		assertFalse( move.hasPathData( cost0risk1 ));
		assertTrue( move.hasPathData( cost0risk0 ));
		
		move.removePathData( cost0risk0 );
		template.setHorizontallySymmetric( false );
		
		// test with no path data, using asymmetric template
		assertTrue( move.shouldKeepPath( cost0risk0 ));
		assertTrue( move.shouldKeepPath( cost0risk1 ));
		assertTrue( move.shouldKeepPath( cost1risk0 ));
		assertTrue( move.shouldKeepPath( cost1risk1 ));
		
		// test removing for lower risk
		move.addPathData( cost1risk1 );
		assertTrue( move.shouldKeepPath( cost1risk0 ));
		assertFalse( move.hasPathData( cost1risk1 ));
		assertFalse( move.hasPathData( cost1risk0 ));
		
		// test removing for lower cost
		move.addPathData( cost1risk1 );
		assertTrue( move.shouldKeepPath( cost0risk1 ));
		assertFalse( move.hasPathData( cost1risk1 ));
		assertFalse( move.hasPathData( cost0risk1 ));
		
		// test removing multiple path data
		move.addPathData( cost1risk1 );
		move.addPathData( cost0risk1 );
		move.addPathData( cost1risk0 );
		assertTrue( move.shouldKeepPath( cost0risk0 ));
		assertFalse( move.hasPathData( cost1risk1 ));
		assertFalse( move.hasPathData( cost0risk1 ));
		assertFalse( move.hasPathData( cost1risk0 ));
		assertFalse( move.hasPathData( cost0risk0 ));
		
		// test removing one for lower risk, while a better one exists
		move.addPathData( cost1risk1 );
		move.addPathData( cost0risk0 );
		assertFalse( move.shouldKeepPath( cost1risk0 ));
		assertFalse( move.hasPathData( cost1risk1 ));
		assertFalse( move.hasPathData( cost1risk0 ));
		assertTrue( move.hasPathData( cost0risk0 ));
		
		// test removing one for lower cost, while a better one exists
		move.addPathData( cost1risk1 );
		assertFalse( move.shouldKeepPath( cost0risk1 ));
		assertFalse( move.hasPathData( cost1risk1 ));
		assertFalse( move.hasPathData( cost0risk1 ));
		assertTrue( move.hasPathData( cost0risk0 ));
		
		move.removePathData( cost0risk0 );
		
		cost0risk0 = new PathData( null, null, 0, null, null, null, SOUTH, 0 );
		cost0risk1 = new PathData( null, null, 0, null, null, null, NORTH, 1 );
		cost1risk0 = new PathData( null, null, 1, null, null, null, NORTH, 0 );
		cost1risk1 = new PathData( null, null, 1, null, null, null, NORTH, 1 );
		
		// test again with vertical facing directions
		assertTrue( move.shouldKeepPath( cost0risk0 ));
		assertTrue( move.shouldKeepPath( cost0risk1 ));
		assertTrue( move.shouldKeepPath( cost1risk0 ));
		assertTrue( move.shouldKeepPath( cost1risk1 ));
		
		// test removing for lower risk
		move.addPathData( cost1risk1 );
		assertTrue( move.shouldKeepPath( cost1risk0 ));
		assertFalse( move.hasPathData( cost1risk1 ));
		assertFalse( move.hasPathData( cost1risk0 ));
		
		// test removing for lower cost
		move.addPathData( cost1risk1 );
		assertTrue( move.shouldKeepPath( cost0risk1 ));
		assertFalse( move.hasPathData( cost1risk1 ));
		assertFalse( move.hasPathData( cost0risk1 ));
		
		// test removing multiple path data
		move.addPathData( cost1risk1 );
		move.addPathData( cost0risk1 );
		move.addPathData( cost1risk0 );
		assertTrue( move.shouldKeepPath( cost0risk0 ));
		assertFalse( move.hasPathData( cost1risk1 ));
		assertFalse( move.hasPathData( cost0risk1 ));
		assertFalse( move.hasPathData( cost1risk0 ));
		assertFalse( move.hasPathData( cost0risk0 ));
		
		// test removing one for lower risk, while a better one exists
		move.addPathData( cost1risk1 );
		move.addPathData( cost0risk0 );
		assertFalse( move.shouldKeepPath( cost1risk0 ));
		assertFalse( move.hasPathData( cost1risk1 ));
		assertFalse( move.hasPathData( cost1risk0 ));
		assertTrue( move.hasPathData( cost0risk0 ));
		
		// test removing one for lower cost, while a better one exists
		move.addPathData( cost1risk1 );
		assertFalse( move.shouldKeepPath( cost0risk1 ));
		assertFalse( move.hasPathData( cost1risk1 ));
		assertFalse( move.hasPathData( cost0risk1 ));
		assertTrue( move.hasPathData( cost0risk0 ));
		
		move.removePathData( cost0risk0 );

		// test with different horizontal facing directions
		cost0risk0 = new PathData( null, null, 0, null, null, null, WEST, 0 );
		cost0risk1 = new PathData( null, null, 0, null, null, null, EAST, 1 );
		cost1risk0 = new PathData( null, null, 1, null, null, null, EAST, 0 );
		cost1risk1 = new PathData( null, null, 1, null, null, null, WEST, 1 );
		
		// test without any existing path data
		assertTrue( move.shouldKeepPath( cost0risk0 ));
		assertTrue( move.shouldKeepPath( cost0risk1 ));
		assertTrue( move.shouldKeepPath( cost1risk0 ));
		assertTrue( move.shouldKeepPath( cost1risk1 ));
		
		// not removed because has different facing from the better path data
		move.addPathData( cost1risk1 );
		assertTrue( move.shouldKeepPath( cost1risk0 ));
		assertTrue( move.hasPathData( cost1risk1 ));
		assertFalse( move.hasPathData( cost1risk0 ));
		
		assertTrue( move.shouldKeepPath( cost0risk1 ));
		assertTrue( move.hasPathData( cost1risk1 ));
		assertFalse( move.hasPathData( cost0risk1 ));
		
		// better path data removes one with same facing, but not ones with opposite
		move.addPathData( cost0risk1 );
		move.addPathData( cost1risk0 );
		assertTrue( move.shouldKeepPath( cost0risk0 ));
		assertFalse( move.hasPathData( cost1risk1 ));
		assertTrue( move.hasPathData( cost0risk1 ));
		assertTrue( move.hasPathData( cost1risk0 ));
		assertFalse( move.hasPathData( cost0risk0 ));
		
		// path data with opposite facings don't affect each other
		move.addPathData( cost1risk1 );
		move.addPathData( cost0risk0 );
		move.removePathData( cost0risk1 );
		move.removePathData( cost1risk0 );
		assertTrue( move.shouldKeepPath( cost1risk0 ));
		assertTrue( move.hasPathData( cost1risk1 ));
		assertFalse( move.hasPathData( cost1risk0 ));
		assertTrue( move.hasPathData( cost0risk0 ));
		
		assertTrue( move.shouldKeepPath( cost0risk1 ));
		assertTrue( move.hasPathData( cost1risk1 ));
		assertFalse( move.hasPathData( cost0risk1 ));
		assertTrue( move.hasPathData( cost0risk0 ));
		
		move.removePathData( cost1risk1 );
		move.removePathData( cost0risk0 );
		
		// data exists, no need to keep similar data
		move.addPathData( cost0risk1 );
		assertFalse( move.shouldKeepPath( cost0risk1 ));
		move.removePathData( cost0risk1 );
		
		move.addPathData( cost1risk0 );
		assertFalse( move.shouldKeepPath( cost1risk0 ));
		move.removePathData( cost1risk0 );
		
		// no path data for null, but it's a valid parameter
		assertArrayEquals( new PathData[0], move.getPathData( null ));
		assertNull( move.selectBestPath( new PathData[0], null ));
		
		template.setHorizontallySymmetric( true );
		
		// with symmetric template, select path with lowest cost and risk regardless of facing
		PathData[] pathData = { cost0risk0, cost0risk1, cost1risk0, cost1risk1 };
		for (Direction d : Direction.values())
			assertSame( cost0risk0, move.selectBestPath( pathData, d ));
		
		// select path with higher cost and lower risk
		pathData = new PathData[] { cost0risk1, cost1risk0, cost1risk1 };
		for (Direction d : Direction.values())
			assertSame( cost1risk0, move.selectBestPath( pathData, d ));
		
		// select path with lower cost and equal risk
		pathData = new PathData[] { cost1risk1, cost0risk1 };
		for (Direction d : Direction.values())
			assertSame( cost0risk1, move.selectBestPath( pathData, d ));
		
		// select only available option
		pathData = new PathData[] { cost1risk1 };
		for (Direction d : Direction.values())
			assertSame( cost1risk1, move.selectBestPath( pathData, d ));
		
		template.setHorizontallySymmetric( false );
		
		// with asymmetric template, only path data with similar facing can be selected
		pathData = new PathData[] { cost0risk0, cost0risk1, cost1risk0, cost1risk1 };
		for (Direction d : Direction.values())
			assertSame( d.isDueEast() ? cost1risk0 : cost0risk0,
				move.selectBestPath( pathData, d ));
		
		pathData = new PathData[] { cost1risk1, cost1risk0, cost0risk1 };
		for (Direction d : Direction.values())
			assertSame( !d.isDueEast() ? cost1risk1 : cost1risk0,
				move.selectBestPath( pathData, d ));
	}

	@Test (expected = NullPointerException.class)
	public void testHostNullPointerException1() {
		new DefaultMovement( null, 0 ).shouldKeepPath( null );
	}

	@Test (expected = NullPointerException.class)
	public void testTemplateNullPointerException1() {
		new DefaultMovement( new TestMobileObject( null, null ), 0 ).shouldKeepPath( null );
	}

	@Test (expected = NullPointerException.class)
	public void testHeuristicsNullPointerException1() {
		new DefaultMovement( new TestMobileObject(
				null, new TestMovementTemplate() ), 0 ).shouldKeepPath( null );
	}
	
	@Test (expected = NullPointerException.class)
	public void testHeuristicsNullPointerException2() {
		new DefaultMovement( null, 0 ).selectBestPath( null, null );
	}

	@Test (expected = NullPointerException.class)
	public void testHostNullPointerException2() {
		new DefaultMovement( null, 0 ).selectBestPath( new PathData[] { null }, null );
	}

	@Test (expected = NullPointerException.class)
	public void testTemplateNullPointerException2() {
		new DefaultMovement( new TestMobileObject(
				null, null ), 0 ).selectBestPath( new PathData[] { null }, null );
	}

	@Test (expected = NullPointerException.class)
	public void testHeuristicsNullPointerException3() {
		new DefaultMovement( new TestMobileObject( null, new TestMovementTemplate(
				false, false )), 0 ).selectBestPath( new PathData[] { null }, null );
	}

	@Test (expected = NullPointerException.class)
	public void testHeuristicsNullPointerException4() {
		new DefaultMovement( new TestMobileObject( null,
				new TestMovementTemplate( false, false )), 0 ).selectBestPath( new PathData[] {
					new PathData( null, null, 0, null, null, null, null, 0 )}, null );
	}

	@Test (expected = NullPointerException.class)
	public void testHeuristicsNullPointerException5() {
		new DefaultMovement( new TestMobileObject( null,
				new TestMovementTemplate( false, false )), 0 ).selectBestPath( new PathData[] {
					new PathData( null, null, 0, null, null, null, NORTH, 0 )}, null );
	}
	
	@Test (expected = NullPointerException.class)
	public void testTerrainHeightNullPointerException1() {
		new DefaultMovement( null, 0 ).getTerrainHeight( null, (Tile[])null );
	}

	@Test (expected = NullPointerException.class)
	public void testTerrainHeightNullPointerException2() {
		DefaultMovement move = new DefaultMovement( null, 0 );
		move.addMovementModifier( null );
		move.getTerrainHeight( null, (Tile[])null );
	}

	@Test (expected = NullPointerException.class)
	public void testTerrainHeightNullPointerException3() {
		new DefaultMovement( null, 0 ).getTerrainHeight(
				null, new FilledSquareGrid( 0, 1, 1 ).getTileAtRC( 0, 0 ));
	}

	@Test
	public void testProgressTiles() {
		try {
			testDetermineSuccessors( new FilledSquareGrid( 0, 7, 7 ));
			testDetermineSuccessors( FilledRowHexGrid.createWithHexSize( 0, 0, 7, 7, 7 ));
			
			testAddProgressTiles( new FilledSquareGrid( 1, 7, 7 ));
			testAddProgressTiles( FilledRowHexGrid.createWithHexSize( 1, 1, 7, 7, 7 ));
			
		} catch (NoSuchFieldException | IllegalAccessException x) {
			x.printStackTrace();
			fail();
		}
	}
	
	@Test (expected = NullPointerException.class)
	public void testDetermineSuccessorsNullPointerException1() {
		new DefaultMovement( null, 0 ).determineSuccessors( null );
	}

	@Test (expected = NullPointerException.class)
	public void testDetermineSuccessorsNullPointerException2() {
		new DefaultMovement( new TestMobileObject( LOW, null ), 0 ).determineSuccessors( null );
	}

	@Test (expected = NullPointerException.class)
	public void testDetermineSuccessorsNullPointerException3() {
		new DefaultMovement( new TestMobileObject( LOW, new TestMovementTemplate( true, true )),
				0 ).determineSuccessors( null );
	}

	@Test (expected = NullPointerException.class)
	public void testDetermineSuccessorsNullPointerException4() {
		new DefaultMovement( new TestMobileObject( LOW, new TestMovementTemplate( true, true )),
				0 ).determineSuccessors( new PathData( null, null, 0, null, null, null, null, 0 ));
	}

	@Test (expected = NullPointerException.class)
	public void testDetermineSuccessorsNullPointerException5() {
		new DefaultMovement( new TestMobileObject( LOW, new TestMovementTemplate( false, false )),
				0 ).determineSuccessors( null );
	}

	@Test (expected = NullPointerException.class)
	public void testDetermineSuccessorsNullPointerException6() {
		new DefaultMovement( new TestMobileObject( LOW, new TestMovementTemplate( false, false )),
				0 ).determineSuccessors( new PathData( null, null, 0, null, null, null, null, 0 ));
	}

	@Test (expected = NullPointerException.class)
	public void testDetermineSuccessorsNullPointerException7() {
		new DefaultMovement( new TestMobileObject( LOW, new TestMovementTemplate( true, true )),
				0 ).determineSuccessors( new PathData( null, null, 0, null, null, null, WEST, 0 ));
	}

	@Test
	public void testMovementRadius() {
		testMovementRadius( new FilledSquareGrid( 1, 7, 7 ));
		testMovementRadius( FilledRowHexGrid.createWithHexSize( 1, 1, 7, 7, 7 ));
		
		testSizeTwoSquareTemplate( new FilledSquareGrid( 1, 7, 7 ));
		testSizeTwoHexTemplate( FilledRowHexGrid.createWithHexSize( 1, 1, 7, 7, 7 ));
		testSizeFourSquareTemplate( new FilledSquareGrid( 1, 7, 7 ));
		testSizeSevenHexTemplate( FilledRowHexGrid.createWithHexSize( 1, 1, 7, 7, 7 ));
		testSizeNineSquareTemplate( new FilledSquareGrid( 1, 7, 7 ));
		
		MobileObject mob = new TestMobileObject( LOW, new TestMovementTemplate( true, false ));
		MovementMode mode = new DefaultMovement( mob, 100 );
		
		TileGrid <Square> squareGrid = new FilledSquareGrid( 1, 7, 7 );
		Terrain flatTerrain = new Terrain( 1f, FLAT );
		
		for (Square[] row : squareGrid.getTiles())
			for (Square s : row)
				s.setTerrain( flatTerrain );
		
		mob.setPosition( NORTH, squareGrid.getTileAtRC( 3, 3 ));
		mode.movementRadius( 5 );
	}

	@Test
	public void testMovementPath() {
		testMovementPath( new FilledSquareGrid( 1, 7, 7 ));
		testMovementPath( FilledRowHexGrid.createWithHexSize( 1, 1, 7, 7, 7 ));
	}
	
	@Test (expected = Exception.class)
	public void testIllegalInterruption() {
		TileGrid <?> grid = new FilledSquareGrid( 1, 2, 2 );
		Terrain flatTerrain = new Terrain( 1f, FLAT );
		Tile[][] tiles = grid.getTiles();
		
		for (Tile[] row : tiles)
			for (Tile t : row)
				t.setTerrain( flatTerrain );
		
		Tile starting = grid.getTileAtRC( 0, 0 );
		Tile destination = grid.getTileAtRC( 1, 1 );
		
		TestMovementEvent noEntry = new TestMovementEvent( 0f, FLAT, true, false );
		noEntry.setShowMovementNotAllowed( false );
		destination.addMovementEvent( noEntry );

		MobileObject mob = new TestMobileObject( LOW, new SingleTileTemplate() );
		DefaultMovement move = new DefaultMovement( mob, 100 );
		mob.setPosition( EAST, starting );
		move.movementRadius( 10 );
		
		// there's always a tile to occupy after interrupt when everything works correctly
		// create a situation where there's no tile to occupy
		try {
			Field occupyHeightsField = MovementMode.class.getDeclaredField( "occupyHeights" );
			occupyHeightsField.setAccessible( true );
			
			Map <?, ?> occupyHeights = (Map <?, ?>)occupyHeightsField.get( move );
			occupyHeights.remove( starting );
		
		} catch (NoSuchFieldException | IllegalAccessException x) {
			x.printStackTrace();
		}
		
		// will now fail
		move.executeMovementPath( destination, EAST );
	}
	
	private <T extends Tile> void testPathData( T first, T second ) {
		float totalCost = 3f;
		Height minHeight = HIGH;
		Height moveHeight = FLAT;
		Direction facing = WEST;
		Direction templateFacing = WEST;
		float risk = 1f;
		
		// basic path data properties set in constructor
		PathData firstData = new PathData(
				first, null, totalCost, minHeight, moveHeight, facing, templateFacing, risk );
		assertSame( first, firstData.getTile() );
		assertNull( firstData.getPath() );

		assertEquals( totalCost, firstData.getTotalCost(), 0.001 );
		firstData.setTotalCost( totalCost = 5f );
		assertEquals( totalCost, firstData.getTotalCost(), 0.001 );
		
		assertSame( moveHeight, firstData.getMoveHeight() );
		firstData.setMoveHeight( moveHeight = LOW );
		assertSame( moveHeight, firstData.getMoveHeight() );
		
		assertSame( minHeight, firstData.getMinHeight() );
		firstData.setMinHeight( minHeight = FLAT );
		assertSame( minHeight, firstData.getMinHeight() );
		
		assertSame( facing, firstData.getFacing() );
		firstData.setFacing( facing = NORTH );
		assertSame( facing, firstData.getFacing() );
		
		assertSame( templateFacing, firstData.getTemplateFacing() );
		firstData.setTemplateFacing( templateFacing = SOUTH );
		assertSame( templateFacing, firstData.getTemplateFacing() );
		
		assertEquals( risk, firstData.getRisk(), 0.001 );
		firstData.setRisk( risk = 2f );
		assertEquals( risk, firstData.getRisk(), 0.001 );
		
		float secondTotal = 7f;
		Height secondMin = LOW;
		Height secondHeight = HIGH;
		Direction secondFacing = SOUTH;
		Direction secondTemplateFacing = SOUTH;
		float secondRisk = 4f;
		
		// basic data with a path
		PathData secondData = new PathData( second, firstData, secondTotal,
				secondMin, secondHeight, secondFacing, secondTemplateFacing, secondRisk );
		assertSame( second, secondData.getTile() );
		
		assertSame( firstData, secondData.getPath() );
		firstData.setPath( secondData );
		assertSame( secondData, firstData.getPath() );
		
		assertEquals( secondTotal, secondData.getTotalCost(), 0.001 );
		assertSame( secondFacing, secondData.getFacing() );
		assertSame( secondTemplateFacing, secondData.getTemplateFacing() );
		assertEquals( secondRisk, secondData.getRisk(), 0.001 );
	}
	
	private <T extends Tile> void testPathDataStorage( DefaultMovement move, T first, T second ) {
		
		// no path data stored, null is a valid parameter
		assertArrayEquals( new PathData[0], move.getPathData( null ));
		assertArrayEquals( new PathData[0], move.getPathData( first ));
		assertArrayEquals( new PathData[0], move.getPathData( second ));
		
		PathData firstData = new PathData( first, null, 0, null, null, null, null, 0 );
		PathData secondData = new PathData( second, null, 0, null, null, null, null, 0 );
		
		// initially has no path data, and removing does nothing
		assertFalse( move.hasPathData( firstData ));
		assertFalse( move.hasPathData( secondData ));
		
		move.removePathData( firstData );
		move.removePathData( secondData );
		
		// test adding path data
		move.addPathData( firstData );
		assertArrayEquals( new PathData[] { firstData }, move.getPathData( first ));
		assertTrue( move.hasPathData( firstData ));
		assertFalse( move.hasPathData( secondData ));
		
		// test adding another path data for a different tile
		move.addPathData( secondData );
		assertArrayEquals( new PathData[] { firstData }, move.getPathData( first ));
		assertArrayEquals( new PathData[] { secondData }, move.getPathData( second ));
		assertTrue( move.hasPathData( firstData ));
		assertTrue( move.hasPathData( secondData ));
		
		// same path data can be added multiple times
		move.addPathData( firstData );
		assertArrayEquals( new PathData[] { firstData, firstData }, move.getPathData( first ));
		
		// test removing path data
		move.removePathData( firstData );
		assertArrayEquals( new PathData[] { firstData }, move.getPathData( first ));
		assertTrue( move.hasPathData( firstData ));
		
		// added twice, must be removed twice
		move.removePathData( firstData );
		assertArrayEquals( new PathData[0], move.getPathData( first ));
		assertFalse( move.hasPathData( firstData ));
		
		// multiple path data objects for one tile
		PathData thirdData = new PathData( second, null, 0, null, null, null, null, 0 );
		move.addPathData( thirdData );
		assertArrayEquals( new PathData[] { secondData, thirdData }, move.getPathData( second ));
		
		// remove when there's another path data object for the tile
		move.removePathData( secondData );
		assertArrayEquals( new PathData[] { thirdData }, move.getPathData( second ));
		assertFalse( move.hasPathData( secondData ));
		assertTrue( move.hasPathData( thirdData ));
		
		// add the removed path data back, it becomes the last data object
		move.addPathData( secondData );
		assertArrayEquals( new PathData[] { thirdData, secondData }, move.getPathData( second ));
		assertTrue( move.hasPathData( secondData ));
		assertTrue( move.hasPathData( thirdData ));
		
		// null tile in path data doesn't cause problems
		PathData nullData = new PathData( null, null, 0, null, null, null, null, 0 );
		assertFalse( move.hasPathData( nullData ));
		assertArrayEquals( new PathData[0], move.getPathData( null ));
		move.removePathData( nullData );
		
		// path data can be added for a null tile
		move.addPathData( nullData );
		assertTrue( move.hasPathData( nullData ));
		assertArrayEquals( new PathData[] { nullData }, move.getPathData( null ));
		
		// test removing path data for a null tile
		move.removePathData( nullData );
		assertFalse( move.hasPathData( nullData ));
		assertArrayEquals( new PathData[0], move.getPathData( null ));
	}
	
	private void testDetermineSuccessors( TileGrid <?> grid )
			throws NoSuchFieldException, IllegalAccessException {
		TestMovementTemplate template = new TestMovementTemplate( true, true );
		DefaultMovement move = new DefaultMovement( new TestMobileObject( LOW, template ), 0 );
		Tile center = grid.getTileAtRC( 2, 2 );
		
		Terrain testTerrain = new Terrain( 0f, FLAT );
		for (Tile[] row : grid.getTiles())
			for (Tile t : row)
				t.setTerrain( testTerrain );
		
		// for a symmetric template, expect all neighbors to be accessible
		LinkedList <PathData> progressTiles = getPathDataList( move, "progressTiles" );
		Tile[] expectedSuccessors = center.getAccessibleNeighbors( null );
		ArrayList <Tile> expectedList = new ArrayList <Tile>();
		for (Tile t : expectedSuccessors)
			expectedList.add( t );
		
		// compare in order-independent manner
		Tile[] actualSuccessors = move.determineSuccessors(
				new PathData( center, null, 0, null, null, null, null, 0 ));
		for (Tile t : actualSuccessors)
			assertTrue( expectedList.contains( t ));
		assertEquals( expectedSuccessors.length, actualSuccessors.length );
		
		// determine successors doesn't set occupy data or add progress tiles
		assertFalse( move.canBeOccupied( center, WEST ));
		assertNull( move.getOccupyHeight( center, WEST ));
		assertTrue( progressTiles.isEmpty() );
		
		template.setHorizontallySymmetric( false );
		PathData path = new PathData( center, null, 0, null, null, EAST, EAST, 0 );
		
		// for asymmetric template, neighbors to one horizontal direction are not available
		expectedSuccessors = center.getAccessibleNeighbors(
				Templates.getMoveDirections( EAST, template ));
		actualSuccessors = move.determineSuccessors( path );
		for (Tile t : actualSuccessors)
			assertTrue( expectedList.contains( t ));
		assertEquals( expectedSuccessors.length, actualSuccessors.length );
		
		// for asymmetric template, occupy data and progress tile for turning in place are set
		assertTrue( move.canBeOccupied( center, WEST ));
		assertSame( move.calculateOccupyHeight( center, WEST ),
				move.getOccupyHeight( center, WEST ));
		assertEquals( 1, progressTiles.size() );
		PathData pathMirror = progressTiles.getFirst();
		assertTrue( move.hasPathData( pathMirror ));
		move.removePathData( pathMirror );
		
		// removing path data doesn't remove it from progress tiles
		PathData higherCost = new PathData( center, null, 1, null, null, EAST, EAST, 0 );
		assertEquals( 1, progressTiles.size() );
		
		// determine successors with a higher total cost
		actualSuccessors = move.determineSuccessors( higherCost );
		for (Tile t : actualSuccessors)
			assertTrue( expectedList.contains( t ));
		assertEquals( expectedSuccessors.length, actualSuccessors.length );
		
		// turn in place tile was added again, but nothing else, because of higher total cost
		assertEquals( 2, progressTiles.size() );
		PathData higherCostMirror = progressTiles.getLast();
		assertTrue( move.hasPathData( higherCostMirror ));

		// determine successors with lower cost again
		actualSuccessors = move.determineSuccessors( path );
		for (Tile t : actualSuccessors)
			assertTrue( expectedList.contains( t ));
		assertEquals( expectedSuccessors.length, actualSuccessors.length );
		
		// turn in place path data with higher total cost was removed
		assertEquals( 3, progressTiles.size() );
		assertFalse( move.hasPathData( higherCostMirror ));
		assertTrue( move.hasPathData( progressTiles.getLast() ));
		
		// retry higher cost, turn in place tile isn't added because there's one with lower cost
		actualSuccessors = move.determineSuccessors( higherCost );
		for (Tile t : actualSuccessors)
			assertTrue( expectedList.contains( t ));
		assertEquals( expectedSuccessors.length, actualSuccessors.length );
		assertEquals( 3, progressTiles.size() );
		
		LinkedList <PathData> riskPaths = getPathDataList( move, "riskPaths" );
		PathData higherRisk = new PathData( center, null, 0, null, null, EAST, EAST, 1 );
		move.removePathData( progressTiles.getLast() );		// would prevent new path with risk
		
		// determine successors with same cost and higher risk
		actualSuccessors = move.determineSuccessors( higherRisk );
		for (Tile t : actualSuccessors)
			assertTrue( expectedList.contains( t ));
		assertEquals( expectedSuccessors.length, actualSuccessors.length );
		
		// the successor for the path with risk is added to a different progress list
		assertEquals( 3, progressTiles.size() );
		assertEquals( 1, riskPaths.size() );
		PathData higherRiskMirror = riskPaths.getLast();
		assertTrue( move.hasPathData( higherRiskMirror ));
		
		// determine successors for the path without risk again
		actualSuccessors = move.determineSuccessors( path );
		for (Tile t : actualSuccessors)
			assertTrue( expectedList.contains( t ));
		assertEquals( expectedSuccessors.length, actualSuccessors.length );
		assertEquals( 4, progressTiles.size() );
		assertEquals( 1, riskPaths.size() );
		
		// the successor with same cost and higher risk was removed
		assertTrue( move.hasPathData( progressTiles.getLast() ));
		assertFalse( move.hasPathData( higherRiskMirror ));
		
		// retry the risk path, no new data is added because the risk-free path is better
		actualSuccessors = move.determineSuccessors( higherRisk );
		for (Tile t : actualSuccessors)
			assertTrue( expectedList.contains( t ));
		assertEquals( expectedSuccessors.length, actualSuccessors.length );
		assertEquals( 4, progressTiles.size() );
		assertEquals( 1, riskPaths.size() );
		
		// remove the good path and try with one that has both higher cost and risk
		PathData worst = new PathData( center, null, 1, null, null, EAST, EAST, 1 );
		move.removePathData( progressTiles.getLast() );
		
		actualSuccessors = move.determineSuccessors( worst );
		for (Tile t : actualSuccessors)
			assertTrue( expectedList.contains( t ));
		assertEquals( expectedSuccessors.length, actualSuccessors.length );
		
		// successor for the costlier risk path was added to the risk path list
		assertEquals( 4, progressTiles.size() );
		assertEquals( 2, riskPaths.size() );
		PathData worstMirror = riskPaths.getLast();
		assertTrue( move.hasPathData( worstMirror ));
		
		// path with same risk and lower cost removes the previous risk path
		actualSuccessors = move.determineSuccessors( higherRisk );
		for (Tile t : actualSuccessors)
			assertTrue( expectedList.contains( t ));
		assertEquals( expectedSuccessors.length, actualSuccessors.length );
		assertEquals( 4, progressTiles.size() );
		assertEquals( 3, riskPaths.size() );
		assertTrue( move.hasPathData( riskPaths.getLast() ));
		assertFalse( move.hasPathData( worstMirror ));
		
		// now there's path data with lower cost, so the worse one's successor isn't added
		actualSuccessors = move.determineSuccessors( worst );
		for (Tile t : actualSuccessors)
			assertTrue( expectedList.contains( t ));
		assertEquals( expectedSuccessors.length, actualSuccessors.length );
		assertEquals( 4, progressTiles.size() );
		assertEquals( 3, riskPaths.size() );
	}
	
	private void testAddProgressTiles( TileGrid <?> grid )
			throws NoSuchFieldException, IllegalAccessException {
		
		Terrain flatTerrain = new Terrain( 1f, FLAT );
		for (Tile[] row : grid.getTiles())
			for (Tile t : row)
				t.setTerrain( flatTerrain );
		
		TestMovementTemplate template = new TestMovementTemplate( true, true );
		DefaultMovement move = new DefaultMovement( new TestMobileObject( LOW, template ), 0 );
		LinkedList <PathData> progressTiles = getPathDataList( move, "progressTiles" );
		LinkedList <PathData> riskPaths = getPathDataList( move, "riskPaths" );
		
		// nothing is added since total move hasn't been set
		Tile center = grid.getTileAtRC( 3, 3 );
		PathData path = new PathData( center, null, 0, null, null, EAST, EAST, 0 );
		move.addProgressTiles( path );
		assertTrue( progressTiles.isEmpty() );
		assertTrue( riskPaths.isEmpty() );
		assertArrayEquals( new MovementEvent[0], move.getBufferedEvents() );
		
		for (Tile t : center.getNeighbors()) {
			assertArrayEquals( new PathData[0], move.getPathData( t ));
			assertFalse( move.canBeOccupied( t, EAST ));
			assertFalse( move.canBeOccupied( t, WEST ));
			assertNull( move.getOccupyHeight( t, EAST ));
			assertNull( move.getOccupyHeight( t, WEST ));
		}
		
		// add progress tiles (all neighbors) with a total move of one
		move.beginSearch( path, 1 );
		assertEquals( center.getNeighbors().length, progressTiles.size() );
		assertArrayEquals( new MovementEvent[0], move.getBufferedEvents() );
		
		// path data is added for each neighbor, cost is equal to terrain, occupy data is set
		for (Tile t : center.getNeighbors()) {
			PathData[] paths = move.getPathData( t );
			Height minHeight = move.getMinimumHeight( false, t );
			
			assertEquals( 1, paths.length );
			assertTrue( progressTiles.contains( paths[0] ));
			
			assertSame( t, paths[0].getTile() );
			assertSame( path, paths[0].getPath() );
			assertEquals( flatTerrain.getCost(), paths[0].getTotalCost(), 0.001 );
			
			assertSame( minHeight, paths[0].getMinHeight() );
			assertSame( move.getTerrainHeight( minHeight, t ), paths[0].getMoveHeight() );
			assertSame( center.getDirection( t ), paths[0].getFacing() );
			assertEquals( 0, paths[0].getRisk(), 0.001 );
			
			assertTrue( move.canBeOccupied( paths[0].getTile(), paths[0].getTemplateFacing() ));
			assertSame( move.calculateOccupyHeight( t ),
					move.getOccupyHeight( paths[0].getTile(), paths[0].getTemplateFacing() ));
			
			assertTrue( move.hasPathData( paths[0] ));
		}
		
		// test that clearing the radius empties progress lists and removes path data
		move.clearRadius();
		assertTrue( progressTiles.isEmpty() );
		assertTrue( riskPaths.isEmpty() );
		
		for (Tile t : center.getNeighbors())
			assertArrayEquals( new PathData[0], move.getPathData( t ));
		
		// create a path data for a neighbor with zero total cost
		Tile west = center.getNeighbor( WEST );
		Height minHeight = move.getMinimumHeight( false, west );
		PathData freeMove = new PathData( west, path, 0,
				minHeight, move.getTerrainHeight( minHeight, west ), WEST, WEST, 0 );
		move.addPathData( freeMove );
		assertTrue( progressTiles.remove( freeMove ));
		
		// new path data isn't added for the neighbor because new data would have a higher cost
		move.beginSearch( path, 1 );
		assertEquals( center.getNeighbors().length - 1, progressTiles.size() );
		assertTrue( riskPaths.isEmpty() );
		assertArrayEquals( new MovementEvent[0], move.getBufferedEvents() );
		assertArrayEquals( new PathData[] { freeMove }, move.getPathData( west ));
		
		Field field = DefaultMovement.class.getDeclaredField( "pathData" );
		field.setAccessible( true );
		
		// repeat tests about data added for each neighbor
		for (Tile t : center.getNeighbors()) {
			PathData[] paths = move.getPathData( t );
			minHeight = move.getMinimumHeight( false, t );
			
			assertEquals( 1, paths.length );
			assertSame( t, paths[0].getTile() );
			assertSame( path, paths[0].getPath() );
			
			assertSame( minHeight, paths[0].getMinHeight() );
			assertSame( move.getTerrainHeight( minHeight, t ), paths[0].getMoveHeight() );
			
			assertSame( center.getDirection( t ), paths[0].getFacing() );
			assertEquals( 0, paths[0].getRisk(), 0.001 );
			assertTrue( move.hasPathData( paths[0] ));
			
			if (t == west)
				continue;
			
			// path data for west neighbor was added directly, and occupy data for it wasn't set
			assertEquals( flatTerrain.getCost(), paths[0].getTotalCost(), 0.001 );
			assertTrue( move.canBeOccupied( paths[0].getTile(), paths[0].getTemplateFacing() ));
			assertSame( move.calculateOccupyHeight( t ),
					move.getOccupyHeight( paths[0].getTile(), paths[0].getTemplateFacing() ));
		}
		
		move.clearRadius();
		Tile northwest = grid.getTileAtRC( 0, 0 );
		path = new PathData( northwest, null, 0, null, null, null, EAST, 0 );
		
		// start search from northwest corner, there are fewer neighbors because of grid edges
		move.beginSearch( path, 1 );
		assertEquals( northwest.getNeighbors().length, progressTiles.size() );
		assertArrayEquals( new MovementEvent[0], move.getBufferedEvents() );
		
		// test data added for neighbors
		for (Tile t : northwest.getNeighbors()) {
			PathData[] paths = move.getPathData( t );
			minHeight = move.getMinimumHeight( false, t );
			
			assertEquals( 1, paths.length );
			assertTrue( progressTiles.contains( paths[0] ));
			
			assertSame( t, paths[0].getTile() );
			assertSame( path, paths[0].getPath() );
			assertEquals( flatTerrain.getCost(), paths[0].getTotalCost(), 0.001 );
			
			assertSame( minHeight, paths[0].getMinHeight() );
			assertSame( move.getTerrainHeight( minHeight, t ), paths[0].getMoveHeight() );
			assertSame( northwest.getDirection( t ), paths[0].getFacing() );
			assertEquals( 0, paths[0].getRisk(), 0.001 );
			
			assertTrue( move.canBeOccupied( paths[0].getTile(), paths[0].getTemplateFacing() ));
			assertSame( move.calculateOccupyHeight( t ),
					move.getOccupyHeight( paths[0].getTile(), paths[0].getTemplateFacing() ));
			
			assertTrue( move.hasPathData( paths[0] ));
		}
		
		// add a blocking event to a neighbor so that it can't be entered
		path = new PathData( center, null, 0, null, null, EAST, EAST, 0 );
		TestMovementEvent blockingEvent = new TestMovementEvent( 1f, BLOCKING, true, false );
		west.addMovementEvent( blockingEvent );
		
		move.clearRadius();
		move.beginSearch( path, 1 );
		assertEquals( center.getNeighbors().length - 1, progressTiles.size() );
		assertArrayEquals( new PathData[0], move.getPathData( west ));
		
		// use a modifier to ignore the event, and the tile can be entered
		TestMovementModifier enablingMod = new TestMovementModifier();
		enablingMod.addEventProtection( blockingEvent, 1 );
		move.addMovementModifier( enablingMod );
		
		move.clearRadius();
		move.beginSearch( path, 1 );
		assertEquals( center.getNeighbors().length, progressTiles.size() );
		
		// change modifier to allow movement despite event, but to not protect against risk
		enablingMod.removeEventProtection( blockingEvent );
		enablingMod.addEventProtection( blockingEvent, 0 );
		
		// the tile with the event is added to risk paths
		move.clearRadius();
		move.beginSearch( path, 1 );
		assertEquals( center.getNeighbors().length - 1, progressTiles.size() );
		assertEquals( 1, riskPaths.size() );
		
		// check the data added for the neighbor with the event
		PathData[] paths = move.getPathData( west );
		assertEquals( 1, paths.length );
		assertNotNull( paths[0] );
		assertEquals( blockingEvent.getRisk(), paths[0].getRisk(), 0.001 );
		assertSame( riskPaths.getLast(), paths[0] );
		
		west.removeMovementEvent( blockingEvent );
		move.removeMovementModifier( enablingMod );
		TestObstacle blockingObstacle = new TestObstacle( FLAT, move.getImpassableMoveCost() );
		blockingObstacle.setPosition( EAST, west );
		
		// obstacle with high cost prevents entry to neighbor
		move.clearRadius();
		move.beginSearch( path, 1 );
		assertEquals( center.getNeighbors().length - 1, progressTiles.size() );
		assertArrayEquals( new PathData[0], move.getPathData( west ));
		
		// use modifier to ignore obstacle, the neighbor can be entered
		enablingMod = new TestMovementModifier( blockingObstacle );
		move.addMovementModifier( enablingMod );
		
		move.clearRadius();
		move.beginSearch( path, 1 );
		assertEquals( center.getNeighbors().length, progressTiles.size() );
		
		// make modifier increase move height instead of ignoring obstacle, moves over the obstacle
		move.removeMovementModifier( enablingMod );
		enablingMod = new TestMovementModifier();
		enablingMod.setMovementHeight( west, LOW );
		move.addMovementModifier( enablingMod );
		
		move.clearRadius();
		move.beginSearch( path, 1 );
		assertEquals( center.getNeighbors().length, progressTiles.size() );
		
		blockingObstacle.setLocation( null );
		move.removeMovementModifier( enablingMod );
		Terrain blockingTerrain = new Terrain( move.getImpassableMoveCost(), FLAT );
		west.setTerrain( blockingTerrain );
		
		// impassable terrain prevents entry to the neighbor
		move.clearRadius();
		move.beginSearch( path, 1 );
		assertEquals( center.getNeighbors().length - 1, progressTiles.size() );
		assertArrayEquals( new PathData[0], move.getPathData( west ));
		
		// modifier increases move height, and terrain is ignored
		move.addMovementModifier( enablingMod );
		move.clearRadius();
		move.beginSearch( path, 1 );
		assertEquals( center.getNeighbors().length, progressTiles.size() );
		
		// change modifier to remove terrain cost instead of increasing move height
		move.removeMovementModifier( enablingMod );
		enablingMod = new TestMovementModifier( 0f );
		move.addMovementModifier( enablingMod );
		
		// terrain doesn't prevent entry to the neighbor, since terrain cost becomes zero
		move.clearRadius();
		move.beginSearch( path, 1 );
		assertEquals( center.getNeighbors().length, progressTiles.size() );
		
		west.setTerrain( center.getTerrain() );
		move.removeMovementModifier( enablingMod );
		TestBlock block = new TestBlock( FLAT, move.getImpassableMoveCost() );
		west.addBlock( block );
		
		// block with impassable move cost prevents entry to the neighbor
		move.clearRadius();
		move.beginSearch( path, 1 );
		assertEquals( center.getNeighbors().length - 1, progressTiles.size() );
		assertArrayEquals( new PathData[0], move.getPathData( west ));
		
		enablingMod = new TestMovementModifier( block, 0f );
		move.addMovementModifier( enablingMod );
		
		// modifier ignores block and movement to neighbor becomes possible
		move.clearRadius();
		move.beginSearch( path, 1 );
		assertEquals( center.getNeighbors().length, progressTiles.size() );
		
		// change modifier to increase move height instead of ignoring block
		move.removeMovementModifier( enablingMod );
		enablingMod = new TestMovementModifier();
		enablingMod.setMovementHeight( west, LOW );
		move.addMovementModifier( enablingMod );
		
		// block is below move height and movement is possible
		move.clearRadius();
		move.beginSearch( path, 1 );
		assertEquals( center.getNeighbors().length, progressTiles.size() );
		
		// change modifier to alter the block's cost
		move.removeMovementModifier( enablingMod );
		enablingMod = new TestMovementModifier( (Block)null, -block.getMoveCost() );
		move.addMovementModifier( enablingMod );
		
		// block isn't ignored, but modifier cancels its cost
		move.clearRadius();
		move.beginSearch( path, 1 );
		assertEquals( center.getNeighbors().length, progressTiles.size() );
		
		west.removeBlock( block );
		move.removeMovementModifier( enablingMod );
		TestMovementModifier blockingMod = new TestMovementModifier( (Block)null, 1f );
		move.addMovementModifier( blockingMod );
		
		// modifier adds one to all move costs, so total move is insufficient to move anywhere
		move.clearRadius();
		move.beginSearch( path, 1 );
		assertTrue( progressTiles.isEmpty() );
		
		move.removeMovementModifier( blockingMod );
		path.setTotalCost( 1 );
		
		// total cost in starting tile is one, other tile's cost is added to it, exceeds total move
		move.clearRadius();
		move.beginSearch( path, 1 );
		assertTrue( progressTiles.isEmpty() );
		
		path.setTotalCost( 0 );
		path.setRisk( 1 );
		
		// starting tile has risk, so progress tiles are added to risk paths
		move.clearRadius();
		move.beginSearch( path, 1 );
		assertTrue( progressTiles.isEmpty() );
		assertEquals( center.getNeighbors().length, riskPaths.size() );
		
		path.setRisk( 0 );
		TestMovementEvent bufferEvent = new TestMovementEvent( 1f, FLAT, false, false, true );
		bufferEvent.setExecuteOnce( true );
		move.getHost().addMovementMode( move );
		west.addMovementEvent( bufferEvent );
		
		// west neighbor is added to risk paths because of an event that causes risk (using buffer)
		move.clearRadius();
		move.beginSearch( path, 1 );
		assertEquals( center.getNeighbors().length - 1, progressTiles.size() );
		assertEquals( 1, riskPaths.size() );
		assertArrayEquals( new PathData[] { riskPaths.getLast() }, move.getPathData( west ));
		assertEquals( bufferEvent.getRisk(), riskPaths.getLast().getRisk(), 0.001 );
		
		west.removeMovementEvent( bufferEvent );
		template.setSize( 2 );
		template.setHorizontallySymmetric( false );
		template.setAlternateTile( null );
		
		// size two template whose secondary tile is null, data is added for null tile
		move.clearRadius();
		move.beginSearch( path, 1 );
		assertEquals( 1, progressTiles.size() );
		assertArrayEquals( new PathData[] { progressTiles.getFirst() }, move.getPathData( null ));
		
		template.setAlternateTile( west );
		blockingObstacle.setLocation( west );
		
		// secondary tile for each neighbor is blocked by an obstacle, so no movement
		move.clearRadius();
		move.beginSearch( path, 1 );
		
		// turning in place assumes the position is valid for the mobile object
		assertEquals( 1, progressTiles.size() );
		assertArrayEquals( new PathData[] { progressTiles.getFirst() }, move.getPathData( west ));
		
		enablingMod = new TestMovementModifier( blockingObstacle );
		move.addMovementModifier( enablingMod );
		
		// modifier ignores the obstacle, neighbors allowed by facing become accessible
		move.clearRadius();
		move.beginSearch( path, 1 );
		assertEquals( center.getAccessibleNeighbors(		// turn in place in addition to others
				Templates.getMoveDirections( EAST, template )).length + 1, progressTiles.size() );
		
		blockingObstacle.setLocation( null );
		move.removeMovementModifier( enablingMod );
		block = new TestBlock( FLAT, 1f );
		west.addBlock( block );
		
		// block prevents movement, only turn in place is possible
		move.clearRadius();
		move.beginSearch( path, 1 );
		assertEquals( 1, progressTiles.size() );
		assertArrayEquals( new PathData[] { progressTiles.getFirst() }, move.getPathData( west ));
		
		enablingMod = new TestMovementModifier( block, 0f );
		move.addMovementModifier( enablingMod );
		
		// modifier ignores block and movement becomes possible
		move.clearRadius();
		move.beginSearch( path, 1 );
		assertEquals( center.getAccessibleNeighbors(
				Templates.getMoveDirections( EAST, template )).length + 1, progressTiles.size() );
		
		block.setMoveCost( 0f );
		west.removeBlock( block );
		move.removeMovementModifier( enablingMod );
		
		// test with a block that doesn't allow the template
		Tile southeast = center.getNeighbor( SOUTHEAST );
		Tile blockTile = southeast.getNeighbor( WEST );
		template.setAlternateTile( blockTile );
		block.addToBlockTemplate( blockTile );
		blockTile.addBlock( block );
		
		move.clearRadius();
		move.beginSearch( path, 1 );
		
		// different number of blocked tiles for hexes and squares
		int blockedTiles = 0;
		for (Tile t : center.getAccessibleNeighbors(
				Templates.getMoveDirections( EAST, template )))
			if (t != blockTile && t.isAdjacent( blockTile )) {
				blockedTiles++;
				
				assertArrayEquals( new PathData[0], move.getPathData( t ));
			}
		
		// block only works for tiles that are adjacent to its tile, and there's at least one
		assertTrue( blockedTiles > 0 );
		assertEquals( center.getAccessibleNeighbors(
				Templates.getMoveDirections( EAST, template )).length - blockedTiles + 1,
				progressTiles.size() );
		
		// modifier ignores the block
		move.addMovementModifier( enablingMod );
		move.clearRadius();
		move.beginSearch( path, 1 );
		assertEquals( center.getAccessibleNeighbors(
				Templates.getMoveDirections( EAST, template )).length + 1, progressTiles.size() );
		
		// the modifier can't ignore the block for occupying, only for movement
		for (Tile t : center.getAccessibleNeighbors(
				Templates.getMoveDirections( EAST, template )))
			assertFalse( move.canOccupy( t, EAST ));
		
		blockTile.removeBlock( block );
		move.removeMovementModifier( enablingMod );
		template.setAlternateTile( west );
		west.addMovementEvent( blockingEvent );
		
		// event prevents movement, but turn in place is possible
		move.clearRadius();
		move.beginSearch( path, 1 );
		assertEquals( 1, progressTiles.size() );
		assertArrayEquals( new PathData[] { progressTiles.getFirst() }, move.getPathData( west ));
		
		enablingMod.removeEventProtection( blockingEvent );
		enablingMod.addEventProtection( blockingEvent, 1 );
		move.addMovementModifier( enablingMod );
		
		// modifier ignores event, movement becomes possible
		move.clearRadius();
		move.beginSearch( path, 1 );
		assertEquals( center.getAccessibleNeighbors(
				Templates.getMoveDirections( EAST, template )).length + 1, progressTiles.size() );
		
		enablingMod.removeEventProtection( blockingEvent );
		enablingMod.addEventProtection( blockingEvent, 0 );
		for (Tile t : center.getAccessibleNeighbors(
				Templates.getMoveDirections( EAST, template )))
			t.addMovementEvent( blockingEvent );
		
		// the event is added to all neighbors, and causes a risk when moving
		move.clearRadius();
		move.beginSearch( path, 1 );
		assertEquals( 1, progressTiles.size() );
		assertArrayEquals( new PathData[] { progressTiles.getFirst() }, move.getPathData( west ));
		assertEquals( center.getAccessibleNeighbors(
				Templates.getMoveDirections( EAST, template )).length, riskPaths.size() );
		
		// event causes risk for each tile in the template
		for (PathData data : riskPaths)
			assertEquals( template.getSize() * blockingEvent.getRisk(), data.getRisk(), 0.001 );
		
		west.removeMovementEvent( blockingEvent );
		move.removeMovementModifier( enablingMod );
		west.setTerrain( blockingTerrain );
		
		for (Tile t : center.getAccessibleNeighbors(
				Templates.getMoveDirections( EAST, template )))
			t.removeMovementEvent( blockingEvent );
		
		// impassable terrain prevents movement and only turn in place is possible
		move.clearRadius();
		move.beginSearch( path, 1 );
		assertEquals( 1, progressTiles.size() );
		assertArrayEquals( new PathData[] { progressTiles.getFirst() }, move.getPathData( west ));
		
		// modifier removes terrain cost, movement becomes possible
		enablingMod = new TestMovementModifier( 0f );
		move.addMovementModifier( enablingMod );
		move.clearRadius();
		move.beginSearch( path, 1 );
		assertEquals( center.getAccessibleNeighbors(
				Templates.getMoveDirections( EAST, template )).length + 1, progressTiles.size() );
		
		west.setTerrain( flatTerrain );
		west.addMovementEvent( bufferEvent );
		move.removeMovementModifier( enablingMod );
		
		for (Tile t : center.getAccessibleNeighbors(
				Templates.getMoveDirections( EAST, template )))
			t.addMovementEvent( bufferEvent );
		
		// test with buffered event, it causes risk for all neighbors, but not for turn in place
		move.clearRadius();
		move.beginSearch( path, 1 );
		assertEquals( 1, progressTiles.size() );
		assertArrayEquals( new PathData[] { progressTiles.getFirst() }, move.getPathData( west ));
		
		assertEquals( center.getAccessibleNeighbors(
				Templates.getMoveDirections( EAST, template )).length, riskPaths.size() );
		for (PathData data : riskPaths)
			assertEquals( bufferEvent.getRisk(), data.getRisk(), 0.001 );
		
		enablingMod.addEventProtection( bufferEvent, 1 );
		move.addMovementModifier( enablingMod );
		
		// modifier removes risk from the event
		move.clearRadius();
		move.beginSearch( path, 1 );
		assertEquals( center.getAccessibleNeighbors(
				Templates.getMoveDirections( EAST, template )).length + 1, progressTiles.size() );
		assertTrue( riskPaths.isEmpty() );
		
		for (PathData data : progressTiles)
			assertEquals( 0, data.getRisk(), 0.001 );
		
		int adjacentAccessible = center.getAccessibleNeighbors(
				Templates.getMoveDirections( EAST, template )).length;
		
		if (center instanceof Square)
			((Square)center).addRemoteNeighbor( (Square)grid.getTileAtRC( 6, 6 ));
		else if (center instanceof Hex)
			((Hex)center).addRemoteNeighbor( (Hex)grid.getTileAtRC( 6, 6 ));
		
		// test with a remote neighbor, it's included in accessible neighbors
		move.clearRadius();
		move.beginSearch( path, 1 );
		assertEquals( adjacentAccessible + 1, center.getAccessibleNeighbors(
				Templates.getMoveDirections( EAST, template )).length );
		assertEquals( center.getAccessibleNeighbors(
				Templates.getMoveDirections( EAST, template )).length + 1, progressTiles.size() );
		assertTrue( riskPaths.isEmpty() );
		
		// test a situation where event prevents entry on occupation, but not on move
		Tile east = center.getNeighbor( EAST );
		TestMovementEvent noEntry = new TestMovementEvent( 0f, FLAT, true, false );
		east.addMovementEvent( noEntry );
		
		// movement modifier will allow moving over event, but doesn't increase occupy height
		TestMovementModifier heights = new TestMovementModifier( FLAT );
		heights.setMovementHeight( east, LOW );
		move.addMovementModifier( heights );

		move.clearRadius();
		template.setSize( 1 );
		template.setHorizontallySymmetric( true );
		move.beginSearch( path, 1 );
		
		// the neighbor can't be occupied because event prevents entry at occupy height
		for (Direction d : Direction.values())
			assertFalse( move.canBeOccupied( east, d ));
		
		// without the event, the neighbor can be occupied
		east.removeMovementEvent( noEntry );
		move.clearRadius();
		move.beginSearch( path, 1 );
		
		for (Direction d : Direction.values())
			assertTrue( move.canBeOccupied( east, d ));
		
		// do the same with a size two template
		template.setSize( 2 );
		template.setHorizontallySymmetric( false );
		template.setAlternateTile( center );
		
		move.clearRadius();
		move.beginSearch( path, 1 );
		
		for (Direction d : Direction.values())
			assertSame( Templates.isTemplateDirection( template, d ) && d.isDueEast(),
					move.canBeOccupied( east, d ));
		
		east.addMovementEvent( noEntry );
		move.clearRadius();
		move.beginSearch( path, 1 );
		
		for (Direction d : Templates.getMoveDirections( EAST, template ))
			assertFalse( move.canBeOccupied( east, d ));
	}

	// movement radius tests using a single-tile template
	private <T extends Tile> void testMovementRadius( TileGrid <T> grid ) {
		Terrain flatTerrain = new Terrain( 1f, FLAT );
		for (T[] row : grid.getTiles())
			for (T t : row)
				t.setTerrain( flatTerrain );
		
		MovementTemplate template = new SingleTileTemplate();
		TestMobileObject mob = new TestMobileObject( LOW, template );
		DefaultMovement move = new DefaultMovement( mob, 0 );
		
		T starting = grid.getTileAtRC( 3, 3 );
		mob.setPosition( EAST, starting );
		mob.addMovementMode( move );
		
		// test movement radius with total move 1-4
		for (int i = 1; i <= 4; i++) {
			move.movementRadius( i );
			
			for (T[] row : grid.getTiles())
				for (T t : row) {
					PathData data[] = move.getPathData( t );
					int distance = calculateTileDistance( grid.createLineHelper( starting, t ));
					
					// no obstacles etc so can reach any tile whose distance is up to total move
					if (distance <= i) {
						assertTrue( move.canBeOccupied( t, EAST ));
						assertEquals( 1, data.length );
					}
					
					// can't reach tiles farther away than total move
					else
						assertFalse( move.canBeOccupied( t, EAST ));
					
					// data exists for first neighbors outside the movement radius as well
					if (data.length > 0)
						assertEquals( distance, data[0].getTotalCost(), 0.001 );
				}
		}
		
		// movement event that prevents leaving has no effect in initial tile
		TestMovementEvent noLeaving = new TestMovementEvent( 0f, FLAT, false, true );
		starting.addMovementEvent( noLeaving );
		move.movementRadius( 10 );
		
		for (T[] row : grid.getTiles())
			for (T t : row)
				assertTrue( move.canBeOccupied( t, EAST ));
		
		// add event to another tile, so mobile object can't move through it
		T northwest = getNeighbor( starting, NORTHWEST );
		northwest.addMovementEvent( noLeaving );
		move.movementRadius( 3 );
		
		// the event blocks or hinders movement to tiles that are past the blocked tile
		T fartherNW = getNeighbor( northwest, NORTHWEST );
		T farthestNW = getNeighbor( fartherNW, NORTHWEST );
		
		for (T[] row : grid.getTiles())
			for (T t : row) {
				// the farthest tile on the other side of the blocked tile can't be reached
				assertEquals( calculateTileDistance( grid.createLineHelper( starting, t )) <= 3 &&
						t != farthestNW, move.canBeOccupied( t, EAST ));
				
				// can enter the tile with the event, but can't move further from it
				PathData[] data = move.getPathData( t );
				if (data.length > 0 && data[0].getPath() != null)
					assertNotSame( data[0].getPath().getTile(), northwest );
			}
		
		TestMovementModifier enablingMod = new TestMovementModifier();
		enablingMod.addEventProtection( noLeaving, 1 );
		move.addMovementModifier( enablingMod );
		
		// use a modifier to allow the mobile object to move across the event
		move.movementRadius( 3 );
		for (T[] row : grid.getTiles())
			for (T t : row)
				assertEquals( calculateTileDistance(
						grid.createLineHelper( starting, t )) <= 3, move.canBeOccupied( t, EAST ));
		
		starting.removeMovementEvent( noLeaving );
		northwest.removeMovementEvent( noLeaving );
		move.removeMovementModifier( enablingMod );
		
		TestMovementModifier lowMoveMod = new TestMovementModifier();
		lowMoveMod.setMovementHeight( northwest, LOW );
		move.addMovementModifier( lowMoveMod );
		
		// test with a modifier that changes move height, movement radius is unchanged
		move.movementRadius( 3 );
		for (T[] row : grid.getTiles())
			for (T t : row)
				assertEquals( calculateTileDistance(
						grid.createLineHelper( starting, t )) <= 3, move.canBeOccupied( t, EAST ));
		
		move.removeMovementModifier( lowMoveMod );
		TestObstacle obstacle = new TestObstacle( LOW, 1f );
		obstacle.setPosition( EAST, northwest );
		
		// place an obstacle next to starting tile, it increases move cost in that tile
		move.movementRadius( 3 );
		for (T[] row : grid.getTiles())
			for (T t : row)
				assertEquals(				// obstacle tile can't be occupied
						t.getClass().getSimpleName() + ": row " + t.getRow() + ", column: " +
						t.getColumn(),
						calculateTileDistance( grid.createLineHelper( starting, t )) <= 3 &&
						t != farthestNW && t != northwest, move.canBeOccupied( t, EAST ));
		
		obstacle.setObstacleCost( 2f );
		move.movementRadius( 3 );
		
		// increase obstacle cost, movement around obstacle as before
		for (T[] row : grid.getTiles())
			for (T t : row) {
				assertEquals(
						calculateTileDistance( grid.createLineHelper( starting, t )) <= 3 &&
						t != farthestNW && t != northwest, move.canBeOccupied( t, EAST ));
				
				// now takes full movement to move to obstacle tile, so no movement further
				PathData[] data = move.getPathData( t );
				if (data.length > 0 && data[0].getPath() != null)
					assertNotSame( data[0].getPath().getTile(), northwest );
			}
		
		enablingMod = new TestMovementModifier( obstacle );
		move.addMovementModifier( enablingMod );
		
		// use a modifier that ignores the obstacle, same as even terrain
		move.movementRadius( 3 );
		for (T[] row : grid.getTiles())
			for (T t : row)
				assertEquals(
						calculateTileDistance( grid.createLineHelper( starting, t )) <= 3 &&
						t != northwest, move.canBeOccupied( t, EAST ));
		
		move.removeMovementModifier( enablingMod );
		lowMoveMod.setMovementHeight( northwest, HIGH );
		move.addMovementModifier( lowMoveMod );
		
		// use a modifier that makes move height higher than obstacle, same as even terrain
		move.movementRadius( 3 );
		for (T[] row : grid.getTiles())
			for (T t : row)
				assertEquals(
						calculateTileDistance( grid.createLineHelper( starting, t )) <= 3 &&
						t != northwest, move.canBeOccupied( t, EAST ));
		
		move.removeMovementModifier( lowMoveMod );
		obstacle.setOccupying( false );
		
		// higher total move, obstacle tile can now be occupied, movement around it is possible
		move.movementRadius( 4 );
		for (T[] row : grid.getTiles())
			for (T t : row)
				assertEquals(
						calculateTileDistance( grid.createLineHelper( starting, t )) <= 4,
						move.canBeOccupied( t, EAST ));
		
		obstacle.setLocation( null );
		TestMovementEvent noEntry = new TestMovementEvent( 0f, FLAT, true, false );
		northwest.addMovementEvent( noEntry );
		
		// test with a movement event that prevents entry to the tile
		move.movementRadius( 3 );
		for (T[] row : grid.getTiles())
			for (T t : row)
				// can't reach farthest tile behind event tile, can't enter event tile
				assertEquals(
						calculateTileDistance( grid.createLineHelper( starting, t )) <= 3 &&
						t != farthestNW && t != northwest, move.canBeOccupied( t, EAST ));
		
		enablingMod.addEventProtection( noEntry, 1 );
		move.addMovementModifier( enablingMod );
		
		// modifier ignores event, same as even terrain
		move.movementRadius( 3 );
		for (T[] row : grid.getTiles())
			for (T t : row)
				assertEquals(
						calculateTileDistance( grid.createLineHelper( starting, t )) <= 3,
						move.canBeOccupied( t, EAST ));
		
		move.removeMovementModifier( enablingMod );
		lowMoveMod.setMovementHeight( northwest, LOW );
		move.addMovementModifier( lowMoveMod );
		
		// modifier increases move height above event, same as even terrain
		move.movementRadius( 3 );
		for (T[] row : grid.getTiles())
			for (T t : row)
				assertEquals(
						calculateTileDistance( grid.createLineHelper( starting, t )) <= 3,
						move.canBeOccupied( t, EAST ));
		
		move.removeMovementModifier( lowMoveMod );
		northwest.removeMovementEvent( noEntry );
		northwest.addMovementEvent( noLeaving );
		noLeaving.setBufferOnLeave( true );
		
		// test an event which purposefully buffers itself so that it doesn't get executed
		move.movementRadius( 3 );
		for (T[] row : grid.getTiles())
			for (T t : row)
				assertEquals(
						calculateTileDistance( grid.createLineHelper( starting, t )) <= 3 &&
						t != farthestNW, move.canBeOccupied( t, EAST ));
		
		// event buffer wasn't executed with the event, but it was cleared
		assertFalse( noLeaving.isBufferExecuted() );
		assertFalse( move.isEventBuffered( noLeaving, null ));
		
		northwest.removeMovementEvent( noLeaving );
		TestMovementEvent riskEvent = new TestMovementEvent( 1f, FLAT, false, false );
		northwest.addMovementEvent( riskEvent );
		
		// test with an event that causes risk
		move.movementRadius( 3 );
		for (T[] row : grid.getTiles())
			for (T t : row) {
				if (calculateTileDistance( grid.createLineHelper( starting, t )) > 3)
					assertFalse( move.canBeOccupied( t, EAST ));
				
				else {
					assertTrue( move.canBeOccupied( t, EAST ));

					// all tiles within distance equal to total move can be reached
					PathData[] data = move.getPathData( t );

					// two paths: across the event, and around it
					if (t == fartherNW) {
						assertEquals( 2, data.length );
						assertNotNull( data[0] );
						assertNotNull( data[1] );

						// path with lower cost is across the event and has a risk
						assertEquals( riskEvent.getRisk(),
								(data[0].getTotalCost() < 2.999f ? data[0] : data[1]).getRisk(),
								0.001 );

						// path with higher cost is around the event and has no risk
						assertEquals( 0f,
								(data[0].getTotalCost() < 2.999f ? data[1] : data[0]).getRisk(),
								0.001 );
					}

					// other tiles have only one path
					else {
						assertEquals( 1, data.length );
						assertNotNull( data[0] );

						// event tile has risk, farthest tile can be reached only across the event
						assertEquals( t == farthestNW || t == northwest ? riskEvent.getRisk() : 0f,
							data[0].getRisk(), 0.001 );
					}
				}
			}
		
		TestMovementEvent biggerRisk = new TestMovementEvent( 1.5f, FLAT, false, false );
		farthestNW.addMovementEvent( biggerRisk );
		
		// movement radius with another risk event added to the farthest tile
		move.movementRadius( 3 );
		for (T[] row : grid.getTiles())
			for (T t : row) {
				if (calculateTileDistance( grid.createLineHelper( starting, t )) > 3)
					assertFalse( move.canBeOccupied( t, EAST ));
				
				else {
					assertTrue( move.canBeOccupied( t, EAST ));

					PathData[] data = move.getPathData( t );
					
					// risk path across event (lower cost), risk-free path around it (higher cost)
					if (t == fartherNW) {
						assertEquals( 2, data.length );
						assertNotNull( data[0] );
						assertNotNull( data[1] );
						
						assertEquals( riskEvent.getRisk(),
								(data[0].getTotalCost() < 2.999f ? data[0] : data[1]).getRisk(),
								0.001 );
						assertEquals( 0f,
								(data[0].getTotalCost() < 2.999f ? data[1] : data[0]).getRisk(),
								0.001 );
					}
					
					else {
						assertEquals( 1, data.length );
						assertNotNull( data[0] );

						// most tiles can be reached without risk
						if (t != farthestNW && t != northwest)
							assertEquals( 0f, data[0].getRisk(), 0.001 );

						// first event tile has risk from first event, farthest tile from both
						else {
							float risk = riskEvent.getRisk();
							if (t == farthestNW)
								risk += biggerRisk.getRisk();

							assertEquals( risk, data[0].getRisk(), 0.001 );
						}
					}
				}
			}
		
		TestMovementModifier safeMod = new TestMovementModifier();
		safeMod.addEventProtection( riskEvent, 1 );
		move.addMovementModifier( safeMod );
		
		// modifier ignores first risk event, so only the farthest tile has a risk
		move.movementRadius( 3 );
		for (T[] row : grid.getTiles())
			for (T t : row) {
				if (calculateTileDistance( grid.createLineHelper( starting, t )) > 3)
					assertFalse( move.canBeOccupied( t, EAST ));
				
				else {
					assertTrue( move.canBeOccupied( t, EAST ));

					PathData[] data = move.getPathData( t );
					assertEquals( 1, data.length );
					assertEquals( t == farthestNW ? biggerRisk.getRisk() : 0f,
						data[0].getRisk(), 0.001 );
				}
			}
		
		move.removeMovementModifier( safeMod );
		move.addMovementModifier( lowMoveMod );
		lowMoveMod.setMovementHeight( farthestNW, LOW );
		
		// modifier increases move height to higher than both risk events
		move.movementRadius( 3 );
		for (T[] row : grid.getTiles())
			for (T t : row) {
				if (calculateTileDistance( grid.createLineHelper( starting, t )) > 3)
					assertFalse( move.canBeOccupied( t, EAST ));
				
				else {
					assertTrue( move.canBeOccupied( t, EAST ));
				
					PathData[] data = move.getPathData( t );
					assertEquals( 1, data.length );
					assertEquals( 0f, data[0].getRisk(), 0.001 );
				}
			}
	}
	
	// test movement radius with a template of two horizontally adjacent squares
	private void testSizeTwoSquareTemplate( TileGrid <Square> grid ) {
		Terrain flatTerrain = new Terrain( 1f, FLAT );
		for (Square[] row : grid.getTiles())
			for (Square s : row)
				s.setTerrain( flatTerrain );
		
		TestMobileObject mob = new TestMobileObject( LOW, new HorizontalTwoTileTemplate() );
		DefaultMovement move = new DefaultMovement( mob, 0 );
		
		Square starting = grid.getTileAtRC( 3, 3 );
		Square secondary = starting.getNeighbor( WEST );
		mob.setPosition( EAST, starting );
		mob.addMovementMode( move );
		
		// test movement radius generation on even terrain with total move varying from one to four
		for (int i = 1; i <= 4; i++) {
			move.movementRadius( i );
			
			for (Square[] row : grid.getTiles())
				for (Square s : row) {
					// distance calculation which considers mobile object turning in place first
					int stDist = calculateTileDistance( grid.createLineHelper( starting, s ));
					int secDist = calculateTileDistance( grid.createLineHelper( secondary, s ));
					int distance = Math.min( stDist, secDist );
					
					// can't occupy first column facing east, second tile outside grid
					if (s.getColumn() == 0 || distance > i)
						assertFalse( move.canBeOccupied( s, EAST ));
					else if (distance < i)
						assertTrue( move.canBeOccupied( s, EAST ));
					else
						assertSame( move.canBeOccupied( s.getNeighbor( WEST ), WEST ),
								move.canBeOccupied( s, EAST ));
					
					// can't occupy last column facing west, second tile outside grid
					if (s.getColumn() == row.length - 1 || distance > i)
						assertFalse( move.canBeOccupied( s, WEST ));
					else if (distance < i)
						assertTrue( move.canBeOccupied( s, WEST ));
					else
						assertSame( move.canBeOccupied( s.getNeighbor( EAST ), EAST ),
								move.canBeOccupied( s, WEST ));
					
					// can have two path data for two facing directions
					if (distance <= i)
						for (PathData pd : move.getPathData( s )) {
							int expectCost = distance;
							int rowDiff = Math.abs( pd.getTile().getRow() - starting.getRow() );
							int colDiff = pd.getTile().getColumn() - starting.getColumn();
							
							// cases where necessary to move one tile farther and turn in place
							if (pd.getTemplateFacing().isDueWest() && stDist < secDist &&
									colDiff >= rowDiff || pd.getTemplateFacing().isDueEast() &&
									stDist > secDist && colDiff < -rowDiff)
								expectCost++;
							
							assertEquals( expectCost, pd.getTotalCost(), 0.001 );
						}
				}
		}
		
		TestMovementEvent noLeaving = new TestMovementEvent( 0f, FLAT, false, true );
		starting.addMovementEvent( noLeaving );
		
		// test with an event that prevents leaving a tile, ignored in starting tile
		move.movementRadius( 10 );
		for (Square[] row : grid.getTiles())
			for (Square s : row) {
				assertSame( s.getColumn() > 0, move.canBeOccupied( s, EAST ));
				assertSame( s.getColumn() < row.length - 1, move.canBeOccupied( s, WEST ));
			}
		
		Square northwest = starting.getNeighbor( NORTHWEST );
		northwest.addMovementEvent( noLeaving );
		
		// put blocking event in a neighbor tile, mobile object must go around it
		for (int i = 2; i <= 4; i++) {
			move.movementRadius( i );
			
			for (Square[] row : grid.getTiles())
				for (Square s : row) {
					// distance calculation which considers mobile object turning in place first
					int stDist = calculateTileDistance( grid.createLineHelper( starting, s ));
					int secDist = calculateTileDistance( grid.createLineHelper( secondary, s ));
					int distance = Math.min( stDist, secDist );
					
					// additional tiles which are unreachable because of the event
					if (s.getColumn() == 0 || distance > i ||
							i == 2 && (s.getRow() == 0 || s.getRow() == 1 && s.getColumn() <= 2) ||
							i == 3 && s.getRow() == 0 && s.getColumn() < 2)
						assertFalse( move.canBeOccupied( s, EAST ));
					else if (distance < i)
						assertTrue( move.canBeOccupied( s, EAST ));
					else if (distance == i)
						// cases where would have to move one tile farther and then turn in place
						assertSame( secondary.getColumn() - s.getColumn() < Math.abs(
								s.getRow() - starting.getRow() ), move.canBeOccupied( s, EAST ));
					
					// additional tiles which are unreachable because of the event
					if (s.getColumn() == row.length - 1 || distance > i ||
							i == 2 && (s.getRow() == 0 || s.getRow() == 1 && s.getColumn() <= 1) ||
							i == 3 && s.getRow() == 0 && s.getColumn() == 0)
						assertFalse( move.canBeOccupied( s, WEST ));
					else if (distance < i)
						assertTrue( move.canBeOccupied( s, WEST ));
					else if (distance == i)
						// cases where would have to move one tile farther and then turn in place
						assertSame( s.getColumn() - starting.getColumn() < Math.abs(
								s.getRow() - starting.getRow() ), move.canBeOccupied( s, WEST ));
				}
		}
		
		starting.removeMovementEvent( noLeaving );
		northwest.removeMovementEvent( noLeaving );
		
		TestObstacle obstacle = new TestObstacle( LOW, 1f );
		obstacle.setPosition( EAST, northwest );
		
		// test with an obstacle in a neighbor
		move.movementRadius( 3 );
		for (Square[] row : grid.getTiles())
			for (Square s : row) {
				// distance calculation which considers mobile object turning in place first
				int stDist = calculateTileDistance( grid.createLineHelper( starting, s ));
				int secDist = calculateTileDistance( grid.createLineHelper( secondary, s ));
				int distance = Math.min( stDist, secDist );
				
				if (distance > 3)
					assertFalse( move.canBeOccupied( s, EAST ) || move.canBeOccupied( s, WEST ));
				
				else {
					// top left can't be reached, some tiles can't be occupied because of obstacle
					assertSame( (s.getRow() > 0 || s.getColumn() > 1) && s.getColumn() > 0 &&
							(s.getRow() != 2 || s.getColumn() < 2 || s.getColumn() > 3),
							move.canBeOccupied( s, EAST ));

					assertSame( (s.getRow() > 0 || s.getColumn() > 0) &&
							s.getColumn() < row.length - 1 &&
							(s.getRow() != 2 || s.getColumn() == 0 || s.getColumn() > 2),
							move.canBeOccupied( s, WEST ));
					
					for (PathData pd : move.getPathData( s )) {
						int expectCost = distance;
						int rowDiff = Math.abs( pd.getTile().getRow() - starting.getRow() );
						
						// cases where necessary to move one tile farther and turn in place
						if (stDist < secDist && pd.getTemplateFacing().isDueWest() &&
								pd.getTile().getColumn() - starting.getColumn() >= rowDiff)
							expectCost++;
						
						else if (stDist > secDist && pd.getTemplateFacing().isDueEast() &&
								secondary.getColumn() - pd.getTile().getColumn() >= rowDiff)
							expectCost++;
						
						// cases where mobile object must move onto the obstacle
						if (s.getRow() == 2 && (s.getColumn() == 2 ||
								s.getColumn() == 1 && pd.getTemplateFacing().isDueWest() ||
								s.getColumn() == 3 && pd.getTemplateFacing().isDueEast()) ||
								s.getRow() == 1 && (s.getColumn() < 2 ||
										s.getColumn() == 2 && pd.getTemplateFacing().isDueEast()))
							expectCost += obstacle.getObstacleCost();
						
						assertEquals( expectCost, pd.getTotalCost(), 0.001 );
					}
				}
			}

		// test with a higher obstacle cost
		obstacle.setObstacleCost( 2f );
		move.movementRadius( 3 );
		
		for (Square[] row : grid.getTiles())
			for (Square s : row) {
				// distance calculation which considers mobile object turning in place first
				int stDist = calculateTileDistance( grid.createLineHelper( starting, s ));
				int secDist = calculateTileDistance( grid.createLineHelper( secondary, s ));
				int distance = Math.min( stDist, secDist );
				
				if (distance > 3)
					assertFalse( move.canBeOccupied( s, EAST ) || move.canBeOccupied( s, WEST ));
				
				else {
					// top left can't be reached, some tiles can't be occupied because of obstacle
					assertSame( (s.getRow() > 0 || s.getColumn() > 1) && s.getColumn() > 0 &&
							(s.getRow() != 2 || s.getColumn() < 2 || s.getColumn() > 3),
							move.canBeOccupied( s, EAST ));

					assertSame( (s.getRow() > 0 || s.getColumn() > 0) &&
							s.getColumn() < row.length - 1 &&
							(s.getRow() != 2 || s.getColumn() == 0 || s.getColumn() > 2),
							move.canBeOccupied( s, WEST ));
					
					for (PathData pd : move.getPathData( s )) {
						int expectCost = distance;
						int rowDiff = Math.abs( pd.getTile().getRow() - starting.getRow() );

						// cases where necessary to move one tile farther and turn in place
						if (stDist < secDist && pd.getTemplateFacing().isDueWest() &&
								pd.getTile().getColumn() - starting.getColumn() >= rowDiff)
							expectCost++;
						
						else if (stDist > secDist && pd.getTemplateFacing().isDueEast() &&
								secondary.getColumn() - pd.getTile().getColumn() >= rowDiff)
							expectCost++;
						
						// cases where mobile object must move onto the obstacle
						if (s.getRow() == 2 && (s.getColumn() == 2 ||
								s.getColumn() == 1 && pd.getTemplateFacing().isDueWest() ||
								s.getColumn() == 3 && pd.getTemplateFacing().isDueEast()))
							expectCost += obstacle.getObstacleCost();
						
						// cases where mobile object must move around the obstacle
						else if (s.getRow() == 1 && (s.getColumn() < 2 ||
								s.getColumn() == 2 && pd.getTemplateFacing().isDueEast()))
							expectCost++;
						
						assertEquals( expectCost, pd.getTotalCost(), 0.001 );
					}
				}
			}

		obstacle.setOccupying( false );
		move.movementRadius( 3 );
		
		// test with an obstacle (cost 2) that doesn't occupy the tile
		for (Square[] row : grid.getTiles())
			for (Square s : row) {
				// distance calculation which considers mobile object turning in place first
				int stDist = calculateTileDistance( grid.createLineHelper( starting, s ));
				int secDist = calculateTileDistance( grid.createLineHelper( secondary, s ));
				int distance = Math.min( stDist, secDist );
				
				if (distance > 3)
					assertFalse( move.canBeOccupied( s, EAST ) || move.canBeOccupied( s, WEST ));
				
				else {
					// top left can't be reached
					assertSame( (s.getRow() > 0 || s.getColumn() > 1) && s.getColumn() > 0,
							move.canBeOccupied( s, EAST ));

					assertSame( (s.getRow() > 0 || s.getColumn() > 0) &&
							s.getColumn() < row.length - 1, move.canBeOccupied( s, WEST ));

					for (PathData pd : move.getPathData( s )) {
						int expectCost = distance;
						int rowDiff = Math.abs( pd.getTile().getRow() - starting.getRow() );

						// cases where necessary to move one tile farther and turn in place
						if (stDist < secDist && pd.getTemplateFacing().isDueWest() &&
								pd.getTile().getColumn() - starting.getColumn() >= rowDiff)
							expectCost++;
						
						else if (stDist > secDist && pd.getTemplateFacing().isDueEast() &&
								secondary.getColumn() - pd.getTile().getColumn() >= rowDiff)
							expectCost++;
						
						// cases where mobile object must move onto the obstacle
						if (s.getRow() == 2 && (s.getColumn() == 2 ||
								s.getColumn() == 1 && pd.getTemplateFacing().isDueWest() ||
								s.getColumn() == 3 && pd.getTemplateFacing().isDueEast()))
							expectCost += obstacle.getObstacleCost();
						
						// cases where mobile object must move around the obstacle
						else if (s.getRow() == 1 && (s.getColumn() < 2 ||
								s.getColumn() == 2 && pd.getTemplateFacing().isDueEast()))
							expectCost++;
						
						assertEquals( expectCost, pd.getTotalCost(), 0.001 );
					}
				}
			}

		obstacle.setObstacleCost( move.getImpassableMoveCost() );
		TestMovementEvent noEntry = new TestMovementEvent( 0f, FLAT, true, false );
		
		// test with an obstacle that prevents movement onto its tile, higher total move
		for (int i = 0; i < 2; i++) {
			move.movementRadius( 10 );

			for (Square[] row : grid.getTiles())
				for (Square s : row) {
					// distance calculation which considers mobile object turning in place first
					int stDist = calculateTileDistance( grid.createLineHelper( starting, s ));
					int secDist = calculateTileDistance( grid.createLineHelper( secondary, s ));
					int distance = Math.min( stDist, secDist );
					
					// all tiles can be reached, except those where obstacle/event is
					assertSame( s.getColumn() > 0 &&
							(s.getRow() != 2 || s.getColumn() < 2 || s.getColumn() > 3),
							move.canBeOccupied( s, EAST ));

					assertSame( s.getColumn() < row.length - 1 &&
							(s.getRow() != 2 || s.getColumn() == 0 || s.getColumn() > 2),
							move.canBeOccupied( s, WEST ));

					for (PathData pd : move.getPathData( s )) {
						int expectCost = distance;
						int rowDiff = Math.abs( pd.getTile().getRow() - starting.getRow() );

						// cases where necessary to move one tile farther and turn in place
						if (stDist < secDist && pd.getTemplateFacing().isDueWest() &&
								pd.getTile().getColumn() - starting.getColumn() >= rowDiff)
							expectCost++;
						
						else if (stDist > secDist && pd.getTemplateFacing().isDueEast() &&
								secondary.getColumn() - pd.getTile().getColumn() >= rowDiff)
							expectCost++;
						
						// cases where mobile object must move around the obstacle/event
						if (s.getRow() == 1 && (s.getColumn() < 2 ||
								s.getColumn() == 2 && pd.getTemplateFacing().isDueEast()) ||
								s.getRow() == 0 && (s.getColumn() == 0 ||
								s.getColumn() == 1 && pd.getTemplateFacing().isDueEast()))
							expectCost++;
						
						assertEquals( expectCost, pd.getTotalCost(), 0.001 );
					}
				}
			
			// same results with an event that prevents entry
			obstacle.setLocation( null );
			northwest.addMovementEvent( noEntry );
		}
	}
	
	// test movement radius with a template of two horizontally adjacent hexes
	private void testSizeTwoHexTemplate( TileGrid <Hex> grid ) {
		Terrain flatTerrain = new Terrain( 1f, FLAT );
		for (Hex[] row : grid.getTiles())
			for (Hex h : row)
				h.setTerrain( flatTerrain );
		
		TestMobileObject mob = new TestMobileObject( LOW, new HorizontalTwoTileTemplate() );
		DefaultMovement move = new DefaultMovement( mob, 0 );
		
		Hex starting = grid.getTileAtRC( 3, 3 );
		Hex secondary = starting.getNeighbor( WEST );
		mob.setPosition( EAST, starting );
		mob.addMovementMode( move );
		
		// test movement radius generation on even terrain with total move varying from one to five
		for (int i = 1; i <= 5; i++) {
			move.movementRadius( i );
			
			for (Hex[] row : grid.getTiles())
				for (Hex h : row) {
					// distance calculation which considers mobile object turning in place first
					int stDist = calculateTileDistance( grid.createLineHelper( starting, h ));
					int secDist = calculateTileDistance( grid.createLineHelper( secondary, h ));
					int distance = Math.min( stDist, secDist );
					
					// can't occupy first column facing east, second tile outside grid
					if (h.getColumn() == 0 || distance > i)
						assertFalse( move.canBeOccupied( h, EAST ));
					else if (distance < i)
						assertTrue( move.canBeOccupied( h, EAST ));
					else
						assertSame( move.canBeOccupied( h.getNeighbor( WEST ), WEST ),
								move.canBeOccupied( h, EAST ));
					
					// can't occupy last column facing west, second tile outside grid
					if (h.getColumn() == row.length - 1 || distance > i)
						assertFalse( move.canBeOccupied( h, WEST ));
					else if (distance < i)
						assertTrue( move.canBeOccupied( h, WEST ));
					else
						assertSame( move.canBeOccupied( h.getNeighbor( EAST ), EAST ),
								move.canBeOccupied( h, WEST ));
					
					// can have two path data for two facing directions
					if (distance <= i)
						for (PathData pd : move.getPathData( h )) {
							int expectCost = distance;
							int rowDiff = Math.abs( pd.getTile().getRow() - starting.getRow() );
							
							// cases where necessary to move one tile farther and turn in place
							if (stDist < secDist) {
								int colDiff = (rowDiff + 1) / 2;
								
								if (pd.getTemplateFacing().isDueWest() &&
										pd.getTile().getColumn() - starting.getColumn() >= colDiff)
									expectCost++;
							}
							
							else if (stDist > secDist) {
								int colDiff = -(rowDiff / 2);
								
								if (pd.getTemplateFacing().isDueEast() &&
										secondary.getColumn() - pd.getTile().getColumn() >= colDiff)
									expectCost++;
							}
							
							assertEquals( expectCost, pd.getTotalCost(), 0.001 );
						}
				}
		}
		
		TestMovementEvent noLeaving = new TestMovementEvent( 0f, FLAT, false, true );
		starting.addMovementEvent( noLeaving );
		
		// test with an event that prevents leaving a tile, ignored in starting tile
		move.movementRadius( 10 );
		for (Hex[] row : grid.getTiles())
			for (Hex h : row) {
				assertSame( h.getColumn() > 0, move.canBeOccupied( h, EAST ));
				assertSame( h.getColumn() < row.length - 1, move.canBeOccupied( h, WEST ));
			}
		
		Hex northwest = starting.getNeighbor( NORTHWEST );
		northwest.addMovementEvent( noLeaving );
		
		// put blocking event in a neighbor tile, mobile object must go around it
		for (int i = 2; i <= 3; i++) {
			move.movementRadius( i );
		
			for (Hex[] row : grid.getTiles())
				for (Hex h : row) {
					// distance calculation which considers mobile object turning in place first
					int stDist = calculateTileDistance( grid.createLineHelper( starting, h ));
					int secDist = calculateTileDistance( grid.createLineHelper( secondary, h ));
					int distance = Math.min( stDist, secDist );

					// additional tiles which are unreachable because of the event
					if (h.getColumn() == 0 || distance > i ||
							i == 2 && h.getRow() < 2 ||
							i == 3 && (h.getRow() == 0 || h.getRow() == 1 && h.getColumn() == 3))
						assertFalse( move.canBeOccupied( h, EAST ));
					else if (distance < i)
						assertTrue( move.canBeOccupied( h, EAST ));
					else if (distance == i)
						// cases where would have to move one tile farther and then turn in place
						assertSame( secondary.getColumn() - h.getColumn() < Math.abs(
								h.getRow() - starting.getRow() ) / 2,
								move.canBeOccupied( h, EAST ));

					// additional tiles which are unreachable because of the event
					if (h.getColumn() == row.length - 1 || distance > i ||
							i == 2 && h.getRow() < 2 ||
							i == 3 && (h.getRow() == 0 || h.getRow() == 1 && h.getColumn() == 2))
						assertFalse( move.canBeOccupied( h, WEST ));
					else if (distance < i)
						assertTrue( move.canBeOccupied( h, WEST ));
					else
						// cases where would have to move one tile farther and then turn in place
						assertSame( h.getColumn() - starting.getColumn() < (Math.abs(
								h.getRow() - starting.getRow() ) + 1) / 2,
								move.canBeOccupied( h, WEST ));
				}
		}
		
		starting.removeMovementEvent( noLeaving );
		northwest.removeMovementEvent( noLeaving );
		
		TestObstacle obstacle = new TestObstacle( LOW, 1f );
		obstacle.setPosition( EAST, northwest );
		
		// test with an obstacle in a neighbor
		move.movementRadius( 3 );
		for (Hex[] row : grid.getTiles())
			for (Hex h : row) {
				
				// distance calculation which considers mobile object turning in place first
				int stDist = calculateTileDistance( grid.createLineHelper( starting, h ));
				int secDist = calculateTileDistance( grid.createLineHelper( secondary, h ));
				int distance = Math.min( stDist, secDist );
				
				if (distance > 3)
					assertFalse( move.canBeOccupied( h, EAST ) || move.canBeOccupied( h, WEST ));
				
				else {
					// top left can't be reached, some tiles can't be occupied because of obstacle
					assertSame( h.getRow() > 0 && h.getColumn() > 0 &&
							(distance < 3 || stDist <= secDist) &&
							(h.getRow() != 2 || h.getColumn() < 3 || h.getColumn() > 4),
							move.canBeOccupied( h, EAST ));
					
					assertSame( h.getRow() > 0 && h.getColumn() < row.length - 1 &&
							(distance < 3 || stDist >= secDist) &&
							(h.getRow() != 2 || h.getColumn() < 2 || h.getColumn() > 3),
							move.canBeOccupied( h, WEST ));
					
					for (PathData pd : move.getPathData( h )) {
						int expectCost = distance;
						int rowDiff = Math.abs( pd.getTile().getRow() - starting.getRow() );

						// cases where necessary to move one tile farther and turn in place
						if (stDist < secDist) {
							int colDiff = (rowDiff + 1) / 2;

							if (pd.getTemplateFacing().isDueWest() &&
									pd.getTile().getColumn() - starting.getColumn() >= colDiff)
								expectCost++;
						}

						else if (stDist > secDist) {
							int colDiff = rowDiff / 2;

							if (pd.getTemplateFacing().isDueEast() &&
									secondary.getColumn() - pd.getTile().getColumn() >= colDiff)
								expectCost++;
						}

						// cases where mobile object must move onto the obstacle
						if (h.getRow() == 2 && (h.getColumn() == 3 ||
								h.getColumn() == 2 && pd.getTemplateFacing().isDueWest() ||
								h.getColumn() == 4 && pd.getTemplateFacing().isDueEast()) ||
								h.getRow() == 1 && (h.getColumn() == 2 || h.getColumn() == 3 ||
								h.getColumn() == 1 && pd.getTemplateFacing().isDueWest() ||
								h.getColumn() == 4 && pd.getTemplateFacing().isDueEast()))
							expectCost += obstacle.getObstacleCost();

						assertEquals( expectCost, pd.getTotalCost(), 0.001 );
					}
				}
			}
		
		// test with a higher obstacle cost
		obstacle.setObstacleCost( 2f );
		move.movementRadius( 3 );
		
		for (Hex[] row : grid.getTiles())
			for (Hex h : row) {
				// distance calculation which considers mobile object turning in place first
				int stDist = calculateTileDistance( grid.createLineHelper( starting, h ));
				int secDist = calculateTileDistance( grid.createLineHelper( secondary, h ));
				int distance = Math.min( stDist, secDist );
				
				if (distance > 3)
					assertFalse( move.canBeOccupied( h, EAST ) || move.canBeOccupied( h, WEST ));
				
				else {
					// top left can't be reached, some tiles can't be occupied because of obstacle
					assertSame( h.getRow() > 0 && h.getColumn() > 0 &&
							(h.getRow() != 1 || h.getColumn() != 3) &&
							(distance < 3 || stDist <= secDist) &&
							(h.getRow() != 2 || h.getColumn() < 3 || h.getColumn() > 4),
							move.canBeOccupied( h, EAST ));
					
					assertSame( h.getRow() > 0 && h.getColumn() < row.length - 1 &&
							(h.getRow() != 1 || h.getColumn() != 2) &&
							(distance < 3 || stDist >= secDist) &&
							(h.getRow() != 2 || h.getColumn() < 2 || h.getColumn() > 3),
							move.canBeOccupied( h, WEST ));
					
					for (PathData pd : move.getPathData( h )) {
						int expectCost = distance;
						int rowDiff = Math.abs( pd.getTile().getRow() - starting.getRow() );

						// cases where necessary to move one tile farther and turn in place
						if (stDist < secDist) {
							int colDiff = (rowDiff + 1) / 2;

							if (pd.getTemplateFacing().isDueWest() &&
									pd.getTile().getColumn() - starting.getColumn() >= colDiff)
								expectCost++;
						}

						else if (stDist > secDist) {
							int colDiff = rowDiff / 2;

							if (pd.getTemplateFacing().isDueEast() &&
									secondary.getColumn() - pd.getTile().getColumn() >= colDiff)
								expectCost++;
						}

						// cases where mobile object must move onto the obstacle
						if (h.getRow() == 2 && (h.getColumn() == 3 ||
								h.getColumn() == 2 && pd.getTemplateFacing().isDueWest() ||
								h.getColumn() == 4 && pd.getTemplateFacing().isDueEast()))
							expectCost += obstacle.getObstacleCost();
						
						// cases where mobile object moves around the obstacle
						else if (h.getRow() == 1 &&
								(h.getColumn() == 1 && pd.getTemplateFacing().isDueWest() ||
								h.getColumn() == 2 && pd.getTemplateFacing().isDueEast() ||
								h.getColumn() == 3 && pd.getTemplateFacing().isDueWest() ||
								h.getColumn() == 4 && pd.getTemplateFacing().isDueEast()))
							expectCost++;
						
						assertEquals( expectCost, pd.getTotalCost(), 0.001 );
					}
				}
			}
		
		obstacle.setOccupying( false );
		move.movementRadius( 3 );
		
		// test with an obstacle (cost 2) that doesn't occupy the tile
		for (Hex[] row : grid.getTiles())
			for (Hex h : row) {
				// distance calculation which considers mobile object turning in place first
				int stDist = calculateTileDistance( grid.createLineHelper( starting, h ));
				int secDist = calculateTileDistance( grid.createLineHelper( secondary, h ));
				int distance = Math.min( stDist, secDist );
				
				if (distance > 3)
					assertFalse( move.canBeOccupied( h, EAST ) || move.canBeOccupied( h, WEST ));
				
				else {
					// top row and some positions in second row can't be reached
					assertSame( h.getRow() > 0 && h.getColumn() > 0 &&
							(h.getRow() != 1 || h.getColumn() != 3) &&
							(distance < 3 || stDist <= secDist),
							move.canBeOccupied( h, EAST ));
					
					assertSame( h.getRow() > 0 && h.getColumn() < row.length - 1 &&
							(h.getRow() != 1 || h.getColumn() != 2) &&
							(distance < 3 || stDist >= secDist),
							move.canBeOccupied( h, WEST ));
					
					for (PathData pd : move.getPathData( h )) {
						int expectCost = distance;
						int rowDiff = Math.abs( pd.getTile().getRow() - starting.getRow() );

						// cases where necessary to move one tile farther and turn in place
						if (stDist < secDist) {
							int colDiff = (rowDiff + 1) / 2;

							if (pd.getTemplateFacing().isDueWest() &&
									pd.getTile().getColumn() - starting.getColumn() >= colDiff)
								expectCost++;
						}

						else if (stDist > secDist) {
							int colDiff = rowDiff / 2;

							if (pd.getTemplateFacing().isDueEast() &&
									secondary.getColumn() - pd.getTile().getColumn() >= colDiff)
								expectCost++;
						}

						// cases where mobile object must move onto the obstacle
						if (h.getRow() == 2 && (h.getColumn() == 3 ||
								h.getColumn() == 2 && pd.getTemplateFacing().isDueWest() ||
								h.getColumn() == 4 && pd.getTemplateFacing().isDueEast()))
							expectCost += obstacle.getObstacleCost();
						
						// cases where mobile object must move around the obstacle
						else if (h.getRow() == 1 &&
								(h.getColumn() == 1 && pd.getTemplateFacing().isDueWest() ||
								h.getColumn() == 2 && pd.getTemplateFacing().isDueEast() ||
								h.getColumn() == 3 && pd.getTemplateFacing().isDueWest() ||
								h.getColumn() == 4 && pd.getTemplateFacing().isDueEast()))
							expectCost++;
						
						assertEquals( expectCost, pd.getTotalCost(), 0.001 );
					}
				}
			}
		
		obstacle.setObstacleCost( move.getImpassableMoveCost() );
		TestMovementEvent noEntry = new TestMovementEvent( 0f, FLAT, true, false );
		
		// test with an obstacle that prevents movement onto its tile, higher total move
		for (int i = 0; i < 2; i++) {
			move.movementRadius( 10 );

			for (Hex[] row : grid.getTiles())
				for (Hex h : row) {
					// distance calculation which considers mobile object turning in place first
					int stDist = calculateTileDistance( grid.createLineHelper( starting, h ));
					int secDist = calculateTileDistance( grid.createLineHelper( secondary, h ));
					int distance = Math.min( stDist, secDist );

					// top row, obstacle tiles and some other positions can't be reached
					assertSame( h.getColumn() > 0 &&
							(h.getRow() != 2 || h.getColumn() != 3 && h.getColumn() != 4),
							move.canBeOccupied( h, EAST ));

					assertSame( h.getColumn() < row.length - 1 &&
							(h.getRow() != 2 || h.getColumn() != 2 && h.getColumn() != 3),
							move.canBeOccupied( h, WEST ));

					for (PathData pd : move.getPathData( h )) {
						int expectCost = distance;
						int rowDiff = Math.abs( pd.getTile().getRow() - starting.getRow() );

						// cases where necessary to move one tile farther and turn in place
						if (stDist < secDist) {
							int colDiff = (rowDiff + 1) / 2;

							if (pd.getTemplateFacing().isDueWest() &&
									pd.getTile().getColumn() - starting.getColumn() >= colDiff)
								expectCost++;
						}

						else if (stDist > secDist) {
							int colDiff = rowDiff / 2;

							if (pd.getTemplateFacing().isDueEast() &&
									secondary.getColumn() - pd.getTile().getColumn() >= colDiff)
								expectCost++;
						}

						// cases where mobile object must move around the obstacle
						if (h.getRow() == 0 && (h.getColumn() > 1 && h.getColumn() < 5 ||
								h.getColumn() == 1 && pd.getTemplateFacing().isDueWest() ||
								h.getColumn() == 5 && pd.getTemplateFacing().isDueEast()) ||
								h.getRow() == 1 && ((h.getColumn() == 1 || h.getColumn() == 3) &&
										pd.getTemplateFacing().isDueWest() || (h.getColumn() == 2 ||
										h.getColumn() == 4) && pd.getTemplateFacing().isDueEast()))
							expectCost++;

						// positions right behind the obstacle, relatively longer route around
						else if (h.getRow() == 1 && (h.getColumn() == 2 || h.getColumn() == 3))
							expectCost += 2;

						assertEquals( expectCost, pd.getTotalCost(), 0.001 );
					}
				}
			
			// same results with an event that prevents entry
			obstacle.setLocation( null );
			northwest.addMovementEvent( noEntry );
		}
	}
	
	// test movement radius with a 2x2 template of squares
	private void testSizeFourSquareTemplate( TileGrid <Square> grid ) {
		Terrain flatTerrain = new Terrain( 1f, FLAT );
		for (Square[] row : grid.getTiles())
			for (Square s : row)
				s.setTerrain( flatTerrain );
		
		Square[][] squares = grid.getTiles();
		TestMobileObject mob = new TestMobileObject( LOW, new FourSquareTemplate() );
		DefaultMovement move = new DefaultMovement( mob, 0 );
		
		Square first = grid.getTileAtRC( 3, 3 );
		Square second = first.getNeighbor( WEST );
		Square third = first.getNeighbor( SOUTH );
		Square fourth = first.getNeighbor( SOUTHWEST );
		
		mob.setPosition( NORTHEAST, first );
		mob.addMovementMode( move );
		
		// test movement radius generation on even terrain with total move varying from one to four
		for (int i = 1; i <= 4; i++) {
			move.movementRadius( i );
			
			for (Square[] row : squares)
				for (Square s : row) {
					
					// distance calculation which considers mobile object turning in place first
					int dist1 = calculateTileDistance( grid.createLineHelper( first, s ));
					int dist2 = calculateTileDistance( grid.createLineHelper( second, s ));
					int dist3 = calculateTileDistance( grid.createLineHelper( third, s ));
					int dist4 = calculateTileDistance( grid.createLineHelper( fourth, s ));
					int distance = Math.min( Math.min( dist1, dist2 ), Math.min( dist3, dist4 ));
					
					// can't occupy first column or last row facing northeast, tiles outside grid
					if (s.getColumn() == 0 || s.getRow() == squares.length - 1 || distance > i)
						assertFalse( move.canBeOccupied( s, NORTHEAST ));
					else if (distance < i || distance == i && dist1 == i)
						assertTrue( move.canBeOccupied( s, NORTHEAST ));
					else
						assertFalse( move.canBeOccupied( s, NORTHEAST ));

					// can't occupy last column or last row facing northwest, tiles outside grid
					if (s.getColumn() == row.length - 1 || s.getRow() == squares.length - 1 ||
							distance > i)
						assertFalse( move.canBeOccupied( s, NORTHWEST ));
					else if (distance < i || distance == i && dist2 == i)
						assertTrue( move.canBeOccupied( s, NORTHWEST ));
					else
						assertFalse( move.canBeOccupied( s, NORTHWEST ));
					
					// can't occupy first column or first row facing southeast, tiles outside grid
					if (s.getColumn() == 0 || s.getRow() == 0 || distance > i)
						assertFalse( move.canBeOccupied( s, SOUTHEAST ));
					else if (distance < i || distance == i && dist3 == i)
						assertTrue( move.canBeOccupied( s, SOUTHEAST ));
					else
						assertFalse( move.canBeOccupied( s, SOUTHEAST ));

					// can't occupy last column or first row facing southwest, tiles outside grid
					if (s.getColumn() == row.length - 1 || s.getRow() == 0 || distance > i)
						assertFalse( move.canBeOccupied( s, SOUTHWEST ));
					else if (distance < i || distance == i && dist4 == i)
						assertTrue( move.canBeOccupied( s, SOUTHWEST ));
					else
						assertFalse( move.canBeOccupied( s, SOUTHWEST ));
					
					// can never occupy with horizontal or vertical facing
					assertFalse( move.canBeOccupied( s, NORTH ));
					assertFalse( move.canBeOccupied( s, EAST ));
					assertFalse( move.canBeOccupied( s, SOUTH ));
					assertFalse( move.canBeOccupied( s, WEST ));
					
					// can have four path data for four facing directions
					if (distance <= i)
						for (PathData pd : move.getPathData( s )) {
							int expectCost = distance;
							int rowDiff = pd.getTile().getRow() - first.getRow();
							int colDiff = pd.getTile().getColumn() - first.getColumn();
							
							// cases where necessary to move one tile farther and turn in place
							if (rowDiff <= 0 && colDiff >= rowDiff - 1 && colDiff <= -rowDiff &&
									pd.getTemplateFacing().isDueSouth() || rowDiff > 0 &&
									colDiff >= -rowDiff && colDiff < rowDiff &&
									pd.getTemplateFacing().isDueNorth() || colDiff >= 0 &&
									rowDiff >= -colDiff && rowDiff <= colDiff + 1 &&
									pd.getTemplateFacing().isDueWest() || colDiff < 0 &&
									rowDiff > colDiff && rowDiff <= -colDiff &&
									pd.getTemplateFacing().isDueEast())
								expectCost++;

							assertEquals( expectCost, pd.getTotalCost(), 0.001 );
						}
				}
		}
		
		TestMovementEvent noLeaving = new TestMovementEvent( 0f, FLAT, false, true );
		first.addMovementEvent( noLeaving );
		
		// test with an event that prevents leaving a tile, ignored in starting tile
		move.movementRadius( 10 );
		for (Square[] row : grid.getTiles())
			for (Square s : row) {

				// can't occupy first column or last row facing northeast, tiles outside grid
				assertSame( s.getColumn() > 0 && s.getRow() < squares.length - 1,
						move.canBeOccupied( s, NORTHEAST ));
				
				// can't occupy last column or last row facing northwest, tiles outside grid
				assertSame( s.getColumn() < row.length - 1 && s.getRow() < squares.length - 1,
						move.canBeOccupied( s, NORTHWEST ));
				
				// can't occupy first column or first row facing southeast, tiles outside grid
				assertSame( s.getColumn() > 0 && s.getRow() > 0,
						move.canBeOccupied( s, SOUTHEAST ));
				
				// can't occupy last column or first row facing southwest, tiles outside grid
				assertSame( s.getColumn() < row.length - 1 && s.getRow() > 0,
						move.canBeOccupied( s, SOUTHWEST ));

				// can never occupy with horizontal or vertical facing
				assertFalse( move.canBeOccupied( s, NORTH ));
				assertFalse( move.canBeOccupied( s, EAST ));
				assertFalse( move.canBeOccupied( s, SOUTH ));
				assertFalse( move.canBeOccupied( s, WEST ));
			}
		
		Square northwest = first.getNeighbor( NORTHWEST );
		northwest.addMovementEvent( noLeaving );
		
		// put blocking event in a neighbor tile, mobile object must go around it
		for (int i = 2; i <= 4; i++) {
			move.movementRadius( i );
			
			for (Square[] row : grid.getTiles())
				for (Square s : row) {

					// distance calculation which considers mobile object turning in place first
					int dist1 = calculateTileDistance( grid.createLineHelper( first, s ));
					int dist2 = calculateTileDistance( grid.createLineHelper( second, s ));
					int dist3 = calculateTileDistance( grid.createLineHelper( third, s ));
					int dist4 = calculateTileDistance( grid.createLineHelper( fourth, s ));
					int distance = Math.min( Math.min( dist1, dist2 ), Math.min( dist3, dist4 ));

					// occupy checks with extra tiles unreachable because of event
					if (s.getColumn() == 0 || s.getRow() == squares.length - 1 || distance > i ||
							i == 3 && s.getRow() == 0 && s.getColumn() < 3 ||
							i == 2 && s.getRow() == 1 && s.getColumn() < 3)
						assertFalse( move.canBeOccupied( s, NORTHEAST ));
					else if (distance < i || distance == i && dist1 == i)
						assertTrue( move.canBeOccupied( s, NORTHEAST ));
					else
						assertFalse( move.canBeOccupied( s, NORTHEAST ));

					if (s.getColumn() == row.length - 1 || s.getRow() == squares.length - 1 ||
							distance > i || i == 3 && s.getRow() == 0 && s.getColumn() < 2 ||
							i == 2 && s.getRow() == 1 && s.getColumn() < 2)
						assertFalse( move.canBeOccupied( s, NORTHWEST ));
					else if (distance < i || distance == i && dist2 == i)
						assertTrue( "distance " + i + ", can occupy: " + s,
								move.canBeOccupied( s, NORTHWEST ));
					else
						assertFalse( move.canBeOccupied( s, NORTHWEST ));
					
					if (s.getColumn() == 0 || s.getRow() == 0 || distance > i ||
							i == 3 && s.getRow() == 1 && s.getColumn() < 3 ||
							i == 2 && s.getRow() == 2 && s.getColumn() < 3)
						assertFalse( move.canBeOccupied( s, SOUTHEAST ));
					else if (distance < i || distance == i && dist3 == i)
						assertTrue( move.canBeOccupied( s, SOUTHEAST ));
					else
						assertFalse( move.canBeOccupied( s, SOUTHEAST ));

					if (s.getColumn() == row.length - 1 || s.getRow() == 0 || distance > i ||
							i == 3 && s.getRow() == 1 && s.getColumn() < 2 ||
							i == 2 && s.getRow() == 2 && s.getColumn() < 2)
						assertFalse( move.canBeOccupied( s, SOUTHWEST ));
					else if (distance < i || distance == i && dist4 == i)
						assertTrue( move.canBeOccupied( s, SOUTHWEST ));
					else
						assertFalse( move.canBeOccupied( s, SOUTHWEST ));
				}
		}
		
		first.removeMovementEvent( noLeaving );
		northwest.removeMovementEvent( noLeaving );
		
		TestObstacle obstacle = new TestObstacle( LOW, 1f );
		obstacle.setPosition( EAST, northwest );
		
		// test with an obstacle in a neighbor
		move.movementRadius( 3 );
		for (Square[] row : grid.getTiles())
			for (Square s : row) {
				// distance calculation which considers mobile object turning in place first
				int dist1 = calculateTileDistance( grid.createLineHelper( first, s ));
				int dist2 = calculateTileDistance( grid.createLineHelper( second, s ));
				int dist3 = calculateTileDistance( grid.createLineHelper( third, s ));
				int dist4 = calculateTileDistance( grid.createLineHelper( fourth, s ));
				int distance = Math.min( Math.min( dist1, dist2 ), Math.min( dist3, dist4 ));
				
				if (distance > 3)
					for (Direction d : Direction.values())
						assertFalse( move.canBeOccupied( s, d ));
				
				else {
					// occupy checks with extra tiles unreachable/unoccupiable because of obstacle
					assertSame( s.getColumn() > 0 && s.getRow() < squares.length - 1 &&
							(s.getRow() > 0 || s.getColumn() > 2) && (s.getRow() < 1 ||
									s.getRow() > 2 || s.getColumn() < 2 || s.getColumn() > 3),
									move.canBeOccupied( s, NORTHEAST ));

					assertSame( s.getColumn() < row.length - 1 &&
							s.getRow() < squares.length - 1 &&
							(s.getRow() > 0 || s.getColumn() > 1) && (s.getRow() < 1 ||
									s.getRow() > 2 || s.getColumn() < 1 || s.getColumn() > 2),
									move.canBeOccupied( s, NORTHWEST ));
					
					assertSame( s.getColumn() > 0 && s.getRow() > 0 &&
							(s.getRow() > 1 || s.getColumn() > 2) && (s.getRow() < 2 ||
									s.getRow() > 3 || s.getColumn() < 2 || s.getColumn() > 3),
									move.canBeOccupied( s, SOUTHEAST ));
					
					assertSame( s.getColumn() < row.length - 1 && s.getRow() > 0 &&
							(s.getRow() > 1 || s.getColumn() > 1) && (s.getRow() < 2 ||
									s.getRow() > 3 || s.getColumn() < 1 || s.getColumn() > 2),
									move.canBeOccupied( s, SOUTHWEST ));

					// can have four path data for four facing directions
					for (PathData pd : move.getPathData( s )) {
						int expectCost = distance;
						int rowDiff = pd.getTile().getRow() - first.getRow();
						int colDiff = pd.getTile().getColumn() - first.getColumn();

						// cases where necessary to move one tile farther and then turn in place
						if (rowDiff <= 0 && colDiff >= rowDiff - 1 && colDiff <= -rowDiff &&
								pd.getTemplateFacing().isDueSouth() ||
								rowDiff > 0 && colDiff >= -rowDiff && colDiff < rowDiff &&
								pd.getTemplateFacing().isDueNorth() ||
								colDiff >= 0 && rowDiff >= -colDiff && rowDiff <= colDiff + 1 &&
								pd.getTemplateFacing().isDueWest() ||
								colDiff < 0 && rowDiff > colDiff && rowDiff <= -colDiff &&
								pd.getTemplateFacing().isDueEast())
							expectCost++;

						// moving over or around the obstacle causes extra cost
						if (pd.getTile().getColumn() < 4 && (pd.getTile().getRow() == 1 &&
								pd.getTemplateFacing().isDueNorth() &&
								(pd.getTile().getColumn() != 3 ||
								pd.getTemplateFacing() != NORTHWEST) ||
								pd.getTile().getRow() == 2 &&
								(pd.getTile().getColumn() != 0 ||
								pd.getTemplateFacing() != NORTHWEST) &&
								(pd.getTile().getColumn() != 1 ||
								pd.getTemplateFacing() != NORTHEAST) &&
								(pd.getTile().getColumn() != 3 ||
								!pd.getTemplateFacing().isDueWest()) ||
								pd.getTile().getRow() == 3 && (pd.getTile().getColumn() == 1 &&
								pd.getTemplateFacing() == SOUTHWEST ||
								pd.getTile().getColumn() == 2 &&
								pd.getTemplateFacing().isDueSouth() ||
								pd.getTile().getColumn() == 3 &&
								pd.getTemplateFacing() == SOUTHEAST)))
							expectCost++;
						
						assertEquals( expectCost, pd.getTotalCost(), 0.001 );
					}
				}
			}
		
		// test with a higher obstacle cost
		obstacle.setObstacleCost( 2f );
		move.movementRadius( 3 );
		
		for (Square[] row : grid.getTiles())
			for (Square s : row) {
				// distance calculation which considers mobile object turning in place first
				int dist1 = calculateTileDistance( grid.createLineHelper( first, s ));
				int dist2 = calculateTileDistance( grid.createLineHelper( second, s ));
				int dist3 = calculateTileDistance( grid.createLineHelper( third, s ));
				int dist4 = calculateTileDistance( grid.createLineHelper( fourth, s ));
				int distance = Math.min( Math.min( dist1, dist2 ), Math.min( dist3, dist4 ));
				
				if (distance > 3)
					for (Direction d : Direction.values())
						assertFalse( move.canBeOccupied( s, d ));
				
				else {
					// occupy checks with extra tiles unreachable/unoccupiable because of obstacle
					assertSame( s.getColumn() > 0 && s.getRow() < squares.length - 1 &&
							(s.getRow() != 0 || s.getColumn() > 2) && (s.getRow() != 1 ||
							s.getColumn() == 1 || s.getColumn() > 3) && (s.getRow() != 2 ||
							s.getColumn() < 2 || s.getColumn() > 3),
							move.canBeOccupied( s, NORTHEAST ));

					assertSame( s.getColumn() < row.length - 1 &&
							s.getRow() < squares.length - 1 &&
							(s.getRow() != 0 || s.getColumn() > 1) && (s.getRow() != 1 ||
							s.getColumn() == 0 || s.getColumn() > 2) && (s.getRow() != 2 ||
							s.getColumn() < 1 || s.getColumn() > 2),
							move.canBeOccupied( s, NORTHWEST ));
					
					assertSame( s.getColumn() > 0 && s.getRow() > 0 &&
							(s.getRow() != 1 || s.getColumn() > 2) && (s.getRow() < 2 ||
									s.getRow() > 3 || s.getColumn() < 2 || s.getColumn() > 3),
									move.canBeOccupied( s, SOUTHEAST ));
					
					assertSame( s.getColumn() < row.length - 1 && s.getRow() > 0 &&
							(s.getRow() != 1 || s.getColumn() > 1) && (s.getRow() < 2 ||
									s.getRow() > 3 || s.getColumn() < 1 || s.getColumn() > 2),
									move.canBeOccupied( s, SOUTHWEST ));

					// can have four path data for four facing directions
					for (PathData pd : move.getPathData( s )) {
						int expectCost = distance;
						int rowDiff = pd.getTile().getRow() - first.getRow();
						int colDiff = pd.getTile().getColumn() - first.getColumn();

						// cases where necessary to move one tile farther and then turn in place
						if (rowDiff <= 0 && colDiff >= rowDiff - 1 && colDiff <= -rowDiff &&
								pd.getTemplateFacing().isDueSouth() ||
								rowDiff > 0 && colDiff >= -rowDiff && colDiff < rowDiff &&
								pd.getTemplateFacing().isDueNorth() ||
								colDiff >= 0 && rowDiff >= -colDiff && rowDiff <= colDiff + 1 &&
								pd.getTemplateFacing().isDueWest() ||
								colDiff < 0 && rowDiff > colDiff && rowDiff <= -colDiff &&
								pd.getTemplateFacing().isDueEast())
							expectCost++;

						// moving onto the obstacle
						if (pd.getTile().getRow() == 2 && (pd.getTile().getColumn() == 1 &&
								pd.getTemplateFacing() == NORTHWEST ||
								pd.getTile().getColumn() == 2 &&
								pd.getTemplateFacing().isDueNorth() ||
								pd.getTile().getColumn() == 3 &&
								pd.getTemplateFacing() == NORTHEAST) ||
								pd.getTile().getRow() == 3 && (pd.getTile().getColumn() == 1 &&
								pd.getTemplateFacing() == SOUTHWEST ||
								pd.getTile().getColumn() == 2 &&
								pd.getTemplateFacing().isDueSouth() ||
								pd.getTile().getColumn() == 3 &&
								pd.getTemplateFacing() == SOUTHEAST))
							expectCost += obstacle.getObstacleCost();

						// moving around the obstacle
						else if (pd.getTile().getRow() == 1 && pd.getTile().getColumn() <= 1 &&
								pd.getTemplateFacing().isDueNorth() ||
								pd.getTile().getRow() == 2 && pd.getTile().getColumn() <= 1 &&
								pd.getTemplateFacing().isDueSouth())
							expectCost++;
						
						assertEquals( expectCost, pd.getTotalCost(), 0.001 );
					}
				}
			}
		
		obstacle.setOccupying( false );
		move.movementRadius( 3 );
		
		// test with an obstacle (cost 2) that doesn't occupy the tile
		for (Square[] row : grid.getTiles())
			for (Square s : row) {
				// distance calculation which considers mobile object turning in place first
				int dist1 = calculateTileDistance( grid.createLineHelper( first, s ));
				int dist2 = calculateTileDistance( grid.createLineHelper( second, s ));
				int dist3 = calculateTileDistance( grid.createLineHelper( third, s ));
				int dist4 = calculateTileDistance( grid.createLineHelper( fourth, s ));
				int distance = Math.min( Math.min( dist1, dist2 ), Math.min( dist3, dist4 ));
				
				if (distance > 3)
					for (Direction d : Direction.values())
						assertFalse( move.canBeOccupied( s, d ));
				
				else {
					// occupy checks with extra tiles unreachable because of obstacle
					assertSame( s.getColumn() > 0 && s.getRow() < squares.length - 1 &&
							(s.getRow() != 0 || s.getColumn() > 2) && (s.getRow() != 1 ||
							s.getColumn() == 1 || s.getColumn() > 3),
							move.canBeOccupied( s, NORTHEAST ));

					assertSame( s.getColumn() < row.length - 1 &&
							s.getRow() < squares.length - 1 &&
							(s.getRow() != 0 || s.getColumn() > 1) && (s.getRow() != 1 ||
							s.getColumn() == 0 || s.getColumn() > 2),
							move.canBeOccupied( s, NORTHWEST ));
					
					assertSame( s.getColumn() > 0 && s.getRow() > 0 &&
							(s.getRow() != 1 || s.getColumn() > 2) && (s.getRow() != 2 ||
							s.getColumn() < 2 || s.getColumn() > 3),
							move.canBeOccupied( s, SOUTHEAST ));
					
					assertSame( s.getColumn() < row.length - 1 && s.getRow() > 0 &&
							(s.getRow() != 1 || s.getColumn() > 1) && (s.getRow() != 2 ||
							s.getColumn() == 0 || s.getColumn() > 2),
							move.canBeOccupied( s, SOUTHWEST ));

					// can have four path data for four facing directions
					for (PathData pd : move.getPathData( s )) {
						int expectCost = distance;
						int rowDiff = pd.getTile().getRow() - first.getRow();
						int colDiff = pd.getTile().getColumn() - first.getColumn();

						// cases where necessary to move one tile farther and then turn in place
						if (rowDiff <= 0 && colDiff >= rowDiff - 1 && colDiff <= -rowDiff &&
								pd.getTemplateFacing().isDueSouth() ||
								rowDiff > 0 && colDiff >= -rowDiff && colDiff < rowDiff &&
								pd.getTemplateFacing().isDueNorth() ||
								colDiff >= 0 && rowDiff >= -colDiff && rowDiff <= colDiff + 1 &&
								pd.getTemplateFacing().isDueWest() ||
								colDiff < 0 && rowDiff > colDiff && rowDiff <= -colDiff &&
								pd.getTemplateFacing().isDueEast())
							expectCost++;

						// moving onto the obstacle
						if (pd.getTile().getRow() == 2 && (pd.getTile().getColumn() == 1 &&
								pd.getTemplateFacing() == NORTHWEST ||
								pd.getTile().getColumn() == 2 &&
								pd.getTemplateFacing().isDueNorth() ||
								pd.getTile().getColumn() == 3 &&
								pd.getTemplateFacing() == NORTHEAST) ||
								pd.getTile().getRow() == 3 && (pd.getTile().getColumn() == 1 &&
								pd.getTemplateFacing() == SOUTHWEST ||
								pd.getTile().getColumn() == 2 &&
								pd.getTemplateFacing().isDueSouth() ||
								pd.getTile().getColumn() == 3 &&
								pd.getTemplateFacing() == SOUTHEAST))
							expectCost += obstacle.getObstacleCost();

						// moving around the obstacle
						else if (pd.getTile().getRow() == 1 && pd.getTile().getColumn() <= 1 &&
								pd.getTemplateFacing().isDueNorth() ||
								pd.getTile().getRow() == 2 && pd.getTile().getColumn() <= 1 &&
								pd.getTemplateFacing().isDueSouth())
							expectCost++;
						
						assertEquals( expectCost, pd.getTotalCost(), 0.001 );
					}
				}
			}
		
		obstacle.setObstacleCost( move.getImpassableMoveCost() );
		TestMovementEvent noEntry = new TestMovementEvent( 0f, FLAT, true, false );
		
		// test with an obstacle that prevents movement onto its tile, higher total move
		for (int i = 0; i < 2; i++) {
			move.movementRadius( 10 );

			for (Square[] row : grid.getTiles())
				for (Square s : row) {
					// distance calculation which considers mobile object turning in place first
					int dist1 = calculateTileDistance( grid.createLineHelper( first, s ));
					int dist2 = calculateTileDistance( grid.createLineHelper( second, s ));
					int dist3 = calculateTileDistance( grid.createLineHelper( third, s ));
					int dist4 = calculateTileDistance( grid.createLineHelper( fourth, s ));
					int distance = Math.min( Math.min( dist1, dist2 ), Math.min( dist3, dist4 ));
					
					if (distance > 3)
						for (Direction d : Direction.values())
							assertFalse( move.canBeOccupied( s, d ));
					
					else {
						// all tiles can be reached, except those where obstacle/event is
						assertSame( s.getColumn() > 0 && s.getRow() < squares.length - 1 &&
								(s.getRow() < 1 || s.getRow() > 2 || s.getColumn() < 2 ||
										s.getColumn() > 3), move.canBeOccupied( s, NORTHEAST ));

						assertSame( s.getColumn() < row.length - 1 &&
								s.getRow() < squares.length - 1 && (s.getRow() < 1 ||
										s.getRow() > 2 || s.getColumn() < 1 || s.getColumn() > 2),
										move.canBeOccupied( s, NORTHWEST ));
						
						assertSame( s.getColumn() > 0 && s.getRow() > 0 &&
								(s.getRow() < 2 || s.getRow() > 3 || s.getColumn() < 2 ||
										s.getColumn() > 3), move.canBeOccupied( s, SOUTHEAST ));
						
						assertSame( s.getColumn() < row.length - 1 && s.getRow() > 0 &&
								(s.getRow() < 2 || s.getRow() > 3 || s.getColumn() < 1 ||
										s.getColumn() > 2), move.canBeOccupied( s, SOUTHWEST ));

						// can have four path data for four facing directions
						for (PathData pd : move.getPathData( s )) {
							int expectCost = distance;
							int rowDiff = pd.getTile().getRow() - first.getRow();
							int colDiff = pd.getTile().getColumn() - first.getColumn();

							// cases where necessary to move one tile farther and then turn in place
							if (rowDiff <= 0 && colDiff >= rowDiff - 1 && colDiff <= -rowDiff &&
									pd.getTemplateFacing().isDueSouth() ||
									rowDiff > 0 && colDiff >= -rowDiff && colDiff < rowDiff &&
									pd.getTemplateFacing().isDueNorth() ||
									colDiff >= 0 && rowDiff >= -colDiff && rowDiff <= colDiff + 1 &&
									pd.getTemplateFacing().isDueWest() ||
									colDiff < 0 && rowDiff > colDiff && rowDiff <= -colDiff &&
									pd.getTemplateFacing().isDueEast())
								expectCost++;

							// moving around the obstacle/event
							if (pd.getTile().getRow() <= 1 && (pd.getTile().getColumn() < 2 ||
									pd.getTile().getColumn() == 2 &&
									pd.getTemplateFacing().isDueEast()) ||
									pd.getTile().getRow() == 2 && pd.getTile().getColumn() < 2 &&
									pd.getTemplateFacing().isDueSouth())
								expectCost++;
							
							assertEquals( expectCost, pd.getTotalCost(), 0.001 );
						}
					}
				}
			
			// same results with an event that prevents entry
			obstacle.setLocation( null );
			northwest.addMovementEvent( noEntry );
		}
	}
	
	// test movement radius with a template of two horizontally adjacent hexes
	private void testSizeSevenHexTemplate( TileGrid <Hex> grid ) {
		Terrain flatTerrain = new Terrain( 1f, FLAT );
		Hex[][] hexes = grid.getTiles();
		
		for (Hex[] row : hexes)
			for (Hex h : row)
				h.setTerrain( flatTerrain );
		
		TestMobileObject mob = new TestMobileObject( LOW, new HexAndNeighborsTemplate() );
		DefaultMovement move = new DefaultMovement( mob, 0 );
		
		Hex starting = grid.getTileAtRC( 3, 3 );
		mob.setPosition( EAST, starting );
		mob.addMovementMode( move );
		
		// test movement radius generation on even terrain with total move varying from one to five
		for (int i = 1; i <= 5; i++) {
			move.movementRadius( i );
			
			for (Hex[] row : hexes)
				for (Hex h : row) {
					int distance = calculateTileDistance( grid.createLineHelper( starting, h ));

					// template prevents occupying border rows or columns
					boolean occupy = (distance <= i && h.getColumn() > 0 &&
							h.getColumn() < row.length - 1 && h.getRow() > 0 &&
							h.getRow() < hexes.length - 1);
					for (Direction d : Direction.values())
						assertSame( occupy, move.canBeOccupied( h, d ));
					
					// has one path data for each hex that can be reached
					PathData[] pd = move.getPathData( h );
					if (distance <= i && occupy) {
						assertEquals( 1, pd.length );
						assertEquals( distance, pd[0].getTotalCost(), 0.001 );
					}
					
					else
						assertEquals( 0, pd.length );
				}
		}
		
		TestMovementEvent noLeaving = new TestMovementEvent( 0f, FLAT, false, true );
		starting.addMovementEvent( noLeaving );
		
		// test with an event that prevents leaving a tile, ignored in starting tile
		move.movementRadius( 10 );
		for (Hex[] row : hexes)
			for (Hex h : row)
				for (Direction d : Direction.values())
					assertSame( h.getColumn() > 0 && h.getColumn() < row.length - 1 &&
							h.getRow() > 0 && h.getRow() < hexes.length - 1,
							move.canBeOccupied( h, d ));
		
		// adjacent tile is also inside the template
		Hex northwest = starting.getNeighbor( NORTHWEST );
		northwest.addMovementEvent( noLeaving );
		
		move.movementRadius( 10 );
		for (Hex[] row : hexes)
			for (Hex h : row)
				for (Direction d : Direction.values())
					assertSame( h.getColumn() > 0 && h.getColumn() < row.length - 1 &&
							h.getRow() > 0 && h.getRow() < hexes.length - 1,
							move.canBeOccupied( h, d ));
		
		// place event outside the initial tiles, mobile object must go around it
		northwest.removeMovementEvent( noLeaving );
		northwest = northwest.getNeighbor( NORTHWEST );
		northwest.addMovementEvent( noLeaving );
		
		for (int i = 2; i <= 3; i++) {
			move.movementRadius( i );
		
			for (Hex[] row : hexes)
				for (Hex h : row) {
					int distance = calculateTileDistance( grid.createLineHelper( starting, h ));
					
					// additional tiles which are unreachable because of the event
					boolean occupy = (distance <= i && h.getColumn() > 0 &&
							h.getColumn() < row.length - 1 && h.getRow() > 0 &&
							h.getRow() < hexes.length - 1 &&
							(h.getRow() > 1 || h.getColumn() > 2));
					for (Direction d : Direction.values())
						assertSame( occupy, move.canBeOccupied( h, d ));
				}
		}
		
		starting.removeMovementEvent( noLeaving );
		northwest.removeMovementEvent( noLeaving );
		
		TestObstacle obstacle = new TestObstacle( LOW, 1f );
		obstacle.setPosition( EAST, northwest );
		
		// test with an obstacle next to the template tiles
		move.movementRadius( 3 );
		for (Hex[] row : hexes)
			for (Hex h : row) {
				int distance = calculateTileDistance( grid.createLineHelper( starting, h ));
				
				if (distance > 3)
					for (Direction d : Direction.values())
						assertFalse( move.canBeOccupied( h, d ));
				
				else {
					// top left can't be reached, some tiles can't be occupied because of obstacle
					boolean occupy = (h.getColumn() > 0 && h.getColumn() < row.length - 1 &&
							h.getRow() > 0 && h.getRow() < hexes.length - 1 &&
							(h.getRow() > 1 || h.getColumn() > 3) &&
							(h.getRow() != 2 || h.getColumn() == 1 || h.getColumn() > 3));
					for (Direction d : Direction.values())
						assertSame( occupy, move.canBeOccupied( h, d ));
					
					boolean onePathData = true;
					for (PathData pd : move.getPathData( h )) {
						assertTrue( onePathData );
						float expectCost = distance;
						
						// cases where mobile object must move onto the obstacle
						if (h.getRow() == 1 && h.getColumn() > 1 && h.getColumn() < 4 ||
								h.getRow() == 2 && h.getColumn() > 1 && h.getColumn() < 4)
							expectCost += obstacle.getObstacleCost();
						
						assertEquals( expectCost, pd.getTotalCost(), 0.001 );
						onePathData = false;
					}
				}
			}
		
		// test with a higher obstacle cost
		obstacle.setObstacleCost( 2f );
		move.movementRadius( 3 );
		
		for (Hex[] row : hexes)
			for (Hex h : row) {
				int distance = calculateTileDistance( grid.createLineHelper( starting, h ));

				if (distance > 3)
					for (Direction d : Direction.values())
						assertFalse( move.canBeOccupied( h, d ));
				
				else {
					// top left can't be reached, some tiles can't be occupied because of obstacle
					boolean occupy = (h.getColumn() > 0 && h.getColumn() < row.length - 1 &&
							h.getRow() > 0 && h.getRow() < hexes.length - 1 &&
							(h.getRow() > 1 || h.getColumn() > 3) &&
							(h.getRow() != 2 || h.getColumn() == 1 || h.getColumn() > 3));
					for (Direction d : Direction.values())
						assertSame( occupy, move.canBeOccupied( h, d ));
					
					boolean onePathData = true;
					for (PathData pd : move.getPathData( h )) {
						assertTrue( onePathData );
						float expectCost = distance;
						
						// one case where mobile object must move onto the obstacle
						if (h.getRow() == 2 && h.getColumn() == 3)
							expectCost += obstacle.getObstacleCost();
						
						assertEquals( expectCost, pd.getTotalCost(), 0.001 );
						onePathData = false;
					}
				}
			}
		
		obstacle.setOccupying( false );
		move.movementRadius( 3 );
		
		// test with an obstacle (cost 2) that doesn't occupy the tile
		for (Hex[] row : hexes)
			for (Hex h : row) {
				int distance = calculateTileDistance( grid.createLineHelper( starting, h ));

				if (distance > 3)
					for (Direction d : Direction.values())
						assertFalse( move.canBeOccupied( h, d ));
				
				else {
					// top left and some tiles around the obstacle can't be reached
					boolean occupy = (h.getColumn() > 0 && h.getColumn() < row.length - 1 &&
							h.getRow() > 0 && h.getRow() < hexes.length - 1 &&
							(h.getRow() > 1 || h.getColumn() > 3) &&
							(h.getRow() != 2 || h.getColumn() == 1 || h.getColumn() > 2));
					for (Direction d : Direction.values())
						assertSame( occupy, move.canBeOccupied( h, d ));
					
					boolean onePathData = true;
					for (PathData pd : move.getPathData( h )) {
						assertTrue( onePathData );
						float expectCost = distance;
						
						// one case where mobile object must move onto the obstacle
						if (h.getRow() == 2 && h.getColumn() == 3)
							expectCost += obstacle.getObstacleCost();
						
						assertEquals( expectCost, pd.getTotalCost(), 0.001 );
						onePathData = false;
					}
				}
			}
		
		obstacle.setObstacleCost( move.getImpassableMoveCost() );
		TestMovementEvent noEntry = new TestMovementEvent( 0f, FLAT, true, false );
		
		// test with an obstacle that prevents movement onto its tile, higher total move
		for (int i = 0; i < 2; i++) {
			move.movementRadius( 10 );
	
			for (Hex[] row : hexes)
				for (Hex h : row) {
					int distance = calculateTileDistance( grid.createLineHelper( starting, h ));

					// top left and tiles around obstacle can't be reached
					boolean occupy = (h.getColumn() > 0 && h.getColumn() < row.length - 1 &&
							h.getRow() > 0 && h.getRow() < hexes.length - 1 &&
							(h.getRow() > 1 || h.getColumn() > 3) &&
							(h.getRow() != 2 || h.getColumn() == 1 || h.getColumn() > 3));
					for (Direction d : Direction.values())
						assertSame( occupy, move.canBeOccupied( h, d ));

					boolean onePathData = true;
					for (PathData pd : move.getPathData( h )) {
						assertTrue( onePathData );
						assertEquals( distance, pd.getTotalCost(), 0.001 );
						onePathData = false;
					}
				}
			
			// same results with an event that prevents entry
			obstacle.setLocation( null );
			northwest.addMovementEvent( noEntry );
		}
		
		northwest.removeMovementEvent( noEntry );
		northwest.removeMovementEvent( noEntry );
		
		// test with higher cost terrain
		Terrain difficultTerrain = new Terrain( 2f, FLAT );
		grid.getTileAtRC( 1, 2 ).setTerrain( difficultTerrain );
		grid.getTileAtRC( 1, 3 ).setTerrain( difficultTerrain );
		grid.getTileAtRC( 2, 2 ).setTerrain( difficultTerrain );
		
		move.movementRadius( 4 );
		for (Hex[] row : hexes)
			for (Hex h : row) {
				float expectCost = calculateTileDistance( grid.createLineHelper( starting, h ));
				
				// higher cost when moving onto difficult terrain, no effect once already on it
				if (h.getRow() == 1 && h.getColumn() < 5 || h.getRow() == 2 && h.getColumn() < 5 ||
						h.getRow() == 3 && h.getColumn() < 3)
					expectCost += difficultTerrain.getCost() - flatTerrain.getCost();
				
				PathData[] data = move.getPathData( h );
				if (h.getRow() == 0 || h.getRow() == hexes.length - 1 || h.getColumn() == 0 ||
						h.getColumn() == row.length - 1)
					assertEquals( 0, data.length );
				
				else {
					assertEquals( 1, data.length );
					assertEquals( expectCost, data[0].getTotalCost(), 0.001 );
				}
			}
		
		grid.getTileAtRC( 1, 2 ).setTerrain( flatTerrain );
		grid.getTileAtRC( 1, 3 ).setTerrain( flatTerrain );
		grid.getTileAtRC( 2, 2 ).setTerrain( flatTerrain );
		
		// event that causes risk which is added once regardless of how many tiles move onto event
		TestMovementEvent event = new TestMovementEvent( 1f, FLAT, false, false, true );
		event.setExecuteOnce( true );
		
		grid.getTileAtRC( 1, 2 ).addMovementEvent( event );
		grid.getTileAtRC( 1, 3 ).addMovementEvent( event );
		grid.getTileAtRC( 2, 2 ).addMovementEvent( event );
		
		move.movementRadius( 4 );
		for (Hex[] row : hexes)
			for (Hex h : row) {
				float expectRisk = 0f;
				
				// risk when moving onto event, no effect once already on it
				if (h.getRow() == 1 && h.getColumn() < 5 || h.getRow() == 2 && h.getColumn() < 5 ||
						h.getRow() == 3 && h.getColumn() < 3)
					expectRisk += event.getRisk();
				
				PathData[] data = move.getPathData( h );
				if (h.getRow() == 0 || h.getRow() == hexes.length - 1 || h.getColumn() == 0 ||
						h.getColumn() == row.length - 1)
					assertEquals( 0, data.length );
				
				else {
					assertEquals( 1, data.length );
					assertEquals( expectRisk, data[0].getRisk(), 0.001 );
				}
			}
	}

	// test movement radius with a template of two horizontally adjacent hexes
	private void testSizeNineSquareTemplate( TileGrid <Square> grid ) {
		Terrain flatTerrain = new Terrain( 1f, FLAT );
		Square[][] squares = grid.getTiles();
		
		for (Square[] row : squares)
			for (Square s : row)
				s.setTerrain( flatTerrain );
		
		TestMobileObject mob = new TestMobileObject( LOW, new SquareAndNeighborsTemplate() );
		DefaultMovement move = new DefaultMovement( mob, 0 );
		
		Square starting = grid.getTileAtRC( 3, 3 );
		mob.setPosition( EAST, starting );
		mob.addMovementMode( move );
		
		// test movement radius generation on even terrain with total move varying from one to three
		for (int i = 1; i <= 3; i++) {
			move.movementRadius( i );
			
			for (Square[] row : squares)
				for (Square s : row) {
					int distance = calculateTileDistance( grid.createLineHelper( starting, s ));
	
					// template prevents occupying border rows or columns
					boolean occupy = (distance <= i && s.getColumn() > 0 &&
							s.getColumn() < row.length - 1 && s.getRow() > 0 &&
							s.getRow() < squares.length - 1);
					for (Direction d : Direction.values())
						assertSame( occupy, move.canBeOccupied( s, d ));
					
					// has one path data for each square that can be reached
					PathData[] pd = move.getPathData( s );
					if (distance <= i && occupy) {
						assertEquals( 1, pd.length );
						assertEquals( distance, pd[0].getTotalCost(), 0.001 );
					}
					
					else
						assertEquals( 0, pd.length );
				}
		}
		
		TestMovementEvent noLeaving = new TestMovementEvent( 0f, FLAT, false, true );
		starting.addMovementEvent( noLeaving );
		
		// test with an event that prevents leaving a tile, ignored in starting tile
		move.movementRadius( 10 );
		for (Square[] row : squares)
			for (Square s : row)
				for (Direction d : Direction.values())
					assertSame( s.getColumn() > 0 && s.getColumn() < row.length - 1 &&
							s.getRow() > 0 && s.getRow() < squares.length - 1,
							move.canBeOccupied( s, d ));
		
		// adjacent tile is also inside the template
		Square northwest = starting.getNeighbor( NORTHWEST );
		northwest.addMovementEvent( noLeaving );
		
		move.movementRadius( 10 );
		for (Square[] row : squares)
			for (Square s : row)
				for (Direction d : Direction.values())
					assertSame( s.getColumn() > 0 && s.getColumn() < row.length - 1 &&
							s.getRow() > 0 && s.getRow() < squares.length - 1,
							move.canBeOccupied( s, d ));
		
		// place event outside the initial tiles, mobile object must go around it
		northwest.removeMovementEvent( noLeaving );
		northwest = northwest.getNeighbor( NORTHWEST );
		northwest.addMovementEvent( noLeaving );
		
		for (int i = 2; i <= 3; i++) {
			move.movementRadius( i );
		
			for (Square[] row : squares)
				for (Square s : row) {
					int distance = calculateTileDistance( grid.createLineHelper( starting, s ));
					
					// the event's tile is unreachable, because event stops in adjacent tiles
					boolean occupy = (distance <= i && s.getColumn() > 0 &&
							s.getColumn() < row.length - 1 && s.getRow() > 0 &&
							s.getRow() < squares.length - 1 &&
							(s.getRow() != 1 || s.getColumn() != 1));
					for (Direction d : Direction.values())
						assertSame( occupy, move.canBeOccupied( s, d ));
				}
		}
		
		starting.removeMovementEvent( noLeaving );
		northwest.removeMovementEvent( noLeaving );
		
		TestObstacle obstacle = new TestObstacle( LOW, 1f );
		obstacle.setPosition( EAST, northwest );
		
		// test with an obstacle next to the template tiles
		move.movementRadius( 3 );
		for (Square[] row : squares)
			for (Square s : row) {
				int distance = calculateTileDistance( grid.createLineHelper( starting, s ));
				
				if (distance > 3)
					for (Direction d : Direction.values())
						assertFalse( move.canBeOccupied( s, d ));
				
				else {
					// some tiles can't be occupied because of obstacle
					boolean occupy = (s.getColumn() > 0 && s.getColumn() < row.length - 1 &&
							s.getRow() > 0 && s.getRow() < squares.length - 1 &&
							(s.getRow() > 2 || s.getColumn() > 2));
					for (Direction d : Direction.values())
						assertSame( occupy, move.canBeOccupied( s, d ));
					
					boolean onePathData = true;
					for (PathData pd : move.getPathData( s )) {
						assertTrue( onePathData );
						float expectCost = distance;
						
						// cases where mobile object must move onto the obstacle
						if (s.getColumn() < 3 && (s.getRow() == 1 || s.getRow() == 2))
							expectCost += obstacle.getObstacleCost();
						
						assertEquals( expectCost, pd.getTotalCost(), 0.001 );
						onePathData = false;
					}
				}
			}
		
		// test with a higher obstacle cost
		obstacle.setObstacleCost( 2f );
		move.movementRadius( 3 );
		
		for (Square[] row : squares)
			for (Square s : row) {
				int distance = calculateTileDistance( grid.createLineHelper( starting, s ));
	
				if (distance > 3)
					for (Direction d : Direction.values())
						assertFalse( move.canBeOccupied( s, d ));
				
				else {
					// some tiles can't be occupied because of obstacle
					boolean occupy = (s.getColumn() > 0 && s.getColumn() < row.length - 1 &&
							s.getRow() > 0 && s.getRow() < squares.length - 1 &&
							(s.getRow() > 2 || s.getColumn() > 2));
					for (Direction d : Direction.values())
						assertSame( occupy, move.canBeOccupied( s, d ));
					
					boolean onePathData = true;
					for (PathData pd : move.getPathData( s )) {
						assertTrue( onePathData );
						float expectCost = distance;
						
						// one case where mobile object must move onto the obstacle
						if (s.getRow() == 2 && s.getColumn() == 2)
							expectCost += obstacle.getObstacleCost();
						
						assertEquals( expectCost, pd.getTotalCost(), 0.001 );
						onePathData = false;
					}
				}
			}
		
		obstacle.setOccupying( false );
		move.movementRadius( 3 );
		
		// test with an obstacle (cost 2) that doesn't occupy the tile
		for (Square[] row : squares)
			for (Square s : row) {
				int distance = calculateTileDistance( grid.createLineHelper( starting, s ));
	
				if (distance > 3)
					for (Direction d : Direction.values())
						assertFalse( move.canBeOccupied( s, d ));
				
				else {
					// extra move cost from obstacle makes some tiles unreachable
					boolean occupy = (s.getColumn() > 0 && s.getColumn() < row.length - 1 &&
							s.getRow() > 0 && s.getRow() < squares.length - 1 &&
							(s.getRow() > 1 || s.getColumn() > 2) &&
							(s.getRow() != 2 || s.getColumn() > 1));
					for (Direction d : Direction.values())
						assertSame( occupy, move.canBeOccupied( s, d ));
					
					boolean onePathData = true;
					for (PathData pd : move.getPathData( s )) {
						assertTrue( onePathData );
						float expectCost = distance;
						
						// one case where mobile object must move onto the obstacle
						if (s.getRow() == 2 && s.getColumn() == 2)
							expectCost += obstacle.getObstacleCost();
						
						assertEquals( expectCost, pd.getTotalCost(), 0.001 );
						onePathData = false;
					}
				}
			}
		
		obstacle.setObstacleCost( move.getImpassableMoveCost() );
		TestMovementEvent noEntry = new TestMovementEvent( 0f, FLAT, true, false );
		
		// test with an obstacle that prevents movement onto its tile, higher total move
		for (int i = 0; i < 2; i++) {
			move.movementRadius( 10 );
	
			for (Square[] row : squares)
				for (Square s : row) {
					int distance = calculateTileDistance( grid.createLineHelper( starting, s ));
	
					// tiles around obstacle can't be reached
					boolean occupy = (s.getColumn() > 0 && s.getColumn() < row.length - 1 &&
							s.getRow() > 0 && s.getRow() < squares.length - 1 &&
							(s.getRow() > 2 || s.getColumn() > 2));
					for (Direction d : Direction.values())
						assertSame( occupy, move.canBeOccupied( s, d ));
	
					boolean onePathData = true;
					for (PathData pd : move.getPathData( s )) {
						assertTrue( onePathData );
						assertEquals( distance, pd.getTotalCost(), 0.001 );
						onePathData = false;
					}
				}
			
			// same results with an event that prevents entry
			obstacle.setLocation( null );
			northwest.addMovementEvent( noEntry );
		}
		
		northwest.removeMovementEvent( noEntry );
		northwest.removeMovementEvent( noEntry );
		
		// test with higher cost terrain
		Terrain difficultTerrain = new Terrain( 2f, FLAT );
		grid.getTileAtRC( 1, 1 ).setTerrain( difficultTerrain );
		grid.getTileAtRC( 1, 2 ).setTerrain( difficultTerrain );
		grid.getTileAtRC( 2, 1 ).setTerrain( difficultTerrain );
		
		move.movementRadius( 3 );
		for (Square[] row : squares)
			for (Square s : row) {
				float expectCost = calculateTileDistance( grid.createLineHelper( starting, s ));
				
				// higher cost when moving onto difficult terrain, no effect once already on it
				if (s.getColumn() < 4 && (s.getRow() == 1 || s.getRow() == 2) ||
						s.getRow() == 3 && s.getColumn() < 3)
					expectCost += difficultTerrain.getCost() - flatTerrain.getCost();
				
				PathData[] data = move.getPathData( s );
				if (s.getRow() == 0 || s.getRow() == squares.length - 1 || s.getColumn() == 0 ||
						s.getColumn() == row.length - 1)
					assertEquals( 0, data.length );
				
				else {
					assertEquals( 1, data.length );
					assertEquals( expectCost, data[0].getTotalCost(), 0.001 );
				}
			}

		grid.getTileAtRC( 1, 1 ).setTerrain( flatTerrain );
		grid.getTileAtRC( 1, 2 ).setTerrain( flatTerrain );
		grid.getTileAtRC( 2, 1 ).setTerrain( flatTerrain );
		
		// event that causes risk which is added once regardless of how many tiles move onto event
		TestMovementEvent event = new TestMovementEvent( 1f, FLAT, false, false, true );
		event.setExecuteOnce( true );
		
		grid.getTileAtRC( 1, 1 ).addMovementEvent( event );
		grid.getTileAtRC( 1, 2 ).addMovementEvent( event );
		grid.getTileAtRC( 2, 1 ).addMovementEvent( event );
		
		move.movementRadius( 3 );
		for (Square[] row : squares)
			for (Square s : row) {
				float expectRisk = 0f;
				
				// risk when moving onto event, no effect once already on it
				if (s.getColumn() < 4 && (s.getRow() == 1 || s.getRow() == 2) ||
						s.getRow() == 3 && s.getColumn() < 3)
					expectRisk += event.getRisk();
				
				PathData[] data = move.getPathData( s );
				if (s.getRow() == 0 || s.getRow() == squares.length - 1 || s.getColumn() == 0 ||
						s.getColumn() == row.length - 1)
					assertEquals( 0, data.length );
				
				else {
					assertEquals( 1, data.length );
					assertEquals( expectRisk, data[0].getRisk(), 0.001 );
				}
			}
	}

	// tests various cases of movement path creation and execution
	private <T extends Tile> void testMovementPath( TileGrid <T> grid ) {
		Terrain flatTerrain = new Terrain( 1f, FLAT );
		Tile[][] tiles = grid.getTiles();
		
		for (Tile[] row : tiles)
			for (Tile t : row)
				t.setTerrain( flatTerrain );
		
		T starting = grid.getTileAtRC( 0, 0 );
		T destination = grid.getTileAtRC( 6, 6 );
		int pathLen = calculateTileDistance( grid.createLineHelper( starting, destination ));
		MobileObject mob = new TestMobileObject( LOW, new SingleTileTemplate() );
		DefaultMovement move = new DefaultMovement( mob, 100 );
		
		// test without any movement data in existence
		assertArrayEquals( new Tile[0], move.getMovementPath( destination, EAST ));
		assertNull( move.executeMovementPath( destination, EAST ));
		
		mob.setPosition( EAST, starting );
		move.movementRadius( 10 );
		
		// test moving to starting tile
		assertArrayEquals( new Tile[0], move.getMovementPath( starting, EAST ));
		assertArrayEquals( new Tile[0], move.executeMovementPath( starting, EAST ));
		assertArrayEquals( new Tile[0], move.executeMovementPath( starting, WEST ));
		
		assertSame( starting, mob.getLocation() );
		assertSame( WEST, mob.getTemplateFacing() );
		assertSame( WEST, mob.getFacing() );
		
		// test with an existing path
		Tile[] path = move.getMovementPath( destination, EAST );
		assertNotNull( path );
		assertEquals( pathLen, path.length );
		
		Tile previous = starting;
		Point startCenter = starting.getCenter();
		Point dstCenter = destination.getCenter();
		double previousSrcDist = 0, previousDstDist = startCenter.distance( dstCenter );
		
		for (Tile t : path) {
			assertSame( previous.getNeighbor( previous.getDirection( t )), t );
			
			Point newCenter = t.getCenter();
			double newSrcDist = newCenter.distance( startCenter );
			double newDstDist = newCenter.distance( dstCenter );
			assertTrue( newSrcDist > previousSrcDist );
			assertTrue( newDstDist < previousDstDist );
			
			previousSrcDist = newSrcDist;
			previousDstDist = newDstDist;
			previous = t;
		}
		
		// test with an obstacle blocking the destination
		TestObstacle obstacle = new TestObstacle( LOW, move.getImpassableMoveCost() );
		obstacle.setPosition( WEST, destination );
		
		move.movementRadius( move.getImpassableMoveCost() );
		assertArrayEquals( new Tile[0], move.getMovementPath( destination, EAST ));
		assertNull( move.executeMovementPath( destination, EAST ));

		obstacle.setLocation( null );
		
		// test with an event that prevents leaving initial location
		TestMovementEvent noLeave = new TestMovementEvent( 0f, FLAT, false, true );
		noLeave.setShowMovementNotAllowed( false );
		starting.addMovementEvent( noLeave );
		
		move.movementRadius( 10 );
		path = move.getMovementPath( destination, EAST );
		assertEquals( pathLen, path.length );
		Tile[] execPath = move.executeMovementPath( destination, EAST );
		assertArrayEquals( path, execPath );
		
		// event in initial location can't prevent leaving, but is executed
		assertSame( mob, noLeave.getLeavingObject() );
		assertSame( starting, noLeave.getLeftTile() );
		assertSame( flatTerrain.getHeight(), noLeave.getLeavingHeight() );
		
		mob.setPosition( EAST, starting );
		noLeave.resetLeavingData();
		
		// increase movement height to move over the event
		TestMovementModifier lowMove = new TestMovementModifier( LOW );
		for (Tile[] row : tiles)
			for (Tile t : row)
				lowMove.setMovementHeight( t, LOW );
		
		move.addMovementModifier( lowMove );
		move.movementRadius( 10 );
		assertEquals( pathLen, move.executeMovementPath( destination, WEST ).length );
		assertSame( destination, mob.getLocation() );
		assertSame( WEST, mob.getTemplateFacing() );
		assertSame( WEST, mob.getFacing() );
		
		// event was ignored because of increased movement height
		assertNull( noLeave.getLeavingObject() );
		assertNull( noLeave.getLeftTile() );
		assertNull( noLeave.getLeavingHeight() );
		
		mob.setPosition( EAST, starting );
		move.removeMovementModifier( lowMove );
		starting.removeMovementEvent( noLeave );
		
		// add events to tiles between starting tile and destination
		TestMovementEvent moveTracker = new TestMovementEvent( 0f, FLAT, false, false );
		for (Tile[] row : tiles)
			for (Tile t : row)
				t.addMovementEvent( moveTracker );
		
		move.movementRadius( 10 );
		assertEquals( pathLen, move.executeMovementPath( destination, EAST ).length );
		assertEquals( pathLen, moveTracker.getEnteringCounter() );	// tiles in path
		assertEquals( pathLen, moveTracker.getLeavingCounter() );	// start, path, not destination
		
		mob.setLocation( starting );
		moveTracker.resetEnteringData();
		moveTracker.resetLeavingData();
		
		// increase movement height with a modifier to move over events
		move.addMovementModifier( lowMove );
		move.movementRadius( 10 );
		assertEquals( pathLen, move.executeMovementPath( destination, EAST ).length );
		assertEquals( 0, moveTracker.getEnteringCounter() );
		assertEquals( 0, moveTracker.getLeavingCounter() );
		
		mob.setLocation( starting );
		move.removeMovementModifier( lowMove );
		TestMovementEvent riskEvent = new TestMovementEvent( 1f, FLAT, false, false );
		
		previous = destination;
		Tile current = destination.getNeighbor( NORTHWEST ), next = null;
		
		while ((next = current.getNeighbor( NORTHWEST )) != null) {
			previous = current;
			current = next;
		}
		
		// make mobile object move along a longer path in order to avoid a risk event
		current = previous;
		do
			current.addMovementEvent( riskEvent );
		while ((current = current.getNeighbor( WEST )) != null);
		
		move.movementRadius( 10 );
		assertEquals( pathLen + 1, move.executeMovementPath( destination, EAST ).length );
		assertEquals( 0, riskEvent.getEnteringCounter() );
		
		// use lower total move so that unit has to move across risk event
		mob.setLocation( starting );
		move.movementRadius( pathLen );
		assertEquals( pathLen, move.executeMovementPath( destination, EAST ).length );
		assertEquals( 1, riskEvent.getEnteringCounter() );
		
		mob.setLocation( starting );
		for (Tile[] row : tiles)
			for (Tile t : row)
				t.removeMovementEvent( moveTracker );
		
		current = previous;
		do
			current.removeMovementEvent( riskEvent );
		while ((current = current.getNeighbor( WEST )) != null);
		
		// test with interrupting event in the destination tile
		TestMovementEvent noEntry = new TestMovementEvent( 0f, FLAT, true, false );
		noEntry.setShowMovementNotAllowed( false );
		
		destination.addMovementEvent( noEntry );
		for (Tile t : destination.getNeighbors())
			t.addMovementEvent( moveTracker );
		
		moveTracker.resetEnteringData();
		moveTracker.resetLeavingData();
		
		move.movementRadius( 10 );
		assertEquals( pathLen - 1, move.executeMovementPath( destination, EAST ).length );
		assertEquals( 1, noEntry.getEnteringCounter() );
		
		Tile inNeighbor = null;
		for (Tile t : destination.getNeighbors())		// ends up in one of the neighboring tiles
			if (t == mob.getLocation())
				inNeighbor = t;
		
		assertNotNull( inNeighbor );
		assertEquals( 2, moveTracker.getEnteringCounter() );
		assertEquals( 1, moveTracker.getLeavingCounter() );		// entered, left, entered again
		
		mob.setLocation( starting );
		noEntry.resetEnteringData();
		moveTracker.resetEnteringData();
		moveTracker.resetLeavingData();

		// add obstacles to make the mobile object move along a specific route
		HashMap <Tile, TestObstacle> obstacles = new HashMap <>();
		Tile northwest = destination.getNeighbor( NORTHWEST );
		
		for (Tile t : destination.getNeighbors())
			if (t != northwest) {		// leave only destination's northwest neighbor accessible
				obstacle = new TestObstacle( LOW, move.getImpassableMoveCost() );
				obstacle.setPosition( EAST, t );
				obstacles.put( t, obstacle );
			}
		
		Tile nwnw = northwest.getNeighbor( NORTHWEST );
		for (Tile t : northwest.getNeighbors())		// same for previous step from northwest
			if (!obstacles.containsKey( t ) && t != destination && t != nwnw) {
				obstacle = new TestObstacle( LOW, move.getImpassableMoveCost() );
				obstacle.setPosition( EAST, t );
				obstacles.put( t, obstacle );
			}
		
		for (Tile t : destination.getNeighbors())
			t.removeMovementEvent( moveTracker );
		northwest.addMovementEvent( moveTracker );
		
		move.movementRadius( 10 );
		assertEquals( pathLen - 1, move.executeMovementPath( destination, EAST ).length );
		assertEquals( 1, noEntry.getEnteringCounter() );
		assertSame( northwest, mob.getLocation() );
		assertSame( SOUTHEAST, mob.getFacing() );
		
		assertEquals( 2, moveTracker.getEnteringCounter() );
		assertEquals( 1, moveTracker.getLeavingCounter() );		// entered, left, entered again
		moveTracker.resetEnteringData();
		moveTracker.resetLeavingData();
		
		// test with a different obstacle positioning
		current = tiles[5][ tiles[5].length - 1 ];
		obstacle = obstacles.get( current );
		obstacle.setLocation( northwest );
		obstacles.put( northwest, obstacle );
		
		previous = current.getNeighbor( NORTHWEST );
		obstacle = obstacles.get( previous );
		obstacle.setLocation( nwnw );
		obstacles.put( nwnw, obstacle );
		
		northwest.removeMovementEvent( moveTracker );
		current.addMovementEvent( moveTracker );

		noEntry.resetEnteringData();
		mob.setLocation( starting );
		move.movementRadius( 10 );
		
		// needs to go around the obstacle, so path is one longer, but doesn't enter destination
		assertEquals( pathLen, move.executeMovementPath( destination, EAST ).length );
		assertEquals( 1, noEntry.getEnteringCounter() );
		assertSame( current, mob.getLocation() );
		assertSame( current.getDirection( destination ), mob.getFacing() );
		
		assertEquals( 2, moveTracker.getEnteringCounter() );
		assertEquals( 1, moveTracker.getLeavingCounter() );		// entered, left, entered again
		moveTracker.resetEnteringData();
		moveTracker.resetLeavingData();
		
		// place a movement-interrupting leave event before the destination
		current.addMovementEvent( noLeave );
		noLeave.resetLeavingData();
		noEntry.resetEnteringData();
		
		mob.setLocation( starting );
		move.movementRadius( 10 );
		assertEquals( pathLen, move.executeMovementPath( destination, EAST ).length );
		
		// same path and end tile as before, but interrupted for a different reason
		assertSame( current, mob.getLocation() );
		assertSame( current.getDirection( destination ), mob.getFacing() );
		assertEquals( 1, noLeave.getLeavingCounter() );
		assertEquals( 0, noEntry.getEnteringCounter() );		// interrupted before this event
		
		// after failing to leave, entry events are executed again
		assertEquals( 2, moveTracker.getEnteringCounter() );
		assertEquals( 1, moveTracker.getLeavingCounter() );		// entered, left, entered, sort of
		moveTracker.resetEnteringData();
		moveTracker.resetLeavingData();
		
		// place the no entry event in the tile before the destination
		current.addMovementEvent( noEntry );
		noEntry.resetEnteringData();
		noLeave.resetLeavingData();
		mob.setLocation( starting );
		move.movementRadius( 10 );
		
		// one longer than direct distance, can't access last two tiles
		assertEquals( pathLen - 1, move.executeMovementPath( destination, EAST ).length );
		assertSame( previous, mob.getLocation() );
		assertSame( previous.getDirection( current ), mob.getFacing() );
		
		assertEquals( 1, noEntry.getEnteringCounter() );
		assertEquals( 1, noLeave.getLeavingCounter() );		// executed after enter event interrupt
		assertEquals( 1, moveTracker.getEnteringCounter() );
		assertEquals( 1, moveTracker.getLeavingCounter() );
		
		// add an obstacle which allows movement across it, but no occupying
		TestObstacle moveOver = new TestObstacle( FLAT, 0f, true, new SingleTileTemplate() );
		moveOver.setPosition( EAST, current );
		
		// allow movement over previous tiles and interrupt on entry to destination
		current.removeMovementEvent( noEntry );
		current.removeMovementEvent( noLeave );
		
		noEntry.resetEnteringData();
		noLeave.resetLeavingData();
		moveTracker.resetEnteringData();
		moveTracker.resetLeavingData();
		
		mob.setLocation( starting );
		move.movementRadius( 10 );
		
		// one longer than shortest path, last two tiles are not accessible
		assertEquals( pathLen - 1, move.executeMovementPath( destination, EAST ).length );
		assertSame( previous, mob.getLocation() );
		assertSame( previous.getDirection( current ), mob.getFacing() );
		
		assertEquals( 1, noEntry.getEnteringCounter() );
		assertEquals( 2, moveTracker.getEnteringCounter() );	// moves across this twice
		assertEquals( 2, moveTracker.getLeavingCounter() );
	}
	
	// gets a default movement mode's list of path data for tiles to be searched
	@SuppressWarnings( "unchecked" )
	private LinkedList <PathData> getPathDataList( DefaultMovement move, String fieldName )
			throws NoSuchFieldException, IllegalAccessException {
		Field field = DefaultMovement.class.getDeclaredField( fieldName );
		field.setAccessible( true );
		return (LinkedList <PathData>)field.get( move );		
	}
	
	// calculates distance between two tiles using a line helper
	private int calculateTileDistance( LineHelper <?> helper ) {
		if (helper.getSource() == helper.getTarget())
			return 0;

		// don't use a path, since it contains equal tiles, instead count steps
		int pathLength = 0;
		while (!helper.targetReached()) {
			helper.nextTiles();
			pathLength++;
		}

		return pathLength;		
	}
	
	// gets a tile's neighbor, casting it to the same type as the tile
	@SuppressWarnings( "unchecked" )
	private <T extends Tile> T getNeighbor( T tile, Direction direction ) {
		return (T)tile.getNeighbor( direction );
	}
}
