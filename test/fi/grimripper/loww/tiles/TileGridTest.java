package fi.grimripper.loww.tiles;

import static fi.grimripper.loww.Direction.EAST;
import static fi.grimripper.loww.Direction.NORTH;
import static fi.grimripper.loww.Direction.NORTHEAST;
import static fi.grimripper.loww.Direction.NORTHWEST;
import static fi.grimripper.loww.Direction.SOUTH;
import static fi.grimripper.loww.Direction.SOUTHEAST;
import static fi.grimripper.loww.Direction.SOUTHWEST;
import static fi.grimripper.loww.Direction.WEST;
import static fi.grimripper.loww.Height.LOW;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;

import fi.grimripper.loww.Direction;
import fi.grimripper.loww.test.TestObstacle;
import fi.grimripper.loww.test.TestTileGrid;
import fi.grimripper.loww.tiles.FilledRowHexGrid;
import fi.grimripper.loww.tiles.Hex;
import fi.grimripper.loww.tiles.Square;
import fi.grimripper.loww.tiles.Tile;
import fi.grimripper.loww.tiles.TileGrid;

public class TileGridTest {

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
	public void testTileGrid() {
		TestTileGrid grid = new TestTileGrid( 3, 8 );
		
		// constructor sets tile width and height
		assertEquals( 3, grid.getTileWidth(), 0.001 );
		assertEquals( 8, grid.getTileHeight(), 0.001 );

		Tile middle = new Square( 3, 3, 1, 0, 0 );
		
		// directions between tiles for a non-existent 7x7 square grid
		Direction[][] directions = {
			{ NORTHWEST, NORTHWEST, NORTH, NORTH, NORTH, NORTHEAST, NORTHEAST },
			{ NORTHWEST, NORTHWEST, NORTHWEST, NORTH, NORTHEAST, NORTHEAST, NORTHEAST },
			{ WEST, NORTHWEST, NORTHWEST, NORTH, NORTHEAST, NORTHEAST, EAST },
			{ WEST, WEST, WEST, null, EAST, EAST, EAST },
			{ WEST, SOUTHWEST, SOUTHWEST, SOUTH, SOUTHEAST, SOUTHEAST, EAST },
			{ SOUTHWEST, SOUTHWEST, SOUTHWEST, SOUTH, SOUTHEAST, SOUTHEAST, SOUTHEAST },
			{ SOUTHWEST, SOUTHWEST, SOUTH, SOUTH, SOUTH, SOUTHEAST, SOUTHEAST }};
		
		// test direction estimation with center square
		for (int y = 0; y < 7; y++)
			for (int x = 0; x < 7; x++)
				assertSame( directions[y][x],
						TileGrid.getDirection( middle, new Square( x, y, 1, 0, 0 )));
		
		// hexes have different coordinates and dimensions
		double[] hexDim = FilledRowHexGrid.calculateRegularHexDimensions( 1, 1 );
		middle = new Hex( 4 * hexDim[0], 2.25 * hexDim[1], hexDim[0], hexDim[1], 0, 0 );
		
		// different directions for a 7x7 hex grid
		directions = new Direction[][] {
			{ NORTHWEST, NORTHWEST, NORTHWEST, NORTH, NORTH, NORTHEAST, NORTHEAST },
			{ NORTHWEST, NORTHWEST, NORTHWEST, NORTH, NORTHEAST, NORTHEAST, NORTHEAST },
			{ WEST, WEST, NORTHWEST, NORTHWEST, NORTHEAST, NORTHEAST, EAST },
			{ WEST, WEST, WEST, null, EAST, EAST, EAST },
			{ WEST, WEST, SOUTHWEST, SOUTHWEST, SOUTHEAST, SOUTHEAST, EAST },
			{ SOUTHWEST, SOUTHWEST, SOUTHWEST, SOUTH, SOUTHEAST, SOUTHEAST, SOUTHEAST },
			{ SOUTHWEST, SOUTHWEST, SOUTHWEST, SOUTH, SOUTH, SOUTHEAST, SOUTHEAST }};
		
		// test direction estimation with center hex
		for (int y = 0; y < 7; y++)
			for (int x = 0; x < 7; x++)
				assertSame( directions[y][x], TileGrid.getDirection( middle,
						new Hex( (x + y % 2 * 0.5 + 0.5) * hexDim[0], y * 0.75 * hexDim[1],
								hexDim[0], hexDim[1], 0, 0 )));
		
		middle = new Hex( 3.5 * hexDim[0], 2.25 * hexDim[1], hexDim[0], hexDim[1], 0, 0 );
		
		// test with a hex grid whose odd rows are longer than even ones
		directions = new Direction[][] {
			{ NORTHWEST, NORTHWEST, NORTH, NORTH, NORTHEAST, NORTHEAST, NORTHEAST },
			{ NORTHWEST, NORTHWEST, NORTHWEST, NORTH, NORTHEAST, NORTHEAST, NORTHEAST, NORTHEAST },
			{ WEST, NORTHWEST, NORTHWEST, NORTHEAST, NORTHEAST, EAST, EAST },
			{ WEST, WEST, WEST, null, EAST, EAST, EAST, EAST },
			{ WEST, SOUTHWEST, SOUTHWEST, SOUTHEAST, SOUTHEAST, EAST, EAST },
			{ SOUTHWEST, SOUTHWEST, SOUTHWEST, SOUTH, SOUTHEAST, SOUTHEAST, SOUTHEAST, SOUTHEAST },
			{ SOUTHWEST, SOUTHWEST, SOUTH, SOUTH, SOUTHEAST, SOUTHEAST, SOUTHEAST }};
		
		for (int y = 0; y < 7; y++)
			for (int x = 0; x < 7 + y % 2; x++)
				assertSame( directions[y][x], TileGrid.getDirection( middle,
						new Hex( (x + 1 - y % 2 * 0.5) * hexDim[0], y * 0.75 * hexDim[1],
								hexDim[0], hexDim[1], 0, 0 )));
	}

	@Test( expected = NullPointerException.class )
	public void testLineOfSightNullPointerException() {
		TileGrid.hasLineOfSight( null, new TestObstacle( LOW ));
	}
}
