package fi.grimripper.loww.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import fi.grimripper.loww.movement.MobileObject;
import fi.grimripper.loww.movement.MoveModeListener;
import fi.grimripper.loww.movement.MovementMode;

public class TestMoveModeListener implements MoveModeListener {

	private MobileObject mOb = null;
	private MovementMode mode = null;
	private boolean canAdd = true;
	private int canAddCheck = 0;
	
	private MovementMode addedMode = null;
	private MovementMode removedMode = null;
	
	private MovementMode deactivatedMode = null;
	private MovementMode activatedMode = null;
	
	private int disableOnAdd = 0;
	
	@Override
	public boolean canAddMovementMode( MobileObject mobile, MovementMode mode ) {
		assertSame( mOb, mobile );
		assertSame( this.mode, mode );
		canAddCheck++;
		return canAdd;
	}

	@Override
	public void movementModeAdded( MobileObject mobile, MovementMode mode ) {
		assertSame( mOb, mobile );
		assertNotNull( mode );
		addedMode = mode;
		
		if (disableOnAdd > 0)
			mobile.setMovementModeDisabled( mode, true );
		
		else if (disableOnAdd < 0) try {
			Method disable = MovementMode.class.getDeclaredMethod( "setDisabled", boolean.class );
			disable.setAccessible( true );
			disable.invoke( mode, true );
			
		} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException x) {
			x.printStackTrace();
		}
	}

	@Override
	public void movementModeRemoved( MobileObject mobile, MovementMode mode ) {
		assertSame( mOb, mobile );
		assertNotNull( mode );
		removedMode = mode;
	}

	@Override
	public void activeModeChanged( MobileObject mobile, MovementMode deactivated,
			MovementMode activated ) {
		assertSame( mOb, mobile );
		deactivatedMode = deactivated;
		activatedMode = activated;
	}
	
	public void setMobileObject( MobileObject mOb ) {
		this.mOb = mOb;
	}
	
	public void setMovementMode( MovementMode mode ) {
		this.mode = mode;
	}
	
	public MovementMode getAddedMovementMode() {
		return addedMode;
	}

	public MovementMode getActivatedMovementMode() {
		return activatedMode;
	}
	
	public MovementMode getDeactivatedMovementMode() {
		return deactivatedMode;
	}
	
	public MovementMode getRemovedMovementMode() {
		return removedMode;
	}
	
	public void setCanAdd( boolean canAdd ) {
		this.canAdd = canAdd;
	}
	
	public int getCanAddCheck() {
		return canAddCheck;
	}
	
	public void setDisableOnAdd( int disable ) {
		disableOnAdd = disable;
	}
	
	public int getDisableOnAdd() {
		return disableOnAdd;
	}

	public void clear() {
		addedMode = removedMode = deactivatedMode = activatedMode = null;
	}
}