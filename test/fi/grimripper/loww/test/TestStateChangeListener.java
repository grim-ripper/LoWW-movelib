package fi.grimripper.loww.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import fi.grimripper.loww.StateChangeListener;
import fi.grimripper.loww.movement.MovementMode;
import fi.grimripper.loww.movement.MovementModifier;

public class TestStateChangeListener implements StateChangeListener <MovementMode> {

	private MovementMode mode = null;
	private int movement = -1;
	
	private MovementModifier hasModifier = null;
	private MovementModifier doesntHaveModifier = null;
	
	private boolean expectedDisableState = false;
	private boolean stateChanged = false;
	
	@Override
	public void stateChanged( MovementMode changed ) {
		assertSame( mode, changed );
		
		if (movement >= 0)
			assertEquals( movement, changed.getMovement() );
		
		if (hasModifier != null)
			assertTrue( changed.hasMovementModifier( hasModifier ));
		
		if (doesntHaveModifier != null)
			assertFalse( changed.hasMovementModifier( doesntHaveModifier ));
	
		assertEquals( expectedDisableState, changed.isDisabled() );
		stateChanged = true;
	}
	
	public void setMovementMode( MovementMode mode ) {
		this.mode = mode;
	}
	
	public void setMovementValue( int movement ) {
		this.movement = movement;
	}
	
	public void setExpectedDisableState( boolean expectedDisableState ) {
		this.expectedDisableState = expectedDisableState;
	}

	public void setHasMovementModifier( MovementModifier modifier ) {
		hasModifier = modifier;
	}
	
	public void setDoesntHaveMovementModifier( MovementModifier modifier ) {
		doesntHaveModifier = modifier;
	}
	
	public boolean stateChanged() {
		return stateChanged;
	}
	
	public void clearStateChanged() {
		stateChanged = false;
	}
}
