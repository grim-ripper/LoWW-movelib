package fi.grimripper.loww;

import static fi.grimripper.loww.Direction.*;
import static org.junit.Assert.*;

import org.junit.Test;

import fi.grimripper.loww.Direction;

public class DirectionTest {

	@Test
	public void testDirections() {
		Direction north = Direction.valueOf( "NORTH" );
		assertSame( north, NORTH );
		
		// no hex neighbors north or south, but in all other directions
		assertTrue( north.hexDir() < 0 );
		assertTrue( SOUTH.hexDir() < 0 );
		
		assertEquals( 0, NORTHEAST.hexDir() );
		assertEquals( 1, EAST.hexDir() );
		assertEquals( 2, SOUTHEAST.hexDir() );
		assertEquals( 3, SOUTHWEST.hexDir() );
		assertEquals( 4, WEST.hexDir() );
		assertEquals( 5, NORTHWEST.hexDir() );
		
		// no hex points east or west, but in all other directions
		assertTrue( EAST.hexPoint() < 0 );
		assertTrue( WEST.hexPoint() < 0 );

		assertEquals( 0, NORTH.hexPoint() );
		assertEquals( 1, NORTHEAST.hexPoint() );
		assertEquals( 2, SOUTHEAST.hexPoint() );
		assertEquals( 3, SOUTH.hexPoint() );
		assertEquals( 4, SOUTHWEST.hexPoint() );
		assertEquals( 5, NORTHWEST.hexPoint() );

		// square neighbors in all directions
		assertEquals( 0, NORTH.squareDir() );
		assertEquals( 1, NORTHEAST.squareDir() );
		assertEquals( 2, EAST.squareDir() );
		assertEquals( 3, SOUTHEAST.squareDir() );
		assertEquals( 4, SOUTH.squareDir() );
		assertEquals( 5, SOUTHWEST.squareDir() );
		assertEquals( 6, WEST.squareDir() );
		assertEquals( 7, NORTHWEST.squareDir() );
		
		// no square points in vertical and horizontal directions
		assertTrue( NORTH.squarePoint() < 0 );
		assertTrue( EAST.squarePoint() < 0 );
		assertTrue( SOUTH.squarePoint() < 0 );
		assertTrue( WEST.squarePoint() < 0 );
		
		// hex angles change in multiples of 60 degrees, except north and south which are vertical
		assertEquals( Math.toRadians( 0 ), EAST.hexAngle(), 0.001 );
		assertEquals( Math.toRadians( 60 ), NORTHEAST.hexAngle(), 0.001 );
		assertEquals( Math.toRadians( 90 ), NORTH.hexAngle(), 0.001 );
		assertEquals( Math.toRadians( 120 ), NORTHWEST.hexAngle(), 0.001 );
		assertEquals( Math.toRadians( 180 ), WEST.hexAngle(), 0.001 );
		assertEquals( Math.toRadians( -120 ), SOUTHWEST.hexAngle(), 0.001 );
		assertEquals( Math.toRadians( -90 ), SOUTH.hexAngle(), 0.001 );
		assertEquals( Math.toRadians( -60 ), SOUTHEAST.hexAngle(), 0.001 );
	}

	@Test
	public void testGetOpposite() {
		Direction[] opposites = { NORTH.getOpposite(), NORTHEAST.getOpposite(),
			EAST.getOpposite(), SOUTHEAST.getOpposite(), SOUTH.getOpposite(),
			SOUTHWEST.getOpposite(), WEST.getOpposite(), NORTHWEST.getOpposite() };
		
		assertArrayEquals( new Direction[] {
			SOUTH, SOUTHWEST, WEST, NORTHWEST, NORTH, NORTHEAST, EAST, SOUTHEAST }, opposites );
	}
	
	@Test
	public void testGetClosest() {
		Direction[] closest = { NORTHWEST, WEST, SOUTHEAST };
		assertEquals( WEST, SOUTHWEST.getClosest( closest ));
		assertEquals( SOUTHEAST, SOUTH.getClosest( closest ));
		
		assertEquals( NORTH,
				NORTHWEST.getClosest( new Direction[] { NORTH, NORTHEAST, SOUTHWEST }));
		assertEquals( NORTHWEST,
				NORTH.getClosest( new Direction[] { NORTHWEST, EAST }));
		assertEquals( EAST, EAST.getClosest( Direction.values() ));
		
		// for two equally close directions, return value is unspecified
		Direction notSoClose = NORTH.getClosest( new Direction[] { SOUTHWEST, SOUTHEAST });
		assertTrue( notSoClose == SOUTHWEST || notSoClose == SOUTHEAST );
	}
	
	@Test( expected = NullPointerException.class )
	public void testGetClosestNullPointer1() {
		WEST.getClosest( null );
	}

	@Test( expected = NullPointerException.class )
	public void testGetClosestNullPointer2() {
		WEST.getClosest( new Direction[] { NORTHWEST, null, SOUTHWEST });
	}

	@Test( expected = ArrayIndexOutOfBoundsException.class )
	public void testGetClosestArrayIndexOutOfBounds() {
		WEST.getClosest( new Direction[0] );
	}
	
	@Test
	public void testGetAdjacentCW() {
		Direction[] adjacentsCW = { NORTH.getAdjacentCW(), NORTHEAST.getAdjacentCW(),
			EAST.getAdjacentCW(), SOUTHEAST.getAdjacentCW(), SOUTH.getAdjacentCW(),
			SOUTHWEST.getAdjacentCW(), WEST.getAdjacentCW(), NORTHWEST.getAdjacentCW() };
		
		assertArrayEquals( new Direction[] {
			NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST, WEST, NORTHWEST, NORTH }, adjacentsCW );
	}

	@Test
	public void testGetAdjacentCCW() {
		Direction[] adjacentsCCW = { NORTH.getAdjacentCCW(), NORTHEAST.getAdjacentCCW(),
			EAST.getAdjacentCCW(), SOUTHEAST.getAdjacentCCW(), SOUTH.getAdjacentCCW(),
			SOUTHWEST.getAdjacentCCW(), WEST.getAdjacentCCW(), NORTHWEST.getAdjacentCCW() };
		
		assertArrayEquals( new Direction[] {
			NORTHWEST, NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST, WEST }, adjacentsCCW );
	}
	
	@Test
	public void testIsDueEast() {
		assertTrue( NORTHEAST.isDueEast() );
		assertTrue( EAST.isDueEast() );
		assertTrue( SOUTHEAST.isDueEast() );
		
		assertFalse( NORTH.isDueEast() );
		assertFalse( SOUTH.isDueEast() );
		assertFalse( SOUTHWEST.isDueEast() );
		assertFalse( WEST.isDueEast() );
		assertFalse( NORTHWEST.isDueEast() );
	}

	@Test
	public void testIsDueNorth() {
		assertTrue( NORTHEAST.isDueNorth() );
		assertTrue( NORTH.isDueNorth() );
		assertTrue( NORTHWEST.isDueNorth() );
		
		assertFalse( EAST.isDueNorth() );
		assertFalse( SOUTHEAST.isDueNorth() );
		assertFalse( SOUTH.isDueNorth() );
		assertFalse( SOUTHWEST.isDueNorth() );
		assertFalse( WEST.isDueNorth() );
	}

	@Test
	public void testIsDueSouth() {
		assertTrue( SOUTHEAST.isDueSouth() );
		assertTrue( SOUTH.isDueSouth() );
		assertTrue( SOUTHWEST.isDueSouth() );
		
		assertFalse( EAST.isDueSouth() );
		assertFalse( NORTHEAST.isDueSouth() );
		assertFalse( NORTH.isDueSouth() );
		assertFalse( NORTHWEST.isDueSouth() );
		assertFalse( WEST.isDueSouth() );
	}	

	@Test
	public void testIsDueWest() {
		assertTrue( NORTHWEST.isDueWest() );
		assertTrue( WEST.isDueWest() );
		assertTrue( SOUTHWEST.isDueWest() );
		
		assertFalse( NORTH.isDueWest() );
		assertFalse( SOUTH.isDueWest() );
		assertFalse( SOUTHEAST.isDueWest() );
		assertFalse( EAST.isDueWest() );
		assertFalse( NORTHEAST.isDueWest() );
	}

	@Test
	public void testIsHorizontal() {
		assertTrue( EAST.isHorizontal() );
		assertTrue( WEST.isHorizontal() );

		assertFalse( NORTH.isHorizontal() );
		assertFalse( NORTHEAST.isHorizontal() );
		assertFalse( SOUTHEAST.isHorizontal() );
		assertFalse( SOUTH.isHorizontal() );
		assertFalse( SOUTHWEST.isHorizontal() );
		assertFalse( NORTHWEST.isHorizontal() );
	}

	@Test
	public void testIsVertical() {
		assertTrue( NORTH.isVertical() );
		assertTrue( SOUTH.isVertical() );

		assertFalse( NORTHEAST.isVertical() );
		assertFalse( EAST.isVertical() );
		assertFalse( SOUTHEAST.isVertical() );
		assertFalse( SOUTHWEST.isVertical() );
		assertFalse( WEST.isVertical() );
		assertFalse( NORTHWEST.isVertical() );
	}
}
