package fi.grimripper.loww.tiles;

import static fi.grimripper.loww.Direction.EAST;
import static fi.grimripper.loww.Direction.NORTHEAST;
import static fi.grimripper.loww.Direction.SOUTH;
import static fi.grimripper.loww.Direction.SOUTHEAST;
import static fi.grimripper.loww.Direction.SOUTHWEST;
import static fi.grimripper.loww.Direction.WEST;
import static fi.grimripper.loww.Height.BLOCKING;
import static fi.grimripper.loww.Height.DEEP;
import static fi.grimripper.loww.Height.FLAT;
import static fi.grimripper.loww.Height.HIGH;
import static fi.grimripper.loww.Height.LOW;
import static fi.grimripper.loww.Height.VERY_HIGH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fi.grimripper.loww.AdditionalProperties.Properties;
import fi.grimripper.loww.test.TestMovementTemplate;
import fi.grimripper.loww.test.TestObstacle;
import fi.grimripper.loww.test.TestObstacleTemplate;
import fi.grimripper.loww.tiles.Obstacle;
import fi.grimripper.loww.tiles.Terrain;

public class ObstacleTest {

	@Test
	public void testObstacle() {
		
		Obstacle obstacle = new Obstacle( LOW, new TestObstacleTemplate() ) {};
		
		// basic effects on move height, line of sight and occupying
		assertNull( obstacle.modifyMoveHeight( null ));
		assertSame( LOW, obstacle.modifyMoveHeight( LOW ));
		assertTrue( obstacle.blocksLineOfSight( null ));
		assertTrue( obstacle.blocksLineOfSight( obstacle ));
		assertTrue( obstacle.occupiesTile() );
		
		// test basic height and template
		TestObstacleTemplate template = new TestObstacleTemplate( false, true );
		TestObstacle first = new TestObstacle( VERY_HIGH, template );
		
		assertEquals( VERY_HIGH, first.getHeight() );
		assertSame( template, first.getTemplate() );
		
		// test basic height and template with extra properties
		Properties props = new Properties();
		TestObstacle second = new TestObstacle( BLOCKING, template, props );
		
		assertEquals( BLOCKING, second.getHeight() );
		assertSame( template, second.getTemplate() );
		assertSame( props, second.getProperties() );
		
		// not placed, so no occupy height or total height
		assertTrue( second.getOccupyHeight() == null );
		assertTrue( second.getTotalHeight() == null );
		
		// maximum occupy height is blocking, which can't be reduced by obstacle height
		second.setOccupyHeight( BLOCKING );
		assertEquals( BLOCKING, second.getOccupyHeight() );
		assertEquals( BLOCKING, second.getTotalHeight() );
		
		first.setOccupyHeight( BLOCKING );
		assertEquals( BLOCKING, first.getTotalHeight() );
		
		first.setHeight( DEEP );
		assertEquals( DEEP, first.getHeight() );
		assertEquals( BLOCKING, first.getOccupyHeight() );
		assertEquals( BLOCKING, first.getTotalHeight() );
		
		// test combinations of occupy height and obstacle height
		first.setOccupyHeight( DEEP );
		assertEquals( DEEP, first.getTotalHeight() );
		
		first.setOccupyHeight( FLAT );
		assertEquals( FLAT, first.getTotalHeight() );
		
		first.setOccupyHeight( HIGH );
		assertEquals( HIGH, first.getTotalHeight() );
		
		first.setHeight( LOW );
		first.setOccupyHeight( LOW );
		assertEquals( HIGH, first.getTotalHeight() );
		
		// no occupy height or total height once removed from grid
		first.setOccupyHeight( null );
		assertNull( first.getOccupyHeight() );
		assertNull( first.getTotalHeight() );
		
		// location and facing haven't been set yet
		assertNull( first.getLocation() );
		assertNull( first.getFacing() );
		assertNull( first.getTemplateFacing() );
		
		// location can be set without facing, but obstacle isn't placed in template tiles
		TrackerTile tracker = new TrackerTile();
		first.setLocation( tracker );
		assertFalse( tracker.obstacleHasBeenAdded() );
		assertEquals( tracker, first.getLocation() );
		
		// check clearing location doesn't affect tiles, since obstacle wasn't added
		first.setLocation( null );
		assertFalse( tracker.obstacleHasBeenRemoved() );
		assertNull( first.getLocation() );
		
		// setting facing doesn't affect location
		assertNull( first.getFacing() );
		first.setFacing( EAST );
		assertNull( first.getLocation() );
		assertEquals( EAST, first.getFacing() );
		assertEquals( EAST, first.getTemplateFacing() );
		
		// with both location and facing, obstacle is placed
		tracker.setTerrain( new Terrain( 0f, FLAT ));
		first.setLocation( tracker );
		assertTrue( tracker.obstacleHasBeenAdded() );
		assertEquals( FLAT, first.getOccupyHeight() );
		assertEquals( tracker, first.getLocation() );
		assertEquals( EAST, first.getFacing() );
		assertEquals( EAST, first.getTemplateFacing() );
		
		// test moving obstacle to another location
		TrackerTile anotherTile = new TrackerTile();
		anotherTile.setTerrain( new Terrain( 0f, null ));
		first.setLocation( anotherTile );
		assertTrue( tracker.obstacleHasBeenRemoved() );
		assertTrue( anotherTile.obstacleHasBeenAdded() );
		assertNull( first.getOccupyHeight() );		// terrain has null height
		
		// remove facing, obstacle is removed from tile
		first.setFacing( null );
		assertTrue( anotherTile.obstacleHasBeenRemoved() );
		assertNull( first.getFacing() );

		// set facing, obstacle is added to tile again
		anotherTile.clearTrackerState();
		first.setFacing( EAST );
		assertTrue( anotherTile.obstacleHasBeenAdded() );
		assertFalse( anotherTile.obstacleHasBeenRemoved() );
		assertEquals( EAST, first.getFacing() );
		assertEquals( EAST, first.getTemplateFacing() );
		
		template.setSize( 2 );
		TrackerTile alternateTile = new TrackerTile();
		alternateTile.setTerrain( new Terrain( 0f, FLAT ));
		template.setAlternateTile( alternateTile );
		
		// with size two template, obstacle is placed in secondary tile as well
		anotherTile.clearTrackerState();
		first.setFacing( NORTHEAST );
		assertTrue( anotherTile.obstacleHasBeenAdded() );
		assertTrue( anotherTile.obstacleHasBeenRemoved() );

		anotherTile.clearTrackerState();
		first.setFacing( WEST );

		// changing facing removes obstacle and adds it with new facing
		assertTrue( alternateTile.obstacleHasBeenAdded() );
		assertTrue( anotherTile.obstacleHasBeenRemoved() );
		assertTrue( alternateTile.obstacleHasBeenRemoved() );
		
		// template gives alternate tile as new main tile and secondary tile
		assertFalse( anotherTile.obstacleHasBeenAdded() );
		assertEquals( FLAT, first.getOccupyHeight() );
		
		// remove, both template tiles are now alternate tile
		anotherTile.clearTrackerState();
		alternateTile.clearTrackerState();
		first.setLocation( null );
		assertTrue( alternateTile.obstacleHasBeenRemoved() );
		assertFalse( anotherTile.obstacleHasBeenRemoved() );
		assertNull( first.getLocation() );
		
		first.setFacing( null );
		tracker.clearTrackerState();
		anotherTile.clearTrackerState();
		alternateTile.clearTrackerState();
		
		// test setting location and facing at the same time
		first.setPosition( EAST, tracker );
		assertEquals( tracker, first.getLocation() );
		assertEquals( EAST, first.getFacing() );
		assertEquals( EAST, first.getTemplateFacing() );
		assertTrue( tracker.obstacleHasBeenAdded() );
		assertFalse( tracker.obstacleHasBeenRemoved() );
		assertTrue( alternateTile.obstacleHasBeenAdded() );
		assertFalse( alternateTile.obstacleHasBeenRemoved() );

		tracker.clearTrackerState();
		alternateTile.clearTrackerState();
		
		// change only facing by setting the same location
		first.setPosition( WEST, tracker );
		assertEquals( tracker, first.getLocation() );
		assertEquals( WEST, first.getFacing() );
		assertEquals( WEST, first.getTemplateFacing() );
		assertTrue( tracker.obstacleHasBeenAdded() );
		assertTrue( tracker.obstacleHasBeenRemoved() );
		assertTrue( alternateTile.obstacleHasBeenAdded() );
		assertTrue( alternateTile.obstacleHasBeenRemoved() );
		
		tracker.clearTrackerState();
		anotherTile.clearTrackerState();
		alternateTile.clearTrackerState();
		
		// change only location by setting the same facing
		first.setPosition( WEST, anotherTile );
		assertEquals( anotherTile, first.getLocation() );
		assertEquals( WEST, first.getFacing() );
		assertEquals( WEST, first.getTemplateFacing() );
		assertTrue( anotherTile.obstacleHasBeenAdded() );
		assertTrue( tracker.obstacleHasBeenRemoved() );
		assertFalse( anotherTile.obstacleHasBeenRemoved() );
		assertFalse( tracker.obstacleHasBeenAdded() );
		assertTrue( alternateTile.obstacleHasBeenAdded() );
		assertTrue( alternateTile.obstacleHasBeenRemoved() );
		
		tracker.clearTrackerState();
		anotherTile.clearTrackerState();
		alternateTile.clearTrackerState();
		
		// change both facing and location at the same time
		first.setPosition( NORTHEAST, tracker );
		assertEquals( tracker, first.getLocation() );
		assertEquals( NORTHEAST, first.getFacing() );
		assertEquals( NORTHEAST, first.getTemplateFacing() );
		assertTrue( anotherTile.obstacleHasBeenRemoved() );
		assertTrue( tracker.obstacleHasBeenAdded() );
		assertFalse( anotherTile.obstacleHasBeenAdded() );
		assertFalse( tracker.obstacleHasBeenRemoved() );
		assertTrue( alternateTile.obstacleHasBeenAdded() );
		assertTrue( alternateTile.obstacleHasBeenRemoved() );
		
		// test an asymmetric template
		TestObstacleTemplate altTempl = new TestObstacleTemplate();
		TrackerTile newAlternateTile = new TrackerTile();
		altTempl.setAlternateTile( newAlternateTile );
		newAlternateTile.setTerrain( new Terrain( 0f, HIGH ));
		altTempl.setHorizontallySymmetric( false );
		altTempl.setSize( 2 );
		
		tracker.clearTrackerState();
		anotherTile.clearTrackerState();
		alternateTile.clearTrackerState();
		
		// template can't be removed, and attempting to set null has no effect
		first.setTemplate( null );
		assertSame( template, first.getTemplate() );
		assertEquals( NORTHEAST, first.getFacing() );
		assertEquals( NORTHEAST, first.getTemplateFacing() );
		assertEquals( tracker, first.getLocation() );
		assertFalse( tracker.obstacleHasBeenAdded() );
		assertFalse( tracker.obstacleHasBeenRemoved() );
		assertFalse( alternateTile.obstacleHasBeenAdded() );
		assertFalse( alternateTile.obstacleHasBeenRemoved() );
		
		// set a new template, obstacle is removed and re-added
		first.setTemplate( altTempl );
		assertEquals( NORTHEAST, first.getFacing() );
		assertEquals( NORTHEAST, first.getTemplateFacing() );
		assertEquals( tracker, first.getLocation() );
		assertEquals( altTempl, first.getTemplate() );
		assertTrue( tracker.obstacleHasBeenRemoved() );
		assertTrue( tracker.obstacleHasBeenAdded() );
		assertFalse( alternateTile.obstacleHasBeenAdded() );
		assertTrue( alternateTile.obstacleHasBeenRemoved() );
		assertTrue( newAlternateTile.obstacleHasBeenAdded() );
		assertFalse( newAlternateTile.obstacleHasBeenRemoved() );

		tracker.clearTrackerState();
		newAlternateTile.clearTrackerState();
		
		// vertical directions aren't valid for a horizontally asymmetric template
		first.setFacing( SOUTH );
		assertEquals( SOUTH, first.getFacing() );
		assertSame( SOUTHEAST, first.getTemplateFacing() );
		assertTrue( tracker.obstacleHasBeenAdded() );
		assertTrue( tracker.obstacleHasBeenRemoved() );
		assertTrue( newAlternateTile.obstacleHasBeenAdded() );
		assertTrue( newAlternateTile.obstacleHasBeenRemoved() );
		
		tracker.clearTrackerState();
		newAlternateTile.clearTrackerState();
		
		// remove obstacle from the tile grid
		first.setLocation( null );
		assertTrue( tracker.obstacleHasBeenRemoved() );
		assertTrue( newAlternateTile.obstacleHasBeenRemoved() );
		assertFalse( tracker.obstacleHasBeenAdded() );
		assertFalse( newAlternateTile.obstacleHasBeenAdded() );

		tracker.clearTrackerState();
		newAlternateTile.clearTrackerState();
		
		// changing template without location, obstacle isn't removed or added
		first.setTemplate( template );
		assertFalse( tracker.obstacleHasBeenRemoved() );
		assertFalse( newAlternateTile.obstacleHasBeenRemoved() );
		assertFalse( tracker.obstacleHasBeenAdded() );
		assertFalse( newAlternateTile.obstacleHasBeenAdded() );
		
		// no placing with null location or null facing
		first.setPosition( null, tracker );
		assertFalse( tracker.obstacleHasBeenRemoved() );
		assertFalse( newAlternateTile.obstacleHasBeenRemoved() );
		assertFalse( tracker.obstacleHasBeenAdded() );
		assertFalse( newAlternateTile.obstacleHasBeenAdded() );
		
		// changing template without facing, obstacle isn't removed or added
		first.setTemplate( altTempl );
		assertFalse( tracker.obstacleHasBeenRemoved() );
		assertFalse( newAlternateTile.obstacleHasBeenRemoved() );
		assertFalse( tracker.obstacleHasBeenAdded() );
		assertFalse( newAlternateTile.obstacleHasBeenAdded() );
		
		// no placing with null location and null facing
		first.setPosition( null, null );
		assertFalse( tracker.obstacleHasBeenRemoved() );
		assertFalse( newAlternateTile.obstacleHasBeenRemoved() );
		assertFalse( tracker.obstacleHasBeenAdded() );
		assertFalse( newAlternateTile.obstacleHasBeenAdded() );
		
		// changing template without location or facing, obstacle isn't removed or added
		first.setTemplate( template );
		assertFalse( tracker.obstacleHasBeenRemoved() );
		assertFalse( newAlternateTile.obstacleHasBeenRemoved() );
		assertFalse( tracker.obstacleHasBeenAdded() );
		assertFalse( newAlternateTile.obstacleHasBeenAdded() );

		// set asymmetric template while there's no facing
		first.setTemplate( altTempl );
		assertFalse( tracker.obstacleHasBeenRemoved() );
		assertFalse( newAlternateTile.obstacleHasBeenRemoved() );
		assertFalse( tracker.obstacleHasBeenAdded() );
		assertFalse( newAlternateTile.obstacleHasBeenAdded() );
		
		// test setting facing that's invalid for template, while there's no location
		first.setFacing( SOUTH );
		assertSame( SOUTH, first.getFacing() );
		assertSame( SOUTHWEST, first.getTemplateFacing() );
		assertFalse( tracker.obstacleHasBeenRemoved() );
		assertFalse( newAlternateTile.obstacleHasBeenRemoved() );
		assertFalse( tracker.obstacleHasBeenAdded() );
		assertFalse( newAlternateTile.obstacleHasBeenAdded() );
		
		// set location, obstacle is placed
		first.setLocation( tracker );
		assertSame( tracker, first.getLocation() );
		assertFalse( tracker.obstacleHasBeenRemoved() );
		assertFalse( newAlternateTile.obstacleHasBeenRemoved() );
		assertTrue( tracker.obstacleHasBeenAdded() );
		assertTrue( newAlternateTile.obstacleHasBeenAdded() );
		
		TestMovementTemplate symmetric = new TestMovementTemplate( true, true );
		first.setTemplate( symmetric );

		tracker.clearTrackerState();
		newAlternateTile.clearTrackerState();
		
		first.setFacing( SOUTH );
		assertSame( SOUTH, first.getTemplateFacing() );
		assertTrue( tracker.obstacleHasBeenAdded() );
		assertTrue( tracker.obstacleHasBeenRemoved() );
		assertFalse( newAlternateTile.obstacleHasBeenAdded() );
		assertFalse( newAlternateTile.obstacleHasBeenRemoved() );
		
		tracker.clearTrackerState();
		newAlternateTile.clearTrackerState();
		
		// setting an asymmetric template changes template facing
		first.setTemplate( altTempl );
		assertSame( SOUTH, first.getFacing() );
		assertSame( SOUTHWEST, first.getTemplateFacing() );
		assertTrue( tracker.obstacleHasBeenRemoved() );
		assertFalse( newAlternateTile.obstacleHasBeenRemoved() );
		assertTrue( tracker.obstacleHasBeenAdded() );
		assertTrue( newAlternateTile.obstacleHasBeenAdded() );
		
		// test effects on movement, move height and occupying
		first.setBlocking( true );
		second.setBlocking( false );
		first.setOccupying( true );
		second.setOccupying( false );
		
		assertTrue( first.preventsMovement( 0 ));
		assertFalse( second.preventsMovement( 100 ));
		assertEquals( 100f, first.modifyMoveCost( 0f, 100 ), 0.001 );
		assertEquals( 0f, second.modifyMoveCost( 0f, 100 ), 0.001 );
		assertEquals( VERY_HIGH, first.modifyMoveHeight( DEEP ));
		assertEquals( DEEP, second.modifyMoveHeight( DEEP ));
		assertTrue( first.occupiesTile() );
		assertFalse( second.occupiesTile() );
	}
}
