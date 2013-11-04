package fi.grimripper.loww.test;

import fi.grimripper.loww.Direction;
import fi.grimripper.loww.movement.MobileObject;
import fi.grimripper.loww.movement.MovementMode;
import fi.grimripper.loww.tiles.Tile;

public class TestMovementMode extends MovementMode {

	public TestMovementMode( MobileObject host, int movement ) {
		super( host, movement );
	}

	@Override
	public void movementRadius( int totalMove ) {
		
	}

	@Override
	public Tile[] getMovementPath( Tile pathTo, Direction facing ) {
		return null;
	}

	@Override
	public Tile[] executeMovementPath( Tile destination, Direction facing ) {
		return null;
	}		
}