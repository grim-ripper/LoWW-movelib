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

import java.util.Arrays;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.junit.Test;

import fi.grimripper.loww.Direction;
import fi.grimripper.loww.Height;
import fi.grimripper.loww.Point;
import fi.grimripper.loww.test.TestObstacle;
import fi.grimripper.loww.tiles.FilledRowHexGrid;
import fi.grimripper.loww.tiles.Hex;
import fi.grimripper.loww.tiles.Obstacle;
import fi.grimripper.loww.tiles.Terrain;
import fi.grimripper.loww.tiles.TileGrid;
import fi.grimripper.loww.tiles.TileGrid.LineHelper;

public class FilledRowHexGridTest {

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
	public void testFilledRowHexGrid() {
		// tests for the calculations of hex dimensions with regular hex geometry
		double[] hexDimension = FilledRowHexGrid.calculateGridHexDimensions( 30, 40, 1, 1, 1 );
		assertEquals( hexDimension[0], hexDimension[1] / 2 * Math.sqrt( 3 ), 0.001 );
		assertEquals( 30, hexDimension[0], 0.001 );
		
		FilledRowHexGrid grid = FilledRowHexGrid.createWithGridSize( 30, 40, 1, 1, 1 );
		assertEquals( hexDimension[0], grid.getTileWidth(), 0.001 );
		assertEquals( hexDimension[1], grid.getTileHeight(), 0.001 );
		
		hexDimension = FilledRowHexGrid.calculateGridHexDimensions( 40, 30, 1, 1, 1 );
		assertEquals( hexDimension[0], hexDimension[1] / 2 * Math.sqrt( 3 ), 0.001 );
		assertEquals( 30, hexDimension[1], 0.001 );
		
		grid = FilledRowHexGrid.createWithGridSize( 40, 30, 1, 1, 1 );
		assertEquals( hexDimension[0], grid.getTileWidth(), 0.001 );
		assertEquals( hexDimension[1], grid.getTileHeight(), 0.001 );
		
		hexDimension = FilledRowHexGrid.calculateGridHexDimensions( 50, 75, 2, 2, 2 );
		assertEquals( 20, hexDimension[0], 0.001 );
		
		grid = FilledRowHexGrid.createWithGridSize( 50, 75, 2, 2, 2 );
		assertEquals( hexDimension[0], grid.getTileWidth(), 0.001 );
		assertEquals( hexDimension[1], grid.getTileHeight(), 0.001 );
		
		hexDimension = FilledRowHexGrid.calculateGridHexDimensions( 60, 90, 2, 2, 3 );
		assertEquals( 20, hexDimension[0], 0.001 );
		
		grid = FilledRowHexGrid.createWithGridSize( 60, 90, 2, 2, 3 );
		assertEquals( hexDimension[0], grid.getTileWidth(), 0.001 );
		assertEquals( hexDimension[1], grid.getTileHeight(), 0.001 );
		
		hexDimension = FilledRowHexGrid.calculateGridHexDimensions( 90, 60, 2, 2, 2 );
		assertEquals( 60 / 1.75, hexDimension[1], 0.001 );
		
		grid = FilledRowHexGrid.createWithGridSize( 90, 60, 2, 2, 2 );
		assertEquals( hexDimension[0], grid.getTileWidth(), 0.001 );
		assertEquals( hexDimension[1], grid.getTileHeight(), 0.001 );
		
		// test that odd row width is ignored on a single-row grid
		grid = FilledRowHexGrid.createWithHexSize( 0, 0, 1, 2, 0 );
		assertEquals( 1, grid.getTiles().length );
		assertEquals( 2, grid.getTiles()[0].length );
		
		// create a symmetric grid with longer odd rows
		hexDimension = FilledRowHexGrid.calculateRegularHexDimensions( 30, 40 );
		grid = FilledRowHexGrid.createWithHexSize( 30, 40, 5, 6, 7 );
		
		assertEquals( hexDimension[0], grid.getTileWidth(), 0.001 );
		assertEquals( hexDimension[1], grid.getTileHeight(), 0.001 );
		assertEquals( 7 * hexDimension[0], grid.getTotalWidth(), 0.001 );
		assertEquals( (int)Math.ceil( (4 * 0.75 + 1) * hexDimension[1] ), grid.getTotalHeight() );
		assertEquals( 3 * 6 + 2 * 7, grid.getTileCount() );
		
		// test the shape of the grid
		assertNull( grid.getTileAtRC( -1, 0 ));
		assertNull( grid.getTileAtRC( 0, -1 ));
		assertNull( grid.getTileAtRC( 0, 6 ));
		assertNull( grid.getTileAtRC( 1, 7 ));
		assertNull( grid.getTileAtRC( 5, 0 ));
		assertNotNull( grid.getTileAtRC( 1, 6 ));
		
		// test the coordinates of northwest corner hex
		Hex northwest = grid.getTileAtRC( 0, 0 );
		assertNotNull( northwest );
		
		// top of the grid, left edge half hex width from grid left
		double xnw = northwest.getPoint( NORTHWEST ).getX();
		double ynw = northwest.getPoint( NORTHWEST ).getY();
		assertEquals( hexDimension[0] / 2, xnw, 0.001 );
		assertEquals( 0, northwest.getPoint( NORTH ).getY(), 0.001 );
		
		// test coordinates outside the grid, near nw corner
		assertNull( grid.getTileAtXY( -1, 0 ));
		assertNull( grid.getTileAtXY( 0, -1 ));
		assertNull( grid.getTileAtXY( northwest.getPoint( NORTH ).getX() - 1, 0 ));
		assertNull( grid.getTileAtXY( xnw, ynw - 1 ));
		assertNull( grid.getTileAtXY( xnw - 1, ynw ));
		
		// test the coordinates of northeast corner hex
		Hex northeast = grid.getTileAtRC( 0, 5 );
		assertNotNull( northeast );
		
		// top of the grid, right edge half hex width from grid right
		double xne = northeast.getPoint( NORTHEAST ).getX();
		double yne = northeast.getPoint( NORTHEAST ).getY();
		assertEquals( 6.5 * hexDimension[0], xne, 0.001 );
		assertEquals( 0, northeast.getPoint( NORTH ).getY(), 0.001 );
		
		// test coordinates outside the grid, near ne corner
		assertNull( grid.getTileAtXY( grid.getTotalWidth(), 0 ));
		assertNull( grid.getTileAtXY( grid.getTotalWidth() - 1, -1 ));
		assertNull( grid.getTileAtXY( northeast.getPoint( NORTH ).getX() + 1, 0 ));
		assertNull( grid.getTileAtXY( xne, yne - 1 ));
		assertNull( grid.getTileAtXY( xne + 1, yne ));
		
		// test the coordinates of southwest corner hex
		Hex southwest = grid.getTileAtRC( 4, 0 );
		assertNotNull( southwest );
		
		// bottom of the grid, left edge half hex width from grid left
		double xsws = southwest.getPoint( SOUTH ).getX();
		double xsw = southwest.getPoint( SOUTHWEST ).getX();
		double ysw = southwest.getPoint( SOUTHWEST ).getY();
		assertEquals( hexDimension[0] / 2, xsw, 0.001 );
		assertEquals( 4 * hexDimension[1], southwest.getPoint( SOUTH ).getY(), 0.001 );
		
		// test coordinates outside the grid, near sw corner
		assertNull( grid.getTileAtXY( xsws, grid.getTotalHeight() + 1 ));
		assertNull( grid.getTileAtXY( xsws - 1, southwest.getPoint( SOUTH ).getY() ));
		assertNull( grid.getTileAtXY( xsw, ysw + 1 ));
		assertNull( grid.getTileAtXY( xsw - 1, ysw ));
		
		// test the coordinates of southeast corner hex
		Hex southeast = grid.getTileAtRC( 4, 5 );
		assertNotNull( southeast );
		
		// bottom of the grid, right edge half hex width from grid right
		double xses = southeast.getPoint( SOUTH ).getX();
		double xse = southeast.getPoint( SOUTHEAST ).getX();
		double yse = southeast.getPoint( SOUTHEAST ).getY();
		assertEquals( 6.5 * hexDimension[0], xse, 0.001 );
		assertEquals( 4 * hexDimension[1], southeast.getPoint( SOUTH ).getY(), 0.001 );
		
		// test coordinates outside the grid, near se corner
		assertNull( grid.getTileAtXY( xses, grid.getTotalHeight() + 1 ));
		assertNull( grid.getTileAtXY( xses + 1, southeast.getPoint( SOUTH ).getY() ));
		assertNull( grid.getTileAtXY( xse, yse + 1 ));
		assertNull( grid.getTileAtXY( xse + 1, yse ));
		
		// test the coordinates of a hex on the left edge
		Hex west = grid.getTileAtRC( 1, 0 );
		assertNotNull( west );
		
		// left edge on grid left, top point 0.75 * hex height below grid top
		double wx = west.getPoint( NORTHWEST ).getX();
		double wy = west.getPoint( NORTH ).getY();
		assertEquals( 0, wx, 0.001 );
		assertEquals( 0.75 * hexDimension[1], wy, 0.001 );
		
		// test coordinates outside the grid, near the first hex that's on grid's left edge
		assertNull( grid.getTileAtXY( wx, west.getPoint( NORTHWEST ).getY() - 1 ));
		assertNull( grid.getTileAtXY( west.getPoint( NORTH ).getX() - 1, wy ));
		
		// left edge on grid left, bottom point 1.75 * hex height below grid top
		wx = west.getPoint( SOUTHWEST ).getX();
		wy = west.getPoint( SOUTH ).getY();
		assertEquals( 0, wx, 0.001 );
		assertEquals( 1.75 * hexDimension[1], wy, 0.001 );

		// test coordinates outside the grid, near the first hex that's on grid's left edge
		assertNull( grid.getTileAtXY( wx, west.getPoint( SOUTHWEST ).getY() + 1 ));
		assertNull( grid.getTileAtXY( west.getPoint( SOUTH ).getX() - 1, wy ));

		// test the coordinates of a hex on the right edge
		Hex east = grid.getTileAtRC( 1, 6 );
		assertNotNull( east );

		// right edge on grid right, top point 0.75 * hex height below grid top
		double ex = east.getPoint( NORTHEAST ).getX();
		double ey = east.getPoint( NORTH ).getY();
		assertEquals( 7 * hexDimension[0], ex, 0.001 );
		assertEquals( 0.75 * hexDimension[1], ey, 0.001 );

		// test coordinates outside the grid, near the first hex that's on grid's left edge
		assertNull( grid.getTileAtXY( ex, east.getPoint( NORTHEAST ).getY() - 1 ));
		assertNull( grid.getTileAtXY( east.getPoint( NORTH ).getX() + 1, ey ));
		
		// right edge on grid left, bottom point 1.75 * hex height below grid top
		ex = east.getPoint( SOUTHEAST ).getX();
		ey = east.getPoint( SOUTH ).getY();
		assertEquals( 7 * hexDimension[0], ex, 0.001 );
		assertEquals( 1.75 * hexDimension[1], ey, 0.001 );
		
		// test coordinates outside the grid, near the first hex that's on grid's left edge
		assertNull( grid.getTileAtXY( ex, east.getPoint( SOUTHEAST ).getY() + 1 ));
		assertNull( grid.getTileAtXY( east.getPoint( SOUTH ).getX() + 1, ey ));
		
		// test hex neighbors, hexes don't have vertical neighbors
		for (int i = 0; i < 5; i++)
			for (int j = 0; j < 6 + i % 2; j++) {
				Hex hex = grid.getTileAtRC( i, j );
				assertNull( hex.getNeighbor( NORTH ));
				assertNull( hex.getNeighbor( SOUTH ));
			}
		
		// no northern neighbors on first row, and no southern on bottom row
		for (int i = 0; i < 6; i++) {
			Hex hex = grid.getTileAtRC( 0, i );
			assertNull( hex.getNeighbor( NORTHWEST ));
			assertNull( hex.getNeighbor( NORTHEAST ));
			
			hex = grid.getTileAtRC( 4, i );
			assertNull( hex.getNeighbor( SOUTHWEST ));
			assertNull( hex.getNeighbor( SOUTHEAST ));
		}
		
		// no west neighbors for first hexes in a row, and no east neighbors for last
		for (int i = 0; i < 5; i++) {
			west = grid.getTileAtRC( i, 0 );
			assertNull( west.getNeighbor( WEST ));
			
			east = grid.getTileAtRC( i, 5 + i % 2 );
			assertNull( east.getNeighbor( EAST ));
			
			if (i % 2 == 0)
				continue;
			
			// further on the left/right than adjacent rows, no diagonal neighbors on one side
			assertNull( west.getNeighbor( NORTHWEST ));
			assertNull( west.getNeighbor( SOUTHWEST ));
			
			assertNull( east.getNeighbor( NORTHEAST ));
			assertNull( east.getNeighbor( SOUTHEAST ));
		}
		
		// all other hexes have west neighbors
		for (int i = 0; i < 5; i++)
			for (int j = 1; j < 6 + i % 2; j++)
				assertSame( grid.getTileAtRC( i, j - 1 ),
						grid.getTileAtRC( i, j ).getNeighbor( WEST ));

		// all other hexes have east neighbors
		for (int i = 0; i < 5; i++)
			for (int j = 0; j < 5 + i % 2; j++)
				assertSame( grid.getTileAtRC( i, j + 1 ),
						grid.getTileAtRC( i, j ).getNeighbor( EAST ));

		// all other hexes have northwest neighbors
		for (int i = 1; i < 5; i++)
			for (int j = 0; j < 6; j++)
				assertSame( grid.getTileAtRC( i - 1, j ),
						grid.getTileAtRC( i, j + i % 2 ).getNeighbor( NORTHWEST ));

		// all other hexes have northeast neighbors
		for (int i = 1; i < 5; i++)
			for (int j = 0; j < 6; j++)
				assertSame( grid.getTileAtRC( i - 1, j + (i - 1) % 2 ),
						grid.getTileAtRC( i, j ).getNeighbor( NORTHEAST ));

		// all other hexes have southwest neighbors
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 6; j++)
				assertSame( grid.getTileAtRC( i + 1, j ),
						grid.getTileAtRC( i, j + i % 2 ).getNeighbor( SOUTHWEST ));

		// all other hexes have southeast neighbors
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 6; j++)
				assertSame( grid.getTileAtRC( i + 1, j + (i + 1) % 2 ),
						grid.getTileAtRC( i, j ).getNeighbor( SOUTHEAST ));
		
		// create a grid with same number of hexes on each row
		hexDimension = FilledRowHexGrid.calculateRegularHexDimensions( 20, 25 );
		grid = FilledRowHexGrid.createWithHexSize( 20, 25, 4, 3, 3 );
		
		assertEquals( hexDimension[0], grid.getTileWidth(), 0.001 );
		assertEquals( hexDimension[1], grid.getTileHeight(), 0.001 );
		assertEquals( (int)Math.ceil( 3.5 * hexDimension[0] ), grid.getTotalWidth() );
		assertEquals( (int)Math.ceil( 3.25 * hexDimension[1] ), grid.getTotalHeight() );
		assertEquals( 4 * 3, grid.getTileCount() );
		
		assertNull( grid.getTileAtRC( 0, 3 ));
		assertNull( grid.getTileAtRC( 1, 3 ));
		
		// test the coordinates of northwest corner hex
		northwest = grid.getTileAtRC( 0, 0 );
		assertNotNull( northwest );
		
		// left edge on grid left, top point half hex width from grid left
		assertEquals( hexDimension[0] / 2, northwest.getPoint( NORTH ).getX(), 0.001 );
		assertEquals( 0, northwest.getPoint( NORTH ).getY(), 0.001 );
		assertEquals( 0, northwest.getPoint( SOUTHWEST ).getX(), 0.001 );
		assertEquals( hexDimension[1], northwest.getPoint( SOUTH ).getY(), 0.001 );

		// test the coordinates of northeast corner hex
		northeast = grid.getTileAtRC( 0, 2 );
		assertNotNull( northeast );
		
		// right edge half hex width from grid right, top point full hex width from grid right
		assertEquals( 2.5 * hexDimension[0], northeast.getPoint( NORTH ).getX(), 0.001 );
		assertEquals( 0, northeast.getPoint( NORTH ).getY(), 0.001 );
		assertEquals( 3 * hexDimension[0], northeast.getPoint( SOUTHEAST ).getX(), 0.001 );
		assertEquals( hexDimension[1], northeast.getPoint( SOUTH ).getY(), 0.001 );
		
		// test the coordinates of southwest corner hex
		southwest = grid.getTileAtRC( 3, 0 );
		assertNotNull( southwest );
		
		// left edge half hex width from grid left, bottom point full hex width from grid left
		assertEquals( hexDimension[0], southwest.getPoint( SOUTH ).getX(), 0.001 );
		assertEquals( 3.25 * hexDimension[1], southwest.getPoint( SOUTH ).getY(), 0.001 );
		assertEquals( hexDimension[0] / 2, southwest.getPoint( NORTHWEST ).getX(), 0.001 );
		assertEquals( 2.25 * hexDimension[1], southwest.getPoint( NORTH ).getY(), 0.001 );
		
		// test the coordinates of southeast corner hex
		southeast = grid.getTileAtRC( 3, 2 );
		assertNotNull( southeast );
		
		// right edge on grid right, bottom point half hex width from grid right
		assertEquals( 3 * hexDimension[0], southeast.getPoint( SOUTH ).getX(), 0.001 );
		assertEquals( 3.25 * hexDimension[1], southeast.getPoint( SOUTH ).getY(), 0.001 );
		assertEquals( 3.5 * hexDimension[0], southeast.getPoint( NORTHEAST ).getX(), 0.001 );
		assertEquals( 2.25 * hexDimension[1], southeast.getPoint( NORTH ).getY(), 0.001 );
		
		// test neighbors, no hex has neighbors to north or south
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 3; j++) {
				Hex hex = grid.getTileAtRC( i, j );
				assertNull( hex.getNeighbor( NORTH ));
				assertNull( hex.getNeighbor( SOUTH ));
			}
		
		// top row has no northern neighbors, bottom has no southern
		for (int i = 0; i < 3; i++) {
			Hex hex = grid.getTileAtRC( 0, i );
			assertNull( hex.getNeighbor( NORTHWEST ));
			assertNull( hex.getNeighbor( NORTHEAST ));
			
			hex = grid.getTileAtRC( 3, i );
			assertNull( hex.getNeighbor( SOUTHWEST ));
			assertNull( hex.getNeighbor( SOUTHEAST ));
		}
		
		// first hex on a row has no west neighbor, and last has no east neighbor
		for (int i = 0; i < 4; i++) {
			west = grid.getTileAtRC( i, 0 );
			assertNull( west.getNeighbor( WEST ));
			
			east = grid.getTileAtRC( i, 2 );
			assertNull( east.getNeighbor( EAST ));
			
			// further on the left/right than adjacent rows, no diagonal neighbors on one side
			if (i % 2 == 1) {
				assertNull( east.getNeighbor( NORTHEAST ));
				assertNull( east.getNeighbor( SOUTHEAST ));
			}
			
			else {
				assertNull( west.getNeighbor( NORTHWEST ));
				assertNull( west.getNeighbor( SOUTHWEST ));
			}
		}
		
		// all other hexes have west neighbors
		for (int i = 0; i < 4; i++)
			for (int j = 1; j < 3; j++)
				assertSame( grid.getTileAtRC( i, j - 1 ),
						grid.getTileAtRC( i, j ).getNeighbor( WEST ));

		// all other hexes have east neighbors
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 2; j++)
				assertSame( grid.getTileAtRC( i, j + 1 ),
						grid.getTileAtRC( i, j ).getNeighbor( EAST ));

		// all other hexes have northwest neighbors
		for (int i = 1; i < 4; i++)
			for (int j = 0; j < 2 + i % 2; j++)
				assertSame( grid.getTileAtRC( i - 1, j ),
						grid.getTileAtRC( i, j + 1 - i % 2 ).getNeighbor( NORTHWEST ));
		
		// all other hexes have northeast neighbors
		for (int i = 1; i < 4; i++)
			for (int j = 0; j < 3; j++)
				assertSame( grid.getTileAtRC( i - 1, j + i % 2 ),
						grid.getTileAtRC( i, j ).getNeighbor( NORTHEAST ));

		// all other hexes have southwest neighbors
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 2 + i % 2; j++)
				assertSame( grid.getTileAtRC( i + 1, j ),
						grid.getTileAtRC( i, j + 1 - i % 2 ).getNeighbor( SOUTHWEST ));

		// all other hexes have southeast neighbors
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++)
				assertSame( grid.getTileAtRC( i + 1, j + i % 2 ),
						grid.getTileAtRC( i, j ).getNeighbor( SOUTHEAST ));
		
		// create a grid with longer even rows, and even number of rows (vertically asymmetric)
		hexDimension = FilledRowHexGrid.calculateRegularHexDimensions( 25, 10 );
		grid = FilledRowHexGrid.createWithHexSize( 25, 10, 4, 7, 6 );
		
		assertEquals( hexDimension[0], grid.getTileWidth(), 0.001 );
		assertEquals( hexDimension[1], grid.getTileHeight(), 0.001 );
		assertEquals( (int)Math.ceil( 7 * hexDimension[0] ), grid.getTotalWidth() );
		assertEquals( (int)Math.ceil( 3.25 * hexDimension[1] ), grid.getTotalHeight() );
		assertEquals( 2 * 7 + 2 * 6, grid.getTileCount() );
		
		// test grid shape
		assertNull( grid.getTileAtRC( 0, 7 ));
		assertNull( grid.getTileAtRC( 1, 6 ));
		assertNull( grid.getTileAtRC( 2, 7 ));
		assertNull( grid.getTileAtRC( 3, 6 ));
		assertNotNull( grid.getTileAtRC( 0, 6 ));
		assertNotNull( grid.getTileAtRC( 2, 6 ));
		
		// test the coordinates of northwest corner hex
		northwest = grid.getTileAtRC( 0, 0 );
		assertNotNull( northwest );
		
		// left edge on grid left, top point half hex width from grid left
		assertEquals( hexDimension[0] / 2, northwest.getPoint( NORTH ).getX(), 0.001 );
		assertEquals( 0, northwest.getPoint( NORTH ).getY(), 0.001 );
		assertEquals( 0, northwest.getPoint( NORTHWEST ).getX(), 0.001 );
		assertEquals( hexDimension[1], northwest.getPoint( SOUTH ).getY(), 0.001 );
		
		// test the coordinates of northeast corner hex
		northeast = grid.getTileAtRC( 0, 6 );
		assertNotNull( northeast );
		
		// right edge on grid right, top point half hex width from grid right
		assertEquals( 6.5 * hexDimension[0], northeast.getPoint( NORTH ).getX(), 0.001 );
		assertEquals( 0, northeast.getPoint( NORTH ).getY(), 0.001 );
		assertEquals( 7 * hexDimension[0], northeast.getPoint( NORTHEAST ).getX(), 0.001 );
		assertEquals( hexDimension[1], northeast.getPoint( SOUTH ).getY(), 0.001 );
		
		// test the coordinates of southwest corner hex
		southwest = grid.getTileAtRC( 3, 0 );
		assertNotNull( southwest );
		
		// left edge half hex width from grid left, top point full hex width from grid left
		assertEquals( hexDimension[0], southwest.getPoint( NORTH ).getX(), 0.001 );
		assertEquals( 3.25 * hexDimension[1], southwest.getPoint( SOUTH ).getY(), 0.001 );
		assertEquals( hexDimension[0] / 2, southwest.getPoint( NORTHWEST ).getX(), 0.001 );
		assertEquals( 2.25 * hexDimension[1], southwest.getPoint( NORTH ).getY(), 0.001 );
		
		// test the coordinates of southeast corner hex
		southeast = grid.getTileAtRC( 3, 5 );
		assertNotNull( southeast );
		
		// right edge half hex width from grid right, top point full hex width from grid right
		assertEquals( 6 * hexDimension[0], southeast.getPoint( NORTH ).getX(), 0.001 );
		assertEquals( 3.25 * hexDimension[1], southeast.getPoint( SOUTH ).getY(), 0.001 );
		assertEquals( 6.5 * hexDimension[0], southeast.getPoint( NORTHEAST ).getX(), 0.001 );
		assertEquals( 2.25 * hexDimension[1], southeast.getPoint( NORTH ).getY(), 0.001 );

		// test hex neighbors, no hex has neighbors to north or south
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 7 - i % 2; j++) {
				Hex hex = grid.getTileAtRC( i, j );
				assertNull( hex.getNeighbor( NORTH ));
				assertNull( hex.getNeighbor( SOUTH ));
			}
		
		// no northern neighbors on top row
		for (int i = 0; i < 7; i++) {
			Hex hex = grid.getTileAtRC( 0, i );
			assertNull( hex.getNeighbor( NORTHWEST ));
			assertNull( hex.getNeighbor( NORTHEAST ));
		}
		
		// no southern neighbors on bottom row
		for (int i = 0; i < 6; i++) {
			Hex hex = grid.getTileAtRC( 3, i );
			assertNull( hex.getNeighbor( SOUTHWEST ));
			assertNull( hex.getNeighbor( SOUTHEAST ));
		}
		
		// first hex on a row has no west neighbor, last has no east neighbor
		for (int i = 0; i < 4; i++) {
			west = grid.getTileAtRC( i, 0 );
			assertNull( west.getNeighbor( WEST ));
			
			east = grid.getTileAtRC( i, 6 - i % 2 );
			assertNull( east.getNeighbor( EAST ));
			
			if (i % 2 == 1)
				continue;
			
			// first and last hex on shorter row has diagonal neighbors on only one side
			assertNull( east.getNeighbor( NORTHEAST ));
			assertNull( east.getNeighbor( SOUTHEAST ));
			
			assertNull( west.getNeighbor( NORTHWEST ));
			assertNull( west.getNeighbor( SOUTHWEST ));
		}
		
		// all other hexes have west neighbors
		for (int i = 0; i < 4; i++)
			for (int j = 1; j < 7 - i % 2; j++)
				assertSame( grid.getTileAtRC( i, j - 1 ),
						grid.getTileAtRC( i, j ).getNeighbor( WEST ));

		// all other hexes have east neighbors
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 6 - i % 2; j++)
				assertSame( grid.getTileAtRC( i, j + 1 ),
						grid.getTileAtRC( i, j ).getNeighbor( EAST ));

		// all other hexes have northwest neighbors
		for (int i = 1; i < 4; i++)
			for (int j = 0; j < 6; j++)
				assertSame( grid.getTileAtRC( i - 1, j ),
						grid.getTileAtRC( i, j + 1 - i % 2 ).getNeighbor( NORTHWEST ));

		// all other hexes have northeast neighbors
		for (int i = 1; i < 4; i++)
			for (int j = 0; j < 6; j++)
				assertSame( grid.getTileAtRC( i - 1, j + i % 2 ),
						grid.getTileAtRC( i, j ).getNeighbor( NORTHEAST ));

		// all other hexes have southwest neighbors
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 6; j++)
				assertSame( grid.getTileAtRC( i + 1, j ),
						grid.getTileAtRC( i, j + 1 - i % 2 ).getNeighbor( SOUTHWEST ));

		// all other hexes have southeast neighbors
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 6; j++)
				assertSame( grid.getTileAtRC( i + 1, j + i % 2 ),
						grid.getTileAtRC( i, j ).getNeighbor( SOUTHEAST ));
		
		// test hex dimension calculations when row length is reduced
		hexDimension = FilledRowHexGrid.calculateGridHexDimensions( 40, 80, 3, 3, 4 );
		double[] cutHexDim = FilledRowHexGrid.calculateGridHexDimensions( 40, 80, 3, 3, 5 );
		
		assertEquals( hexDimension[0], cutHexDim[0], 0.001 );
		assertEquals( hexDimension[1], cutHexDim[1], 0.001 );
		
		hexDimension = FilledRowHexGrid.calculateGridHexDimensions( 40, 20, 3, 3, 4 );
		cutHexDim = FilledRowHexGrid.calculateGridHexDimensions( 40, 20, 3, 3, 5 );

		assertEquals( hexDimension[0], cutHexDim[0], 0.001 );
		assertEquals( hexDimension[1], cutHexDim[1], 0.001 );
		
		hexDimension = FilledRowHexGrid.calculateGridHexDimensions( 40, 80, 3, 4, 3 );
		cutHexDim = FilledRowHexGrid.calculateGridHexDimensions( 40, 80, 3, 5, 3 );
		
		assertEquals( hexDimension[0], cutHexDim[0], 0.001 );
		assertEquals( hexDimension[1], cutHexDim[1], 0.001 );
		
		hexDimension = FilledRowHexGrid.calculateGridHexDimensions( 40, 20, 3, 4, 3 );
		cutHexDim = FilledRowHexGrid.calculateGridHexDimensions( 40, 20, 3, 5, 3 );

		assertEquals( hexDimension[0], cutHexDim[0], 0.001 );
		assertEquals( hexDimension[1], cutHexDim[1], 0.001 );
		
		// test hex grid creation when row length is reduced
		grid = FilledRowHexGrid.createWithHexSize( 10, 10, 3, 3, 4 );
		FilledRowHexGrid cutGrid = FilledRowHexGrid.createWithHexSize( 10, 10, 3, 3, 5 );
		assertEquals( grid.getTotalWidth(), cutGrid.getTotalWidth() );
		
		Hex[][] hexes = grid.getTiles();
		assertEquals( 3, hexes.length );
		assertEquals( 3, hexes[0].length );
		assertEquals( 4, hexes[1].length );
		
		hexes = cutGrid.getTiles();
		assertEquals( 3, hexes.length );
		assertEquals( 3, hexes[0].length );
		assertEquals( 4, hexes[1].length );
		
		grid = FilledRowHexGrid.createWithHexSize( 10, 10, 3, 4, 3 );
		cutGrid = FilledRowHexGrid.createWithHexSize( 10, 10, 3, 5, 3 );
		assertEquals( grid.getTotalWidth(), cutGrid.getTotalWidth() );

		hexes = grid.getTiles();
		assertEquals( 3, hexes.length );
		assertEquals( 4, hexes[0].length );
		assertEquals( 3, hexes[1].length );
		
		hexes = cutGrid.getTiles();
		assertEquals( 3, hexes.length );
		assertEquals( 4, hexes[0].length );
		assertEquals( 3, hexes[1].length );
		
		// test that hexes are in a copy array
		hexes[0][1] = null;
		assertNotNull( cutGrid.getTileAtRC( 0, 1 ));
	}
	
	@Test
	public void testHexLineHelper() {
		FilledRowHexGrid grid = FilledRowHexGrid.createWithHexSize( 1, 1, 20, 20, 21 );
		
		// source and target are same, line goes nowhere
		LineHelper <Hex> helper =
				grid.createLineHelper( grid.getTileAtRC( 0, 0 ), grid.getTileAtRC( 0, 0 ));
		assertTrue( helper.targetReached() );
		assertTrue( helper.pathEnds() );
		
		// direct lines to horizontal and diagonal directions (vertical direct lines not possible)
		testDirectLine( grid, grid.getTileAtRC( 3, 1 ), grid.getTileAtRC( 3, 12 ), EAST );
		testDirectLine( grid, grid.getTileAtRC( 7, 14 ), grid.getTileAtRC( 7, 4 ), WEST );
		
		testDirectLine( grid, grid.getTileAtRC( 10, 16 ), grid.getTileAtRC( 17, 20 ), SOUTHEAST );
		testDirectLine( grid, grid.getTileAtRC( 15, 12 ), grid.getTileAtRC( 19, 10 ), SOUTHWEST );
		testDirectLine( grid, grid.getTileAtRC( 9, 4 ), grid.getTileAtRC( 5, 6 ), NORTHEAST );
		testDirectLine( grid, grid.getTileAtRC( 17, 13 ), grid.getTileAtRC( 8, 8 ), NORTHWEST );
		
		// lines where there's alternately one (through center) and two (along edge) hexes
		testAlternatingLine( grid,
				grid.getTileAtRC( 6, 12 ), grid.getTileAtRC( 0, 12 ), NORTHWEST, NORTHEAST );
		testAlternatingLine( grid,
				grid.getTileAtRC( 3, 1 ), grid.getTileAtRC( 9, 1 ), SOUTHWEST, SOUTHEAST );
		testAlternatingLine( grid,
				grid.getTileAtRC( 8, 12 ), grid.getTileAtRC( 1, 2 ), WEST, NORTHWEST );
		testAlternatingLine( grid,
				grid.getTileAtRC( 1, 18 ), grid.getTileAtRC( 5, 12 ), WEST, SOUTHWEST );
		testAlternatingLine( grid,
				grid.getTileAtRC( 12, 1 ), grid.getTileAtRC( 8, 7 ), EAST, NORTHEAST );
		testAlternatingLine( grid,
				grid.getTileAtRC( 10, 8 ), grid.getTileAtRC( 15, 16 ), EAST, SOUTHEAST );
		
		// test various indirect lines with ends at hex grid edges
		for (int i = 1; i < 19; i++) {
			testIndirectLine( grid, grid.getTileAtRC( 0, 0 ), grid.getTileAtRC( i, 19 ));
			if (i != 10)
				testIndirectLine( grid, grid.getTileAtRC( 0, 0 ), grid.getTileAtRC( 19, i ));
			
			testIndirectLine( grid, grid.getTileAtRC( 0, 19 ), grid.getTileAtRC( i, 0 ));
			if (i != 10)
				testIndirectLine( grid, grid.getTileAtRC( 0, 19 ), grid.getTileAtRC( 19, i ));
			
			testIndirectLine( grid, grid.getTileAtRC( 19, 0 ), grid.getTileAtRC( i, 19 ));
			if (i != 9)
				testIndirectLine( grid, grid.getTileAtRC( 19, 0 ), grid.getTileAtRC( 0, i ));
			
			testIndirectLine( grid, grid.getTileAtRC( 19, 20 ), grid.getTileAtRC( i, 0 ));
			if (i != 10)
				testIndirectLine( grid, grid.getTileAtRC( 19, 20 ), grid.getTileAtRC( 0, i ));
		}
		
		// miscellaneous direct path tests where line ends at the target
		testDirectPath( grid, 0, 0, 0, 0, false );
		testDirectPath( grid, 10, 6, 10, 15, false );
		testDirectPath( grid, 4, 6, 15, 12, false );
		testDirectPath( grid, 8, 15, 18, 10, false );
		testDirectPath( grid, 15, 18, 15, 3, false );
		testDirectPath( grid, 16, 17, 0, 9, false );
		testDirectPath( grid, 17, 2, 2, 9, false );
		
		// miscellaneous direct path tests where line goes through the target to a grid edge
		testDirectPath( grid, 3, 14, 3, 19, true );
		testDirectPath( grid, 6, 12, 17, 18, true );
		testDirectPath( grid, 5, 10, 15, 5, true );
		testDirectPath( grid, 19, 16, 19, 1, true );
		testDirectPath( grid, 10, 16, 5, 14, true );
		testDirectPath( grid, 15, 8, 4, 13, true );
		
		// indirect path tests where start hex varies by one row and column
		for (int i = 0; i < 4; i++)
			for (int j = 1; j < 20; j++) {
				testIndirectPath( grid, 10 - i / 2, 10 - i % 2, 0, j, false );
				testIndirectPath( grid, 10 - i / 2, 10 - i % 2, 18, j, false );
				testIndirectPath( grid, 10 - i / 2, 10 - i % 2, j, 19, false );
				testIndirectPath( grid, 10 - i / 2, 10 - i % 2, j, 1, false );
			}
		
		// miscellaneous indirect path tests where line goes through the target to a grid edge
		testIndirectPath( grid, 8, 6, 7, 5, true );
		testIndirectPath( grid, 8, 6, 7, 4, true );
		testIndirectPath( grid, 8, 6, 6, 4, true );
		
		testIndirectPath( grid, 8, 6, 7, 8, true );
		testIndirectPath( grid, 8, 6, 7, 9, true );
		testIndirectPath( grid, 8, 6, 6, 8, true );
		
		testIndirectPath( grid, 8, 6, 9, 5, true );
		testIndirectPath( grid, 8, 6, 9, 4, true );
		testIndirectPath( grid, 8, 6, 10, 4, true );
		
		testIndirectPath( grid, 8, 6, 9, 8, true );
		testIndirectPath( grid, 8, 6, 9, 9, true );
		testIndirectPath( grid, 8, 6, 10, 8, true );
		
		testIndirectPath( grid, 8, 6, 6, 6, true );
		testIndirectPath( grid, 8, 6, 5, 6, true );
		testIndirectPath( grid, 8, 6, 5, 7, true );
		
		testIndirectPath( grid, 8, 6, 10, 6, true );
		testIndirectPath( grid, 8, 6, 11, 6, true );
		testIndirectPath( grid, 8, 6, 11, 7, true );
		
		testIndirectPath( grid, 3, 0, 11, 0, true );
		testIndirectPath( grid, 17, 20, 5, 20, true );
		
		testIndirectPath( grid, 18, 7, 19, 13, true );
		testIndirectPath( grid, 1, 13, 0, 4, true );
		
		testIndirectPath( grid, 8, 6, 9, 5, true );
		testIndirectPath( grid, 8, 6, 10, 8, true );
		
		testIndirectPath( grid, 8, 13, 6, 11, true );
		testIndirectPath( grid, 8, 13, 7, 15, true );
		testIndirectPath( grid, 8, 13, 9, 15, true );
		testIndirectPath( grid, 8, 13, 10, 11, true );
		
		testIndirectPath( grid, 9, 7, 8, 5, true );
		testIndirectPath( grid, 9, 7, 10, 5, true );
		testIndirectPath( grid, 9, 13, 8, 14, true );
		testIndirectPath( grid, 9, 13, 10, 14, true );
	}
	
	@Test( expected = NullPointerException.class )
	public void testHexLineHelperNullPointerException1() {
		FilledRowHexGrid grid = FilledRowHexGrid.createWithHexSize( 1, 1, 1, 1, 1 );
		grid.createLineHelper( null, grid.getTileAtRC( 0, 0 ));
	}

	@Test( expected = NullPointerException.class )
	public void testHexLineHelperNullPointerException2() {
		FilledRowHexGrid grid = FilledRowHexGrid.createWithHexSize( 1, 1, 1, 1, 1 );
		grid.createLineHelper( grid.getTileAtRC( 0, 0 ), null );
	}

	@Test( expected = NullPointerException.class )
	public void testHexLineHelperNullPointerException3() {
		FilledRowHexGrid grid = FilledRowHexGrid.createWithHexSize( 1, 1, 1, 1, 1 );
		grid.createLineHelper( grid.getTileAtRC( 0, 0 ),
				grid.getTileAtRC( 0, 0 )).getDistanceToLine( (Hex)null );
	}

	@Test( expected = NullPointerException.class )
	public void testHexLineHelperNullPointerException4() {
		FilledRowHexGrid grid = FilledRowHexGrid.createWithHexSize( 1, 1, 1, 1, 1 );
		grid.createLineHelper( grid.getTileAtRC( 0, 0 ),
				grid.getTileAtRC( 0, 0 )).getDistanceToLine( (Point)null );
	}

	@Test( expected = NullPointerException.class )
	public void testHexLineHelperNullPointerException5() {
		FilledRowHexGrid grid = FilledRowHexGrid.createWithHexSize( 1, 1, 1, 1, 1 );
		grid.createLineHelper( grid.getTileAtRC( 0, 0 ),
				grid.getTileAtRC( 0, 0 )).getDistanceToSource( (Hex)null );
	}

	@Test( expected = NullPointerException.class )
	public void testHexLineHelperNullPointerException6() {
		FilledRowHexGrid grid = FilledRowHexGrid.createWithHexSize( 1, 1, 1, 1, 1 );
		grid.createLineHelper( grid.getTileAtRC( 0, 0 ),
				grid.getTileAtRC( 0, 0 )).getDistanceToSource( (Point)null );
	}
	
	@Test
	public void testLineOfSight() {
		TileGrid <Hex> grid = FilledRowHexGrid.createWithHexSize( 1, 1, 7, 10, 10 );
		Hex hex = grid.getTileAtRC( 3, 3 );
		Terrain testTerrain = new Terrain( 1f );
		
		for (Hex[] row : grid.getTiles())
			for (Hex h : row)
				h.setTerrain( testTerrain );
		
		// always has line of sight to hex itself
		LineHelper <Hex> helper = grid.createLineHelper( hex, hex );
		assertTrue( TileGrid.hasLineOfSight( helper, null ));
		
		Obstacle obstacle = new TestObstacle( LOW );
		helper = grid.createLineHelper( hex, hex );
		assertTrue( TileGrid.hasLineOfSight( helper, obstacle ));
		
		obstacle.setPosition( EAST, hex );
		helper = grid.createLineHelper( hex, hex );
		assertTrue( TileGrid.hasLineOfSight( helper, null ));
		helper = grid.createLineHelper( hex, hex );
		assertTrue( TileGrid.hasLineOfSight( helper, obstacle ));
		
		// test with the obstacle in the line's source and target hexes
		helper = grid.createLineHelper( grid.getTileAtRC( 5, 4 ), hex );
		assertTrue( TileGrid.hasLineOfSight( helper, obstacle ));
		
		helper = grid.createLineHelper( hex, grid.getTileAtRC( 5, 4 ));
		assertTrue( TileGrid.hasLineOfSight( helper, obstacle ));

		Hex source = grid.getTileAtRC( 3, 2 );
		Hex dst = grid.getTileAtRC( 3, 4 );
		Obstacle lowObstacle = new TestObstacle( LOW );
		lowObstacle.setPosition( EAST, dst );
		
		// obstacle in target hex doesn't block line of sight to that hex
		helper = grid.createLineHelper( source, dst );
		assertTrue( TileGrid.hasLineOfSight( helper, obstacle ));
		
		// obstacle doesn't block its own line of sight, but blocks another's
		helper = grid.createLineHelper( source, dst );
		assertFalse( TileGrid.hasLineOfSight( helper, lowObstacle ));
		
		// low obstacle blocks the line of sight of a high obstacle
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
		dst = grid.getTileAtRC( 2, 5 );
		highObstacle.setLocation( source );
		helper = grid.createLineHelper( source, dst );
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
		
		Direction[] neighbors = Hex.getHexSides();
		
		int fromRow = 5;
		int fromCol = 4;
		
		// tests for seeing over an obstacle to another obstacle
		Hex losToHex = grid.getTileAtRC( fromRow, fromCol - 4 );
		Hex losOverHex = grid.getTileAtRC( fromRow, fromCol - 2 );
		Hex losFromHex = grid.getTileAtRC( fromRow, fromCol );
		TestObstacle losForObstacle = new TestObstacle( LOW );
		TestObstacle losOverObstacle = new TestObstacle( LOW );
		TestObstacle losToObstacle = new TestObstacle( LOW );
		
		losForObstacle.setPosition( WEST, losFromHex );
		losOverObstacle.setPosition( EAST, losOverHex );
		losToObstacle.setPosition( EAST, losToHex );

		helper = grid.createLineHelper( losFromHex, losToHex );
		assertFalse( TileGrid.hasLineOfSight( helper, losForObstacle ));
		
		helper = grid.createLineHelper( losFromHex, losToHex );
		losOverObstacle.setBlocksLineOfSight( false );
		assertTrue( TileGrid.hasLineOfSight( helper, losForObstacle ));
		losOverObstacle.setBlocksLineOfSight( true );
		
		// the obstacle doesn't block its own line of sight
		helper = grid.createLineHelper( losFromHex, losToHex );
		assertTrue( TileGrid.hasLineOfSight( helper, losOverObstacle ));
		
		// the obstacle also doesn't block if it's the line of sight's target
		helper = grid.createLineHelper( losFromHex, losToHex );
		assertTrue( TileGrid.hasLineOfSight( helper, losForObstacle, losOverObstacle ));
		
		losOverObstacle.setLocation( null );
		
		// test obstacle height ratios at a distance of four, obstacle in the middle
		Height[] heights = { FLAT, LOW, HIGH, VERY_HIGH, BLOCKING };
		for (Height fromH : heights) {
			losForObstacle.setHeight( fromH );
			
			// line of sight to ground
			helper = grid.createLineHelper( losFromHex, losToHex );
			assertTrue( TileGrid.hasLineOfSight( helper, losForObstacle ));
			
			// line of sight to target obstacle without intervening obstacle
			helper = grid.createLineHelper( losFromHex, losToHex );
			assertTrue( TileGrid.hasLineOfSight( helper, losForObstacle, losToObstacle ));
			
			losOverObstacle.setLocation( losOverHex );
			
			// test different heights for the intervening obstacle
			for (Height overH : heights) {
				losOverObstacle.setHeight( overH );
			
				// test to ground
				helper = grid.createLineHelper( losFromHex, losToHex );
				assertSame( fromH != FLAT && (fromH.compareTo( HIGH ) <= 0 && overH == FLAT ||
						fromH.compareTo( HIGH ) > 0 && overH.compareTo( LOW ) <= 0),
						TileGrid.hasLineOfSight( helper, losForObstacle ));
				
				// test to target obstacles of varying height
				for (Height toH : heights) {
					losToObstacle.setHeight( toH );
				
					helper = grid.createLineHelper( losFromHex, losToHex );
					assertSame( toH.compareTo( overH ) > 0 && fromH.compareTo( overH ) >= 0 ||
							toH.ordinal() > overH.ordinal() + 1 &&
							fromH.ordinal() >= overH.ordinal() - 1 ||
							fromH.compareTo( overH ) > 0 && toH.compareTo( overH ) >= 0 ||
							fromH.ordinal() > overH.ordinal() + 1 &&
							toH.ordinal() >= overH.ordinal() - 1,
							TileGrid.hasLineOfSight( helper, losForObstacle, losToObstacle ));
					
					// test line of sight from the source hex's neighbors
					for (Direction d : neighbors) {
						helper = grid.createLineHelper( losFromHex.getNeighbor( d ), losToHex );
						
						int index = heights.length * heights.length * (fromH.ordinal() -
								heights[0].ordinal()) + heights.length * (overH.ordinal() -
										heights[0].ordinal()) + toH.ordinal() -
										heights[0].ordinal();
						double distRatio = losOverHex.getCenter().distance(
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
		Hex losOverHex2 = losFromHex;
		losFromHex = grid.getTileAtRC( fromRow, fromCol );
		losForObstacle.setLocation( losFromHex );
		Obstacle losOverObstacle2 = new TestObstacle( LOW );
		losOverObstacle2.setFacing( EAST );
		
		// test obstacle height ratios at a distance of six
		for (Height fromH : heights) {
			losForObstacle.setHeight( fromH );
			
			// line of sight to ground
			helper = grid.createLineHelper( losFromHex, losToHex );
			assertTrue( TileGrid.hasLineOfSight( helper, losForObstacle ));
			
			// line of sight to target obstacle without intervening obstacle
			helper = grid.createLineHelper( losFromHex, losToHex );
			assertTrue( TileGrid.hasLineOfSight( helper, losForObstacle, losToObstacle ));
			
			// place obstacle at two thirds of distance from the source to the target
			losOverObstacle.setLocation( losOverHex );
			
			// test different heights for the intervening obstacle
			for (Height overH : heights) {
				losOverObstacle.setHeight( overH );
				
				// test to ground
				helper = grid.createLineHelper( losFromHex, losToHex );
				assertSame( fromH != FLAT && overH == FLAT || fromH == BLOCKING && overH == LOW,
						TileGrid.hasLineOfSight( helper, losForObstacle ));
				
				// test to target obstacles of varying height
				for (Height toH : heights) {
					losToObstacle.setHeight( toH );
					
					helper = grid.createLineHelper( losFromHex, losToHex );
					assertSame( toH.compareTo( overH ) > 0 && fromH.compareTo( overH ) >= 0 ||
							toH.ordinal() > overH.ordinal() + 1 &&
							fromH.ordinal() >= overH.ordinal() - 1 ||
							fromH.compareTo( overH ) > 0 && toH.compareTo( overH ) >= 0 ||
							toH.compareTo( overH ) > 0 &&
							toH.ordinal() - overH.ordinal() == overH.ordinal() - fromH.ordinal() ||
							fromH == BLOCKING && toH == FLAT && overH.compareTo( LOW ) <= 0,
							TileGrid.hasLineOfSight( helper, losForObstacle, losToObstacle ));
					
					// test line of sight from the source hex's neighbors
					for (Direction d : neighbors) {
						helper = grid.createLineHelper( losFromHex.getNeighbor( d ), losToHex );
						
						int index = heights.length * heights.length * (fromH.ordinal() -
								heights[0].ordinal()) + heights.length * (overH.ordinal() -
										heights[0].ordinal()) + toH.ordinal() -
										heights[0].ordinal();
						double distRatio = losOverHex.getCenter().distance(
								helper.getSource().getCenter() ) / helper.getLineLength();
						distRatio = fromH.compareTo( toH ) < 0 ? distRatio : 1 - distRatio;
						
						assertSame( distRatio > heightRatios[ index ],
								TileGrid.hasLineOfSight( helper, losForObstacle, losToObstacle ));
					}
				}
			}
			
			// place obstacle at one third of distance from the source to the target
			losOverObstacle.setLocation( null );
			losOverObstacle2.setLocation( losOverHex2 );
			
			// test different heights for the intervening obstacle
			for (Height overH : heights) {
				losOverObstacle2.setHeight( overH );
				
				// test to ground
				helper = grid.createLineHelper( losFromHex, losToHex );
				assertSame( fromH.ordinal() > overH.ordinal() + 1 ||
						fromH.compareTo( overH ) > 0 && overH.compareTo( LOW ) <= 0,
						TileGrid.hasLineOfSight( helper, losForObstacle ));
				
				// test to target obstacles of varying height
				for (Height toH : heights) {
					losToObstacle.setHeight( toH );
					
					helper = grid.createLineHelper( losFromHex, losToHex );
					assertSame( toH.compareTo( overH ) > 0 && fromH.compareTo( overH ) >= 0 ||
							fromH.compareTo( overH ) > 0 && toH.compareTo( overH ) >= 0 ||
							fromH.ordinal() > overH.ordinal() + 1 &&
							toH.ordinal() >= overH.ordinal() - 1 || fromH.compareTo( overH ) > 0 &&
							fromH.ordinal() - overH.ordinal() == overH.ordinal() - toH.ordinal() ||
							fromH == FLAT && toH == BLOCKING && overH.compareTo( LOW ) <= 0,
							TileGrid.hasLineOfSight( helper, losForObstacle, losToObstacle ));

					boolean[] crossesObstacle = new boolean[ neighbors.length ];
					Arrays.fill( crossesObstacle, true );
					crossesObstacle[ NORTHWEST.hexDir() ] =
							crossesObstacle[ SOUTHWEST.hexDir() ] = false;
					
					// test line of sight from the source hex's neighbors
					// from some, line of sight isn't across the hex with the obstacle
					for (Direction d : neighbors) {
						helper = grid.createLineHelper( losFromHex.getNeighbor( d ), losToHex );
						
						int index = heights.length * heights.length * (fromH.ordinal() -
								heights[0].ordinal()) + heights.length * (overH.ordinal() -
										heights[0].ordinal()) + toH.ordinal() -
										heights[0].ordinal();
						double distRatio = losOverHex2.getCenter().distance(
								helper.getSource().getCenter() ) / helper.getLineLength();
						distRatio = fromH.compareTo( toH ) < 0 ? distRatio : 1 - distRatio;
						
						assertSame( distRatio > heightRatios[ index ] ||
								!crossesObstacle[ d.hexDir() ],
								TileGrid.hasLineOfSight( helper, losForObstacle, losToObstacle ));
					}
				}
			}
			
			// reset for next iteration
			losOverObstacle2.setLocation( null );
			losToObstacle.setHeight( LOW );
		}
		
		fromCol = 8;
		Hex losOverHex3 = losFromHex;
		losFromHex = grid.getTileAtRC( fromRow, fromCol );
		losForObstacle.setLocation( losFromHex );
		Obstacle losOverObstacle3 = new TestObstacle( LOW );
		losOverObstacle3.setFacing( EAST );
		
		// test obstacle height ratios at a distance of eight
		for (Height fromH : heights) {
			losForObstacle.setHeight( fromH );
			
			// line of sight to ground
			helper = grid.createLineHelper( losFromHex, losToHex );
			assertTrue( TileGrid.hasLineOfSight( helper, losForObstacle ));
			
			// line of sight to target obstacle without intervening obstacle
			helper = grid.createLineHelper( losFromHex, losToHex );
			assertTrue( TileGrid.hasLineOfSight( helper, losForObstacle, losToObstacle ));
			
			// place obstacle at three quarters of distance from the source to the target
			losOverObstacle.setLocation( losOverHex );
			
			// test different heights for the intervening obstacle
			for (Height overH : heights) {
				losOverObstacle.setHeight( overH );
				
				// test to ground
				helper = grid.createLineHelper( losFromHex, losToHex );
				assertSame( fromH != FLAT && overH == FLAT,
						TileGrid.hasLineOfSight( helper, losForObstacle ));
				
				// test to target obstacles of varying height
				for (Height toH : heights) {
					losToObstacle.setHeight( toH );
					
					helper = grid.createLineHelper( losFromHex, losToHex );
					assertSame( toH.compareTo( overH ) > 0 && (fromH != FLAT || toH != BLOCKING ||
							overH != VERY_HIGH) || fromH.compareTo( overH ) > 0 &&
							toH.compareTo( overH ) >= 0,
							TileGrid.hasLineOfSight( helper, losForObstacle, losToObstacle ));
					
					// test line of sight from the source hex's neighbors
					for (Direction d : neighbors) {
						helper = grid.createLineHelper( losFromHex.getNeighbor( d ), losToHex );
						
						int index = heights.length * heights.length * (fromH.ordinal() -
								heights[0].ordinal()) + heights.length * (overH.ordinal() -
										heights[0].ordinal()) + toH.ordinal() -
										heights[0].ordinal();
						double distRatio = losOverHex.getCenter().distance(
								helper.getSource().getCenter() ) / helper.getLineLength();
						distRatio = fromH.compareTo( toH ) < 0 ? distRatio : 1 - distRatio;
						
						assertSame( distRatio > heightRatios[ index ],
								TileGrid.hasLineOfSight( helper, losForObstacle, losToObstacle ));
					}
				}
			}
			
			// place obstacle at half the distance from the source to the target
			losOverObstacle.setLocation( null );
			losOverObstacle2.setLocation( losOverHex2 );
			
			// test different heights for the intervening obstacle
			for (Height overH2 : heights) {
				losOverObstacle2.setHeight( overH2 );
				
				// test to ground
				helper = grid.createLineHelper( losFromHex, losToHex );
				assertSame( fromH != FLAT && (fromH.compareTo( HIGH ) <= 0 && overH2 == FLAT ||
						fromH.compareTo( HIGH ) > 0 && overH2.compareTo( LOW ) <= 0),
						TileGrid.hasLineOfSight( helper, losForObstacle ));
				
				// test to target obstacles of varying height
				for (Height toH : heights) {
					losToObstacle.setHeight( toH );
					
					helper = grid.createLineHelper( losFromHex, losToHex );
					assertSame( toH.compareTo( overH2 ) > 0 && fromH.compareTo( overH2 ) >= 0 ||
							toH.ordinal() > overH2.ordinal() + 1 &&
							fromH.ordinal() >= overH2.ordinal() - 1 ||
							fromH.compareTo( overH2 ) > 0 && toH.compareTo( overH2 ) >= 0 ||
							fromH.ordinal() > overH2.ordinal() + 1 &&
							toH.ordinal() >= overH2.ordinal() - 1,
							TileGrid.hasLineOfSight( helper, losForObstacle, losToObstacle ));

					// test line of sight from the source hex's neighbors
					for (Direction d : neighbors) {
						helper = grid.createLineHelper( losFromHex.getNeighbor( d ), losToHex );
						
						int index = heights.length * heights.length * (fromH.ordinal() -
								heights[0].ordinal()) + heights.length * (overH2.ordinal() -
										heights[0].ordinal()) + toH.ordinal() -
										heights[0].ordinal();
						double distRatio = losOverHex2.getCenter().distance(
								helper.getSource().getCenter() ) / helper.getLineLength();
						distRatio = fromH.compareTo( toH ) < 0 ? distRatio : 1 - distRatio;
						
						assertSame( distRatio > heightRatios[ index ],
								TileGrid.hasLineOfSight( helper, losForObstacle, losToObstacle ));
					}
				}
			}
			
			// place obstacle at one quarter of distance from the source to the target
			losOverObstacle2.setLocation( null );
			losOverObstacle3.setLocation( losOverHex3 );
			
			// test different heights for the intervening obstacle
			for (Height overH3 : heights) {
				losOverObstacle3.setHeight( overH3 );
				
				// test to ground
				helper = grid.createLineHelper( losFromHex, losToHex );
				assertSame( fromH.compareTo( overH3 ) > 0 && (fromH != BLOCKING ||
						fromH.ordinal() > overH3.ordinal() + 1),
						TileGrid.hasLineOfSight( helper, losForObstacle ));
				
				// test to target obstacles of varying height
				for (Height toH : heights) {
					losToObstacle.setHeight( toH );
					
					helper = grid.createLineHelper( losFromHex, losToHex );
					assertSame( fromH.compareTo( overH3 ) > 0 && (fromH != BLOCKING ||
							toH != FLAT || overH3 != VERY_HIGH) || toH.compareTo( overH3 ) > 0 &&
							fromH.compareTo( overH3 ) >= 0,
							TileGrid.hasLineOfSight( helper, losForObstacle, losToObstacle ));
					
					boolean[] crossesObstacle = new boolean[ neighbors.length ];
					crossesObstacle[ WEST.hexDir() ] = crossesObstacle[ EAST.hexDir() ] = true;
					
					// test line of sight from the source hex's neighbors
					// from some, line of sight isn't across the hex with the obstacle
					for (Direction d : neighbors) {
						helper = grid.createLineHelper( losFromHex.getNeighbor( d ), losToHex );
						
						int index = heights.length * heights.length * (fromH.ordinal() -
								heights[0].ordinal()) + heights.length * (overH3.ordinal() -
										heights[0].ordinal()) + toH.ordinal() -
										heights[0].ordinal();
						double distRatio = losOverHex3.getCenter().distance(
								helper.getSource().getCenter() ) / helper.getLineLength();
						distRatio = fromH.compareTo( toH ) < 0 ? distRatio : 1 - distRatio;
						
						assertSame( distRatio > heightRatios[ index ] ||
								!crossesObstacle[ d.hexDir() ],
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
	private void testDirectLine( TileGrid <Hex> grid, Hex from, Hex to, Direction direction ) {
		LineHelper <Hex> helper = grid.createLineHelper( from, to );
		Hex current = from, previous = null, next = null;
		
		// there's always only one hex, and the line goes through its center
		while (!helper.targetReached()) {
			next = helper.getNextTile( true );
			assertSame( current.getNeighbor( direction ), next );
			assertNull( helper.getNextTile( false ));
			
			assertSame( current, helper.getCurrentTile( true ));
			assertNull( helper.getCurrentTile( false ));
			assertEquals( 0.0, helper.getDistanceToLine( current ), 0.0001 );
			
			assertSame( current, helper.getBetterTile( true ));
			assertNull( helper.getBetterTile( false ));
			
			helper.nextTiles();
			previous = current;
			current = next;
			
			assertSame( previous, helper.getPreviousTile( true ));
			assertNull( helper.getPreviousTile( false ));
		}
		
		// there's no secondary option for target hex, and line goes through its center
		assertSame( current, helper.getCurrentTile( true ));
		assertNull( helper.getCurrentTile( false ));
		assertEquals( 0.0, helper.getDistanceToLine( current ), 0.0001 );
		
		assertSame( to, helper.getCurrentTile( true ));
	}
	
	// tests a path which has alternately one and two hexes
	private void testAlternatingLine( TileGrid <Hex> grid, Hex from, Hex to,
			Direction dir1, Direction dir2 ) {
		LineHelper <Hex> helper = grid.createLineHelper( from, to );
		Hex single = from, previous1 = null, next1 = null;
		Hex current1 = null, current2 = null, previous2 = null, next2 = null;
		
		Point center = from.getCenter();
		double x1 = center.getX();
		double y1 = center.getY();
		
		center = to.getCenter();
		double x2 = center.getX();
		double y2 = center.getY();
		
		while (!helper.targetReached()) {
			next1 = helper.getNextTile( true );
			next2 = helper.getNextTile( false );
			
			// every other step there's only one hex, and the line goes through its center
			if (single != null) {
				assertSame( single, helper.getCurrentTile( true ));
				if (single != from)
					assertFalse( TileGrid.intersects(
							helper.getCurrentTile( false ), x1, y1, x2, y2 ));
				assertEquals( 0.0, helper.getDistanceToLine( single ), 0.001 );
				
				assertSame( single, helper.getBetterTile( true ));
				assertNull( helper.getBetterTile( false ));
				
				assertTrue(
						next1 == single.getNeighbor( dir1 ) &&
						next2 == single.getNeighbor( dir2 ) ||
						next1 == single.getNeighbor( dir2 ) &&
						next2 == single.getNeighbor( dir1 ));
			}
			
			// every other step has two hexes, and the line goes along the edge between them
			else {
				assertTrue( helper.tilesAreEqual() );
				assertEquals( helper.getDistanceToLine( current1 ),
						helper.getDistanceToLine( current2 ), 0.001 );
				
				assertTrue(
						current1 == helper.getBetterTile( true ) &&
						current2 == helper.getBetterTile( false ) ||
						current1 == helper.getBetterTile( false ) &&
						current2 == helper.getBetterTile( true ));
				
				assertTrue(
						next1 == current1.getNeighbor( dir1 ) &&
						next2 == current2.getNeighbor( dir1 ) ||
						next1 == current1.getNeighbor( dir2 ) &&
						next2 == current2.getNeighbor( dir2 ));
			}
			
			helper.nextTiles();
			
			// every other step there's only one hex, and the line goes through its center
			if (single != null) {
				previous1 = single;
				previous2 = current2;
				single = null;
				
				current1 = helper.getCurrentTile( true );
				current2 = helper.getCurrentTile( false );
				
				assertTrue(
						current1 == previous1.getNeighbor( dir1 ) &&
						current2 == previous1.getNeighbor( dir2 ) ||
						current1 == previous1.getNeighbor( dir2 ) &&
						current2 == previous1.getNeighbor( dir1 ));
			}
			
			// every other step has two hexes, and the line goes along the edge between them
			else {
				previous1 = current1;
				previous2 = current2;

				single = helper.getCurrentTile( true );
				current1 = null;
				current2 = helper.getCurrentTile( false );
				
				assertTrue(
						single == previous1.getNeighbor( dir1 ) &&
						single == previous2.getNeighbor( dir2 ) ||
						single == previous1.getNeighbor( dir2 ) &&
						single == previous2.getNeighbor( dir1 ));
			}
			
			assertTrue(
					previous1 == helper.getPreviousTile( true ) &&
					previous2 == helper.getPreviousTile( false ) ||
					previous1 == helper.getPreviousTile( false ) &&
					previous2 == helper.getPreviousTile( true ));
		}
		
		// ends with a single hex, which is the target
		assertSame( to, single );
		assertSame( to, helper.getBetterTile( true ));
		assertFalse( TileGrid.intersects( helper.getCurrentTile( false ), x1, y1, x2, y2 ));
		assertNull( helper.getBetterTile( false ));
		assertEquals( 0.0, helper.getDistanceToLine( to ), 0.001 );
	}
	
	// tests indirect lines where the shape of the path isn't easily defined
	private void testIndirectLine( TileGrid <Hex> grid, Hex from, Hex to ) {
		LineHelper <Hex> helper = grid.createLineHelper( from, to );
		
		Point center = from.getCenter();
		double x1 = center.getX();
		double y1 = center.getY();
		
		center = to.getCenter();
		double x2 = center.getX();
		double y2 = center.getY();
		
		Hex current1 = from, current2 = null;
		Hex previous1 = null, previous2 = null;
		Hex next1 = null, next2 = null;
		
		// check all hexes until the target
		while (!helper.targetReached()) {
			next1 = helper.getNextTile( true );
			next2 = helper.getNextTile( false );
			
			assertSame( current1, helper.getCurrentTile( true ));
			assertSame( current2, helper.getCurrentTile( false ));
			
			Hex better = helper.getBetterTile( true );
			Hex worse = helper.getBetterTile( false );
			assertNotNull( better );
			
			// if hexes are equal, they have the same distance to the line
			if (helper.tilesAreEqual())
				assertEquals( helper.getDistanceToLine( better ),
						helper.getDistanceToLine( worse ), 0.001 );
			
			// worse option is farther away from the line than the better option
			else if (worse != null)
				assertTrue(
						helper.getDistanceToLine( worse ) > helper.getDistanceToLine( better ));
			
			else if (current2 != null)
				assertFalse( TileGrid.intersects( current2, x1, y1, x2, y2 ));
			
			helper.nextTiles();
			previous1 = current1;
			previous2 = current2;
			current1 = next1;
			current2 = next2;
			
			assertSame( previous1, helper.getPreviousTile( true ));
			assertSame( previous2, helper.getPreviousTile( false ));
			
			// current hexes are switched in order to keep the hex path along the line
			if (current1 != helper.getCurrentTile( true )) {
				assertSame( current1, helper.getCurrentTile( false ));
				
				Hex hex = current1;
				current1 = current2;
				current2 = hex;
			}
			
			else
				assertSame( previous2, helper.getPreviousTile( false ));
		}
		
		// ends at the target, which is the better and only hex
		assertNotNull( current1 );
		assertSame( current1, helper.getCurrentTile( true ));
		assertSame( current2, helper.getCurrentTile( false ));
		
		assertSame( to, helper.getBetterTile( true ));
		assertNull( helper.getBetterTile( false ));
	}
	
	// tests direct paths with the option that the path continues through the target
	private void testDirectPath( TileGrid <Hex> grid,
			int row1, int col1, int row2, int col2, boolean hitEdge ) {
		LineHelper <Hex> helper = grid.createLineHelper(
				grid.getTileAtRC( row1, col1 ), grid.getTileAtRC( row2, col2 ));
		Hex[] directPath = FilledRowHexGrid.getDirectPath( helper, hitEdge );
		int pathLen = 0;
		
		// calculation of number of hexes along hex row and straight diagonal path
		if (!hitEdge)
			pathLen = Math.max( Math.abs( col2 - col1 ), Math.abs( row2 - row1 )) + 1;
		
		// number of hexes on a row from source to a grid edge
		else if (row2 == row1)
			pathLen = (col2 > col1 ?
				(int)(grid.getTotalWidth() / grid.getTileWidth()) - col1 : (col1 + 1));

		// estimation of hexes on a non-horizontal path that continues to a grid edge
		else
			pathLen = (row2 > row1 ? (int)((grid.getTotalHeight() - 1.5 * grid.getTileHeight()) /
					0.75 / grid.getTileHeight()) - row1 : row1) + 1;
		
		Hex[] expectedPath = new Hex[ pathLen ];
		expectedPath[0] = grid.getTileAtRC( row1, col1 );
		int index = 0;
		Direction direction = FilledRowHexGrid.getDirection(
				grid.getTileAtRC( row1, col1 ), grid.getTileAtRC( row2, col2 ));
		
		// direct path goes neighbor to neighbor, always to the same direction
		while (++index < expectedPath.length)
			expectedPath[index] = expectedPath[index - 1].getNeighbor( direction );
		
		assertArrayEquals( expectedPath, directPath );
	}
	
	// tests indirect paths with the option that the line continues to a grid edge
	private void testIndirectPath( TileGrid <Hex> grid,
			int row1, int col1, int row2, int col2, boolean hitEdge ) {
		LineHelper <Hex> helper = grid.createLineHelper(
				grid.getTileAtRC( row1, col1 ), grid.getTileAtRC( row2, col2 ));
		
		Hex[] directPath = FilledRowHexGrid.getDirectPath( helper, hitEdge );
		Hex dst = null;
		
		int pathLen = 0;
		if (!hitEdge)		// line ends at the target
			dst = helper.getTarget();
		
		else				// line goes through target and ends at a grid edge
			dst = findLineDestination( grid, helper )[0];
		
		int startRow = helper.getSource().getRow();
		int rows = Math.abs( startRow - dst.getRow() );
		int columns = Math.abs( helper.getSource().getColumn() - dst.getColumn() );
		Hex[][] hexes = grid.getTiles();

		// estimate number of hexes that are on the path
		if (startRow < hexes.length - 1 &&
				hexes[ startRow ].length < hexes[ startRow + 1 ].length || startRow > 0 &&
				hexes[ startRow ].length < hexes[ startRow - 1 ].length) {
			pathLen = rows / 2;

			if (dst.getColumn() > helper.getSource().getColumn() &&
					dst.getRow() % 2 != startRow % 2)
				columns--;
		}

		else {
			pathLen = (rows + 1) / 2;

			if (dst.getColumn() > helper.getSource().getColumn() &&
					dst.getRow() % 2 != startRow % 2)
				columns++;
		}

		pathLen = columns > pathLen ? rows + columns - pathLen + 1 : rows + 1;
		
		Hex current = grid.getTileAtRC( row1, col1 );
		Point dstCenter = dst.getCenter();
		double previousDist = Point.distance( current.getCenter(), dstCenter );
		assertSame( current, directPath[0] );
		
		// test hexes on the indirect line
		for (int i = 1; i < pathLen; i++) {
			double minDist = Double.MAX_VALUE;
			Hex next = null, equalTile = null;
			
			for (Direction d : Hex.getHexSides()) {
				Hex neighbor = current.getNeighbor( d );

				// each hex must be closer to line end than the previous step's hex
				if (neighbor == null || neighbor.getCenter().distance( dstCenter ) > previousDist)
					continue;
				
				double dist = helper.getDistanceToLine( neighbor );

				// compare distances to find equal hexes
				if (dist + 0.001 >= minDist && dist - 0.001 <= minDist)
					equalTile = neighbor;
				
				else if (dist < minDist) {
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

				// path length estimate doesn't include equal hexes
				i++;
				pathLen++;
				
				previousDist = Math.min( previousDist,
						equalTile.getCenter().distance( dstCenter ));
			}
			
			else
				assertSame( next, directPath[i] );
			
			current = next;
		}
		
		// all hexes have been checked, ensure there was the right number of them
		assertEquals( pathLen, directPath.length );
	}
	
	// finds destination hex for an indirect path which continues to a grid edge
	private Hex[] findLineDestination( TileGrid <Hex> grid, LineHelper <Hex> helper ) {
		int startCol = 0, endCol = 0, startRow = 0, endRow = 0;
		Hex[][] hexes = grid.getTiles();
		
		Hex from = helper.getSource();
		Hex to = helper.getTarget();
		
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
			
			if (from.getColumn() == to.getColumn())
				endCol++;
		}
		
		else if (right && !down) {
			startCol = to.getColumn();
			endCol = hexes[0].length - 1;
			startRow = 1;
			endRow = to.getRow();
			
			if (from.getColumn() == to.getColumn())
				startCol--;
		}
		
		else if (!right && down) {
			startCol = 0;
			endCol = to.getColumn();
			startRow = to.getRow();
			endRow = hexes.length - 2;
			
			if (from.getColumn() == to.getColumn())
				endCol++;
		}
		
		else if (right && down) {
			startCol = to.getColumn();
			endCol = hexes[ hexes.length - 1 ].length - 1;
			startRow = to.getRow();
			endRow = hexes.length - 2;
			
			if (from.getColumn() == to.getColumn())
				startCol--;
		}
		
		LinkedList <Hex> options = new LinkedList <>();
		int row = down ? hexes.length - 1 : 0;
		
		// find horizontal edge hexes which the line intersects (quick check, not precise)
		for (int i = startCol; i <= endCol && i < hexes[row].length; i++)
			if (helper.getDistanceToLine( hexes[row][i] ) < grid.getTileHeight() / 2)
				options.add( hexes[row][i] );
		
		// also search along vertical edge
		for (int i = startRow; i <= endRow; i++) {
			int col = !right ? 0 : hexes[i].length - 1;

			if (helper.getDistanceToLine( hexes[i][col] ) < grid.getTileHeight() / 2)
				options.add( hexes[i][col] );
		}
		
		double maxDist = Double.MIN_VALUE;
		Hex[] hexOptions = options.toArray( new Hex[ options.size() ]);
		Hex best = null, equal = null;
		
		// find hex that's furthest away from the line's source
		for (int i = 0; i < hexOptions.length; i++) {
			double dist = p1.distance( hexOptions[i].getCenter() );
			
			if (dist < maxDist - 0.001)
				options.remove( hexOptions[i] );
			
			else if (dist > maxDist + 0.001) {
				maxDist = dist;
				
				if (best != null)
					options.remove( best );
				
				if (equal != null)
					options.remove( equal );
				
				best = hexOptions[i];
				equal = null;
			}
			
			// line ending in two equal hexes is also possible
			else
				equal = hexOptions[i];
		}
		
		return options.toArray( new Hex[ options.size() ]);
	}
}
