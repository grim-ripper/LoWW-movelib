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
import static fi.grimripper.loww.Height.DEEP;
import static fi.grimripper.loww.Height.FLAT;
import static fi.grimripper.loww.Height.HIGH;
import static fi.grimripper.loww.Height.LOW;
import static fi.grimripper.loww.Height.SHALLOW;
import static fi.grimripper.loww.Height.VERY_HIGH;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fi.grimripper.loww.Direction;
import fi.grimripper.loww.Height;
import fi.grimripper.loww.StateChangeListener;
import fi.grimripper.loww.movement.MobileObject;
import fi.grimripper.loww.movement.MovementMode;
import fi.grimripper.loww.movement.MovementModifier;
import fi.grimripper.loww.movement.MovementMode.TileData;
import fi.grimripper.loww.templates.FourSquareTemplate;
import fi.grimripper.loww.templates.HorizontalTwoTileTemplate;
import fi.grimripper.loww.templates.MovementTemplate;
import fi.grimripper.loww.templates.SingleTileTemplate;
import fi.grimripper.loww.test.OnceOnlyTerrain;
import fi.grimripper.loww.test.TestBlock;
import fi.grimripper.loww.test.TestMobileObject;
import fi.grimripper.loww.test.TestMovementEvent;
import fi.grimripper.loww.test.TestMovementMode;
import fi.grimripper.loww.test.TestMovementModifier;
import fi.grimripper.loww.test.TestMovementTemplate;
import fi.grimripper.loww.test.TestObstacle;
import fi.grimripper.loww.test.TestStateChangeListener;
import fi.grimripper.loww.tiles.Block;
import fi.grimripper.loww.tiles.FilledRowHexGrid;
import fi.grimripper.loww.tiles.FilledSquareGrid;
import fi.grimripper.loww.tiles.MovementEvent;
import fi.grimripper.loww.tiles.Obstacle;
import fi.grimripper.loww.tiles.Square;
import fi.grimripper.loww.tiles.Terrain;
import fi.grimripper.loww.tiles.Tile;
import fi.grimripper.loww.tiles.TileGrid;

public class MovementModeTest {
	
	@Test
	public void testStateChangeListeners() {
		MobileObject mob = new TestMobileObject( FLAT, new SingleTileTemplate() );
		MovementMode mode = new TestMovementMode( mob, 100 );
		assertSame( mob, mode.getHost() );
		
		StateChangeListener <MovementMode> scl1 = new TestStateChangeListener();
		StateChangeListener <MovementMode> scl2 = new TestStateChangeListener();

		// initially no state change listeners, and trying to remove has no effect
		assertArrayEquals( new StateChangeListener <?>[0], mode.getStateChangeListeners() );
		assertNull( mode.removeStateChangeListener( null ));
		assertNull( mode.removeStateChangeListener( scl1 ));
		
		// test adding a state change listener
		mode.addStateChangeListener( scl1 );
		assertArrayEquals( new StateChangeListener <?>[] { scl1 },
				mode.getStateChangeListeners() );
		
		// the listener can now be removed, trying to remove others still has no effect
		assertNull( mode.removeStateChangeListener( null ));
		assertNull( mode.removeStateChangeListener( scl2 ));
		assertSame( scl1, mode.removeStateChangeListener( scl1 ));
		
		// once removed, the listener's gone
		assertNull( mode.removeStateChangeListener( scl1 ));
		assertArrayEquals( new StateChangeListener <?>[0], mode.getStateChangeListeners() );
		
		// test adding two listeners
		mode.addStateChangeListener( scl1 );
		mode.addStateChangeListener( scl2 );
		assertArrayEquals( new StateChangeListener <?>[] { scl1, scl2 },
				mode.getStateChangeListeners() );
		
		// removing first listener leaves the second
		assertSame( scl1, mode.removeStateChangeListener( scl1 ));
		assertArrayEquals( new StateChangeListener <?>[] { scl2 },
				mode.getStateChangeListeners() );
		
		// add and remove again
		mode.addStateChangeListener( scl1 );
		assertArrayEquals( new StateChangeListener <?>[] { scl2, scl1 },
				mode.getStateChangeListeners() );

		assertSame( scl1, mode.removeStateChangeListener( scl1 ));
		assertArrayEquals( new StateChangeListener <?>[] { scl2 },
				mode.getStateChangeListeners() );
		
		// test removing while there are no listeners
		assertSame( scl2, mode.removeStateChangeListener( scl2 ));
		assertNull( mode.removeStateChangeListener( null ));
		assertNull( mode.removeStateChangeListener( scl1 ));
		assertArrayEquals( new StateChangeListener <?>[0], mode.getStateChangeListeners() );	
	}
	
	@Test
	public void testMovementValue() {
		
		// test setting initial movement value with constructor
		int initialMove = 10, newMove = 15;
		MobileObject mob = new TestMobileObject( FLAT, new SingleTileTemplate() );
		MovementMode mode = new TestMovementMode( mob, initialMove );
		assertEquals( initialMove, mode.getMovement() );

		TestStateChangeListener movement = new TestStateChangeListener();
		movement.setMovementMode( mode );
		movement.setMovementValue( newMove );
		
		// listener hasn't been added, so it's not notified
		mode.setMovement( newMove );
		assertEquals( newMove, mode.getMovement() );
		assertFalse( movement.stateChanged() );
		
		mode.setMovement( initialMove );
		assertEquals( initialMove, mode.getMovement() );
		
		// added listener is notified when movement value changes
		mode.addStateChangeListener( movement );
		mode.setMovement( newMove );
		assertTrue( movement.stateChanged() );
	}
	
	@Test
	public void testDisabling() {
		MobileObject mob = new TestMobileObject( FLAT, new SingleTileTemplate() );
		MovementMode mode = new TestMovementMode( mob, 100 );
		
		// basic disabling and enabling
		assertFalse( mode.isDisabled() );
		mode.setDisabled( true );
		assertTrue( mode.isDisabled() );
		mode.setDisabled( false );
		assertFalse( mode.isDisabled() );

		TestStateChangeListener disabling = new TestStateChangeListener();
		disabling.setMovementMode( mode );
		disabling.setExpectedDisableState( false );
		disabling.setExpectedDisableState( true );
		
		// state not changed when enabled mode is re-enabled
		mode.addStateChangeListener( disabling );
		mode.setDisabled( false );
		assertFalse( disabling.stateChanged() );
		
		// after two enables, must be disabled twice, so one doesn't change state
		assertFalse( mode.isDisabled() );
		mode.setDisabled( true );
		assertFalse( disabling.stateChanged() );
		
		// now the mode is disabled
		assertFalse( mode.isDisabled() );
		mode.setDisabled( true );
		assertTrue( disabling.stateChanged() );
		
		// no change if disabled twice
		assertTrue( mode.isDisabled() );
		disabling.clearStateChanged();
		disabling.setExpectedDisableState( false );
		mode.setDisabled( true );
		assertFalse( disabling.stateChanged() );
		
		// needs to be enabled twice, so no change for one
		assertTrue( mode.isDisabled() );
		mode.setDisabled( false );
		assertFalse( disabling.stateChanged() );
		
		// enable again, and state changes
		assertTrue( mode.isDisabled() );
		mode.setDisabled( false );
		assertTrue( disabling.stateChanged() );
		assertFalse( mode.isDisabled() );
	}
	
	@Test
	public void testMovementModifiers() {
		MobileObject mob = new TestMobileObject( FLAT, new SingleTileTemplate() );
		MovementMode mode = new TestMovementMode( mob, 100 );

		TestStateChangeListener modifiers = new TestStateChangeListener();
		modifiers.setMovementMode( mode );
		
		MovementModifier mod1 = new TestMovementModifier();
		MovementModifier mod2 = new TestMovementModifier();

		// doesn't have any modifiers, trying to remove has no effect
		assertArrayEquals( new MovementModifier[0], mode.getMovementModifiers() );
		assertFalse( mode.hasMovementModifier( null ));
		assertFalse( mode.hasMovementModifier( mod1 ));
		assertNull( mode.removeMovementModifier( null ));
		assertNull( mode.removeMovementModifier( mod1 ));
		
		// null modifier can't be added
		mode.addMovementModifier( null );
		assertArrayEquals( new MovementModifier[0], mode.getMovementModifiers() );
		
		// test adding and removing a modifier
		mode.addMovementModifier( mod1 );
		assertArrayEquals( new MovementModifier[] { mod1 }, mode.getMovementModifiers() );
		assertTrue( mode.hasMovementModifier( mod1 ));
		assertSame( mod1, mode.removeMovementModifier( mod1 ));
		assertFalse( mode.hasMovementModifier( mod1 ));
		
		// test nulls and removing nonexistent modifiers after data structures have been created
		mode.addMovementModifier( mod1 );
		assertArrayEquals( new MovementModifier[] { mod1 }, mode.getMovementModifiers() );
		assertFalse( mode.hasMovementModifier( null ));
		assertTrue( mode.hasMovementModifier( mod1 ));
		assertFalse( mode.hasMovementModifier( mod2 ));
		assertNull( mode.removeMovementModifier( null ));
		assertNull( mode.removeMovementModifier( mod2 ));
		
		modifiers.setHasMovementModifier( mod2 );
		mode.addStateChangeListener( modifiers );
		
		// listener is notified when a modifier is added
		mode.addMovementModifier( mod2 );		
		assertTrue( modifiers.stateChanged() );
		assertArrayEquals( new MovementModifier[] { mod1, mod2 }, mode.getMovementModifiers() );
		assertTrue( mode.hasMovementModifier( mod1 ));

		modifiers.setHasMovementModifier( null );
		modifiers.setDoesntHaveMovementModifier( mod1 );
		modifiers.clearStateChanged();
		
		// listener is notified when a modifier is removed
		assertSame( mod1, mode.removeMovementModifier( mod1 ));
		assertTrue( modifiers.stateChanged() );
		assertArrayEquals( new MovementModifier[] { mod2 }, mode.getMovementModifiers() );
		assertTrue( mode.hasMovementModifier( mod2 ));
		
		modifiers.setHasMovementModifier( mod1 );
		modifiers.setDoesntHaveMovementModifier( null );
		modifiers.clearStateChanged();

		// test adding removed modifier again
		mode.addMovementModifier( mod1 );
		assertTrue( modifiers.stateChanged() );
		assertArrayEquals( new MovementModifier[] { mod2, mod1 }, mode.getMovementModifiers() );
		assertTrue( mode.hasMovementModifier( mod1 ));
		
		modifiers.setHasMovementModifier( null );
		modifiers.setDoesntHaveMovementModifier( mod1 );
		modifiers.clearStateChanged();
		
		// test removing the last modifier
		assertSame( mod1, mode.removeMovementModifier( mod1 ));
		assertTrue( modifiers.stateChanged() );
		
		// test removing the only remaining modifier
		modifiers.setDoesntHaveMovementModifier( mod2 );
		modifiers.clearStateChanged();
		assertSame( mod2, mode.removeMovementModifier( mod2 ));
		assertTrue( modifiers.stateChanged() );
		
		// modifiers are gone, re-test null after data structure has been created
		assertFalse( mode.hasMovementModifier( mod1 ));
		assertFalse( mode.hasMovementModifier( mod2 ));
		assertFalse( mode.hasMovementModifier( null ));
		assertNull( mode.removeMovementModifier( null ));
		assertNull( mode.removeMovementModifier( mod1 ));
		assertNull( mode.removeMovementModifier( mod2 ));
		assertArrayEquals( new MovementModifier[0], mode.getMovementModifiers() );
		
		modifiers.setHasMovementModifier( mod2 );
		modifiers.setDoesntHaveMovementModifier( null );
		modifiers.clearStateChanged();

		// test adding modifier after all modifiers have been removed
		mode.addMovementModifier( mod2 );
		assertTrue( modifiers.stateChanged() );
		assertArrayEquals( new MovementModifier[] { mod2 }, mode.getMovementModifiers() );
		assertTrue( mode.hasMovementModifier( mod2 ));
		
		modifiers.setHasMovementModifier( null );
		modifiers.setDoesntHaveMovementModifier( mod2 );
		modifiers.clearStateChanged();
		
		// test removing the only modifier again
		assertSame( mod2, mode.removeMovementModifier( mod2 ));
		assertTrue( modifiers.stateChanged() );
	}

	@Test
	public void testTileUtilities() {
		testMinimumHeight( new FilledSquareGrid( 0, 2, 2 ));
		testMinimumHeight( FilledRowHexGrid.createWithHexSize( 0, 0, 2, 2, 2 ));
		
		testTerrainHeight( new FilledSquareGrid( 0, 2, 2 ));
		testTerrainHeight( FilledRowHexGrid.createWithHexSize( 0, 0, 2, 2, 2 ));
	}
	
	@Test
	public void testOccupyHeight() {
		testOccupyHeight( new FilledSquareGrid( 0, 5, 5 ));
		testOccupyHeight( FilledRowHexGrid.createWithHexSize( 0, 0, 5, 5, 5 ));
	}
	
	@Test (expected = NullPointerException.class)
	public void testMinimumHeightNullPointerException1() {
		new TestMovementMode( null, 0 ).getMinimumHeight( true, (Tile[])null );
	}

	@Test (expected = NullPointerException.class)
	public void testMinimumHeightNullPointerException2() {
		TestMovementMode move = new TestMovementMode( null, 0 );
		move.addMovementModifier( null );
		move.getMinimumHeight( true, (Tile[])null );
	}

	@Test
	public void testTileData() {
		testTileData( new FilledSquareGrid( 0, 1, 2 ));
		testTileData( FilledRowHexGrid.createWithHexSize( 0, 0, 1, 2, 0 ));
	}
	
	@Test
	public void testMoveCost() {
		testMoveCost( new FilledSquareGrid( 0, 1, 2 ));
		testMoveCost( FilledRowHexGrid.createWithHexSize( 0, 0, 1, 2, 0 ));
	}
	
	@Test( expected=NullPointerException.class )
	public void testObstacleCostNullPointerException() {
		MobileObject mob = new TestMobileObject( FLAT, new SingleTileTemplate() );
		MovementMode mode = new TestMovementMode( mob, 100 );
		
		mode.addObstacleCosts( null, FLAT, 0f );
	}
	
	@Test
	public void testBlocks() {
		testBlocks( new FilledSquareGrid( 0, 3, 3 ));
		testBlocks( FilledRowHexGrid.createWithHexSize( 0, 0, 3, 3, 3 ));
	}
	
	@Test( expected=NullPointerException.class )
	public void testBlockNullPointerException() {
		MobileObject mob = new TestMobileObject( FLAT, new SingleTileTemplate() );
		MovementMode mode = new TestMovementMode( mob, 100 );
		
		mode.blocksAllowTemplate( null, FLAT );
	}
	
	@Test
	public void testCanOccupy() {
		testCanOccupy( new FilledSquareGrid( 0, 5, 5 ));
		testCanOccupy( FilledRowHexGrid.createWithHexSize( 0, 0, 5, 5, 5 ));
		
		TileGrid <Square> squareGrid = new FilledSquareGrid( 0, 5, 5 );
		Square occupySquare = squareGrid.getTileAtRC( 3, 3 );
		MobileObject mob = new TestMobileObject( FLAT, new FourSquareTemplate() );
		MovementMode mode = new TestMovementMode( mob, 100 );
		
		Terrain terrain = new Terrain( 0f );
		for (Square[] row : squareGrid.getTiles())
			for (Square s : row)
				s.setTerrain( terrain );

		// horizontal and vertical facing directions are invalid for the template
		assertFalse( mode.canOccupy( occupySquare, NORTH, FLAT ));
		assertFalse( mode.canOccupy( occupySquare, EAST, FLAT ));
		assertFalse( mode.canOccupy( occupySquare, SOUTH, FLAT ));
		assertFalse( mode.canOccupy( occupySquare, WEST, FLAT ));
	}
	
	@Test
	public void testOccupyHeightSaved() {
		testOccupyHeightSaved( new FilledSquareGrid( 0, 5, 5 ));
		testOccupyHeightSaved( FilledRowHexGrid.createWithHexSize( 0, 0, 5, 5, 5 ));
		
		TileGrid <Square> squareGrid = new FilledSquareGrid( 0, 5, 5 );
		Square occupySquare = squareGrid.getTileAtRC( 3, 3 );
		MobileObject mob = new TestMobileObject( FLAT, new FourSquareTemplate() );
		MovementMode mode = new TestMovementMode( mob, 100 );
		
		Terrain terrain = new Terrain( 0f );
		for (Square[] row : squareGrid.getTiles())
			for (Square s : row)
				s.setTerrain( terrain );
		
		// test saving occupy height, saved for only one direction with asymmetric template
		Height occupyHeight = mode.calculateOccupyHeight( occupySquare, NORTHEAST );
		mode.setOccupyHeightForPosition( occupySquare, NORTHEAST, occupyHeight );
		assertEquals( occupyHeight, mode.getOccupyHeight( occupySquare, NORTHEAST ));
		assertTrue( mode.canBeOccupied( occupySquare, NORTHEAST ));
		
		for (Direction d : Direction.values())
			if (d != NORTHEAST)
				assertNull( mode.getOccupyHeight( occupySquare, d ));
		
		mode.clearOccupyData();
		
		// horizontal and vertical directions aren't valid for the template
		assertNull( mode.calculateOccupyHeight( occupySquare, NORTH ));
		assertNull( mode.calculateOccupyHeight( occupySquare, EAST ));
		assertNull( mode.calculateOccupyHeight( occupySquare, SOUTH ));
		assertNull( mode.calculateOccupyHeight( occupySquare, WEST ));
		
		mode.setOccupyHeightForPosition( occupySquare, NORTH, occupyHeight );
		mode.setOccupyHeightForPosition( occupySquare, EAST, occupyHeight );
		mode.setOccupyHeightForPosition( occupySquare, SOUTH, occupyHeight );
		mode.setOccupyHeightForPosition( occupySquare, WEST, occupyHeight );
		
		for (Direction d : Direction.values())
			assertNull( mode.getOccupyHeight( occupySquare, d ));
		
		assertNull( mode.calculateOrGetOccupyHeight( occupySquare, NORTH ));
		assertNull( mode.calculateOrGetOccupyHeight( occupySquare, EAST ));
		assertNull( mode.calculateOrGetOccupyHeight( occupySquare, SOUTH ));
		assertNull( mode.calculateOrGetOccupyHeight( occupySquare, WEST ));
		
		mode.setOccupyHeight( occupySquare, NORTH, FLAT );
		mode.setOccupyHeight( occupySquare, EAST, FLAT );
		mode.setOccupyHeight( occupySquare, SOUTH, FLAT );
		mode.setOccupyHeight( occupySquare, WEST, FLAT );
		
		for (Direction d : Direction.values())
			assertNull( mode.getOccupyHeight( occupySquare, d ));
		
		// test a valid vertical and invalid horizontal facing for vertically asymmetric template
		mob = new TestMobileObject( FLAT, new TestMovementTemplate( true, false ));
		mode = new TestMovementMode( mob, 100 );
		
		Height height = mode.calculateOccupyHeight( occupySquare );
		mode.setOccupyHeight( occupySquare, NORTH,
				mode.calculateOrGetOccupyHeight( occupySquare, NORTH ));
		mode.setOccupyHeight( occupySquare, WEST,
				mode.calculateOrGetOccupyHeight( occupySquare, WEST ));
		
		assertSame( height, mode.getOccupyHeight( occupySquare, NORTH ));
		assertSame( height, mode.getOccupyHeight( occupySquare, NORTHEAST ));
		assertSame( height, mode.getOccupyHeight( occupySquare, NORTHWEST ));
		assertNull( mode.getOccupyHeight( occupySquare, WEST ));
		assertNull( mode.getOccupyHeight( occupySquare, SOUTHWEST ));
		assertNull( mode.getOccupyHeight( occupySquare, SOUTH ));
		assertNull( mode.getOccupyHeight( occupySquare, SOUTHEAST ));
		assertNull( mode.getOccupyHeight( occupySquare, EAST ));
		
		mode.clearOccupyData();
		
		mode.setOccupyHeight( occupySquare, SOUTH,
				mode.calculateOrGetOccupyHeight( occupySquare, SOUTH ));
		mode.setOccupyHeight( occupySquare, EAST,
				mode.calculateOrGetOccupyHeight( occupySquare, EAST ));
		
		assertSame( height, mode.getOccupyHeight( occupySquare, SOUTH ));
		assertSame( height, mode.getOccupyHeight( occupySquare, SOUTHEAST ));
		assertSame( height, mode.getOccupyHeight( occupySquare, SOUTHWEST ));
		assertNull( mode.getOccupyHeight( occupySquare, EAST ));
		assertNull( mode.getOccupyHeight( occupySquare, NORTHEAST ));
		assertNull( mode.getOccupyHeight( occupySquare, NORTH ));
		assertNull( mode.getOccupyHeight( occupySquare, NORTHWEST ));
		assertNull( mode.getOccupyHeight( occupySquare, WEST ));
		
		// test a valid horizontal and invalid vertical facing for horizontally asymmetric template
		mob = new TestMobileObject( FLAT, new TestMovementTemplate( false, true ));
		mode = new TestMovementMode( mob, 100 );

		mode.setOccupyHeight( occupySquare, WEST,
				mode.calculateOrGetOccupyHeight( occupySquare, WEST ));
		mode.setOccupyHeight( occupySquare, NORTH,
				mode.calculateOrGetOccupyHeight( occupySquare, NORTH ));

		assertSame( height, mode.getOccupyHeight( occupySquare, WEST ));
		assertSame( height, mode.getOccupyHeight( occupySquare, NORTHWEST ));
		assertSame( height, mode.getOccupyHeight( occupySquare, SOUTHWEST ));
		assertNull( mode.getOccupyHeight( occupySquare, NORTH ));
		assertNull( mode.getOccupyHeight( occupySquare, NORTHEAST ));
		assertNull( mode.getOccupyHeight( occupySquare, EAST ));
		assertNull( mode.getOccupyHeight( occupySquare, SOUTHEAST ));
		assertNull( mode.getOccupyHeight( occupySquare, SOUTH ));

		mode.clearOccupyData();

		mode.setOccupyHeight( occupySquare, EAST,
				mode.calculateOrGetOccupyHeight( occupySquare, EAST ));
		mode.setOccupyHeight( occupySquare, SOUTH,
				mode.calculateOrGetOccupyHeight( occupySquare, SOUTH ));
		
		assertSame( height, mode.getOccupyHeight( occupySquare, EAST ));
		assertSame( height, mode.getOccupyHeight( occupySquare, NORTHEAST ));
		assertSame( height, mode.getOccupyHeight( occupySquare, SOUTHEAST ));
		assertNull( mode.getOccupyHeight( occupySquare, NORTH ));
		assertNull( mode.getOccupyHeight( occupySquare, NORTHWEST ));
		assertNull( mode.getOccupyHeight( occupySquare, WEST ));
		assertNull( mode.getOccupyHeight( occupySquare, SOUTHWEST ));
		assertNull( mode.getOccupyHeight( occupySquare, SOUTH ));
	}
	
	@Test
	public void testEventBuffer() {
		testEventBuffer( new FilledSquareGrid( 0, 1, 2 ));
		testEventBuffer( FilledRowHexGrid.createWithHexSize( 0, 0, 1, 2, 0 ));
	}
	
	@Test
	public void testMiscellaneous() {
		testMiscellaneous( new FilledSquareGrid( 0, 3, 3 ));
		testMiscellaneous( FilledRowHexGrid.createWithHexSize( 0, 0, 3, 3, 3 ));
	}

	@Test( expected=NullPointerException.class )
	public void testEnterEventsNullPointerException() {
		MobileObject mob = new TestMobileObject( FLAT, new SingleTileTemplate() );
		MovementMode mode = new TestMovementMode( mob, 100 );
		
		mode.testEnterEvents( null, FLAT, FLAT );
	}

	private void testMinimumHeight( TileGrid <?> grid ) {
		TestMovementMode move = new TestMovementMode( null, 0 );
		
		// null height for null tiles
		assertNull( move.getMinimumHeight( false, (Tile)null ));
		assertNull( move.getMinimumHeight( false, grid.getTileAtRC( 0, 0 ), null ));
		
		Tile[] tiles = { grid.getTileAtRC( 0, 0 ), grid.getTileAtRC( 0, 1 ),
			grid.getTileAtRC( 1, 0 ), grid.getTileAtRC( 1, 1 )};
		
		// default height is deep when nothing affects it (also for empty template)
		assertSame( DEEP, move.getMinimumHeight( true ));
		assertSame( DEEP, move.getMinimumHeight( true, tiles ));
		
		for (Tile t : tiles)
			t.setTerrain( new Terrain( 0f, FLAT ));
		
		TestMovementModifier flat = new TestMovementModifier();
		flat.setMovementHeight( tiles[0], FLAT );
		move.addMovementModifier( flat );
		
		// modifier checked for each tile, highest move height used, terrain height ignored
		assertSame( FLAT, move.getMinimumHeight( false, tiles ));
		assertEquals( tiles.length, flat.getHeightCheckCount() );
		flat.resetHeightCheckCount();
		
		TestMovementModifier shallow = new TestMovementModifier();
		shallow.setMovementHeight( tiles[1], SHALLOW );
		move.addMovementModifier( shallow );
		
		// lower move height from second modifier is ignored
		assertSame( FLAT, move.getMinimumHeight( false, tiles ));
		assertEquals( tiles.length, flat.getHeightCheckCount() );
		assertEquals( tiles.length, shallow.getHeightCheckCount() );
		
		flat.resetHeightCheckCount();
		shallow.resetHeightCheckCount();
		move.removeMovementModifier( flat );
		
		// second modifier is used after the first has been removed
		assertSame( SHALLOW, move.getMinimumHeight( false, tiles ));
		assertEquals( tiles.length, shallow.getHeightCheckCount() );
		assertEquals( 0, flat.getHeightCheckCount() );
		
		shallow.resetHeightCheckCount();
		move.addMovementModifier( flat );
		
		// add removed modifier, it's used again
		assertSame( FLAT, move.getMinimumHeight( false, tiles ));
		assertEquals( tiles.length, flat.getHeightCheckCount() );
		assertEquals( tiles.length, shallow.getHeightCheckCount() );
		
		TestObstacle shallowObstacle = new TestObstacle( SHALLOW );
		shallowObstacle.setOccupying( true );
		shallowObstacle.setPosition( NORTH, tiles[1] );
		shallowObstacle.setOccupyHeight( DEEP );
		
		// flat from modifier is still highest, obstacle ignored when below move height
		assertSame( FLAT, move.getMinimumHeight( false, tiles ));
		assertEquals( 0, shallowObstacle.getMoveHeightCheck() );
		
		// obstacle checked when not below move height
		move.removeMovementModifier( flat );
		assertSame( SHALLOW, move.getMinimumHeight( false, tiles ));
		assertEquals( 1, shallowObstacle.getMoveHeightCheck() );
		
		shallowObstacle.resetMoveHeightCheck();
		TestObstacle lowObstacle = new TestObstacle( LOW );
		lowObstacle.setOccupying( true );
		lowObstacle.setPosition( NORTH, tiles[2] );
		
		// higher obstacle increases move height (lower obstacle checked first)
		assertSame( LOW, move.getMinimumHeight( false, tiles ));
		assertEquals( 1, shallowObstacle.getMoveHeightCheck() );
		assertEquals( 1, lowObstacle.getMoveHeightCheck() );
		
		shallowObstacle.resetMoveHeightCheck();
		lowObstacle.resetMoveHeightCheck();
		shallowObstacle.setLocation( tiles[3] );
		
		// lower obstacle is ignored when it's below move height
		assertSame( LOW, move.getMinimumHeight( false, tiles ));
		assertEquals( 1, lowObstacle.getMoveHeightCheck() );
		assertEquals( 0, shallowObstacle.getMoveHeightCheck() );
		
		lowObstacle.resetMoveHeightCheck();
		TestMovementModifier ignore = new TestMovementModifier( lowObstacle );
		move.addMovementModifier( ignore );
		
		// obstacle doesn't affect move height since it's ignored
		assertSame( FLAT, move.getMinimumHeight( false, tiles ));
		assertEquals( 1, shallowObstacle.getMoveHeightCheck() );
		assertEquals( 0, lowObstacle.getMoveHeightCheck() );
		assertTrue( ignore.ignoreObstacleChecked() );
		
		// null movement height from modifier is ignored
		TestMovementModifier nullMod = new TestMovementModifier();
		nullMod.setMovementHeight( tiles[0], null );
		move.addMovementModifier( nullMod );
		assertNotNull( move.getMinimumHeight( false, tiles ));
	}
	
	private void testTerrainHeight( TileGrid <?> grid ) {
		TestMovementMode move = new TestMovementMode( null, 0 );
		
		// null height for null tiles
		assertNull( move.getTerrainHeight( null, (Tile)null ));
		assertNull( move.getTerrainHeight( null, null, grid.getTileAtRC( 0, 0 )));
		
		Tile[] tiles = { grid.getTileAtRC( 0, 0 ), grid.getTileAtRC( 0, 1 ),
			grid.getTileAtRC( 1, 0 ), grid.getTileAtRC( 1, 1 )};
		
		// move height is deep without anything that affects it
		assertSame( DEEP, move.getTerrainHeight( DEEP ));
		
		for (Tile t : tiles)
			t.setTerrain( new Terrain( 0f, SHALLOW ));
		
		// move height determines by terrain
		assertSame( SHALLOW, move.getTerrainHeight( null, tiles ));
		
		TestMovementModifier low = new TestMovementModifier();
		low.setMovementHeight( tiles[0], LOW );
		move.addMovementModifier( low );
		
		// modifier doesn't affect terrain height
		assertSame( SHALLOW, move.getTerrainHeight( null, tiles ));
		assertEquals( 0, low.getHeightCheckCount() );
		
		TestObstacle flatObstacle = new TestObstacle( FLAT );
		flatObstacle.setOccupying( true );
		flatObstacle.setPosition( NORTH, tiles[1] );
		flatObstacle.setOccupyHeight( DEEP );
		
		// obstacle doesn't affect movement height
		assertSame( SHALLOW, move.getTerrainHeight( null, tiles ));
		assertEquals( 0, flatObstacle.getMoveHeightCheck() );
				
		// null height from terrain is ignored
		Terrain nullTerrain = new Terrain( 0f, null );
		tiles[0].setTerrain( nullTerrain );
		assertNotNull( move.getTerrainHeight( null, tiles ));
	}
	
	private void testOccupyHeight( TileGrid <?> grid ) {
		MobileObject mob = new TestMobileObject( FLAT, new HorizontalTwoTileTemplate() );
		MovementMode mode = new TestMovementMode( mob, 100 );

		Tile[][] tiles = grid.getTiles();
		Terrain flatTerrain = new Terrain( 0f, FLAT );
		
		for (Tile[] row : tiles)
			for (Tile t : row)
				t.setTerrain( flatTerrain );
		
		// null in the template, no height
		assertNull( mode.calculateOccupyHeight( null, grid.getTileAtRC( 0, 0 )));
		assertNull( mode.calculateOccupyHeight( grid.getTileAtRC( 0, 0 ), EAST ));
		
		// basic height calculation with a set of tiles and a template
		assertEquals( FLAT,
				mode.calculateOccupyHeight( grid.getTileAtRC( 0, 0 ), grid.getTileAtRC( 0, 1 )));
		assertEquals( FLAT, mode.calculateOccupyHeight( grid.getTileAtRC( 0, 2 ), EAST ));
		
		Terrain lowTerrain = new Terrain( 0, LOW );
		
		for (int i = 0; i < tiles.length; i += 2)
			for (int j = 0; j < tiles[i].length; j += 2)
				tiles[i][j].setTerrain( lowTerrain );
		
		// calculate again with low terrain
		assertEquals( LOW,
				mode.calculateOccupyHeight( grid.getTileAtRC( 2, 1 ), grid.getTileAtRC( 2, 2 )));
		assertEquals( LOW, mode.calculateOccupyHeight( grid.getTileAtRC( 2, 1 ), EAST ));
		
		// occupy height while standing on top of an obstacle
		Obstacle highObstacle = new TestObstacle( HIGH, new SingleTileTemplate() );
		highObstacle.setPosition( EAST, grid.getTileAtRC( 1, 1 ));
		highObstacle.setOccupyHeight( FLAT );
		assertEquals( HIGH,
				mode.calculateOccupyHeight( grid.getTileAtRC( 1, 0 ), grid.getTileAtRC( 1, 1 )));
		assertEquals( HIGH, mode.calculateOccupyHeight( grid.getTileAtRC( 1, 2 ), EAST ));
		
		// highest terrain/obstacle height determines occupy height
		Terrain veryHighTerrain = new Terrain( 0, VERY_HIGH );
		grid.getTileAtRC( 1, 2 ).setTerrain( veryHighTerrain );
		assertEquals( VERY_HIGH,
				mode.calculateOccupyHeight( grid.getTileAtRC( 1, 1 ), grid.getTileAtRC( 1, 2 )));
		assertEquals( VERY_HIGH, mode.calculateOccupyHeight( grid.getTileAtRC( 1, 2 ), EAST ));
		
		Obstacle lowObstacle = new TestObstacle( LOW, new SingleTileTemplate() );
		lowObstacle.setPosition( EAST, grid.getTileAtRC( 2, 2 ));
		lowObstacle.setOccupyHeight( FLAT );
		
		Obstacle veryHighObstacle = new TestObstacle( VERY_HIGH, new SingleTileTemplate() );
		veryHighObstacle.setPosition( EAST, grid.getTileAtRC( 3, 3 ));
		veryHighObstacle.setOccupyHeight( FLAT );
		
		MovementModifier occupyHigh = new TestMovementModifier( HIGH );
		mode.addMovementModifier( occupyHigh );
		
		// modifier overrides occupy height for lower terrain/obstacle, but not higher ones
		assertEquals( HIGH,
				mode.calculateOccupyHeight( grid.getTileAtRC( 3, 2 ), grid.getTileAtRC( 3, 1 )));
		assertEquals( HIGH, mode.calculateOccupyHeight( grid.getTileAtRC( 4, 1 ), EAST ));
		assertEquals( HIGH, mode.calculateOccupyHeight( grid.getTileAtRC( 2, 2 )));
		assertEquals( HIGH, mode.calculateOccupyHeight( grid.getTileAtRC( 2, 3 ), EAST ));
		assertEquals( VERY_HIGH, mode.calculateOccupyHeight( grid.getTileAtRC( 1, 2 )));
		assertEquals( VERY_HIGH, mode.calculateOccupyHeight( grid.getTileAtRC( 1, 3 ), EAST ));
		assertEquals( VERY_HIGH, mode.calculateOccupyHeight( grid.getTileAtRC( 3, 3 )));
		assertEquals( VERY_HIGH, mode.calculateOccupyHeight( grid.getTileAtRC( 3, 4 ), EAST ));
	}
	
	private void testTileData( TileGrid <?> grid ) {
		MobileObject mob = new TestMobileObject( FLAT, new SingleTileTemplate() );
		MovementMode mode = new TestMovementMode( mob, 100 );
		
		// null tile is valid, and doesn't cause problems
		TileData forNull = mode.createTileData( null );
		assertNotNull( forNull );
		
		// create new tile data for a tile, and remains in use for the tile
		TileData forTile = mode.createTileData( grid.getTileAtRC( 0, 0 ));
		assertNotNull( forTile );
		assertNotSame( forNull, forTile );
		assertSame( forNull, mode.createTileData( null ));
		assertSame( forTile, mode.createTileData( grid.getTileAtRC( 0, 0 )));
		
		assertNotSame( forTile, mode.createTileData( grid.getTileAtRC( 0, 1 )));
		
		Height[] heights = Height.values();
		for (Height h : heights) {
			
			// no move costs set originally, impassable costs for unset heights
			assertFalse( forTile.isMoveCostSet( h ));
			assertEquals( mode.getImpassableMoveCost(), forTile.getMoveCost( h ), 0.001 );
			
			// test setting move cost
			forTile.setMoveCost( h.ordinal(), h );
			assertTrue( forTile.isMoveCostSet( h ));
			assertEquals( h.ordinal(), forTile.getMoveCost( h ), 0.001 );
			
			// can occupy not set originally, can't occupy if not set
			assertFalse( forTile.isCanOccupySet( h ));
			assertFalse( forTile.canBeOccupied( h ));
			
			// test setting if can occupy
			forTile.setCanBeOccupied( h.ordinal() % 2 == 0, h );
			assertEquals( h.ordinal() % 2 == 0, forTile.canBeOccupied( h ));
		}
		
		// repeat previous tests without setting for all heights, and in reverse order
		heights = new Height[] { BLOCKING, HIGH, FLAT, DEEP };
		for (Height h : heights) {
			assertFalse( forNull.isMoveCostSet( h ));
			assertEquals( mode.getImpassableMoveCost(), forNull.getMoveCost( h ), 0.001 );
			
			forNull.setMoveCost( h.ordinal(), h );
			assertTrue( forNull.isMoveCostSet( h ));
			assertEquals( h.ordinal(), forNull.getMoveCost( h ), 0.001 );
			
			assertFalse( forNull.isCanOccupySet( h ));
			assertFalse( forNull.canBeOccupied( h ));
			
			forNull.setCanBeOccupied( h.ordinal() % 2 == 0, h );
			assertEquals( h.ordinal() % 2 == 0, forNull.canBeOccupied( h ));
		}
	}

	private void testMoveCost( TileGrid <?> grid ) {
		MobileObject mob = new TestMobileObject( FLAT, new SingleTileTemplate() );
		MovementMode mode = new TestMovementMode( mob, 100 );
		
		Tile[][] tiles = grid.getTiles();
		float terrainCost = 2f;
		OnceOnlyTerrain costTwoFlatTerrain = new OnceOnlyTerrain( terrainCost, FLAT );
		for (Tile[] row : tiles)
			for (Tile t : row)
				t.setTerrain( costTwoFlatTerrain );
		
		// test original calculation and retrieving already calculated cost (terrain checked once)
		assertEquals( terrainCost, mode.getTerrainCost( costTwoFlatTerrain ), 0.001 );
		assertEquals( terrainCost, mode.getTerrainCost( costTwoFlatTerrain ), 0.001 );
		
		costTwoFlatTerrain.cancelCostRetrieved();
		mode.clearTerrainCosts();
		
		// test for tile and height, higher than terrain ignores the terrain
		assertEquals( terrainCost, mode.getAndSaveCost( grid.getTileAtRC( 0, 0 ), FLAT ), 0.001 );
		assertEquals( 1f, mode.getAndSaveCost( grid.getTileAtRC( 0, 0 ), LOW ), 0.001 );
		
		TestMovementModifier terrainMod = new TestMovementModifier( 3f );
		mode.addMovementModifier( terrainMod );
		costTwoFlatTerrain.cancelCostRetrieved();
		
		// test modifier replacing terrain cost, terrain checked once
		assertEquals( terrainMod.getTerrainCost(), mode.getTerrainCost( costTwoFlatTerrain ), 0.001 );
		assertTrue( costTwoFlatTerrain.isCostRetrieved() );
		assertEquals( terrainMod.getTerrainCost(), mode.getTerrainCost( costTwoFlatTerrain ), 0.001 );
		
		mode.removeMovementModifier( terrainMod );
		
		// test obstacle being ignored when it's below movement height
		Obstacle deepObstacle = new TestObstacle( DEEP, 1f, false, new SingleTileTemplate() );
		deepObstacle.setOccupyHeight( DEEP );
		assertFalse( mode.isIgnored( deepObstacle, DEEP ));
		assertTrue( mode.isIgnored( deepObstacle, SHALLOW ));
		
		TestMovementModifier obstacleMod = new TestMovementModifier( deepObstacle, 1.5f );
		mode.addMovementModifier( obstacleMod );
		
		// test modifier ignoring obstacle
		assertTrue( mode.isIgnored( deepObstacle, DEEP ));
		assertTrue( obstacleMod.ignoreObstacleChecked() );
		
		// test again with a modifier that doesn't ignore the obstacle
		mode.removeMovementModifier( obstacleMod );
		mode.addMovementModifier( terrainMod );
		assertFalse( mode.isIgnored( deepObstacle, DEEP ));
		assertTrue( terrainMod.ignoreObstacleChecked() );
		mode.removeMovementModifier( terrainMod );
		
		// test adding obstacle costs (the obstacle isn't placed on the grid)
		Tile testTile = grid.getTileAtRC( 0, 0 );
		assertEquals( 0f, mode.addObstacleCosts( testTile, DEEP, 0f ), 0.001f );
		assertNull( obstacleMod.getObstaclesForCost() );
		
		// test modifier changing cost for an obstacle, but without the obstacle
		mode.addMovementModifier( obstacleMod );
		assertEquals( obstacleMod.getObstacleCost(),
				mode.addObstacleCosts( testTile, DEEP, 0f ), 0.001f );
		assertArrayEquals( new Obstacle[0], obstacleMod.getObstaclesForCost() );
		
		// the mobile object doesn't affect its own movement cost
		mob.setPosition( EAST, testTile );
		assertEquals( obstacleMod.getObstacleCost(),
				mode.addObstacleCosts( testTile, DEEP, 0f ), 0.001f );
		assertArrayEquals( new Obstacle[0], obstacleMod.getObstaclesForCost() );
		
		// place obstacle in the tile, but it's ignored because of the modifier
		deepObstacle.setPosition( EAST, testTile );
		assertEquals( obstacleMod.getObstacleCost(),
				mode.addObstacleCosts( testTile, DEEP, 0f ), 0.001f );
		assertArrayEquals( new Obstacle[0], obstacleMod.getObstaclesForCost() );
		
		// place a second obstacle, which isn't ignored
		TestObstacle shallowObstacle =
				new TestObstacle( SHALLOW, 2.5f, false, new SingleTileTemplate() );
		shallowObstacle.setPosition( EAST, testTile );
		shallowObstacle.setOccupyHeight( SHALLOW );
		assertEquals( obstacleMod.getObstacleCost() + shallowObstacle.getObstacleCost(),
				mode.addObstacleCosts( testTile, DEEP, 0f ), 0.001f );
		assertArrayEquals( new Obstacle[] { shallowObstacle }, obstacleMod.getObstaclesForCost() );
		
		// add another modifier, which also affects the cost
		TestMovementModifier obstMod2 = new TestMovementModifier( (Obstacle)null, 3f );
		mode.addMovementModifier( obstMod2 );
		assertEquals( obstacleMod.getObstacleCost() + obstMod2.getObstacleCost() +
				shallowObstacle.getObstacleCost(),
				mode.addObstacleCosts( testTile, DEEP, 0f ), 0.001f );
		assertArrayEquals( new Obstacle[] { shallowObstacle }, obstacleMod.getObstaclesForCost() );
		assertArrayEquals( new Obstacle[] { shallowObstacle }, obstMod2.getObstaclesForCost() );
		
		costTwoFlatTerrain.cancelCostRetrieved();
		mode.clearTerrainCosts();
		
		// calculate cost above the obstacles, so both are ignored, modifiers set terrain cost to 0
		assertEquals( obstacleMod.getObstacleCost() + obstMod2.getObstacleCost(),
				mode.getAndSaveCost( testTile, FLAT ), 0.001 );
		assertArrayEquals( new Obstacle[0], obstacleMod.getObstaclesForCost() );
		assertArrayEquals( new Obstacle[0], obstMod2.getObstaclesForCost() );
		
		// above the terrain, its cost is replaced by 1 and modifiers don't affect that
		assertEquals( obstacleMod.getObstacleCost() + obstMod2.getObstacleCost() + 1f,
				mode.getAndSaveCost( testTile, LOW ), 0.001 );
		assertArrayEquals( new Obstacle[0], obstacleMod.getObstaclesForCost() );
		assertArrayEquals( new Obstacle[0], obstMod2.getObstaclesForCost() );

		// terrain cost was calculated only once
		assertTrue( costTwoFlatTerrain.isCostRetrieved() );
		costTwoFlatTerrain.cancelCostRetrieved();
		obstacleMod.setObstaclesForCost( null );
		obstMod2.setObstaclesForCost( null );
		
		// the cost is saved and isn't calculated again
		assertEquals( obstacleMod.getObstacleCost() + obstMod2.getObstacleCost() + 1f,
				mode.getAndSaveCost( grid.getTileAtRC( 0, 0 ), LOW ), 0.001 );
		assertFalse( costTwoFlatTerrain.isCostRetrieved() );
		assertNull( obstacleMod.getObstaclesForCost() );
		assertNull( obstMod2.getObstaclesForCost() );
	}
	
	private void testBlocks( TileGrid <?> grid ) {
		MobileObject mob = new TestMobileObject( FLAT, new SingleTileTemplate() );
		MovementMode mode = new TestMovementMode( mob, 100 );
		
		Terrain terrain = new Terrain( 0f );
		for (Tile[] row : grid.getTiles())
			for (Tile t : row)
				t.setTerrain( terrain );
		
		Tile firstTile = grid.getTileAtRC( 0, 0 );
		Tile secondTile = grid.getTileAtRC( 0, 1 );
		Tile nextRow = grid.getTileAtRC( 1, 0 );
		
		// blocks not tested for empty template or non-adjacent tiles, no blocks to test
		assertTrue( mode.blocksAllowTemplate( new Tile[0], FLAT ));
		assertTrue( mode.blocksAllowTemplate( new Tile[] { firstTile, secondTile }, FLAT ));
		assertTrue( mode.blocksAllowTemplate(
				new Tile[] { nextRow, grid.getTileAtRC( 2, 1 )}, DEEP ));
		
		// block is ignored when it's below movement height
		TestBlock lowBlock = new TestBlock( LOW, 1.5f );
		assertFalse( mode.isIgnored( lowBlock, firstTile, secondTile, LOW ));
		assertTrue( mode.isIgnored( lowBlock, firstTile, secondTile, HIGH ));
		
		// a block can have different heights at different tiles
		TestBlock variableBlock = new TestBlock( FLAT, 3f );
		variableBlock.addTileHeight( firstTile, LOW );
		variableBlock.addTileHeight( secondTile, HIGH );
		
		assertFalse( mode.isIgnored( variableBlock, firstTile, secondTile, LOW ));
		assertFalse( mode.isIgnored( variableBlock, firstTile, secondTile, HIGH ));
		assertTrue( mode.isIgnored( variableBlock, firstTile, secondTile, VERY_HIGH ));

		// block modifies move cost between two tiles
		firstTile.addBlock( lowBlock );
		assertEquals( lowBlock.getMoveCost(),
				mode.addBlockCosts( firstTile, secondTile, LOW, LOW, 0f ), 0.001f );
		assertEquals( lowBlock.getMoveCost(),
				mode.addBlockCosts( firstTile, secondTile, LOW, HIGH, 0f ), 0.001f );
		assertEquals( 0f, mode.addBlockCosts( firstTile, secondTile, HIGH, LOW, 0f ), 0.001f );
		
		// block is added to different tiles, and tested for each
		secondTile.addBlock( lowBlock );
		assertEquals( 2 * lowBlock.getMoveCost(),
				mode.addBlockCosts( firstTile, secondTile, LOW, LOW, 0f ), 0.001f );
		assertEquals( 2 * lowBlock.getMoveCost(),
				mode.addBlockCosts( firstTile, secondTile, LOW, HIGH, 0f ), 0.001f );
		assertEquals( 0f, mode.addBlockCosts( firstTile, secondTile, HIGH, LOW, 0f ), 0.001f );
		
		// two blocks in a tile, one in another tile, lower ones ignored with greater move height
		TestBlock highBlock = new TestBlock( HIGH, 2.5f );
		firstTile.addBlock( highBlock );
		assertEquals( 2 * lowBlock.getMoveCost() + highBlock.getMoveCost(),
				mode.addBlockCosts( firstTile, secondTile, LOW, LOW, 0f ), 0.001f );
		assertEquals( 2 * lowBlock.getMoveCost() + highBlock.getMoveCost(),
				mode.addBlockCosts( firstTile, secondTile, LOW, HIGH, 0f ), 0.001f );
		assertEquals( highBlock.getMoveCost(),
				mode.addBlockCosts( firstTile, secondTile, HIGH, LOW, 0f ), 0.001f );
		assertEquals( highBlock.getMoveCost(),
				mode.addBlockCosts( firstTile, secondTile, HIGH, HIGH, 0f ), 0.001f );
		
		// variable height block is ignored in one or both tiles depending on move height
		secondTile.addBlock( variableBlock );
		assertEquals( highBlock.getMoveCost() + variableBlock.getMoveCost(),
				mode.addBlockCosts( firstTile, secondTile, HIGH, LOW, 0f ), 0.001f );
		assertEquals( 0f,
				mode.addBlockCosts( firstTile, secondTile, VERY_HIGH, LOW, 0f ), 0.001f );
		
		// block that blocks a template, but only for adjacent tiles and if not below move height
		lowBlock.addToBlockTemplate( nextRow );
		assertFalse( mode.blocksAllowTemplate( new Tile[] { firstTile, nextRow }, DEEP ));
		assertEquals( 1, lowBlock.getAllowTemplateTested() );
		assertTrue( mode.blocksAllowTemplate(
				new Tile[] { grid.getTileAtRC( 0, 2 ), nextRow }, DEEP ));
		assertEquals( 1, lowBlock.getAllowTemplateTested() );
		assertTrue( mode.blocksAllowTemplate( new Tile[] { firstTile, nextRow }, HIGH ));
		assertEquals( 1, lowBlock.getAllowTemplateTested() );
		
		TestMovementModifier doNothing = new TestMovementModifier();
		TestMovementModifier ignoreLow = new TestMovementModifier( lowBlock, 2f );
		TestMovementModifier ignoreVariable = new TestMovementModifier( variableBlock, 4f );
		
		// modifier gets all blocks and does nothing, so all blocks affect move cost
		mode.addMovementModifier( doNothing );
		assertEquals(
				2 * lowBlock.getMoveCost() + highBlock.getMoveCost() + variableBlock.getMoveCost(),
				mode.addBlockCosts( firstTile, secondTile, LOW, HIGH, 0f ), 0.001f );
		assertArrayEquals( new Block[] { lowBlock, highBlock, lowBlock, variableBlock },
				doNothing.getBlocksForCost() );
		
		// test a modifier that ignores a specific block
		mode.addMovementModifier( ignoreLow );
		assertTrue( mode.isIgnored( lowBlock, firstTile, secondTile, LOW ));
		assertTrue( ignoreLow.getIgnoreBlockCheck() );
		assertFalse( mode.isIgnored( variableBlock, firstTile, secondTile, LOW ));
		
		// the modifier ignores the block in all tiles, and alters move cost
		assertEquals(
				highBlock.getMoveCost() + variableBlock.getMoveCost() + ignoreLow.getBlockCost(),
				mode.addBlockCosts( firstTile, secondTile, LOW, LOW, 0f ), 0.001f );
		assertArrayEquals(
				new Block[] { highBlock, variableBlock }, ignoreLow.getBlocksForCost() );
		assertArrayEquals( doNothing.getBlocksForCost(), ignoreLow.getBlocksForCost() );
		
		// modifier affects cost even if all blocks are ignored
		assertEquals( ignoreLow.getBlockCost(),
				mode.addBlockCosts( firstTile, secondTile, VERY_HIGH, LOW, 0f ), 0.001f );
		assertArrayEquals( new Block[0], ignoreLow.getBlocksForCost() );
		
		ignoreLow.resetIgnoreBlockCheck();
		mode.addMovementModifier( ignoreVariable );
		
		// first modifier ignores block, second is checked only when first doesn't ignore
		assertTrue( mode.isIgnored( lowBlock, firstTile, secondTile, LOW ));
		assertTrue( ignoreLow.getIgnoreBlockCheck() );
		assertFalse( ignoreVariable.getIgnoreBlockCheck() );
		assertTrue( mode.isIgnored( variableBlock, firstTile, secondTile, LOW ));
		assertTrue( ignoreVariable.getIgnoreBlockCheck() );
		
		// both modifiers affect cost and each causes a block to be ignored
		assertEquals(
				highBlock.getMoveCost() + ignoreLow.getBlockCost() + ignoreVariable.getBlockCost(),
				mode.addBlockCosts( firstTile, secondTile, LOW, LOW, 0f ), 0.001f );
		assertArrayEquals( new Block[] { highBlock }, ignoreLow.getBlocksForCost() );
		assertArrayEquals( ignoreLow.getBlocksForCost(), ignoreVariable.getBlocksForCost() );
		
		// modifiers affect move cost even if all blocks are ignored
		assertEquals( ignoreLow.getBlockCost() + ignoreVariable.getBlockCost(),
				mode.addBlockCosts( firstTile, secondTile, VERY_HIGH, VERY_HIGH, 0f ), 0.001f );
		assertArrayEquals( new Block[0], ignoreVariable.getBlocksForCost() );
		
		// first block is ignored because of height, but second blocks the template
		highBlock.addToBlockTemplate( nextRow );
		assertFalse( mode.blocksAllowTemplate( new Tile[] { firstTile, nextRow }, HIGH ));
		
		Tile blockTile = grid.getTileAtRC( 1, 1 );
		TestBlock templateBlock = new TestBlock( BLOCKING, 0f );
		templateBlock.addToBlockTemplate( grid.getTileAtRC( 2, 2 ));
		blockTile.addBlock( templateBlock );
		
		// test blocking a template with a larger template, blocking once is enough
		assertFalse( mode.blocksAllowTemplate( new Tile[] {
			firstTile, secondTile, grid.getTileAtRC( 0, 2 ), nextRow, blockTile,
			grid.getTileAtRC( 1, 2 ), grid.getTileAtRC( 2, 0 ), grid.getTileAtRC( 2, 1 ),
			grid.getTileAtRC( 2, 2 )}, VERY_HIGH ));
		assertEquals( 1, templateBlock.getAllowTemplateTested() );
		
		ignoreLow.resetIgnoreBlockCheck();
		ignoreLow.resetBlocksForCost();
		
		// remove modifier and test it's no longer in effect
		mode.removeMovementModifier( ignoreLow );
		assertFalse( mode.isIgnored( lowBlock, firstTile, secondTile, LOW ));
		assertTrue( mode.isIgnored( variableBlock, firstTile, secondTile, LOW ));
		assertFalse( ignoreLow.getIgnoreBlockCheck() );
		
		// corresponding block is no longer ignored, and the removed modifier isn't checked
		assertEquals( 2 * lowBlock.getMoveCost() + highBlock.getMoveCost() +
				ignoreVariable.getBlockCost(),
				mode.addBlockCosts( firstTile, secondTile, LOW, LOW, 0f ), 0.001f );
		assertArrayEquals( new Block[] { lowBlock, highBlock, lowBlock },
				ignoreVariable.getBlocksForCost() );
		assertNull( ignoreLow.getBlocksForCost() );
		
		ignoreVariable.resetIgnoreBlockCheck();
		ignoreVariable.resetBlocksForCost();

		// test again with both modifiers removed
		mode.removeMovementModifier( ignoreVariable );
		assertFalse( mode.isIgnored( variableBlock, firstTile, secondTile, LOW ));
		assertFalse( ignoreVariable.getIgnoreBlockCheck() );
		
		assertEquals(
				2 * lowBlock.getMoveCost() + highBlock.getMoveCost() + variableBlock.getMoveCost(),
				mode.addBlockCosts( firstTile, secondTile, LOW, LOW, 0f ), 0.001f );
		assertNull( ignoreVariable.getBlocksForCost() );
	}
	
	private void testCanOccupy( TileGrid <?> grid ) {
		MovementTemplate template = new HorizontalTwoTileTemplate();
		MobileObject mob = new TestMobileObject( FLAT, template );
		MovementMode mode = new TestMovementMode( mob, 100 );

		Terrain terrain = new Terrain( 0f );
		Tile[][] tiles = grid.getTiles();
		for (Tile[] row : tiles)
			for (Tile t : row)
				t.setTerrain( terrain );

		// can't occupy with template's secondary tile outside the grid
		assertFalse( mode.canOccupy( grid.getTileAtRC( 0, 0 ), EAST ));
		
		Obstacle obstacle = new TestObstacle( FLAT, new SingleTileTemplate() );
		obstacle.setPosition( EAST, grid.getTileAtRC( 1, 1 ));
		obstacle.setOccupyHeight( FLAT );
		
		// can't occupy tile with an occupying obstacle
		assertFalse( mode.canOccupy( grid.getTileAtRC( 1, 1 ), WEST, FLAT ));
		
		mob.setPosition( EAST, grid.getTileAtRC( 2, 2 ));
		mob.setOccupyHeight( FLAT );
		
		// mobile object doesn't prevent itself from occupying
		assertTrue( mode.canOccupy( grid.getTileAtRC( 2, 2 ), WEST, FLAT ));
		
		// can't occupy with an occupying obstacle on template's secondary tile
		Obstacle blockingObstacle = new TestObstacle( BLOCKING, new SingleTileTemplate() );
		blockingObstacle.setPosition( EAST, grid.getTileAtRC( 4, 4 ));
		blockingObstacle.setOccupyHeight( FLAT );
		assertFalse( mode.canOccupy( grid.getTileAtRC( 4, 3 ), WEST ));
		
		// an obstacle that doesn't occupy but prevents occupying because of impassable move cost
		Obstacle nonOccupyingObstacle = new TestObstacle( BLOCKING, mode.getImpassableMoveCost(),
				false, new SingleTileTemplate() );
		nonOccupyingObstacle.setPosition( EAST, grid.getTileAtRC( 4, 2 ));
		nonOccupyingObstacle.setOccupyHeight( FLAT );
		assertFalse( mode.canOccupy( grid.getTileAtRC( 4, 1 ), WEST ));
		
		// modifier ignores obstacle, negating the impassable move cost
		TestMovementModifier obstacleMod = new TestMovementModifier( nonOccupyingObstacle, 0f );
		mode.addMovementModifier( obstacleMod );
		assertTrue( mode.canOccupy( grid.getTileAtRC( 4, 1 ), WEST ));
		mode.removeMovementModifier( obstacleMod );
		
		// movement modifier is checked if it allows occupation
		TestMovementModifier occupationMod = new TestMovementModifier( true );
		mode.addMovementModifier( occupationMod );
		assertTrue( mode.canOccupy( grid.getTileAtRC( 4, 1 ), WEST ));
		assertTrue( occupationMod.getAllowsOccupationCheck() );
		
		// two modifiers that allow occupying, but one is enough and the other isn't checked
		occupationMod.resetAllowsOccupationCheck();
		TestMovementModifier occupationMod2 = new TestMovementModifier( true );
		mode.addMovementModifier( occupationMod2 );
		assertTrue( mode.canOccupy( grid.getTileAtRC( 4, 1 ), WEST ));
		assertTrue( occupationMod.getAllowsOccupationCheck() );
		assertFalse( occupationMod2.getAllowsOccupationCheck() );
		
		// result is saved, and modifiers aren't checked again
		occupationMod.resetAllowsOccupationCheck();
		assertTrue( mode.canOccupy( grid.getTileAtRC( 4, 1 ), WEST ));
		assertFalse( occupationMod.getAllowsOccupationCheck() );
		assertFalse( occupationMod2.getAllowsOccupationCheck() );
		
		// obstacle is ignored, and no need to check if modifiers allow occupation
		mode.addMovementModifier( obstacleMod );
		assertTrue( mode.canOccupy( grid.getTileAtRC( 4, 1 ), WEST ));
		assertFalse( occupationMod.getAllowsOccupationCheck() );
		assertFalse( occupationMod2.getAllowsOccupationCheck() );
		assertFalse( obstacleMod.getAllowsOccupationCheck() );
		
		// block doesn't prevent occupying when it's below occupy height
		mode.clearOccupyData();
		TestBlock lowerBlock = new TestBlock( SHALLOW, 0f );
		lowerBlock.addToBlockTemplate( grid.getTileAtRC( 4, 2 ));
		grid.getTileAtRC( 4, 2 ).addBlock( lowerBlock );
		assertTrue( mode.canOccupy( grid.getTileAtRC( 4, 1 ), WEST ));
		
		// a block that doesn't prevent occupying
		TestBlock notBlocking = new TestBlock( FLAT, 0f );
		grid.getTileAtRC( 4, 2 ).addBlock( notBlocking );
		mode.clearOccupyData();
		assertTrue( mode.canOccupy( grid.getTileAtRC( 4, 1 ), WEST ));
		
		// a block that prevents occupying
		TestBlock blocking = new TestBlock( FLAT, 0f );
		blocking.addToBlockTemplate( grid.getTileAtRC( 4, 1 ));
		grid.getTileAtRC( 4, 2 ).addBlock( blocking );
		mode.clearOccupyData();
		assertFalse( mode.canOccupy( grid.getTileAtRC( 4, 1 ), WEST ));
	}
	
	private void testOccupyHeightSaved( TileGrid <?> grid ) {
		MobileObject mob = new TestMobileObject( FLAT, new HorizontalTwoTileTemplate() );
		MovementMode mode = new TestMovementMode( mob, 100 );
		
		Terrain terrain = new Terrain( 0f );
		for (Tile[] row : grid.getTiles())
			for (Tile t : row)
				t.setTerrain( terrain );
		
		// no data set originally, so can't occupy and no heights
		Tile occupyTile = grid.getTileAtRC( 3, 3 );
		assertFalse( mode.canBeOccupied( occupyTile, EAST ));
		assertFalse( mode.canBeOccupied( occupyTile, WEST ));
		assertNull( mode.getOccupyHeight( occupyTile, EAST ));
		assertNull( mode.getOccupyHeight( occupyTile, WEST ));
		
		// test saving occupy height, saved for either east or west with asymmetric template
		Height occupyHeight = mode.calculateOccupyHeight( occupyTile, EAST );
		mode.setOccupyHeightForPosition( occupyTile, EAST, occupyHeight );
		assertEquals( occupyHeight, mode.getOccupyHeight( occupyTile, EAST ));
		assertTrue( mode.canBeOccupied( occupyTile, EAST ));
		assertNull( mode.getOccupyHeight( occupyTile, WEST ));
		assertFalse( mode.canBeOccupied( occupyTile, WEST ));
		
		// no occupy height for vertical directions with horizontally asymmetric template
		assertNull( mode.getOccupyHeight( occupyTile, NORTH ));
		assertNull( mode.getOccupyHeight( occupyTile, SOUTH ));
		
		// save the same height for the other horizontal direction
		mode.setOccupyHeightForPosition( occupyTile, WEST, occupyHeight );
		assertEquals( occupyHeight, mode.getOccupyHeight( occupyTile, EAST ));
		assertTrue( mode.canBeOccupied( occupyTile, EAST ));
		assertEquals( occupyHeight, mode.getOccupyHeight( occupyTile, WEST ));
		assertTrue( mode.canBeOccupied( occupyTile, WEST ));

		// no occupy height for vertical directions with horizontally asymmetric template
		assertNull( mode.getOccupyHeight( occupyTile, NORTH ));
		assertNull( mode.getOccupyHeight( occupyTile, SOUTH ));
		
		// blocking is not a legal occupy height, again other horizontal direction set separately
		mode.setOccupyHeightForPosition( occupyTile, EAST, BLOCKING );
		assertNull( mode.getOccupyHeight( occupyTile, EAST ));
		assertFalse( mode.canBeOccupied( occupyTile, EAST ));
		assertEquals( occupyHeight, mode.getOccupyHeight( occupyTile, WEST ));
		mode.setOccupyHeightForPosition( occupyTile, WEST, null );
		assertNull( mode.getOccupyHeight( occupyTile, WEST ));
		assertFalse( mode.canBeOccupied( occupyTile, WEST ));
		
		// test method which calculates and sets occupy height
		occupyTile = grid.getTileAtRC( 3, 1 );
		occupyHeight = mode.calculateOccupyHeight( occupyTile, EAST );
		assertEquals( occupyHeight, mode.calculateOrGetOccupyHeight( occupyTile, EAST ));
		
		mode.setOccupyHeight( occupyTile, EAST, occupyHeight );
		assertEquals( occupyHeight, mode.getOccupyHeight( occupyTile, EAST ));
		assertNull( mode.getOccupyHeight( occupyTile, WEST ));
		
		occupyHeight = mode.calculateOccupyHeight( occupyTile, WEST );
		assertEquals( occupyHeight, mode.calculateOrGetOccupyHeight( occupyTile, WEST ));
		mode.setOccupyHeight( occupyTile, WEST, occupyHeight );
		assertEquals( occupyHeight, mode.getOccupyHeight( occupyTile, WEST ));

		// can't occupy when template is partially outside the grid
		Tile cornerTile = grid.getTileAtRC( 0, 0 );
		mode.setOccupyHeight( cornerTile, EAST,
				mode.calculateOrGetOccupyHeight( cornerTile, EAST ));
		assertFalse( mode.canBeOccupied( cornerTile, EAST ));
		assertNull( mode.getOccupyHeight( cornerTile, EAST ));
		
		// adding a modifier clears occupy data, since it's now obsolete
		TestMovementModifier occupyMod = new TestMovementModifier( LOW );
		mode.addMovementModifier( occupyMod );
		
		assertFalse( mode.canBeOccupied( occupyTile, EAST ));
		assertFalse( mode.canBeOccupied( occupyTile, WEST ));
		assertNull( mode.getOccupyHeight( occupyTile, EAST ));
		assertNull( mode.getOccupyHeight( occupyTile, WEST ));
		
		// modifier sets occupy height to low, there's no need to check if it allows occupying
		assertEquals( LOW, mode.calculateOrGetOccupyHeight( occupyTile, EAST ));
		mode.setOccupyHeight( occupyTile, EAST, LOW );
		assertFalse( occupyMod.getAllowsOccupationCheck() );
		assertEquals( LOW, mode.getOccupyHeight( occupyTile, EAST ));
		
		assertEquals( LOW, mode.calculateOrGetOccupyHeight( occupyTile, WEST ));
		mode.setOccupyHeight( occupyTile, WEST, LOW );
		assertFalse( occupyMod.getAllowsOccupationCheck() );
		assertEquals( LOW, mode.getOccupyHeight( occupyTile, WEST ));
		
		TestObstacle blockingObstacle = new TestObstacle(
				BLOCKING, mode.getImpassableMoveCost(), false, new SingleTileTemplate());
		blockingObstacle.setPosition( EAST, occupyTile );
		blockingObstacle.setOccupyHeight( FLAT );
		
		mode.clearOccupyData();
		
		// obstacle prevents occupying
		mode.setOccupyHeight( occupyTile, EAST,
				mode.calculateOrGetOccupyHeight( occupyTile, EAST ));
		mode.setOccupyHeight( occupyTile, WEST,
				mode.calculateOrGetOccupyHeight( occupyTile, WEST ));
		assertFalse( mode.canBeOccupied( occupyTile, EAST ));
		assertFalse( mode.canBeOccupied( occupyTile, WEST ));
		
		occupyMod.setAllowsOccupation( true );
		mode.clearOccupyData();
		
		// modifier allows occupying despite the obstacle
		mode.setOccupyHeight( occupyTile, EAST,
				mode.calculateOrGetOccupyHeight( occupyTile, EAST ));
		mode.setOccupyHeight( occupyTile, WEST,
				mode.calculateOrGetOccupyHeight( occupyTile, WEST ));
		assertTrue( mode.canBeOccupied( occupyTile, EAST ));
		assertTrue( mode.canBeOccupied( occupyTile, WEST ));
		assertTrue( occupyMod.getAllowsOccupationCheck() );
		
		// results are saved and modifier isn't checked again
		occupyMod.resetAllowsOccupationCheck();
		mode.setOccupyHeight( occupyTile, EAST,
				mode.calculateOrGetOccupyHeight( occupyTile, EAST ));
		mode.setOccupyHeight( occupyTile, WEST,
				mode.calculateOrGetOccupyHeight( occupyTile, WEST ));
		assertFalse( occupyMod.getAllowsOccupationCheck() );
		
		// some basic tests with a symmetric template
		mob = new TestMobileObject( FLAT, new SingleTileTemplate() );
		mode = new TestMovementMode( mob, 100 );
		
		occupyTile = grid.getTileAtRC( 2, 3 );
		occupyHeight = mode.calculateOccupyHeight( occupyTile, EAST );
		mode.setOccupyHeight( occupyTile, EAST, occupyHeight );
		
		assertEquals( occupyHeight, mode.getOccupyHeight( occupyTile, EAST ));
		assertEquals( occupyHeight, mode.getOccupyHeight( occupyTile, WEST ));
		assertTrue( mode.canBeOccupied( occupyTile, EAST ));
		assertTrue( mode.canBeOccupied( occupyTile, WEST ));
		
		// test with an obstacle blocking the occupy tile
		mode.clearOccupyData();
		TestObstacle obstacle = new TestObstacle( LOW, 0f, true, new SingleTileTemplate() );
		obstacle.setPosition( NORTH, occupyTile );
		
		TestMovementModifier mod = new TestMovementModifier();
		mode.addMovementModifier( mod );

		mode.setOccupyHeight( occupyTile, EAST,			// marks tile unoccupiable
				mode.calculateOrGetOccupyHeight( occupyTile, EAST ));
		mod.resetHeightCheckCount();
		mode.setOccupyHeight( occupyTile, EAST,			// already marked, so no checks
				mode.calculateOrGetOccupyHeight( occupyTile, EAST ));
		assertEquals( 0, mod.getHeightCheckCount() );	// wasn't checked
	}
	
	private void testEventBuffer( TileGrid <?> grid ) {
		MobileObject mob = new TestMobileObject( FLAT, new SingleTileTemplate() );
		final MovementMode mode = new TestMovementMode( mob, 100 );
		
		// initially buffer is empty and not executing, trying to remove has no effect
		assertFalse( mode.isEventBufferExecuting() );
		assertArrayEquals( new MovementEvent[0], mode.getBufferedEvents() );
		assertNull( mode.removeEventFromBuffer( null, null ));
		
		TestMovementEvent event = new TestMovementEvent( 1f );
		Tile tile = grid.getTileAtRC( 0, 0 );
		
		// nulls are ignored (either tile, event or both)
		mode.addEventToBuffer( null, null );
		assertArrayEquals( new MovementEvent[0], mode.getBufferedEvents() );
		
		mode.addEventToBuffer( null, tile );
		assertArrayEquals( new MovementEvent[0], mode.getBufferedEvents() );
		
		mode.addEventToBuffer( event, null );
		assertArrayEquals( new MovementEvent[0], mode.getBufferedEvents() );
		
		// test adding event to buffer, and checking if it's buffered
		mode.addEventToBuffer( event, tile );
		assertArrayEquals( new MovementEvent[] { event }, mode.getBufferedEvents() );
		assertTrue( mode.isEventBuffered( event, null ));
		assertTrue( mode.isEventBuffered( event, tile ));
		
		Tile tile2 = grid.getTileAtRC( 0, 1 );
		mode.addEventToBuffer( event, tile2 );
		
		// same event is buffered twice for different tiles
		assertTrue( mode.isEventBuffered( event, tile2 ));
		assertArrayEquals( new MovementEvent[] { event, event }, mode.getBufferedEvents() );
		assertTrue( mode.isEventBuffered( event, tile2 ));
		
		// same event is buffered twice for the same tile
		mode.addEventToBuffer( event, tile );
		assertArrayEquals( new MovementEvent[] { event, event, event }, mode.getBufferedEvents() );
		
		// test removing event (null event, null tile, not added, successful removal)
		TestMovementEvent event2 = new TestMovementEvent( 1.5f );
		assertNull( mode.removeEventFromBuffer( null, null ));
		assertNull( mode.removeEventFromBuffer( event2, null ));
		assertNull( mode.removeEventFromBuffer( event2, tile ));
		assertSame( event, mode.removeEventFromBuffer( event, null ));
		
		// the removed event was buffered twice for the tile, so it's still buffered once
		assertTrue( mode.isEventBuffered( event, tile ));
		assertArrayEquals( new MovementEvent[] { event, event }, mode.getBufferedEvents() );
		
		// remove the second instance of the event, and it's gone
		assertSame( event, mode.removeEventFromBuffer( event, tile ));
		assertArrayEquals( new MovementEvent[] { event }, mode.getBufferedEvents() );
		assertFalse( mode.isEventBuffered( event, tile ));
		assertTrue( mode.isEventBuffered( event, null ));
		
		// remove the same event from the other tile
		assertSame( event, mode.removeEventFromBuffer( event, tile2 ));
		assertFalse( mode.isEventBuffered( event, null ));
		assertArrayEquals( new MovementEvent[0], mode.getBufferedEvents() );
		
		// test nulls again after the event buffered is emptied
		assertFalse( mode.isEventBuffered( null, null ));
		assertFalse( mode.isEventBuffered( event2, null ));
		assertNull( mode.removeEventFromBuffer( event, null ));
		
		mode.addEventToBuffer( event, tile );
		mode.addEventToBuffer( event2, tile2 );
		
		// execute event buffer in other thread so that event blocks until notified
		event.setWaitMode( true );
		Thread thread = new Thread() {

			@Override
			public void run() {
				mode.executeEventBuffer( FLAT );
			}
		};

		thread.start();

		synchronized (event) {
			try {
				event.wait();
			} catch (InterruptedException ix) {
				ix.printStackTrace();
			}
		}
		
		// event buffer is now executing, and buffered events can't be accessed or added
		assertTrue( mode.isEventBufferExecuting() );
		assertNull( mode.getBufferedEvents() );
		
		TestMovementEvent event3 = new TestMovementEvent( 2f );
		mode.addEventToBuffer( event3, tile );
		
		// previous events are still buffered, though
		assertTrue( mode.isEventBuffered( event, tile ));
		assertTrue( mode.isEventBuffered( event2, tile2 ));
		assertFalse( mode.isEventBuffered( event3, null ));
		
		// events can't be removed during execution, even though they are buffered
		assertNull( mode.removeEventFromBuffer( event, null ));
		assertTrue( mode.isEventBuffered( event, tile ));
		
		// wake up the waiting event and kill the execution thread
		event.setWaitMode( false );
		synchronized (event) {
			event.notifyAll();
		}
		
		try {
			thread.join();
		} catch (InterruptedException ix) {
			ix.printStackTrace();
		}
		
		// check that events were executed and they received correct data
		assertSame( mob, event.getEnteringObject() );
		assertSame( tile, event.getEnteredTile() );
		assertEquals( FLAT, event.getEnteringHeight() );
		
		assertSame( mob, event2.getEnteringObject() );
		assertSame( tile2, event2.getEnteredTile() );
		assertEquals( FLAT, event2.getEnteringHeight() );
		
		// this event wasn't executed, because the attempt to add it was during execution
		assertNull( event3.getEnteringObject() );
		assertNull( event3.getEnteredTile() );
		assertNull( event3.getEnteringHeight() );

		// event buffer was cleared after executing
		assertArrayEquals( new MovementEvent[0], mode.getBufferedEvents() );
		assertFalse( mode.isEventBuffered( event, null ));
		assertFalse( mode.isEventBuffered( event2, null ));
		assertFalse( mode.isEventBuffered( event3, null ));
		
		event.resetEnteringData();
		event2.resetEnteringData();
		
		// test clearing the event buffer
		mode.addEventToBuffer( event, tile );
		mode.addEventToBuffer( event2, tile2 );
		assertArrayEquals( new MovementEvent[] { event, event2 }, mode.getBufferedEvents() );
		
		mode.clearEventBuffer();
		assertArrayEquals( new MovementEvent[0], mode.getBufferedEvents() );

		mode.addEventToBuffer( event, tile );
		mode.addEventToBuffer( event2, tile2 );
		assertArrayEquals( new MovementEvent[] { event, event2 }, mode.getBufferedEvents() );
		
		// execute buffer in another thread again, with a different execution method
		event.setWaitMode( true );
		final float[] risk = new float[1];
		thread = new Thread() {

			@Override
			public void run() {
				risk[0] = mode.getEventBufferRisk( FLAT );
			}
		};
		
		thread.start();

		synchronized (event) {
			try {
				event.wait();
			} catch (InterruptedException ix) {
				ix.printStackTrace();
			}
		}
		
		// test execution-time properties again, with different execution method
		assertTrue( mode.isEventBufferExecuting() );
		assertNull( mode.getBufferedEvents() );		
		
		mode.addEventToBuffer( event3, tile );
		assertTrue( mode.isEventBuffered( event, tile ));
		assertTrue( mode.isEventBuffered( event2, tile2 ));
		assertFalse( mode.isEventBuffered( event3, null ));
		
		assertNull( mode.removeEventFromBuffer( event, null ));
		assertTrue( mode.isEventBuffered( event, tile ));
		
		// cancel wait mode and kill the thread
		event.setWaitMode( false );
		synchronized (event) {
			event.notifyAll();
		}
		
		try {
			thread.join();
		} catch (InterruptedException ix) {
			ix.printStackTrace();
		}

		// check that correct events were executed and they got the right data
		assertSame( mob, event.getEnteringObject() );
		assertSame( tile, event.getEnteredTile() );
		assertEquals( FLAT, event.getEnteringHeight() );
		
		assertSame( mob, event2.getEnteringObject() );
		assertSame( tile2, event2.getEnteredTile() );
		assertEquals( FLAT, event2.getEnteringHeight() );
		
		assertNull( event3.getEnteringObject() );
		assertNull( event3.getEnteredTile() );
		assertNull( event3.getEnteringHeight() );

		// check correct risk calculation
		assertEquals( event.getRisk() + event2.getRisk(), risk[0], 0.001f );
		
		TestMovementEvent singleExec = new TestMovementEvent( 2.5f, FLAT, false, false, true );
		singleExec.setExecuteOnce( true );
		mob.addMovementMode( mode );
		
		// add single execution event to two tiles, and test events for both, no risk
		tile.addMovementEvent( singleExec );
		tile2.addMovementEvent( singleExec );
		assertEquals( 0f, mode.testEnterEvents( tile, FLAT, FLAT ), 0.001 );
		assertEquals( 0f, mode.testEnterEvents( tile, FLAT, FLAT ), 0.001 );
		
		// event is buffered only once, and risk is added only once
		assertTrue( mode.isEventBuffered( singleExec, tile ));
		assertFalse( mode.isEventBuffered( singleExec, tile2 ));
		assertEquals( singleExec.getRisk(), mode.getEventBufferRisk( FLAT ), 0.001 );
		assertFalse( mode.isEventBuffered( singleExec, null ));
	}
	
	private void testMiscellaneous( TileGrid <?> grid ) {
		MobileObject mob = new TestMobileObject( FLAT, new SingleTileTemplate() );
		MovementMode mode = new TestMovementMode( mob, 100 );
		
		// initial tiles are at first not set
		assertNull( mode.initialTiles );
		assertFalse( mode.isInitialTile( null ));
		assertFalse( mode.isInitialTile( grid.getTileAtRC( 0, 0 )));
		
		// set initial tiles and check again which tiles are initial ones
		mode.initialTiles = new Tile[] { grid.getTileAtRC( 0, 0 ), grid.getTileAtRC( 0, 1 ),
			grid.getTileAtRC( 1, 0 ), grid.getTileAtRC( 1, 1 )};
		
		assertTrue( mode.isInitialTile( grid.getTileAtRC( 0, 0 )));
		assertTrue( mode.isInitialTile( grid.getTileAtRC( 1, 1 )));
		assertFalse( mode.isInitialTile( null ));
		assertFalse( mode.isInitialTile( grid.getTileAtRC( 0, 2 )));
		
		// null tile can be set, although that isn't very useful
		mode.initialTiles = new Tile[] { null };
		assertFalse( mode.isInitialTile( grid.getTileAtRC( 0, 0 )));
		assertTrue( mode.isInitialTile( null ));
		
		// clearing the move radius also clears initial tiles
		mode.clearRadius();
		assertNull( mode.initialTiles );
		
		// test events for a tile that doesn't have any
		Tile eventTile = grid.getTileAtRC( 0, 0 );
		Tile[] eventTemplate = { eventTile };
		int[] eventIndices = { 0 };
		assertEquals( 0f, mode.testEnterEvents( eventTile, DEEP, DEEP ), 0.001f );
		assertTrue( mode.testLeaveEvents( DEEP, eventTile ));
		assertTrue( mode.executeEnterEvents( DEEP, DEEP, eventTemplate, eventIndices ));
		assertTrue( mode.executeLeaveEvents( DEEP, DEEP, eventTile ));
		
		// event is ignored when it's below movement height
		TestMovementEvent event = new TestMovementEvent( 1.5f, SHALLOW, false, false );
		assertTrue( mode.getProtectionAgainst( event, eventTile, FLAT ) > 0 );
		assertTrue( mode.getProtectionAgainst( event, eventTile, SHALLOW ) < 0 );
		
		// event in an initial tile can't interrupt movement
		mode.initialTiles = new Tile[] { eventTile };
		assertEquals( 0, mode.getProtectionAgainst( event, eventTile, SHALLOW ));
		mode.initialTiles = null;
		
		// movement modifier doesn't by default affect event protections
		TestMovementModifier mod = new TestMovementModifier();
		mode.addMovementModifier( mod );
		assertTrue( mode.getProtectionAgainst( event, eventTile, FLAT ) > 0 );
		assertTrue( mode.getProtectionAgainst( event, eventTile, SHALLOW ) < 0 );
		
		// change modifier to set event protection to zero
		mod.addEventProtection( event, 0 );
		assertTrue( mode.getProtectionAgainst( event, eventTile, FLAT ) > 0 );
		assertEquals( 0, mode.getProtectionAgainst( event, eventTile, SHALLOW ));
		
		// change modifier to set event protection to positive
		mod.addEventProtection( event, 1 );
		assertTrue( mode.getProtectionAgainst( event, eventTile, FLAT ) > 0 );
		assertTrue( mode.getProtectionAgainst( event, eventTile, SHALLOW ) > 0 );
		
		// test again with a second modifier that does nothing
		TestMovementModifier mod2 = new TestMovementModifier();
		mod2.addEventProtection( event, 0 );
		mode.addMovementModifier( mod2 );
		assertTrue( mode.getProtectionAgainst( event, eventTile, FLAT ) > 0 );
		assertTrue( mode.getProtectionAgainst( event, eventTile, SHALLOW ) > 0 );
		
		// positive protection from second modifier overrides zero from first
		mod.addEventProtection( event, 0 );
		mod2.addEventProtection( event, 1 );
		assertTrue( mode.getProtectionAgainst( event, eventTile, FLAT ) > 0 );
		assertTrue( mode.getProtectionAgainst( event, eventTile, SHALLOW ) > 0 );
		
		// remove modifiers and test that their effects are gone
		mode.removeMovementModifier( mod2 );
		assertTrue( mode.getProtectionAgainst( event, eventTile, FLAT ) > 0 );
		assertEquals( 0, mode.getProtectionAgainst( event, eventTile, SHALLOW ));
		
		mode.removeMovementModifier( mod );
		assertTrue( mode.getProtectionAgainst( event, eventTile, FLAT ) > 0 );
		assertTrue( mode.getProtectionAgainst( event, eventTile, SHALLOW ) < 0 );
		
		mode.addMovementModifier( mod );
		mod.removeEventProtection( event );
		eventTile.addMovementEvent( event );
		
		// test method that retrieves events from the tile, event gets executed with correct data
		assertEquals( event.getRisk(),
				mode.testEnterEvents( eventTile, SHALLOW, SHALLOW ), 0.001f );
		assertSame( mob, event.getEnteringObject() );
		assertSame( eventTile, event.getEnteredTile() );
		assertEquals( SHALLOW, event.getEnteringHeight() );
		
		assertEquals( event.getRisk(), mode.testEnterEvents( eventTile, SHALLOW, FLAT ), 0.001f );
		assertEquals( 0f, mode.testEnterEvents( eventTile, FLAT, SHALLOW ), 0.001f );
		assertEquals( 0f, mode.testEnterEvents( eventTile, FLAT, FLAT ), 0.001f );
		
		// modifier prevents interrupting movement, but not risk
		mod.addEventProtection( event, 0 );
		assertEquals( event.getRisk(),
				mode.testEnterEvents( eventTile, SHALLOW, SHALLOW ), 0.001f );
		assertEquals( event.getRisk(),
				mode.testEnterEvents( eventTile, SHALLOW, FLAT ), 0.001f );
		assertEquals( 0f, mode.testEnterEvents( eventTile, FLAT, SHALLOW ), 0.001f );
		assertEquals( 0f, mode.testEnterEvents( eventTile, FLAT, FLAT ), 0.001f );
		
		// modifier prevents risk as well
		mod.addEventProtection( event, 1 );
		assertEquals( 0f, mode.testEnterEvents( eventTile, SHALLOW, SHALLOW ), 0.001f );
		
		// test with an event that prevents entering and leaving
		TestMovementEvent event2 = new TestMovementEvent( 2f, SHALLOW, true, true );
		eventTile.addMovementEvent( event2 );
		assertTrue( mode.testEnterEvents( eventTile, SHALLOW, FLAT ) < 0 );
		
		// make modifier prevent event from interrupting movement, risk remains
		mod.addEventProtection( event2, 0 );
		assertEquals( event2.getRisk(), mode.testEnterEvents( eventTile, SHALLOW, FLAT ), 0.001f );
		
		// make modifier protect against the event so that risk is removed as well
		mod.addEventProtection( event2, 1 );
		assertEquals( 0f, mode.testEnterEvents( eventTile, SHALLOW, FLAT ), 0.001f );
		
		// risk from both events, while movement interruption is prevented
		mod.addEventProtection( event, 0 );
		mod.addEventProtection( event2, 0 );
		assertEquals( event.getRisk() + event2.getRisk(),
				mode.testEnterEvents( eventTile, SHALLOW, SHALLOW ), 0.001f );
		
		// without the modifier, tile can't be entered
		mod.clearEventProtections();
		assertTrue( mode.testEnterEvents( eventTile, SHALLOW, SHALLOW ) < 0 );
		
		TestMovementEvent tracker = new TestMovementEvent( 0f, SHALLOW, false, false );
		Tile[] template = { eventTile, grid.getTileAtRC( 0, 1 ), grid.getTileAtRC( 2, 1 )};
		int[] templateIndices = { 0, 1, 2 };
		template[1].addMovementEvent( tracker );
		
		// event in first tile prevents leaving, event in second tile aren't tested
		assertFalse( mode.testLeaveEvents( SHALLOW, template ));
		assertSame( mob, event.getLeavingObject() );
		assertSame( eventTile, event.getLeftTile() );
		assertEquals( SHALLOW, event.getLeavingHeight() );
		assertNull( tracker.getLeavingObject() );
		assertNull( tracker.getLeftTile() );
		assertNull( tracker.getLeavingHeight() );
		
		event2.resetLeavingData();
		
		// events are ignored when they are below move height
		assertTrue( mode.testLeaveEvents( FLAT, template ));
		assertNull( event2.getLeavingObject() );
		assertNull( event2.getLeftTile() );
		assertNull( event2.getLeavingHeight() );
		assertNull( tracker.getLeavingObject() );
		assertNull( tracker.getLeftTile() );
		assertNull( tracker.getLeavingHeight() );
		
		// modifier allows mobile object to leave, and all events in template are checked
		mod.addEventProtection( event2, 0 );
		assertTrue( mode.testLeaveEvents( SHALLOW, template ));
		assertSame( mob, tracker.getLeavingObject() );
		assertSame( template[1], tracker.getLeftTile() );
		assertEquals( SHALLOW, tracker.getLeavingHeight() );
		
		// the event that prevents leaving also works in another tile
		mod.clearEventProtections();
		eventTile.removeMovementEvent( event2 );
		template[1].addMovementEvent( event2 );
		assertFalse( mode.testLeaveEvents( SHALLOW, template ));
		
		// event is in two tiles, but it's tested for the first only, because leaving is prevented
		eventTile.addMovementEvent( event2 );
		event2.resetLeavingData();
		assertFalse( mode.testLeaveEvents( SHALLOW, template ));
		assertSame( eventTile, event2.getLeftTile() );
		template[1].removeMovementEvent( event2 );
		
		// test executing enter events instead of testing leave events
		mod.clearEventProtections();
		assertFalse( mode.executeEnterEvents( SHALLOW, SHALLOW, eventTemplate, eventIndices ));
		assertFalse( mode.executeEnterEvents( SHALLOW, FLAT, eventTemplate, eventIndices ));
		assertSame( eventTile, event.getEnteredTile() );
		assertSame( eventTile, event2.getEnteredTile() );
		
		// events are ignored when below move height
		event.resetEnteringData();
		event2.resetEnteringData();
		assertTrue( mode.executeEnterEvents( FLAT, FLAT, eventTemplate, eventIndices ));
		assertTrue( mode.executeEnterEvents( FLAT, SHALLOW, eventTemplate, eventIndices ));
		assertNull( event.getEnteredTile() );
		assertNull( event2.getEnteredTile() );
		
		// modifier prevents event from interrupting movement
		mod.addEventProtection( event2, 0 );
		event.resetEnteringData();
		event2.resetEnteringData();
		assertTrue( mode.executeEnterEvents( SHALLOW, SHALLOW, eventTemplate, eventIndices ));
		assertSame( eventTile, event.getEnteredTile() );
		assertSame( eventTile, event2.getEnteredTile() );
		
		// modifier causes event to be ignored
		mod.addEventProtection( event2, 1 );
		event.resetEnteringData();
		event2.resetEnteringData();
		assertTrue( mode.executeEnterEvents( SHALLOW, SHALLOW, eventTemplate, eventIndices ));
		assertSame( eventTile, event.getEnteredTile() );
		assertNull( event2.getEnteredTile() );
		
		mod.clearEventProtections();
		event.resetEnteringData();
		event2.resetEnteringData();
		tracker.resetEnteringData();
		
		// all events are executed, even though one of them blocks entry
		assertFalse( mode.executeEnterEvents( SHALLOW, SHALLOW, template, templateIndices ));
		assertSame( mob, event.getEnteringObject() );
		assertSame( eventTile, event.getEnteredTile() );
		assertEquals( SHALLOW, event.getEnteringHeight() );
		assertSame( mob, event2.getEnteringObject() );
		assertSame( eventTile, event2.getEnteredTile() );
		assertEquals( SHALLOW, event2.getEnteringHeight() );
		assertSame( mob, tracker.getEnteringObject() );
		assertSame( template[1], tracker.getEnteredTile() );
		assertEquals( SHALLOW, tracker.getEnteringHeight() );
		
		event.resetEnteringData();
		event2.resetEnteringData();
		tracker.resetEnteringData();
		
		// events are ignored below move height
		assertTrue( mode.executeEnterEvents( FLAT, FLAT, template, templateIndices ));
		assertNull( event.getEnteringObject() );
		assertNull( event.getEnteredTile() );
		assertNull( event.getEnteringHeight() );
		assertNull( event2.getEnteringObject() );
		assertNull( event2.getEnteredTile() );
		assertNull( event2.getEnteringHeight() );
		assertNull( tracker.getEnteringObject() );
		assertNull( tracker.getEnteredTile() );
		assertNull( tracker.getEnteringHeight() );
		
		mod.addEventProtection( tracker, 1 );
		event.resetEnteringData();
		event2.resetEnteringData();
		tracker.resetEnteringData();
		
		// modifier causes an event to be ignored
		assertFalse( mode.executeEnterEvents( SHALLOW, SHALLOW, template, templateIndices ));
		assertSame( mob, event.getEnteringObject() );
		assertSame( eventTile, event.getEnteredTile() );
		assertEquals( SHALLOW, event.getEnteringHeight() );
		assertSame( mob, event2.getEnteringObject() );
		assertSame( eventTile, event2.getEnteredTile() );
		assertEquals( SHALLOW, event2.getEnteringHeight() );
		assertNull( tracker.getEnteringObject() );
		assertNull( tracker.getEnteredTile() );
		assertNull( tracker.getEnteringHeight() );
		
		// test executing leave events
		mod.clearEventProtections();
		assertFalse( mode.executeLeaveEvents( SHALLOW, SHALLOW, eventTile ));
		assertFalse( mode.executeLeaveEvents( SHALLOW, FLAT, eventTile ));
		assertSame( eventTile, event.getLeftTile() );
		assertSame( eventTile, event2.getLeftTile() );
		
		// events below move height are ignored
		event.resetLeavingData();
		event2.resetLeavingData();
		assertTrue( mode.executeLeaveEvents( FLAT, FLAT, eventTile ));
		assertTrue( mode.executeLeaveEvents( FLAT, SHALLOW, eventTile ));
		assertNull( event.getLeftTile() );
		assertNull( event2.getLeftTile() );
		
		// modifier prevents event from interrupting movement, but it's still executed
		mod.addEventProtection( event2, 0 );
		event.resetLeavingData();
		event2.resetLeavingData();
		assertTrue( mode.executeLeaveEvents( SHALLOW, SHALLOW, eventTile ));
		assertSame( eventTile, event.getLeftTile() );
		assertSame( eventTile, event2.getLeftTile() );
		
		// modifier causes event to be ignored completely
		mod.addEventProtection( event2, 1 );
		event.resetLeavingData();
		event2.resetLeavingData();
		assertTrue( mode.executeLeaveEvents( SHALLOW, SHALLOW, eventTile ));
		assertSame( eventTile, event.getLeftTile() );
		assertNull( event2.getLeftTile() );
		
		mod.clearEventProtections();
		event.resetLeavingData();
		event2.resetLeavingData();
		tracker.resetLeavingData();
		
		// all events are executed, even though one of them prevents leaving
		assertFalse( mode.executeLeaveEvents( SHALLOW, SHALLOW, template ));
		assertSame( mob, event.getLeavingObject() );
		assertSame( eventTile, event.getLeftTile() );
		assertEquals( SHALLOW, event.getLeavingHeight() );
		assertSame( mob, event2.getLeavingObject() );
		assertSame( eventTile, event2.getLeftTile() );
		assertEquals( SHALLOW, event2.getLeavingHeight() );
		assertSame( mob, tracker.getLeavingObject() );
		assertSame( template[1], tracker.getLeftTile() );
		assertEquals( SHALLOW, tracker.getLeavingHeight() );
		
		event.resetLeavingData();
		event2.resetLeavingData();
		tracker.resetLeavingData();
		
		// events are ignored below move height
		assertTrue( mode.executeLeaveEvents( FLAT, FLAT, template ));
		assertNull( event.getLeavingObject() );
		assertNull( event.getLeftTile() );
		assertNull( event.getLeavingHeight() );
		assertNull( event2.getLeavingObject() );
		assertNull( event2.getLeftTile() );
		assertNull( event2.getLeavingHeight() );
		assertNull( tracker.getLeavingObject() );
		assertNull( tracker.getLeftTile() );
		assertNull( tracker.getLeavingHeight() );
		
		mod.addEventProtection( tracker, 1 );
		event.resetLeavingData();
		event2.resetLeavingData();
		tracker.resetLeavingData();
		
		// modifier causes an event to be ignored
		assertFalse( mode.executeLeaveEvents( SHALLOW, SHALLOW, template ));
		assertSame( mob, event.getLeavingObject() );
		assertSame( eventTile, event.getLeftTile() );
		assertEquals( SHALLOW, event.getLeavingHeight() );
		assertSame( mob, event2.getLeavingObject() );
		assertSame( eventTile, event2.getLeftTile() );
		assertEquals( SHALLOW, event2.getLeavingHeight() );
		assertNull( tracker.getLeavingObject() );
		assertNull( tracker.getLeftTile() );
		assertNull( tracker.getLeavingHeight() );
	}
}
