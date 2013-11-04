package fi.grimripper.loww.templates;

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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.junit.BeforeClass;
import org.junit.Test;

import fi.grimripper.loww.ArrayUtilities;
import fi.grimripper.loww.Direction;
import fi.grimripper.loww.Point;
import fi.grimripper.loww.templates.FourSquareTemplate;
import fi.grimripper.loww.templates.HexAndNeighborsTemplate;
import fi.grimripper.loww.templates.HorizontalTwoTileTemplate;
import fi.grimripper.loww.templates.SingleTileTemplate;
import fi.grimripper.loww.templates.SquareAndNeighborsTemplate;
import fi.grimripper.loww.templates.Templates;
import fi.grimripper.loww.test.TestMovementTemplate;
import fi.grimripper.loww.tiles.FilledRowHexGrid;
import fi.grimripper.loww.tiles.FilledSquareGrid;
import fi.grimripper.loww.tiles.Tile;
import fi.grimripper.loww.tiles.TileGrid;

public class TemplateTests {

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
	public void testTemplateUtilities() {
		new Templates();
		
		// horizontally and vertically symmetric template
		TestMovementTemplate template = new TestMovementTemplate( true, true );
		Direction[] templateDirs = Templates.getTemplateDirections( template );
		Direction[] directions = Direction.values();
		
		// all directions are accepted
		Arrays.sort( directions );
		Arrays.sort( templateDirs );
		assertArrayEquals( directions, templateDirs );
		
		// no need to change directions
		for (Direction oldDir : directions) {
			assertSame( oldDir, Templates.getTemplateDirection( oldDir, template ));
			
			for (Direction newDir : directions)
				assertSame( newDir, Templates.getTemplateDirection( newDir, oldDir, template ));
			
			Direction[] moveDirs = Templates.getMoveDirections( oldDir, template );
			Arrays.sort( moveDirs );
			assertArrayEquals( directions, moveDirs );
		}

		// no directions for null parameters
		assertNull( Templates.getTemplateDirection( null, template ));
		assertNull( Templates.getTemplateDirection( null, null, template ));
		assertNull( Templates.getTemplateDirection( null, EAST, template ));
		assertNull( Templates.getTemplateDirection( EAST, null, template ));
		assertArrayEquals( new Direction[0], Templates.getMoveDirections( null, template ));
		
		// horizontally asymmetric and vertically symmetric template
		template = new TestMovementTemplate( false, true );
		templateDirs = Templates.getTemplateDirections( template );
		
		// vertical directions are not suitable for determining template tiles
		ArrayList <Direction> directionsList = new ArrayList <>();
		for (Direction d : Direction.values())
			if (!d.isVertical())
				directionsList.add( d );

		assertEquals( directionsList.size(), templateDirs.length );
		for (Direction d : templateDirs) {
			assertFalse( d.isVertical() );
			assertTrue( directionsList.contains( d ));
		}

		// can't move to directions that are horizontally opposite to the facing
		ArrayList <Direction> east = new ArrayList <>();
		for (Direction d : Direction.values())
			if (d.isVertical() || d.isDueEast())
				east.add( d );
		
		ArrayList <Direction> west = new ArrayList <>();
		for (Direction d : Direction.values())
			if (d.isVertical() || d.isDueWest())
				west.add( d );
				
		for (Direction newDir : Direction.values()) {
			assertSame( newDir.isVertical() ? newDir.getAdjacentCW() : newDir,
				Templates.getTemplateDirection( newDir, template ));
			
			Direction[] moveDirs = Templates.getMoveDirections( newDir, template );
			if (newDir.isVertical())
				assertArrayEquals( new Direction[0], moveDirs );
			
			else {
				ArrayList <Direction> expected = newDir.isDueEast() ? east : west;

				assertEquals( expected.size(), moveDirs.length );
				for (Direction move : moveDirs)
					assertTrue( expected.contains( move ));
			}
			
			for (Direction oldDir : templateDirs) {
				Direction dir = Templates.getTemplateDirection( newDir, oldDir, template );
				
				// no need to change directions that aren't vertical
				if (!newDir.isVertical())
					assertSame( newDir, dir );
				
				// vertical direction is replaced with an adjacent direction with same horizontal
				// direction as the old facing
				else {
					assertSame( oldDir.isDueEast(), dir.isDueEast() );
					assertSame( oldDir.isDueWest(), dir.isDueWest() );
					assertSame( newDir.isDueNorth(), dir.isDueNorth() );
					assertSame( newDir.isDueSouth(), dir.isDueSouth() );
				}
			}
		}

		// no directions for null parameters
		assertNull( Templates.getTemplateDirection( null, template ));
		assertNull( Templates.getTemplateDirection( null, null, template ));
		assertNull( Templates.getTemplateDirection( null, EAST, template ));
		assertNull( Templates.getTemplateDirection( EAST, null, template ));
		assertArrayEquals( new Direction[0], Templates.getMoveDirections( null, template ));

		// vertically asymmetric and horizontally symmetric template
		template = new TestMovementTemplate( true, false );
		templateDirs = Templates.getTemplateDirections( template );

		// horizontal directions are not suitable for determining template tiles
		directionsList = new ArrayList <>();
		for (Direction d : Direction.values())
			if (!d.isHorizontal())
				directionsList.add( d );

		assertEquals( directionsList.size(), templateDirs.length );
		for (Direction d : templateDirs) {
			assertFalse( d.isHorizontal() );
			assertTrue( directionsList.contains( d ));
		}

		// can't move to directions that are vertically opposite to the facing
		ArrayList <Direction> north = new ArrayList <>();
		for (Direction d : Direction.values())
			if (d.isHorizontal() || d.isDueNorth())
				north.add( d );
		
		ArrayList <Direction> south = new ArrayList <>();
		for (Direction d : Direction.values())
			if (d.isHorizontal() || d.isDueSouth())
				south.add( d );
		
		for (Direction newDir : Direction.values()) {
			assertSame( newDir.isHorizontal() ? newDir.getAdjacentCW() : newDir,
				Templates.getTemplateDirection( newDir, template ));
			
			Direction[] moveDirs = Templates.getMoveDirections( newDir, template );
			if (newDir.isHorizontal())
				assertArrayEquals( new Direction[0], moveDirs );
			
			else {
				ArrayList <Direction> expected = newDir.isDueNorth() ? north : south;

				assertEquals( expected.size(), moveDirs.length );
				for (Direction move : moveDirs)
					assertTrue( expected.contains( move ));
			}
			
			for (Direction oldDir : templateDirs) {
				Direction dir = Templates.getTemplateDirection( newDir, oldDir, template );

				// no need to change directions that aren't horizontal
				if (!newDir.isHorizontal())
					assertSame( newDir, dir );

				// horizontal direction is replaced with an adjacent direction with same vertical
				// direction as the old facing
				else {
					assertSame( newDir.isDueEast(), dir.isDueEast() );
					assertSame( newDir.isDueWest(), dir.isDueWest() );
					assertSame( oldDir.isDueNorth(), dir.isDueNorth() );
					assertSame( oldDir.isDueSouth(), dir.isDueSouth() );
				}
			}
		}

		// no directions for null parameters
		assertNull( Templates.getTemplateDirection( null, template ));
		assertNull( Templates.getTemplateDirection( null, null, template ));
		assertNull( Templates.getTemplateDirection( null, EAST, template ));
		assertNull( Templates.getTemplateDirection( EAST, null, template ));
		assertArrayEquals( new Direction[0], Templates.getMoveDirections( null, template ));

		// horizontally and vertically asymmetric template
		template = new TestMovementTemplate( false, false );
		templateDirs = Templates.getTemplateDirections( template );

		// only diagonal directions are suitable for determining template tiles
		directionsList = new ArrayList <>();
		for (Direction d : Direction.values())
			if (!d.isVertical() && !d.isHorizontal())
				directionsList.add( d );
		
		assertEquals( directionsList.size(), templateDirs.length );
		for (Direction d : templateDirs) {
			assertFalse( d.isVertical() );
			assertFalse( d.isHorizontal() );
			assertTrue( directionsList.contains( d ));
		}

		// movement directions can't be vertically or horizontally opposite to the facing
		HashMap <Direction, ArrayList <Direction>> moveDirections = new HashMap <>();
		moveDirections.put( NORTHEAST, new ArrayList <Direction>() );
		moveDirections.put( SOUTHEAST, new ArrayList <Direction>() );
		moveDirections.put( SOUTHWEST, new ArrayList <Direction>() );
		moveDirections.put( NORTHWEST, new ArrayList <Direction>() );
		
		for (Direction d : Direction.values()) {
			if (!d.isDueSouth() && (d.isVertical() || d.isDueEast()))
				moveDirections.get( NORTHEAST ).add( d );
		
			if (!d.isDueNorth() && (d.isVertical() || d.isDueEast()))
				moveDirections.get( SOUTHEAST ).add( d );
			
			if (!d.isDueNorth() && (d.isVertical() || d.isDueWest()))
				moveDirections.get( SOUTHWEST ).add( d );
			
			if (!d.isDueSouth() && (d.isVertical() || d.isDueWest()))
				moveDirections.get( NORTHWEST ).add( d );
		}
		
		for (Direction newDir : Direction.values()) {
			assertSame( newDir.isHorizontal() || newDir.isVertical() ? newDir.getAdjacentCW() :
				newDir, Templates.getTemplateDirection( newDir, template ));
			
			Direction[] moveDirs = Templates.getMoveDirections( newDir, template );
			ArrayList <Direction> expected = moveDirections.get( newDir );
			
			if (expected == null)
				assertArrayEquals( new Direction[0], moveDirs );
			
			else {
				assertEquals( expected.size(), moveDirs.length );
				for (Direction move : moveDirs)
					assertTrue( expected.contains( move ));
			}
			
			for (Direction oldDir : templateDirs) {
				Direction dir = Templates.getTemplateDirection( newDir, oldDir, template );
				
				// diagonal directions are not changed
				if (!newDir.isVertical() && !newDir.isHorizontal())
					assertSame( newDir, dir );
				
				// a horizontal direction is replaced with an adjacent direction that has the same
				// vertical direction as the old facing
				else if (newDir.isHorizontal()) {
					assertSame( newDir.isDueEast(), dir.isDueEast() );
					assertSame( newDir.isDueWest(), dir.isDueWest() );
					assertSame( oldDir.isDueNorth(), dir.isDueNorth() );
					assertSame( oldDir.isDueSouth(), dir.isDueSouth() );
				}
				
				// a vertical direction is replaced with an adjacent direction that has the same
				// horizontal direction as the old facing
				else if (newDir.isVertical()) {
					assertSame( newDir.isDueNorth(), dir.isDueNorth() );
					assertSame( newDir.isDueSouth(), dir.isDueSouth() );
					assertSame( oldDir.isDueEast(), dir.isDueEast() );
					assertSame( oldDir.isDueWest(), dir.isDueWest() );
				}
			}
		}

		// no directions for null parameters
		assertNull( Templates.getTemplateDirection( null, template ));
		assertNull( Templates.getTemplateDirection( null, null, template ));
		assertNull( Templates.getTemplateDirection( null, EAST, template ));
		assertNull( Templates.getTemplateDirection( EAST, null, template ));
		assertArrayEquals( new Direction[0], Templates.getMoveDirections( null, template ));
		
		// all directions are accepted for symmetric template
		template = new TestMovementTemplate( true, true );
		for (Direction d : Direction.values())
			assertTrue( Templates.isTemplateDirection( template, d ));
		assertFalse( Templates.isTemplateDirection( template, null ));
		
		// vertical directions are not accepted for horizontally asymmetric template
		template = new TestMovementTemplate( false, true );
		for (Direction d : Direction.values())
			assertSame( !d.isVertical(), Templates.isTemplateDirection( template, d ));
		assertFalse( Templates.isTemplateDirection( template, null ));
		
		// horizontal directions are not accepted for vertically asymmetric template
		template = new TestMovementTemplate( true, false );
		for (Direction d : Direction.values())
			assertSame( !d.isHorizontal(), Templates.isTemplateDirection( template, d ));
		assertFalse( Templates.isTemplateDirection( template, null ));
		
		// only diagonal directions are accepted for template with no symmetricity
		template = new TestMovementTemplate( false, false );
		for (Direction d : Direction.values())
			assertSame( !d.isHorizontal() && !d.isVertical(),
					Templates.isTemplateDirection( template, d ));
		assertFalse( Templates.isTemplateDirection( template, null ));
		
		assertFalse( Templates.isTemplateDirection( null, null ));
	}
	
	@Test( expected = NullPointerException.class )
	public void testTemplateDirectionNullPointerException1() {
		Templates.getTemplateDirection( WEST, null );
	}
	
	@Test( expected = NullPointerException.class )
	public void testTemplateDirectionNullPointerException2() {
		Templates.getTemplateDirection( WEST, WEST, null );
	}
	
	@Test( expected = NullPointerException.class )
	public void testTemplateDirectionNullPointerException3() {
		Templates.getTemplateDirections( null );
	}
	
	@Test( expected = NullPointerException.class )
	public void testMoveDirectionsNullPointerException() {
		Templates.getMoveDirections( WEST, null );
	}
	
	@Test
	public void testSingleTileTemplate() {
		testSingleTileTemplate( new FilledSquareGrid( 1, 3, 3 ));
		testSingleTileTemplate( FilledRowHexGrid.createWithHexSize( 1, 1, 3, 3, 3 ));
	}
	
	@Test
	public void testHorizontalTwoTileTemplate() {
		testHorizontalTwoTileTemplate( new FilledSquareGrid( 1, 3, 3 ));
		testHorizontalTwoTileTemplate( FilledRowHexGrid.createWithHexSize( 1, 1, 3, 3, 3 ));
	}
	
	@Test
	public void testFourSquareTemplate() {
		TileGrid <?> grid = new FilledSquareGrid( 1, 3, 3 );
		FourSquareTemplate template = new FourSquareTemplate();
		
		assertFalse( template.isHorizontallySymmetric() );
		assertFalse( template.isVerticallySymmetric() );
		
		assertEquals( 4, template.getSize() );
		assertEquals( 2, template.getHeight() );
		assertEquals( 2, template.getWidth() );
		
		Tile tile = grid.getTileAtRC( 1, 1 );
		Point tileCenter = tile.getCenter();
		HashMap <Direction, Tile[]> templateTiles = new HashMap <>();
		HashMap <Direction, Point> centers = new HashMap <>();
		
		// main tile, horizontal, vertical and diagonal neighbor, depending on facing
		templateTiles.put( NORTHEAST, new Tile[] { tile,
			tile.getNeighbor( WEST ), tile.getNeighbor( SOUTH ), tile.getNeighbor( SOUTHWEST )});
		templateTiles.put( SOUTHEAST, new Tile[] { tile,
			tile.getNeighbor( WEST ), tile.getNeighbor( NORTH ), tile.getNeighbor( NORTHWEST )});
		templateTiles.put( SOUTHWEST, new Tile[] { tile,
			tile.getNeighbor( EAST ), tile.getNeighbor( NORTH ), tile.getNeighbor( NORTHEAST )});
		templateTiles.put( NORTHWEST, new Tile[] { tile,
			tile.getNeighbor( EAST ), tile.getNeighbor( SOUTH ), tile.getNeighbor( SOUTHEAST )});
		
		// template center is same as one of the main tile's corners (half width/height away)
		Point copyCenter = Point.createPoint( tileCenter );
		copyCenter.setX( copyCenter.getX() - grid.getTileWidth() / 2 );
		copyCenter.setY( copyCenter.getY() + grid.getTileHeight() / 2 );
		centers.put( NORTHEAST, copyCenter );
		
		copyCenter = Point.createPoint( tileCenter );
		copyCenter.setX( copyCenter.getX() - grid.getTileWidth() / 2 );
		copyCenter.setY( copyCenter.getY() - grid.getTileHeight() / 2 );
		centers.put( SOUTHEAST, copyCenter );
		
		copyCenter = Point.createPoint( tileCenter );
		copyCenter.setX( copyCenter.getX() + grid.getTileWidth() / 2 );
		copyCenter.setY( copyCenter.getY() - grid.getTileHeight() / 2 );
		centers.put( SOUTHWEST, copyCenter );
		
		copyCenter = Point.createPoint( tileCenter );
		copyCenter.setX( copyCenter.getX() + grid.getTileWidth() / 2 );
		copyCenter.setY( copyCenter.getY() + grid.getTileHeight() / 2 );
		centers.put( NORTHWEST, copyCenter );
		
		Direction[] templateDirs = Templates.getTemplateDirections( template );
		
		for (Direction newDir : templateDirs) {
			assertArrayEquals( templateTiles.get( newDir ), template.getTiles( tile, newDir ));			
			assertEquals( centers.get( newDir ), template.getCenter( tile, newDir ));
			
			// turns in place to horizontal, vertical or diagonal neighbor
			for (Direction oldDir : templateDirs) {
				Tile turned = template.turnInPlace( tile, oldDir, newDir );
				if (newDir.isDueEast() == oldDir.isDueEast() &&
						newDir.isDueNorth() == oldDir.isDueNorth())
					assertSame( tile, turned );		// didn't turn
				
				else if (newDir.isDueEast() == oldDir.isDueEast())
					assertSame( tile.getNeighbor( newDir.isDueNorth() ? NORTH : SOUTH ), turned );
				
				else if (newDir.isDueNorth() == oldDir.isDueNorth())
					assertSame( tile.getNeighbor( newDir.isDueEast() ? EAST : WEST ), turned );
				
				else
					assertSame( tile.getNeighbor( newDir ), turned );
			}
		}

		int[] verticalMove = { 0, 1 };			// main tile and horizontal neighbor
		int[] horizontalMove = { 0, 2 };		// main tile and vertical neighbor
		int[] diagonalMove = { 0, 1, 2 };		// all but the diagonal neighbor
		
		for (Direction d : Direction.values())
			assertArrayEquals( d.isHorizontal() ? horizontalMove : d.isVertical() ?
				verticalMove : diagonalMove, template.getMoveIndices( d ));
	}

	@Test
	public void testHexAndNeighborsTemplate() {
		TileGrid <?> grid = FilledRowHexGrid.createWithHexSize( 1, 1, 3, 3, 3 );
		HexAndNeighborsTemplate template = new HexAndNeighborsTemplate();
		
		assertTrue( template.isHorizontallySymmetric() );
		assertTrue( template.isVerticallySymmetric() );
		
		assertEquals( 7, template.getSize() );
		assertEquals( 3, template.getHeight() );
		assertEquals( 3, template.getWidth() );
		
		// same center and turn in place tile regardless of facing
		Tile tile = grid.getTileAtRC( 1, 1 );
		for (Direction oldDir : Direction.values()) {
			assertEquals( tile.getCenter(), template.getCenter( tile, oldDir ));
			
			for (Direction newDir : Direction.values())
				assertSame( tile, template.turnInPlace( tile, oldDir, newDir ));
		}
		
		// all directions are accepted
		Direction[] templateDirs = Templates.getTemplateDirections( template );
		
		int sameDirections = 0;
		for (Direction d : Direction.values())
			if (ArrayUtilities.linearSearch( templateDirs, d ) >= 0)
				sameDirections++;
		
		assertEquals( sameDirections, Direction.values().length );
		assertEquals( sameDirections, templateDirs.length );
		
		// 1:northeast, 2:east, 3:southeast, 4:southwest, 5:west, 6:northwest
		int[][] indices =
			{{ 6, 1, 2 }, { 1, 2, 3 }, { 2, 3, 4 }, { 3, 4, 5 }, { 4, 5, 6 }, { 5, 6, 1 }};
		Direction[] directions = { NORTHEAST, EAST, SOUTHEAST, SOUTHWEST, WEST, NORTHWEST };
		
		for (int i = 0; i < indices.length; i++) {
			int[] moveIndices = template.getMoveIndices( directions[i] );
			
			Arrays.sort( indices[i] );
			Arrays.sort( moveIndices );
			
			assertArrayEquals( indices[i], moveIndices );
		}
		
		// gets tile and its neighbors in all tiles, regardless of facing
		for (Direction d : Direction.values())
			for (Tile[] row : grid.getTiles())
				for (Tile t : row) {
					Tile[] templateTiles = template.getTiles( t, d );
					assertSame( t, templateTiles[0] );
					
					for (int i = 0; i < directions.length; i++)
						assertEquals( t.getNeighbor( directions[i] ), templateTiles[i + 1]);
				}
	}
	
	@Test
	public void testSquareAndNeighborsTemplate() {
		TileGrid <?> grid = new FilledSquareGrid( 1, 3, 3 );
		SquareAndNeighborsTemplate template = new SquareAndNeighborsTemplate();
		
		assertTrue( template.isHorizontallySymmetric() );
		assertTrue( template.isVerticallySymmetric() );
		
		assertEquals( 9, template.getSize() );
		assertEquals( 3, template.getHeight() );
		assertEquals( 3, template.getWidth() );
		
		// same center and turn in place tile regardless of facing
		Tile tile = grid.getTileAtRC( 1, 1 );
		for (Direction oldDir : Direction.values()) {
			assertEquals( tile.getCenter(), template.getCenter( tile, oldDir ));
			
			for (Direction newDir : Direction.values())
				assertSame( tile, template.turnInPlace( tile, oldDir, newDir ));
		}
		
		// all directions are accepted
		Direction[] templateDirs = Templates.getTemplateDirections( template );
		
		int sameDirections = 0;
		for (Direction d : Direction.values())
			if (ArrayUtilities.linearSearch( templateDirs, d ) >= 0)
				sameDirections++;
		
		assertEquals( sameDirections, Direction.values().length );
		assertEquals( sameDirections, templateDirs.length );
		
		// 1:north, 2:northeast, 3:east, 4:southeast, 5:south, 6:southwest, 7:west, 8:northwest
		int[][] indices = {{ 8, 1, 2 }, { 8, 1, 2, 3, 4 }, { 2, 3, 4 }, { 2, 3, 4, 5, 6 },
			{ 4, 5, 6 }, { 4, 5, 6, 7, 8 }, { 6, 7, 8 }, { 6, 7, 8, 1, 2 }};
		Direction[] directions =
			{ NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST, WEST, NORTHWEST };
		
		for (int i = 0; i < indices.length; i++) {
			int[] moveIndices = template.getMoveIndices( directions[i] );
			
			Arrays.sort( indices[i] );
			Arrays.sort( moveIndices );
			
			assertArrayEquals( indices[i], moveIndices );
		}
		
		// gets tile and its neighbors in all tiles, regardless of facing
		for (Direction d : Direction.values())
			for (Tile[] row : grid.getTiles())
				for (Tile t : row) {
					Tile[] templateTiles = template.getTiles( t, d );
					assertSame( t, templateTiles[0] );
					
					for (int i = 0; i < directions.length; i++)
						assertEquals( t.getNeighbor( directions[i] ), templateTiles[i + 1]);
				}
	}
	
	private void testSingleTileTemplate( TileGrid <?> grid ) {
		SingleTileTemplate template = new SingleTileTemplate();
		
		assertTrue( template.isHorizontallySymmetric() );
		assertTrue( template.isVerticallySymmetric() );
		
		assertEquals( 1, template.getSize() );
		assertEquals( 1, template.getHeight() );
		assertEquals( 1, template.getWidth() );

		Tile tile = grid.getTileAtRC( 1, 1 );
		Tile[] templateTile = { tile };
		Point center = tile.getCenter();
		int[] moveIndices = { 0 };
		
		// same template, center, move indices and turn in place tile for all directions
		for (Direction oldDir : Direction.values()) {
			assertArrayEquals( templateTile, template.getTiles( tile, oldDir ));
			assertEquals( center, template.getCenter( tile, oldDir ));
			assertArrayEquals( moveIndices, template.getMoveIndices( oldDir ));
			
			for (Direction newDir : Direction.values())
				assertSame( tile, template.turnInPlace( tile, oldDir, newDir ));
		}
	}
	
	private void testHorizontalTwoTileTemplate( TileGrid <?> grid ) {
		HorizontalTwoTileTemplate template = new HorizontalTwoTileTemplate();
		
		assertFalse( template.isHorizontallySymmetric() );
		assertTrue( template.isVerticallySymmetric() );
		
		assertEquals( 2, template.getSize() );
		assertEquals( 1, template.getHeight() );
		assertEquals( 2, template.getWidth() );
		
		Tile tile = grid.getTileAtRC( 1, 1 );
		Tile[] facingEast = { tile, tile.getNeighbor( WEST )};
		Tile[] facingWest = { tile, tile.getNeighbor( EAST )};
		
		Point tileCenter = tile.getCenter();
		Point eastCenter = facingWest[1].getCenter();
		Point westCenter = facingEast[1].getCenter();
		
		// horizontal neighbors, so same y-coordinate
		assertEquals( tileCenter.getY(), eastCenter.getY(), 0.001 );
		assertEquals( tileCenter.getY(), westCenter.getY(), 0.001 );
		
		Direction[] templateDirs = Templates.getTemplateDirections( template );
		
		for (Direction newDir : templateDirs) {
			assertArrayEquals( newDir.isDueEast() ? facingEast : facingWest,
				template.getTiles( tile, newDir ));

			Point center = template.getCenter( tile, newDir );
			Point otherCenter = newDir.isDueEast() ? westCenter : eastCenter;
		
			// same y-coordinate and the average of the two tiles' x-coordinates
			assertEquals( tileCenter.getY(), center.getY(), 0.001 );
			assertEquals( (tileCenter.getX() + otherCenter.getX()) / 2, center.getX(), 0.001 );
			
			// turns in place to the secondary tile
			for (Direction oldDir : templateDirs) {
				Tile turned = template.turnInPlace( tile, oldDir, newDir );
				
				if (newDir.isDueEast() == oldDir.isDueEast() &&
						newDir.isDueWest() == oldDir.isDueWest())
					assertSame( tile, turned );		// didn't turn
				
				else if (newDir.isDueEast())
					assertSame( facingWest[1], turned );
				
				else if (newDir.isDueWest())
					assertSame( facingEast[1], turned );
			}
		}

		int[] horizontalMove = { 0 };		// only main tile moves
		int[] diagonalMove = { 0, 1 };		// both tiles move
				
		for (Direction d : Direction.values())
			assertArrayEquals( d.isHorizontal() ? horizontalMove : diagonalMove,
				template.getMoveIndices( d ));
	}
}
