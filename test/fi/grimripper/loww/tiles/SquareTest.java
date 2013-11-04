package fi.grimripper.loww.tiles;

import static fi.grimripper.loww.Direction.EAST;
import static fi.grimripper.loww.Direction.NORTH;
import static fi.grimripper.loww.Direction.NORTHEAST;
import static fi.grimripper.loww.Direction.NORTHWEST;
import static fi.grimripper.loww.Direction.SOUTH;
import static fi.grimripper.loww.Direction.SOUTHEAST;
import static fi.grimripper.loww.Direction.SOUTHWEST;
import static fi.grimripper.loww.Direction.WEST;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import fi.grimripper.loww.Direction;
import fi.grimripper.loww.Point;
import fi.grimripper.loww.tiles.FilledSquareGrid;
import fi.grimripper.loww.tiles.Hex;
import fi.grimripper.loww.tiles.Square;
import fi.grimripper.loww.tiles.Tile;

public class SquareTest {

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
	public void testSquares() {
		// squares can have neighbors in all eight compass directions
		Direction[] neighbors = Square.getNeighborDirections();
		Arrays.sort( neighbors );
		Direction[] directions = Direction.values();
		Arrays.sort( directions );
		assertArrayEquals( directions, neighbors );
		
		Square square = new Square( 0, 0, 0, 1, 2 );
		square.toString();
		
		assertEquals( 1, square.getRow() );
		assertEquals( 2, square.getColumn() );
		
		// square points are only in diagonal directions
		assertNull( square.getPoint( null ));
		assertNull( square.getPoint( NORTH ));
		assertNull( square.getPoint( EAST ));
		assertNull( square.getPoint( SOUTH ));
		assertNull( square.getPoint( WEST ));
		
		Direction[] points = square.getPointDirections();
		Arrays.sort( points );
		
		Direction[] expected = { NORTHEAST, SOUTHEAST, SOUTHWEST, NORTHWEST };
		Arrays.sort( expected );
		
		assertArrayEquals( expected, points );
		
		testSquare( 15, 20, 25 );
		testSquare( -1, -1, 0 );
		testSquare( 0, 0, -3 );
	}
	
	@Test
	public void testNeighbors() {
		Square square = new Square( 0, 0, 0, 0, 0 );
		Square otherSquare = new Square( 0, 0, 0, 0, 0 );
		Square remoteSquare = new Square( 0, 0, 0, 0, 0 );
		
		// no neighbors set, so none should be available
		assertArrayEquals( new Square[0], square.getNeighbors() );
		assertArrayEquals( new Square[0], square.getRemoteNeighbors() );
		for (Direction d : Direction.values())
			assertNull( square.getNeighbor( d ));
		
		// set one neighbor, so it and only it should be available
		square.setNeighbor( otherSquare, NORTH );
		assertArrayEquals( new Square[] { otherSquare }, square.getNeighbors() );
		assertSame( otherSquare, square.getNeighbor( NORTH ));
		
		// added a remote neighbor, which is available in addition to the regular neighbor
		square.addRemoteNeighbor( remoteSquare );
		assertArrayEquals( new Square[] { remoteSquare }, square.getRemoteNeighbors() );
		
		// remove a neighbor and test that it's no longer available
		square.setNeighbor( null, NORTH );
		assertArrayEquals( new Square[0], square.getNeighbors() );
		assertArrayEquals( new Square[] { remoteSquare }, square.getRemoteNeighbors() );
		
		// remove a remote neighbor and test that it's no longer available
		square.removeRemoteNeighbor( remoteSquare );
		assertArrayEquals( new Square[0], square.getNeighbors() );
		assertArrayEquals( new Square[0], square.getRemoteNeighbors() );
	}
	
	@Test
	public void testSquareGridProperties() {
		FilledSquareGrid grid = new FilledSquareGrid( 20, 5, 5 );
		Square center = grid.getTileAtRC( 2, 2 );

		HashMap <Direction, Square> neighborsByDir = new HashMap <>();
		for (Direction d : Direction.values())
			neighborsByDir.put( d, center.getNeighbor( d ));
		Map <Direction, Point> squarePoints = getSquarePoints( center );

		// test getting non-diagonal neighbors by coordinates, one pixel outside the square
		assertSame( neighborsByDir.get( NORTH ),
				center.containsCoords( squarePoints.get( NORTHWEST ).getX(),
						squarePoints.get( NORTHEAST ).getY() - 1 ));
		assertSame( neighborsByDir.get( EAST ),
				center.containsCoords( squarePoints.get( NORTHEAST ).getX() + 1,
						squarePoints.get( SOUTHEAST ).getY() ));
		assertSame( neighborsByDir.get( SOUTH ),
				center.containsCoords( squarePoints.get( SOUTHEAST ).getX(),
						squarePoints.get( SOUTHWEST ).getY() + 1 ));
		assertSame( neighborsByDir.get( WEST ),
				center.containsCoords( squarePoints.get( SOUTHWEST ).getX() - 1,
						squarePoints.get( NORTHWEST ).getY() ));

		// test getting diagonal neighbors by coordinates, one pixel diagonally outside the square
		assertSame( neighborsByDir.get( NORTHEAST ),
				center.containsCoords( squarePoints.get( NORTHEAST ).getX() + 1,
						squarePoints.get( NORTHEAST ).getY() - 1 ));
		assertSame( neighborsByDir.get( SOUTHEAST ),
				center.containsCoords( squarePoints.get( SOUTHEAST ).getX() + 1,
						squarePoints.get( SOUTHEAST ).getY() + 1 ));
		assertSame( neighborsByDir.get( SOUTHWEST ),
				center.containsCoords( squarePoints.get( SOUTHWEST ).getX() - 1,
						squarePoints.get( SOUTHWEST ).getY() + 1 ));
		assertSame( neighborsByDir.get( NORTHWEST ),
				center.containsCoords( squarePoints.get( NORTHWEST ).getX() - 1,
						squarePoints.get( NORTHWEST ).getY() - 1 ));

		// test that corner coordinates are within the square
		assertSame( center, center.containsCoords(
				squarePoints.get( NORTHEAST ).getX(), squarePoints.get( NORTHEAST ).getY() ));
		assertSame( center, center.containsCoords(
				squarePoints.get( SOUTHEAST ).getX(), squarePoints.get( SOUTHEAST ).getY() ));
		assertSame( center, center.containsCoords(
				squarePoints.get( SOUTHWEST ).getX(), squarePoints.get( SOUTHWEST ).getY() ));
		assertSame( center, center.containsCoords(
				squarePoints.get( NORTHWEST ).getX(), squarePoints.get( NORTHWEST ).getY() ));
		
		// test adjacency
		for (Square[] row : grid.getTiles())
			for (Square s : row)
				assertSame( center.isAdjacent( s ), neighborsByDir.containsValue( s ));

		// test invalid cases of getting direction to a tile
		assertNull( center.getDirection( new Hex( 0, 0, 0, 0, 0, 0 )));
		assertNull( center.getDirection( center ));
	}
	
	@Test( expected = ArrayStoreException.class )
	public void testNeighborArrayStoreException() {
		Square square = new Square( 0, 0, 0, 0, 0 );
		Tile tile = (Tile)square;
		
		tile.setNeighbor( new Hex( 0, 0, 0, 0, 0, 0 ), EAST );
	}
	
	@Test( expected = ArrayStoreException.class )
	public void testRemoteNeighborArrayStoreException() {
		Square square = new Square( 0, 0, 0, 0, 0 );
		Tile tile = (Tile)square;
		tile.addRemoteNeighbor( new Hex( 0, 0, 0, 0, 0, 0 ));
	}

	// tests a square's points based on given position coordinates and dimension
	private void testSquare( int x, int y, double dimension ) {
		Square square = new Square( x, y, dimension, 0, 0 );
		Map <Direction, Point> squarePoints = getSquarePoints( square );
		
		assertEquals( Square.SQUARE_SIZE, squarePoints.size() );

		assertEquals( x, squarePoints.get( NORTHWEST ).getX(), 0.001 );
		assertEquals( x, squarePoints.get( SOUTHWEST ).getX(), 0.001 );
		
		assertEquals( x + dimension, squarePoints.get( NORTHEAST ).getX(), 0.001 );
		assertEquals( x + dimension, squarePoints.get( SOUTHEAST ).getX(), 0.001 );

		assertEquals( y, squarePoints.get( NORTHWEST ).getY(), 0.001 );
		assertEquals( y, squarePoints.get( NORTHEAST ).getY(), 0.001 );
		
		assertEquals( y + dimension, squarePoints.get( SOUTHWEST ).getY(), 0.001 );
		assertEquals( y + dimension, squarePoints.get( SOUTHEAST ).getY(), 0.001 );
		
		Point center = square.getCenter();
		assertEquals( x + dimension / 2, center.getX(), 0.001 );
		assertEquals( y + dimension / 2, center.getY(), 0.001 );
		
		assertEquals( dimension, square.getWidth(), 0.001 );
		assertEquals( dimension, square.getHeight(), 0.001 );
	}
	
	// maps a square's corner points to directions
	private Map <Direction, Point> getSquarePoints( Square square ) {
		HashMap <Direction, Point> squarePoints = new HashMap <>();
		
		for (Direction d : Square.getSquarePoints())
			squarePoints.put( d, square.getPoint( d ));
		
		return squarePoints;
	}
}
