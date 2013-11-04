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

import fi.grimripper.loww.ArrayUtilities;
import fi.grimripper.loww.Direction;
import fi.grimripper.loww.Point;
import fi.grimripper.loww.tiles.FilledRowHexGrid;
import fi.grimripper.loww.tiles.Hex;
import fi.grimripper.loww.tiles.Square;
import fi.grimripper.loww.tiles.Tile;

public class HexTest {

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
	public void testHex() {
		Hex hex = new Hex( 0, 0, 0, 0, 1, 2 );
		hex.toString();

		assertEquals( 1, hex.getRow() );
		assertEquals( 2, hex.getColumn() );
		
		// hex doesn't have points to the east or west
		assertNull( hex.getPoint( null ));
		assertNull( hex.getPoint( EAST ));
		assertNull( hex.getPoint( WEST ));
		
		Direction[] points = hex.getPointDirections();
		Arrays.sort( points );
		
		Direction[] expected = { NORTH, NORTHEAST, SOUTHEAST, SOUTH, SOUTHWEST, NORTHWEST };
		Arrays.sort( expected );
		
		assertArrayEquals( expected, points );

		testHex( 20, 40, 60, 80 );
		testHex( -1, -1, 0, 0 );
		testHex( 0, 0, -5, -5 );
	}
	
	@Test
	public void testRegularHex() {
		testRegularHex( 10, 30, 50, 70 );
		testRegularHex( 30, 10, 70, 50 );
		testRegularHex( -1, -1, 0, 0 );
		testRegularHex( 0, 0, -7, -7 );
	}
	
	@Test
	public void testNeighbors() {
		Hex hex = new Hex( 0, 0, 0, 0, 0, 0 );
		Hex otherHex = new Hex( 0, 0, 0, 0, 0, 0 );
		Hex remoteHex = new Hex( 0, 0, 0, 0, 0, 0 );
		
		// no neighbors have been added
		assertArrayEquals( new Hex[0], hex.getNeighbors() );
		for (Direction d : Direction.values())
			assertNull( hex.getNeighbor( d ));
		
		// neighbors are not allowed to the north or south
		hex.setNeighbor( otherHex, NORTH );
		assertArrayEquals( new Hex[0], hex.getNeighbors() );
		assertNull( hex.getNeighbor( NORTH ));
		
		// test setting neighbor in a valid direction
		hex.setNeighbor( otherHex, NORTHEAST );
		assertArrayEquals( new Hex[] { otherHex }, hex.getNeighbors() );
		assertSame( otherHex, hex.getNeighbor( NORTHEAST ));
		
		// test adding a remote neighbor
		assertArrayEquals( new Hex[0], hex.getRemoteNeighbors() );
		hex.addRemoteNeighbor( remoteHex );
		assertArrayEquals( new Hex[] { remoteHex }, hex.getRemoteNeighbors() );
	}
	
	@Test
	public void testHexGridProperties() {
		FilledRowHexGrid grid = FilledRowHexGrid.createWithHexSize( 10, 15, 5, 5, 6 );
		Hex center = grid.getTileAtRC( 2, 2 );
		
		HashMap <Direction, Hex> neighborsByDir = new HashMap <>();
		Direction[] sides = Hex.getHexSides();
		
		for (Direction d : sides)
			neighborsByDir.put( d, center.getNeighbor( d ));
		
		Map <Direction, Point> hexPoints = getHexPoints( center );
		Point centerCenter = center.getCenter();
		
		// test getting neighbors by coordinates, top and bottom points, one pixel outside the hex
		assertSame( neighborsByDir.get( NORTHEAST ),
				center.containsCoords( centerCenter.getX(), hexPoints.get( NORTH ).getY() - 1 ));
		assertSame( neighborsByDir.get( SOUTHEAST ),
				center.containsCoords( centerCenter.getX(), hexPoints.get( SOUTH ).getY() + 1 ));
		assertSame( center,
				center.containsCoords( centerCenter.getX(), hexPoints.get( NORTH ).getY() ));
		assertSame( center,
				center.containsCoords( centerCenter.getX(), hexPoints.get( SOUTH ).getY() ));

		// test getting neighbors by coordinates, northwest point, one pixel outside the hex
		assertSame( neighborsByDir.get( NORTHWEST ), center.containsCoords(
				hexPoints.get( NORTHWEST ).getX(), hexPoints.get( NORTHWEST ).getY() - 1 ));
		assertSame( neighborsByDir.get( WEST ), center.containsCoords(
				hexPoints.get( NORTHWEST ).getX() - 1, hexPoints.get( NORTHWEST ).getY() ));
		assertSame( center, center.containsCoords(
				hexPoints.get( NORTHWEST ).getX() + 1, hexPoints.get( NORTHWEST ).getY() ));
		assertSame( center, center.containsCoords(
				hexPoints.get( NORTHWEST ).getX(), hexPoints.get( NORTHWEST ).getY() ));

		// test getting neighbors by coordinates, southwest point, one pixel outside the hex
		assertSame( neighborsByDir.get( SOUTHWEST ), center.containsCoords(
				hexPoints.get( SOUTHWEST ).getX(), hexPoints.get( SOUTHWEST ).getY() + 1 ));
		assertSame( neighborsByDir.get( WEST ), center.containsCoords(
				hexPoints.get( SOUTHWEST ).getX() - 1, hexPoints.get( SOUTHWEST ).getY() ));
		assertSame( center, center.containsCoords(
				hexPoints.get( SOUTHWEST ).getX() + 1, hexPoints.get( SOUTHWEST ).getY() ));
		assertSame( center, center.containsCoords(
				hexPoints.get( SOUTHWEST ).getX(), hexPoints.get( NORTHWEST ).getY() ));

		// test getting neighbors by coordinates, northeast point, one pixel outside the hex
		assertSame( neighborsByDir.get( NORTHEAST ), center.containsCoords(
				hexPoints.get( NORTHEAST ).getX(), hexPoints.get( NORTHEAST ).getY() - 1 ));
		assertSame( neighborsByDir.get( EAST ), center.containsCoords(
				hexPoints.get( NORTHEAST ).getX() + 1, hexPoints.get( NORTHEAST ).getY() ));
		assertSame( center, center.containsCoords(
				hexPoints.get( NORTHEAST ).getX() - 1, hexPoints.get( NORTHEAST ).getY() ));
		assertSame( center, center.containsCoords(
				hexPoints.get( NORTHEAST ).getX(), hexPoints.get( NORTHEAST ).getY() ));
		
		// test getting neighbors by coordinates, southeast point, one pixel outside the hex
		assertSame( neighborsByDir.get( SOUTHEAST ), center.containsCoords(
				hexPoints.get( SOUTHEAST ).getX(), hexPoints.get( SOUTHEAST ).getY() + 1 ));
		assertSame( neighborsByDir.get( EAST ), center.containsCoords(
				hexPoints.get( SOUTHEAST ).getX() + 1, hexPoints.get( SOUTHEAST ).getY() ));
		assertSame( center, center.containsCoords(
				hexPoints.get( SOUTHEAST ).getX() - 1, hexPoints.get( SOUTHEAST ).getY() ));
		assertSame( center, center.containsCoords(
				hexPoints.get( SOUTHEAST ).getX(), hexPoints.get( NORTHEAST ).getY() ));
		
		// test adjacency
		for (Hex[] row : grid.getTiles())
			for (Hex h : row)
				assertSame( center.isAdjacent( h ), neighborsByDir.containsValue( h ));
		
		// test invalid cases of getting direction to a tile
		assertNull( center.getDirection( new Square( 0, 0, 0, 0, 0 )));
		assertNull( center.getDirection( center ));
	}

	@Test( expected = ArrayStoreException.class )
	public void testNeighborArrayStoreException() {
		Hex hex = new Hex( 0, 0, 0, 0, 0, 0 );
		Tile tile = (Tile)hex;
		
		tile.setNeighbor( new Square( 0, 0, 0, 0, 0 ), EAST );
	}

	@Test( expected = ArrayStoreException.class )
	public void testRemoteNeighborArrayStoreException() {
		Hex hex = new Hex( 0, 0, 0, 0, 0, 0 );
		Tile tile = (Tile)hex;
		
		tile.addRemoteNeighbor( new Square( 0, 0, 0, 0, 0 ));
	}
	
	// tests a hex's points based on given position coordinates and dimension
	// doesn't expect the hex to be regular
	private void testHex( int x, int y, double width, double height ) {
		Hex hex = new Hex( x, y, width, height, 0, 0 );
		Map <Direction, Point> hexPoints = getHexPoints( hex );
		
		assertEquals( Hex.HEX_SIZE, hexPoints.size() );
		
		assertEquals( x, hexPoints.get( NORTH ).getX(), 0.001 );
		assertEquals( x, hexPoints.get( SOUTH ).getX(), 0.001 );
		
		assertEquals( x + width / 2.0, hexPoints.get( NORTHEAST ).getX(), 0.001 );
		assertEquals( x - width / 2.0, hexPoints.get( NORTHWEST ).getX(), 0.001 );
		
		assertEquals( x + width / 2.0, hexPoints.get( SOUTHEAST ).getX(), 0.001 );
		assertEquals( x - width / 2.0, hexPoints.get( SOUTHWEST ).getX(), 0.001 );
		
		assertEquals( y, hexPoints.get( NORTH ).getY(), 0.001 );
		assertEquals( y + height, hexPoints.get( SOUTH ).getY(), 0.001 );
		
		assertEquals(
				hexPoints.get( NORTHWEST ).getY(), hexPoints.get( NORTHEAST ).getY(), 0.001 );
		assertEquals(
				hexPoints.get( SOUTHWEST ).getY(), hexPoints.get( SOUTHEAST ).getY(), 0.001 );
		
		assertEquals( width,
				hexPoints.get( NORTHEAST ).getX() - hexPoints.get( NORTHWEST ).getX(), 0.001 );
		assertEquals( width,
				hexPoints.get( SOUTHEAST ).getX() - hexPoints.get( SOUTHWEST ).getX(), 0.001 );
		assertEquals( width, hex.getWidth(), 0.001 );
		
		assertEquals( height,
				hexPoints.get( SOUTH ).getY() - hexPoints.get( NORTH ).getY(), 0.001 );
		assertEquals( height, hex.getHeight(), 0.001 );

		Point center = hex.getCenter();
		assertEquals( x, center.getX(), 0.001 );
		assertEquals( y + height / 2.0, center.getY(), 0.001 );
	}
	
	// tests a regular hex's points based on given position coordinates and dimension
	private void testRegularHex( int x, int y, int maxWidth, int maxHeight ) {
		double[] dimensions =
				FilledRowHexGrid.calculateRegularHexDimensions( maxWidth, maxHeight );
		Hex regularHex = new Hex( x, y, dimensions[0], dimensions[1], 0, 0 );
		
		Direction[] pointDirs = Hex.getHexPoints();
		Map <Direction, Point> hexPoints = getHexPoints( regularHex );
		Point center = regularHex.getCenter();
		
		Direction dir = pointDirs[0];
		Direction next = pointDirs[1];
		double dist = Point.distance( hexPoints.get( dir ), hexPoints.get( next ) );
		double centerDist = Point.distance( center, hexPoints.get( dir ));
		
		assertEquals( centerDist, Point.distance( center, hexPoints.get( next )), 0.001 );
		dir = next;
		do {
			next = next.getAdjacentCW();
			
			if (ArrayUtilities.linearSearch( pointDirs, next ) >= 0) {
				assertEquals( dist,
						Point.distance( hexPoints.get( dir ), hexPoints.get( next )), 0.001 );
				assertEquals( centerDist, Point.distance( center, hexPoints.get( next )), 0.001 );
				
				dir = next;
			}
			
		} while (next != pointDirs[0]);
	}
	
	// maps a hex's corner points to directions
	private Map <Direction, Point> getHexPoints( Hex hex ) {

		HashMap <Direction, Point> hexPoints = new HashMap <>();
		Direction[] pointDirs = Hex.getHexPoints();
		
		for (Direction d : pointDirs)
			hexPoints.put( d, hex.getPoint( d ));
		
		return hexPoints;
	}
}
