package fi.grimripper.loww.tiles;

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
import static fi.grimripper.loww.Height.VERY_HIGH;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.junit.Test;

import fi.grimripper.loww.Direction;
import fi.grimripper.loww.Height;
import fi.grimripper.loww.Point;
import fi.grimripper.loww.test.TestObstacle;
import fi.grimripper.loww.tiles.FilledSquareGrid;
import fi.grimripper.loww.tiles.Obstacle;
import fi.grimripper.loww.tiles.Square;
import fi.grimripper.loww.tiles.Terrain;
import fi.grimripper.loww.tiles.TileGrid;
import fi.grimripper.loww.tiles.TileGrid.LineHelper;

public class FilledSquareGridTest {

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
	public void testFilledSquareGrid() {
		FilledSquareGrid grid = new FilledSquareGrid( 1, 5, 8 );

		// some special case tests about intersecting
		assertFalse( FilledSquareGrid.intersects( grid.getTileAtRC( 0, 0 ), 0, 0, 0, 0 ));
		Square zeroSquare = new Square( 0, 0, 0, 0, 0 );
		assertFalse( FilledSquareGrid.intersects( zeroSquare, -1, -1, 1, 1 ));
		
		try {
			Field xField = Square.class.getDeclaredField( "xpoints" );
			xField.setAccessible( true );
			double[] xpoints = (double[])xField.get( zeroSquare );
			
			xpoints[ NORTHEAST.squarePoint() ] = 0.5;
			xpoints[ SOUTHEAST.squarePoint() ] = 0.5;
			
			xField.set( zeroSquare, xpoints );
			assertTrue( FilledSquareGrid.intersects( zeroSquare, 0, -0.5, 0.5, 0.5 ));
			
			Field yField = Square.class.getDeclaredField( "ypoints" );
			yField.setAccessible( true );
			double[] ypoints = (double[])yField.get( zeroSquare );
			
			Arrays.fill( xpoints, 0.0 );
			ypoints[ SOUTHWEST.squarePoint() ] = 0.5;
			ypoints[ SOUTHEAST.squarePoint() ] = 0.5;
			
			xField.set( zeroSquare, xpoints );
			yField.set( zeroSquare, ypoints );
			assertTrue( FilledSquareGrid.intersects( zeroSquare, -0.5, 0, 0.5, 0.5 ));
			
		} catch (NoSuchFieldException nsfx) {
			nsfx.printStackTrace();
			fail();
			
		} catch (IllegalAccessException iax) {
			iax.printStackTrace();
			fail();
		}
		
		// test the grid's basic properties
		assertEquals( grid.getTotalWidth(), 8 );
		assertEquals( grid.getTotalHeight(), 5 );
		assertEquals( grid.getTileCount(), 40 );
		
		// try to get squares outside the grid and assert none are returned
		assertNull( grid.getTileAtXY( -1, 0 ));
		assertNull( grid.getTileAtXY( 0, -1 ));
		assertNull( grid.getTileAtXY( 8, 0 ));
		assertNull( grid.getTileAtXY( 0, 5 ));
		
		assertNull( grid.getTileAtRC( -1, 0 ));
		assertNull( grid.getTileAtRC( 0, -1 ));
		assertNull( grid.getTileAtRC( 5, 0 ));
		assertNull( grid.getTileAtRC( 0, 8 ));
		
		// for size one squares, row and column are same as y and x
		for (int i = 0; i < 5; i++)
			for (int j = 0; j < 8; j++)
				assertSame( grid.getTileAtRC( i, j ), grid.getTileAtXY( j, i ));
		
		// no northern neighbors on the first row and no southern ones on the last row
		for (int i = 0; i < 8; i++) {
			Square square = grid.getTileAtRC( 0, i );
			assertNotNull( square );
			
			assertNull( square.getNeighbor( NORTH ));
			assertNull( square.getNeighbor( NORTHEAST ));
			assertNull( square.getNeighbor( NORTHWEST ));
			
			square = grid.getTileAtRC( 4, i );
			assertNotNull( square );
			
			assertNull( square.getNeighbor( SOUTH ));
			assertNull( square.getNeighbor( SOUTHEAST ));
			assertNull( square.getNeighbor( SOUTHWEST ));
		}
		
		// no western neighbors in the first column and no eastern ones in the last column
		for (int i = 0; i < 5; i++) {
			Square square = grid.getTileAtRC( i, 0 );
			assertNotNull( square );
			
			assertNull( square.getNeighbor( WEST ));
			assertNull( square.getNeighbor( NORTHWEST ));
			assertNull( square.getNeighbor( SOUTHWEST ));
			
			square = grid.getTileAtRC( i, 7 );
			assertNotNull( square );
			
			assertNull( square.getNeighbor( EAST ));
			assertNull( square.getNeighbor( NORTHEAST ));
			assertNull( square.getNeighbor( SOUTHEAST ));
		}
		
		// all squares below first row have a neighbor to the north
		for (int i = 1; i < 5; i++)
			for (int j = 0; j < 8; j++)
				assertSame( grid.getTileAtRC( i - 1, j ),
						grid.getTileAtRC( i, j ).getNeighbor( NORTH ));

		// all squares left from last column have a neighbor to the east
		for (int i = 0; i < 5; i++)
			for (int j = 0; j < 7; j++)
				assertSame( grid.getTileAtRC( i, j + 1 ),
						grid.getTileAtRC( i, j ).getNeighbor( EAST ));
		
		// all squares above last row have a neighbor to the south
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 8; j++)
				assertSame( grid.getTileAtRC( i + 1, j ),
						grid.getTileAtRC( i, j ).getNeighbor( SOUTH ));
		
		// all squares right from first column have a neighbor to the west
		for (int i = 0; i < 5; i++)
			for (int j = 1; j < 8; j++)
				assertSame( grid.getTileAtRC( i, j - 1 ),
						grid.getTileAtRC( i, j ).getNeighbor( WEST ));
		
		// all squares except first row and column have a neighbor to the northwest
		for (int i = 1; i < 5; i++)
			for (int j = 1; j < 8; j++)
				assertSame( grid.getTileAtRC( i - 1, j - 1 ),
						grid.getTileAtRC( i, j ).getNeighbor( NORTHWEST ));

		// all squares except first row and last column have a neighbor to the northeast
		for (int i = 1; i < 5; i++)
			for (int j = 0; j < 7; j++)
				assertSame( grid.getTileAtRC( i - 1, j + 1 ),
						grid.getTileAtRC( i, j ).getNeighbor( NORTHEAST ));

		// all squares except last row and column have a neighbor to the southeast
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 7; j++)
				assertSame( grid.getTileAtRC( i + 1, j + 1 ),
						grid.getTileAtRC( i, j ).getNeighbor( SOUTHEAST ));
		
		// all squares except last row and first column have a neighbor to the southwest
		for (int i = 0; i < 4; i++)
			for (int j = 1; j < 8; j++)
				assertSame( grid.getTileAtRC( i + 1, j - 1 ),
						grid.getTileAtRC( i, j ).getNeighbor( SOUTHWEST ));
		
		Square[][] squares = grid.getTiles();
		for (int i = 0; i < squares.length; i++)
			for (int j = 0; j < squares[i].length; j++)
				assertSame( squares[i][j], grid.getTileAtRC( i, j ));
		
		// test that squares are in a copy array
		squares[2][3] = null;
		assertNotNull( grid.getTileAtRC( 2, 3 ));
	}
	
	@Test
	public void testSquareLineHelper() {
		
		FilledSquareGrid grid = new FilledSquareGrid( 1, 20, 20 );
		LineHelper <Square> helper = grid.createLineHelper(
				grid.getTileAtRC( 0, 0 ), grid.getTileAtRC( 0, 0 ));
		
		// test line that goes nowhere
		assertTrue( helper.targetReached() );
		assertTrue( helper.pathEnds() );
		
		// test a direct line towards each compass direction
		testDirectLine( grid, grid.getTileAtRC( 0, 0 ), grid.getTileAtRC( 0, 19 ), EAST );
		testDirectLine( grid, grid.getTileAtRC( 0, 19 ), grid.getTileAtRC( 0, 15 ), WEST );
		testDirectLine( grid, grid.getTileAtRC( 15, 6 ), grid.getTileAtRC( 1, 6 ), NORTH );
		testDirectLine( grid, grid.getTileAtRC( 2, 10 ), grid.getTileAtRC( 18, 10 ), SOUTH );
		
		testDirectLine( grid, grid.getTileAtRC( 0, 0 ), grid.getTileAtRC( 19, 19 ), SOUTHEAST );
		testDirectLine( grid, grid.getTileAtRC( 1, 18 ), grid.getTileAtRC( 18, 1 ), SOUTHWEST );
		testDirectLine( grid, grid.getTileAtRC( 15, 0 ), grid.getTileAtRC( 8, 7 ), NORTHEAST );
		testDirectLine( grid, grid.getTileAtRC( 16, 12 ), grid.getTileAtRC( 4, 0 ), NORTHWEST );
		
		// test indirect lines between squares on grid edges
		for (int i = 1; i < 19; i++) {
			testIndirectLine( grid, grid.getTileAtRC( 0, 0 ), grid.getTileAtRC( i, 19 ));
			testIndirectLine( grid, grid.getTileAtRC( 0, 0 ), grid.getTileAtRC( 19, i ));
			
			testIndirectLine( grid, grid.getTileAtRC( 0, 19 ), grid.getTileAtRC( i, 0 ));
			testIndirectLine( grid, grid.getTileAtRC( 0, 19 ), grid.getTileAtRC( 19, i ));
			
			testIndirectLine( grid, grid.getTileAtRC( 19, 0 ), grid.getTileAtRC( i, 19 ));
			testIndirectLine( grid, grid.getTileAtRC( 19, 0 ), grid.getTileAtRC( 0, i ));
			
			testIndirectLine( grid, grid.getTileAtRC( 19, 19 ), grid.getTileAtRC( i, 0 ));
			testIndirectLine( grid, grid.getTileAtRC( 19, 19 ), grid.getTileAtRC( 0, i ));
		}
		
		// test miscellaneous direct paths which end in target
		testDirectPath( grid, 0, 0, 0, 0, false );
		testDirectPath( grid, 8, 5, 8, 8, false );
		testDirectPath( grid, 10, 7, 18, 7, false );
		testDirectPath( grid, 13, 12, 13, 3, false );
		testDirectPath( grid, 17, 3, 12, 3, false );
		testDirectPath( grid, 18, 3, 10, 11, false );
		testDirectPath( grid, 9, 12, 16, 19, false );
		testDirectPath( grid, 2, 16, 18, 0, false );
		testDirectPath( grid, 16, 15, 10, 9, false );

		// test miscellaneous direct paths which continue past target
		testDirectPath( grid, 5, 7, 5, 10, true );
		testDirectPath( grid, 7, 9, 15, 9, true );
		testDirectPath( grid, 10, 14, 10, 6, true );
		testDirectPath( grid, 14, 5, 9, 5, true );
		testDirectPath( grid, 15, 5, 7, 13, true );
		testDirectPath( grid, 6, 14, 11, 19, true );
		testDirectPath( grid, 2, 18, 18, 2, true );
		testDirectPath( grid, 13, 17, 7, 11, true );
		
		// test miscellaneous indirect paths that end in target
		for (int i = 3; i < 16; i++) {
			testIndirectPath( grid, 7, 10, i, 15, false );
			testIndirectPath( grid, 10, 5, i, 1, false );
			testIndirectPath( grid, 14, 11, 18, i, false );
			testIndirectPath( grid, 8, 15, 5, i, false );
		}
		
		// test miscellaneous indirect paths that continue past target
		testIndirectPath( grid, 9, 11, 6, 9, true );
		testIndirectPath( grid, 9, 11, 6, 10, true );
		testIndirectPath( grid, 9, 11, 6, 12, true );
		testIndirectPath( grid, 9, 11, 6, 13, true );
		
		testIndirectPath( grid, 11, 8, 9, 11, true );
		testIndirectPath( grid, 11, 8, 10, 11, true );
		testIndirectPath( grid, 11, 8, 12, 11, true );
		testIndirectPath( grid, 11, 8, 13, 11, true );
		
		testIndirectPath( grid, 5, 8, 8, 6, true );
		testIndirectPath( grid, 5, 8, 8, 7, true );
		testIndirectPath( grid, 5, 8, 8, 9, true );
		testIndirectPath( grid, 5, 8, 8, 10, true );
		
		testIndirectPath( grid, 16, 15, 14, 12, true );
		testIndirectPath( grid, 16, 15, 15, 12, true );
		testIndirectPath( grid, 16, 15, 17, 12, true );
		testIndirectPath( grid, 16, 15, 18, 12, true );
		
		testIndirectPath( grid, 8, 14, 5, 16, true );
		testIndirectPath( grid, 8, 14, 7, 16, true );
		testIndirectPath( grid, 8, 14, 9, 16, true );
		testIndirectPath( grid, 8, 14, 11, 16, true );
		
		testIndirectPath( grid, 4, 6, 6, 3, true );
		testIndirectPath( grid, 4, 6, 6, 5, true );
		testIndirectPath( grid, 4, 6, 6, 7, true );
		testIndirectPath( grid, 4, 6, 6, 9, true );
		
		testIndirectPath( grid, 15, 15, 13, 12, true );
		testIndirectPath( grid, 15, 15, 13, 14, true );
		testIndirectPath( grid, 15, 15, 13, 16, true );
		testIndirectPath( grid, 15, 15, 13, 18, true );
		
		testIndirectPath( grid, 12, 7, 9, 5, true );
		testIndirectPath( grid, 12, 7, 11, 5, true );
		testIndirectPath( grid, 12, 7, 13, 5, true );
		testIndirectPath( grid, 12, 7, 15, 5, true );
	}
	
	@Test( expected = NullPointerException.class )
	public void testSquareLineHelperNullPointerException1() {
		FilledSquareGrid grid = new FilledSquareGrid( 1, 1, 1 );
		grid.createLineHelper( null, grid.getTileAtRC( 0, 0 ));
	}

	@Test( expected = NullPointerException.class )
	public void testSquareLineHelperNullPointerException2() {
		FilledSquareGrid grid = new FilledSquareGrid( 1, 1, 1 );
		grid.createLineHelper( grid.getTileAtRC( 0, 0 ), null );
	}

	@Test( expected = NullPointerException.class )
	public void testSquareLineHelperNullPointerException3() {
		FilledSquareGrid grid = new FilledSquareGrid( 1, 1, 1 );
		grid.createLineHelper( grid.getTileAtRC( 0, 0 ),
				grid.getTileAtRC( 0, 0 )).getDistanceToLine( (Square)null );
	}

	@Test( expected = NullPointerException.class )
	public void testSquareLineHelperNullPointerException4() {
		FilledSquareGrid grid = new FilledSquareGrid( 1, 1, 1 );
		grid.createLineHelper( grid.getTileAtRC( 0, 0 ),
				grid.getTileAtRC( 0, 0 )).getDistanceToLine( (Point)null );
	}
	
	@Test( expected = NullPointerException.class )
	public void testSquareLineHelperNullPointerException5() {
		FilledSquareGrid grid = new FilledSquareGrid( 1, 1, 1 );
		grid.createLineHelper( grid.getTileAtRC( 0, 0 ),
				grid.getTileAtRC( 0, 0 )).getDistanceToSource( (Square)null );
	}

	@Test( expected = NullPointerException.class )
	public void testSquareLineHelperNullPointerException6() {
		FilledSquareGrid grid = new FilledSquareGrid( 1, 1, 1 );
		grid.createLineHelper( grid.getTileAtRC( 0, 0 ),
				grid.getTileAtRC( 0, 0 )).getDistanceToSource( (Point)null );
	}
	
	@Test
	public void testLineOfSight() {
		TileGrid <Square> grid = new FilledSquareGrid( 1, 10, 10 );
		Square square = grid.getTileAtRC( 3, 3 );
		Terrain testTerrain = new Terrain( 1f );
		
		for (Square[] row : grid.getTiles())
			for (Square s : row)
				s.setTerrain( testTerrain );
		
		// always has line of sight to itself
		LineHelper <Square> helper = grid.createLineHelper( square, square );
		assertTrue( TileGrid.hasLineOfSight( helper, null ));
		
		Obstacle obstacle = new TestObstacle( LOW );
		helper = grid.createLineHelper( square, square );
		assertTrue( TileGrid.hasLineOfSight( helper, obstacle ));
		
		obstacle.setPosition( EAST, square );
		helper = grid.createLineHelper( square, square );
		assertTrue( TileGrid.hasLineOfSight( helper, null ));
		helper = grid.createLineHelper( square, square );
		assertTrue( TileGrid.hasLineOfSight( helper, obstacle ));
		
		// test with the obstacle in the line's source and target squares
		helper = grid.createLineHelper( grid.getTileAtRC( 5, 4 ), square );
		assertTrue( TileGrid.hasLineOfSight( helper, obstacle ));
		
		helper = grid.createLineHelper( square, grid.getTileAtRC( 5, 4 ));
		assertTrue( TileGrid.hasLineOfSight( helper, obstacle ));

		Square source = grid.getTileAtRC( 3, 2 );
		Square dst = grid.getTileAtRC( 3, 4 );
		Obstacle lowObstacle = new TestObstacle( LOW );
		lowObstacle.setPosition( EAST, dst );
		
		// obstacle in target square doesn't block line of sight to that square
		helper = grid.createLineHelper( source, dst );
		assertTrue( TileGrid.hasLineOfSight( helper, obstacle ));
		
		// obstacle doesn't block its own line of sight, but blocks another's
		helper = grid.createLineHelper( source, dst );
		assertFalse( TileGrid.hasLineOfSight( helper, lowObstacle ));
		
		// low obstacle blocks the line of sight if a high obstacle
		helper = grid.createLineHelper( source, dst );
		Obstacle highObstacle = new TestObstacle( HIGH );
		highObstacle.setPosition( EAST, source );
		assertFalse( TileGrid.hasLineOfSight( helper, highObstacle ));
		
		// low obstacle doesn't block the line of sight of a very high obstacle
		helper = grid.createLineHelper( dst, source );
		Obstacle veryHighObstacle = new TestObstacle( VERY_HIGH );
		veryHighObstacle.setPosition( EAST, dst );
		assertTrue( TileGrid.hasLineOfSight( helper, veryHighObstacle ));
		
		// with longer distance, low obstacle blocks the line of sight of a high obstacle
		source = grid.getTileAtRC( 3, 1 );
		dst = grid.getTileAtRC( 2, 4 );
		helper = grid.createLineHelper( source, dst );
		highObstacle.setLocation( source );
		assertFalse( TileGrid.hasLineOfSight( helper, highObstacle ));
		
		// when low obstacle is moved closer to the high obstacle, it no longer blocks view
		helper = grid.createLineHelper( source, dst );
		obstacle.setLocation( grid.getTileAtRC( 3, 2 ));
		assertTrue( TileGrid.hasLineOfSight( helper, highObstacle ));
		
		// the low obstacle still blocks the line of sight of another low obstacle
		lowObstacle.setPosition( EAST, dst );
		helper = grid.createLineHelper( dst, source );
		assertFalse( TileGrid.hasLineOfSight( helper, lowObstacle ));
		
		// height ratios for different height combinations
		double[] heightRatios = { 1, 0, 0, 0, 0, 1, 1, 1 / 2.0, 1 / 3.0, 1 / 4.0, 1, 1, 1, 2 / 3.0,
			2 / 4.0, 1, 1, 1, 1, 3 / 4.0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 1, 1, 1,
			1 / 2.0, 1 / 3.0, 1, 1, 1, 1, 2 / 3.0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 1 / 2.0, 0, 0, 0,
			0, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1 / 2.0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 1 / 3.0, 0, 0, 0,
			0, 2 / 3.0, 1 / 2.0, 0, 0, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 1 / 4.0, 0,
			0, 0, 0, 1 / 2.0, 1 / 3.0, 0, 0, 0, 3 / 4.0, 2 / 3.0, 1 / 2.0, 0, 0, 1, 1, 1, 1, 1 };
		
		for (int i = 0; i < heightRatios.length; i++)
			if (heightRatios[i] > 0 && heightRatios[i] < 1)
				heightRatios[i] += 0.001;
		
		int fromRow = 5;
		int fromCol = 4;
		
		// tests for seeing over an obstacle to another obstacle
		Square losToSquare = grid.getTileAtRC( fromRow, fromCol - 4 );
		Square losOverSquare = grid.getTileAtRC( fromRow, fromCol - 2 );
		Square losFromSquare = grid.getTileAtRC( fromRow, fromCol );
		TestObstacle losForObstacle = new TestObstacle( LOW );
		TestObstacle losOverObstacle = new TestObstacle( LOW );
		TestObstacle losToObstacle = new TestObstacle( LOW );
		
		losForObstacle.setPosition( WEST, losFromSquare );
		losOverObstacle.setPosition( EAST, losOverSquare );
		losToObstacle.setPosition( EAST, losToSquare );
		
		helper = grid.createLineHelper( losFromSquare, losToSquare );
		assertFalse( TileGrid.hasLineOfSight( helper, losForObstacle ));
		
		helper = grid.createLineHelper( losFromSquare, losToSquare );
		losOverObstacle.setBlocksLineOfSight( false );
		assertTrue( TileGrid.hasLineOfSight( helper, losForObstacle ));
		losOverObstacle.setBlocksLineOfSight( true );
		
		// the obstacle doesn't block its own line of sight
		helper = grid.createLineHelper( losFromSquare, losToSquare );
		assertTrue( TileGrid.hasLineOfSight( helper, losOverObstacle ));
		
		// the obstacle also doesn't block if it's the line of sight's target
		helper = grid.createLineHelper( losFromSquare, losToSquare );
		assertTrue( TileGrid.hasLineOfSight( helper, losForObstacle, losOverObstacle ));
		
		losOverObstacle.setLocation( null );
		
		// test obstacle height ratios at a distance of four, obstacle in the middle
		Height[] heights = { FLAT, LOW, HIGH, VERY_HIGH, BLOCKING };
		for (Height fromH : heights) {
			losForObstacle.setHeight( fromH );
			
			// line of sight to ground
			helper = grid.createLineHelper( losFromSquare, losToSquare );
			assertTrue( TileGrid.hasLineOfSight( helper, losForObstacle ));
			
			// line of sight to target obstacle without intervening obstacle
			helper = grid.createLineHelper( losFromSquare, losToSquare );
			assertTrue( TileGrid.hasLineOfSight( helper, losForObstacle, losToObstacle ));
			
			losOverObstacle.setLocation( losOverSquare );
			
			// test different heights for the intervening obstacle
			for (Height overH : heights) {
				losOverObstacle.setHeight( overH );
			
				// test to ground
				helper = grid.createLineHelper( losFromSquare, losToSquare );
				assertSame( fromH != FLAT && (fromH.compareTo( HIGH ) <= 0 && overH == FLAT ||
						fromH.compareTo( HIGH ) > 0 && overH.compareTo( LOW ) <= 0),
						TileGrid.hasLineOfSight( helper, losForObstacle ));
				
				// test to target obstacles of varying height
				for (Height toH : heights) {
					losToObstacle.setHeight( toH );
				
					helper = grid.createLineHelper( losFromSquare, losToSquare );
					assertSame( toH.compareTo( overH ) > 0 && fromH.compareTo( overH ) >= 0 ||
							toH.ordinal() > overH.ordinal() + 1 &&
							fromH.ordinal() >= overH.ordinal() - 1 ||
							fromH.compareTo( overH ) > 0 && toH.compareTo( overH ) >= 0 ||
							fromH.ordinal() > overH.ordinal() + 1 &&
							toH.ordinal() >= overH.ordinal() - 1,
							TileGrid.hasLineOfSight( helper, losForObstacle, losToObstacle ));
					
					// test line of sight from the source square's neighbors
					for (int i = 0; i < 9; i++) {
						helper = grid.createLineHelper( grid.getTileAtRC(
								fromRow + i / 3 - 1, fromCol + i % 3 - 1 ), losToSquare );
						
						int index = heights.length * heights.length * (fromH.ordinal() -
								heights[0].ordinal()) + heights.length * (overH.ordinal() -
										heights[0].ordinal()) + toH.ordinal() -
										heights[0].ordinal();
						double distRatio = losOverSquare.getCenter().distance(
								helper.getSource().getCenter() ) / helper.getLineLength();
						distRatio = fromH.compareTo( toH ) < 0 ? distRatio : 1 - distRatio;
						
						assertSame( distRatio > heightRatios[ index ],
								TileGrid.hasLineOfSight( helper, losForObstacle, losToObstacle ));
					}
				}
			}
			
			// reset for next iteration
			losToObstacle.setHeight( LOW );
			losOverObstacle.setLocation( null );
		}

		fromCol = 6;
		Square losOverSquare2 = losFromSquare;
		losFromSquare = grid.getTileAtRC( fromRow, fromCol );
		losForObstacle.setLocation( losFromSquare );
		Obstacle losOverObstacle2 = new TestObstacle( LOW );
		losOverObstacle2.setFacing( EAST );
		
		// test obstacle height ratios at a distance of six
		for (Height fromH : heights) {
			losForObstacle.setHeight( fromH );
			
			// line of sight to ground
			helper = grid.createLineHelper( losFromSquare, losToSquare );
			assertTrue( TileGrid.hasLineOfSight( helper, losForObstacle ));
			
			// line of sight to target obstacle without intervening obstacle
			helper = grid.createLineHelper( losFromSquare, losToSquare );
			assertTrue( TileGrid.hasLineOfSight( helper, losForObstacle, losToObstacle ));
			
			// place obstacle at two thirds of distance from the source to the target
			losOverObstacle.setLocation( losOverSquare );
			
			// test different heights for the intervening obstacle
			for (Height overH : heights) {
				losOverObstacle.setHeight( overH );
				
				// test to ground
				helper = grid.createLineHelper( losFromSquare, losToSquare );
				assertSame( fromH != FLAT && overH == FLAT || fromH == BLOCKING && overH == LOW,
						TileGrid.hasLineOfSight( helper, losForObstacle ));
				
				// test to target obstacles of varying height
				for (Height toH : heights) {
					losToObstacle.setHeight( toH );
					
					helper = grid.createLineHelper( losFromSquare, losToSquare );
					assertSame( toH.compareTo( overH ) > 0 && fromH.compareTo( overH ) >= 0 ||
							toH.ordinal() > overH.ordinal() + 1 &&
							fromH.ordinal() >= overH.ordinal() - 1 ||
							fromH.compareTo( overH ) > 0 && toH.compareTo( overH ) >= 0 ||
							toH.compareTo( overH ) > 0 &&
							toH.ordinal() - overH.ordinal() == overH.ordinal() - fromH.ordinal() ||
							fromH == BLOCKING && toH == FLAT && overH.compareTo( LOW ) <= 0,
							TileGrid.hasLineOfSight( helper, losForObstacle, losToObstacle ));
					
					// test line of sight from the source square's neighbors
					for (int i = 0; i < 9; i++) {
						helper = grid.createLineHelper( grid.getTileAtRC(
								fromRow + i / 3 - 1, fromCol + i % 3 - 1 ), losToSquare );
						
						int index = heights.length * heights.length * (fromH.ordinal() -
								heights[0].ordinal()) + heights.length * (overH.ordinal() -
										heights[0].ordinal()) + toH.ordinal() -
										heights[0].ordinal();
						double distRatio = losOverSquare.getCenter().distance(
								helper.getSource().getCenter() ) / helper.getLineLength();
						distRatio = fromH.compareTo( toH ) < 0 ? distRatio : 1 - distRatio;
						
						assertSame( distRatio > heightRatios[ index ],
								TileGrid.hasLineOfSight( helper, losForObstacle, losToObstacle ));
					}
				}
			}
			
			// place obstacle at one third of distance from the source to the target
			losOverObstacle.setLocation( null );
			losOverObstacle2.setLocation( losOverSquare2 );
			
			// test different heights for the intervening obstacle
			for (Height overH : heights) {
				losOverObstacle2.setHeight( overH );
				
				// test to ground
				helper = grid.createLineHelper( losFromSquare, losToSquare );
				assertSame( fromH.ordinal() > overH.ordinal() + 1 ||
						fromH.compareTo( overH ) > 0 && overH.compareTo( LOW ) <= 0,
						TileGrid.hasLineOfSight( helper, losForObstacle ));
				
				// test to target obstacles of varying height
				for (Height toH : heights) {
					losToObstacle.setHeight( toH );
					
					helper = grid.createLineHelper( losFromSquare, losToSquare );
					assertSame( toH.compareTo( overH ) > 0 && fromH.compareTo( overH ) >= 0 ||
							fromH.compareTo( overH ) > 0 && toH.compareTo( overH ) >= 0 ||
							fromH.ordinal() > overH.ordinal() + 1 &&
							toH.ordinal() >= overH.ordinal() - 1 || fromH.compareTo( overH ) > 0 &&
							fromH.ordinal() - overH.ordinal() == overH.ordinal() - toH.ordinal() ||
							fromH == FLAT && toH == BLOCKING && overH.compareTo( LOW ) <= 0,
							TileGrid.hasLineOfSight( helper, losForObstacle, losToObstacle ));

					boolean[] crossesObstacle = {
						false, false, true, true, true, true, false, false, true };
					
					// test line of sight from the source square's neighbors
					// from some, line of sight isn't across the square with the obstacle
					for (int i = 0; i < 9; i++) {
						helper = grid.createLineHelper ( grid.getTileAtRC(
								fromRow + i / 3 - 1, fromCol + i % 3 - 1 ), losToSquare );
						
						int index = heights.length * heights.length * (fromH.ordinal() -
								heights[0].ordinal()) + heights.length * (overH.ordinal() -
										heights[0].ordinal()) + toH.ordinal() -
										heights[0].ordinal();
						double distRatio = losOverSquare2.getCenter().distance(
								helper.getSource().getCenter() ) / helper.getLineLength();
						distRatio = fromH.compareTo( toH ) < 0 ? distRatio : 1 - distRatio;
						
						assertSame( distRatio > heightRatios[ index ] || !crossesObstacle[i],
								TileGrid.hasLineOfSight( helper, losForObstacle, losToObstacle ));
					}
				}
			}
			
			// reset for next iteration
			losOverObstacle2.setLocation( null );
			losToObstacle.setHeight( LOW );
		}
		
		fromCol = 8;
		Square losOverSquare3 = losFromSquare;
		losFromSquare = grid.getTileAtRC( fromRow, fromCol );
		losForObstacle.setLocation( losFromSquare );
		Obstacle losOverObstacle3 = new TestObstacle( LOW );
		losOverObstacle3.setFacing( EAST );
		
		// test obstacle height ratios at a distance of eight
		for (Height fromH : heights) {
			losForObstacle.setHeight( fromH );
			
			// line of sight to ground
			helper = grid.createLineHelper( losFromSquare, losToSquare );
			assertTrue( TileGrid.hasLineOfSight( helper, losForObstacle ));
			
			// line of sight to target obstacle without intervening obstacle
			helper = grid.createLineHelper( losFromSquare, losToSquare );
			assertTrue( TileGrid.hasLineOfSight( helper, losForObstacle, losToObstacle ));
			
			// place obstacle at three quarters of distance from the source to the target
			losOverObstacle.setLocation( losOverSquare );
			
			// test different heights for the intervening obstacle
			for (Height overH : heights) {
				losOverObstacle.setHeight( overH );
				
				// test to ground
				helper = grid.createLineHelper( losFromSquare, losToSquare );
				assertSame( fromH != FLAT && overH == FLAT,
						TileGrid.hasLineOfSight( helper, losForObstacle ));
				
				// test to target obstacles of varying height
				for (Height toH : heights) {
					losToObstacle.setHeight( toH );
					
					helper = grid.createLineHelper( losFromSquare, losToSquare );
					assertSame( toH.compareTo( overH ) > 0 && (fromH != FLAT || toH != BLOCKING ||
							overH != VERY_HIGH) || fromH.compareTo( overH ) > 0 &&
							toH.compareTo( overH ) >= 0,
							TileGrid.hasLineOfSight( helper, losForObstacle, losToObstacle ));
					
					// test line of sight from the source square's neighbors
					for (int i = 0; i < 9; i++) {
						helper = grid.createLineHelper( grid.getTileAtRC(
								fromRow + i / 3 - 1, fromCol + i % 3 - 1 ), losToSquare );
						
						int index = heights.length * heights.length * (fromH.ordinal() -
								heights[0].ordinal()) + heights.length * (overH.ordinal() -
										heights[0].ordinal()) + toH.ordinal() -
										heights[0].ordinal();
						double distRatio = losOverSquare.getCenter().distance(
								helper.getSource().getCenter() ) / helper.getLineLength();
						distRatio = fromH.compareTo( toH ) < 0 ? distRatio : 1 - distRatio;
						
						assertSame( distRatio > heightRatios[ index ],
								TileGrid.hasLineOfSight( helper, losForObstacle, losToObstacle ));
					}
				}
			}
			
			// place obstacle at half the distance from the source to the target
			losOverObstacle.setLocation( null );
			losOverObstacle2.setLocation( losOverSquare2 );
			
			// test different heights for the intervening obstacle
			for (Height overH2 : heights) {
				losOverObstacle2.setHeight( overH2 );
				
				// test to ground
				helper = grid.createLineHelper( losFromSquare, losToSquare );
				assertSame( fromH != FLAT && (fromH.compareTo( HIGH ) <= 0 && overH2 == FLAT ||
						fromH.compareTo( HIGH ) > 0 && overH2.compareTo( LOW ) <= 0),
						TileGrid.hasLineOfSight( helper, losForObstacle ));
				
				// test to target obstacles of varying height
				for (Height toH : heights) {
					losToObstacle.setHeight( toH );
					
					helper = grid.createLineHelper( losFromSquare, losToSquare );
					assertSame( toH.compareTo( overH2 ) > 0 && fromH.compareTo( overH2 ) >= 0 ||
							toH.ordinal() > overH2.ordinal() + 1 &&
							fromH.ordinal() >= overH2.ordinal() - 1 ||
							fromH.compareTo( overH2 ) > 0 && toH.compareTo( overH2 ) >= 0 ||
							fromH.ordinal() > overH2.ordinal() + 1 &&
							toH.ordinal() >= overH2.ordinal() - 1,
							TileGrid.hasLineOfSight( helper, losForObstacle, losToObstacle ));

					// test line of sight from the source square's neighbors
					for (int i = 0; i < 9; i++) {
						helper = grid.createLineHelper( grid.getTileAtRC(
								fromRow + i / 3 - 1, fromCol + i % 3 - 1 ), losToSquare );
						
						int index = heights.length * heights.length * (fromH.ordinal() -
								heights[0].ordinal()) + heights.length * (overH2.ordinal() -
										heights[0].ordinal()) + toH.ordinal() -
										heights[0].ordinal();
						double distRatio = losOverSquare2.getCenter().distance(
								helper.getSource().getCenter() ) / helper.getLineLength();
						distRatio = fromH.compareTo( toH ) < 0 ? distRatio : 1 - distRatio;
						
						assertSame( distRatio > heightRatios[ index ],
								TileGrid.hasLineOfSight( helper, losForObstacle, losToObstacle ));
					}
				}
			}
			
			// place obstacle at one quarter of distance from the source to the target
			losOverObstacle2.setLocation( null );
			losOverObstacle3.setLocation( losOverSquare3 );
			
			// test different heights for the intervening obstacle
			for (Height overH3 : heights) {
				losOverObstacle3.setHeight( overH3 );
				
				// test to ground
				helper = grid.createLineHelper( losFromSquare, losToSquare );
				assertSame( fromH.compareTo( overH3 ) > 0 && (fromH != BLOCKING ||
						fromH.ordinal() > overH3.ordinal() + 1),
						TileGrid.hasLineOfSight( helper, losForObstacle ));
				
				// test to target obstacles of varying height
				for (Height toH : heights) {
					losToObstacle.setHeight( toH );
					
					helper = grid.createLineHelper( losFromSquare, losToSquare );
					assertSame( fromH.compareTo( overH3 ) > 0 && (fromH != BLOCKING ||
							toH != FLAT || overH3 != VERY_HIGH) || toH.compareTo( overH3 ) > 0 &&
							fromH.compareTo( overH3 ) >= 0,
							TileGrid.hasLineOfSight( helper, losForObstacle, losToObstacle ));
					
					boolean[] crossesObstacle = {
						false, false, false, true, true, true, false, false, false };
					
					// test line of sight from the source square's neighbors
					// from some, line of sight isn't across the square with the obstacle
					for (int i = 0; i < 9; i++) {
						helper = grid.createLineHelper( grid.getTileAtRC(
								fromRow + i / 3 - 1, fromCol + i % 3 - 1 ), losToSquare );
						
						int index = heights.length * heights.length * (fromH.ordinal() -
								heights[0].ordinal()) + heights.length * (overH3.ordinal() -
										heights[0].ordinal()) + toH.ordinal() -
										heights[0].ordinal();
						double distRatio = losOverSquare3.getCenter().distance(
								helper.getSource().getCenter() ) / helper.getLineLength();
						distRatio = fromH.compareTo( toH ) < 0 ? distRatio : 1 - distRatio;
						
						assertSame( distRatio > heightRatios[ index ] || !crossesObstacle[i],
								TileGrid.hasLineOfSight( helper, losForObstacle, losToObstacle ));
					}
				}
			}
			
			// reset for next iteration
			losOverObstacle3.setLocation( null );
			losToObstacle.setHeight( LOW );
		}
	}

	// tests a direct line which goes from neighbor to neighbor with a given compass direction
	private void testDirectLine( TileGrid <Square> grid, Square from, Square to, Direction dir ) {
		LineHelper <Square> helper = grid.createLineHelper( from, to );
		Square current = from, previous = null, next = null;
		
		// there's always only one square, and the line goes through its center
		while (!helper.targetReached()) {
			next = helper.getNextTile( true );
			assertSame( current.getNeighbor( dir ), next );
			assertNull( helper.getNextTile( false ));
			
			assertSame( current, helper.getCurrentTile( true ));
			assertNull( helper.getCurrentTile( false ));
			assertEquals( 0.0, helper.getDistanceToLine( current ), 0.001 );
			
			assertSame( current, helper.getBetterTile( true ));
			assertNull( helper.getBetterTile( false ));
			
			helper.nextTiles();
			previous = current;
			current = next;
			
			assertSame( previous, helper.getPreviousTile( true ));
			assertNull( helper.getPreviousTile( false ));
		}
		
		// there's no secondary option for target square, and line goes through its center
		assertSame( current, helper.getCurrentTile( true ));
		assertNull( helper.getCurrentTile( false ));
		assertEquals( 0.0, helper.getDistanceToLine( current ), 0.001 );
		
		assertSame( to, helper.getCurrentTile( true ));
	}
	
	// tests indirect lines where the shape of the path isn't easily defined
	private void testIndirectLine( TileGrid <Square> grid, Square from, Square to ) {
		LineHelper <Square> helper = grid.createLineHelper( from, to );
		
		Point center = from.getCenter();
		double x1 = center.getX();
		double y1 = center.getY();
		
		center = to.getCenter();
		double x2 = center.getX();
		double y2 = center.getY();
		
		Square current1 = from, current2 = null;
		Square previous1 = null, previous2 = null;
		Square next1 = null, next2 = null;
		
		// check all squares until the target
		while (!helper.targetReached()) {
			next1 = helper.getNextTile( true );
			next2 = helper.getNextTile( false );
			
			assertSame( current1, helper.getCurrentTile( true ));
			assertSame( current2, helper.getCurrentTile( false ));
			
			Square better = helper.getBetterTile( true );
			Square worse = helper.getBetterTile( false );
			assertNotNull( better );
			
			// if squares are equal, they have the same distance to the line
			if (helper.tilesAreEqual())
				assertEquals( helper.getDistanceToLine( better ),
						helper.getDistanceToLine( worse ), 0.001 );
			
			// worse option is farther away from the line than the better option
			else if (worse != null)
				assertTrue(
						helper.getDistanceToLine( worse ) > helper.getDistanceToLine( better ));
			
			else if (current1 != from)
				assertFalse( TileGrid.intersects( current2, x1, y1, x2, y2 ));
			
			helper.nextTiles();
			previous1 = current1;
			previous2 = current2;
			current1 = next1;
			current2 = next2;
			
			assertSame( previous1, helper.getPreviousTile( true ));
			assertSame( previous2, helper.getPreviousTile( false ));
			
			// current squares are switched in order to keep the square path along the line
			if (current1 != helper.getCurrentTile( true )) {
				assertSame( current1, helper.getCurrentTile( false ));
				
				Square square = current1;
				current1 = current2;
				current2 = square;
			}
			
			else
				assertSame( previous2, helper.getPreviousTile( false ));
		}

		// ends at the target, which is the better and only square
		assertNotNull( current1 );
		assertNotNull( current2 );
		
		assertSame( current1, helper.getCurrentTile( true ));
		assertSame( current2, helper.getCurrentTile( false ));
		
		assertSame( to, helper.getBetterTile( true ));
		assertNull( helper.getBetterTile( false ));
	}
	
	// tests direct paths with the option that the path continues through the target
	private void testDirectPath( TileGrid <Square> grid,
			int row1, int col1, int row2, int col2, boolean hitEdge ) {
		LineHelper <Square> helper = grid.createLineHelper(
				grid.getTileAtRC( row1, col1 ), grid.getTileAtRC( row2, col2 ));
		Square[] directPath = FilledSquareGrid.getDirectPath( helper, hitEdge );
		int pathLen = 0;
		
		// calculation of number of squares along the path
		if (!hitEdge)
			pathLen = Math.max( Math.abs( col2 - col1 ), Math.abs( row2 - row1 )) + 1;
		
		// estimation of squares on a path that continues to a grid edge
		else {
			int columns = (col2 > col1 ?
				(int)(grid.getTotalWidth() / grid.getTileWidth()) - col1 : (col1 + 1));
			int rows = (row2 > row1 ?
				(int)(grid.getTotalHeight() / grid.getTileHeight()) - row1 : (row1 + 1));
			
			if (row2 == row1)
				pathLen = columns;
			
			else if (col2 == col1)
				pathLen = rows;
			
			else
				pathLen = Math.min( columns, rows );
		}
		
		Square[] expectedPath = new Square[ pathLen ];
		
		// each square's row and column differ at most one from those of the previous square
		for (int i = 0, j = row1, k = col1, rowChange = (int)Math.signum( row2 - row1 ),
				colChange = (int)Math.signum( col2 - col1 ); i < expectedPath.length;
				i++, j += rowChange, k += colChange)
			expectedPath[i] = grid.getTileAtRC( j, k );
		
		assertArrayEquals( expectedPath, directPath );
	}
	
	// test indirect paths with the option that the line continues to a grid edge
	private void testIndirectPath( TileGrid <Square> grid,
			int row1, int col1, int row2, int col2, boolean hitEdge ) {
		LineHelper <Square> helper = grid.createLineHelper(
				grid.getTileAtRC( row1, col1 ), grid.getTileAtRC( row2, col2 ));
		Square[] directPath = FilledSquareGrid.getDirectPath( helper, hitEdge );
		Square dst = null;
		
		if (!hitEdge)		// line ends at target
			dst = helper.getTarget();
		
		else				// line goes through target and ends at a grid edge
			dst = findLineDestination( grid, helper )[0];
		
		int rows = Math.abs( helper.getSource().getRow() - dst.getRow() );
		int columns = Math.abs( helper.getSource().getColumn() - dst.getColumn() );
		int pathLen = Math.max( rows, columns ) + 1;	// one row/column each step, plus source
		
		Square current = grid.getTileAtRC( row1, col1 );
		assertSame( current, directPath[0] );
		
		Point dstCenter = dst.getCenter();
		double previousDist = current.getCenter().distance( dstCenter );
		
		// test squares on the indirect line
		for (int i = 1; i < pathLen; i++) {
			double minDist = Double.MAX_VALUE;
			Square next = null, equalTile = null;
			
			for (Direction d : Square.getNeighborDirections()) {
				Square neighbor = current.getNeighbor( d );

				// on each step, new square must be closer to line end than the previous one
				if (neighbor == null || neighbor.getCenter().distance( dstCenter ) > previousDist)
					continue;
				
				double dist = helper.getDistanceToLine( neighbor );
				
				// compare distances to find equal squares
				if (dist > minDist)
					continue;
				
				else if (dist == minDist)
					equalTile = neighbor;
				
				else {
					minDist = dist;
					next = neighbor;
					equalTile = null;
				}
			}
			
			assertNotNull( next );
			previousDist = next.getCenter().distance( dstCenter );
			
			if (equalTile != null) {
				assertTrue( directPath[i] == next && directPath[i + 1] == equalTile ||
						directPath[i] == equalTile && directPath[i + 1] == next );
				
				// path length estimate doesn't include equal squares
				i++;
				pathLen++;
				
				previousDist = Math.min( previousDist,
						equalTile.getCenter().distance( dstCenter ));
			}
			
			else
				assertSame( next, directPath[i] );
			
			current = next;
		}

		// all squares have been checked, ensure there was the right number of them
		assertEquals( pathLen, directPath.length );
	}
	
	private Square[] findLineDestination( TileGrid <Square> grid, LineHelper <Square> helper ) {
		int startCol = 0, endCol = 0, startRow = 0, endRow = 0;
		Square[][] squares = grid.getTiles();
		
		Square from = helper.getSource();
		Square to = helper.getTarget();
		
		Point p1 = from.getCenter();
		Point p2 = to.getCenter();
		
		boolean right = p1.getX() < p2.getX() - 0.001;
		boolean down = p1.getY() < p2.getY() - 0.001;
		
		// select quadrant of the grid to search from (nw, ne, sw, se)
		if (!right && !down) {
			startCol = 0;
			startRow = 1;
			endCol = to.getColumn();
			endRow = to.getRow();
		}
		
		else if (right && !down) {
			startCol = to.getColumn();
			endCol = squares[0].length - 1;
			startRow = 1;
			endRow = to.getRow();
		}
		
		else if (!right && down) {
			startCol = 0;
			endCol = to.getColumn();
			startRow = to.getRow();
			endRow = squares.length - 2;
		}
		
		else if (right && down) {
			startCol = to.getColumn();
			endCol = squares[ squares.length - 1 ].length - 1;
			startRow = to.getRow();
			endRow = squares.length - 2;
		}
		
		LinkedList <Square> options = new LinkedList <>();
		int row = down ? squares.length - 1 : 0;
		int col = right ? squares[0].length - 1 : 0;
		
		// find horizontal edge squares which the line intersects
		for (int i = startCol; i <= endCol && i < squares[row].length; i++)
			if (TileGrid.intersects( squares[row][i], p1, p2 ))
				options.add( squares[row][i] );
		
		// also search along vertical edge
		for (int i = startRow; i <= endRow; i++)
			if (TileGrid.intersects( squares[i][col], p1, p2 ))
				options.add( squares[i][col] );
		
		double maxDist = Double.MIN_VALUE;
		Square[] squareOptions = options.toArray( new Square[ options.size() ]);
		Square best = null, equal = null;
		
		// find square that's furthest away from the line's source
		for (int i = 0; i < squareOptions.length; i++) {
			double dist = p1.distance( squareOptions[i].getCenter() );
			
			if (dist < maxDist - 0.001)
				options.remove( squareOptions[i] );
			
			else if (dist > maxDist + 0.001) {
				maxDist = dist;
				
				if (best != null)
					options.remove( best );
				
				if (equal != null)
					options.remove( equal );
				
				best = squareOptions[i];
				equal = null;
			}
			
			// line ending in two squares is also possible
			else
				equal = squareOptions[i];
		}
		
		return options.toArray( new Square[ options.size() ]);
	}
}
