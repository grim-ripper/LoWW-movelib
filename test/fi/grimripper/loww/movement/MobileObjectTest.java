package fi.grimripper.loww.movement;

import static fi.grimripper.loww.Height.HIGH;
import static fi.grimripper.loww.Height.LOW;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fi.grimripper.loww.AdditionalProperties.Properties;
import fi.grimripper.loww.movement.MobileObject;
import fi.grimripper.loww.movement.MotionListener;
import fi.grimripper.loww.movement.MovementMode;
import fi.grimripper.loww.templates.MovementTemplate;
import fi.grimripper.loww.test.TestMobileObject;
import fi.grimripper.loww.test.TestMotionListener;
import fi.grimripper.loww.test.TestMoveModeListener;
import fi.grimripper.loww.test.TestMovementMode;
import fi.grimripper.loww.test.TestMovementTemplate;

public class MobileObjectTest {

	@Test
	public void testMobileObject() {
		MovementTemplate template = new TestMovementTemplate();
		Properties props = new Properties();
		
		// test template, height and properties
		MobileObject test = new TestMobileObject( LOW, template );
		assertSame( LOW, test.getHeight() );
		assertSame( template, test.getTemplate() );
		assertNull( test.getProperties() );
		
		test = new TestMobileObject( HIGH, template, props );
		assertSame( HIGH, test.getHeight() );
		assertSame( template, test.getTemplate() );
		assertSame( props, test.getProperties() );
		
		// the mobile object doesn't have any movement modes yet
		MovementMode mode = new TestMovementMode( test, 0 );
		assertFalse( test.hasMovementMode( null ));
		assertFalse( test.hasMovementMode( mode ));
		assertNull( test.getMovementMode() );
		assertArrayEquals( new MovementMode[0], test.getMovementModes() );
		
		// null can't be added, but nothing prevents adding an actual mode
		assertFalse( test.canAddMovementMode( null ));
		assertTrue( test.canAddMovementMode( mode ));
		assertSame( mode, test.addMovementMode( mode ));
		
		// the mobile object now has the movement mode, and it can't be added again
		assertFalse( test.canAddMovementMode( mode ));
		assertSame( mode, test.getMovementMode() );
		assertArrayEquals( new MovementMode[] { mode }, test.getMovementModes() );

		// a disabled mode can be added, and doesn't become the active mode
		MovementMode disabledMode = new TestMovementMode( test, 0 );
		disabledMode.setDisabled( true );
		assertSame( disabledMode, test.addMovementMode( disabledMode ));
		assertSame( mode, test.getMovementMode() );

		// no active mode when both are disabled
		test.setMovementModeDisabled( mode, true );
		assertNull( test.getMovementMode() );

		// still no active mode if disabled twice
		test.setMovementModeDisabled( mode, true );
		assertNull( test.getMovementMode() );

		// if disabled twice, must be enabled twice
		test.setMovementModeDisabled( mode, false );
		assertNull( test.getMovementMode() );
		
		test.setMovementModeDisabled( mode, false );
		assertSame( mode, test.getMovementMode() );
		
		test.setMovementModeDisabled( disabledMode, true );
		assertSame( mode, test.getMovementMode() );
		
		test.setMovementModeDisabled( disabledMode, false );
		assertSame( mode, test.getMovementMode() );
		
		// the mode that was added last becomes active when both are enabled
		test.setMovementModeDisabled( disabledMode, false );
		assertSame( disabledMode, test.getMovementMode() );
		
		// disabling the last mode makes the next one active
		test.setMovementModeDisabled( disabledMode, true );
		assertSame( mode, test.getMovementMode() );
		assertSame( disabledMode, test.removeMovementMode( disabledMode ));
		
		// test removing null and last mode, check that the mode was removed
		assertNull( test.removeMovementMode( null ));
		assertSame( mode, test.removeMovementMode( mode ));
		assertTrue( test.canAddMovementMode( mode ));
		assertNull( test.getMovementMode() );
		assertArrayEquals( new MovementMode[0], test.getMovementModes() );
		
		// adding or removing null listener causes no problems
		test.addMovementModeListener( null );
		assertTrue( test.canAddMovementMode( mode ));
		assertNull( test.removeMovementModeListener( null ));
		
		// add a listener and test check for adding movement modes
		TestMoveModeListener listener = new TestMoveModeListener();
		test.addMovementModeListener( listener );
		
		listener.setMobileObject( test );
		listener.setMovementMode( mode );
		assertTrue( test.canAddMovementMode( mode ));
		assertEquals( 1, listener.getCanAddCheck() );
		
		// try ineffective operations while there are no movement modes
		assertNull( test.addMovementMode( null ));
		assertNull( test.removeMovementMode( null ));
		assertNull( test.removeMovementMode( mode ));
		test.setMovementModeDisabled( mode, true );
		assertEquals( 1, listener.getCanAddCheck() );		// not checked for null
		
		// test listener that prevents adding movement modes
		listener.setCanAdd( false );
		assertFalse( test.canAddMovementMode( mode ));
		assertNull( test.addMovementMode( mode ));
		assertEquals( 3, listener.getCanAddCheck() );
		
		// listener can be added more than once, only one listener that prevents adding is checked
		test.addMovementModeListener( listener );
		assertFalse( test.canAddMovementMode( mode ));
		assertEquals( 4, listener.getCanAddCheck() );
		
		// the first listener prevents adding, so second listener isn't checked
		TestMoveModeListener yesListener = new TestMoveModeListener();
		yesListener.setMobileObject( test );
		yesListener.setMovementMode( mode );
		
		test.addMovementModeListener( yesListener );
		assertFalse( test.canAddMovementMode( mode ));
		assertEquals( 5, listener.getCanAddCheck() );
		assertEquals( 0, yesListener.getCanAddCheck() );
		
		// with listener added twice, only one reference is removed at a time
		assertSame( listener, test.removeMovementModeListener( listener ));
		assertFalse( test.canAddMovementMode( mode ));
		assertEquals( 6, listener.getCanAddCheck() );
		assertEquals( 0, yesListener.getCanAddCheck() );
		
		// mode can be added when all listeners allow it, nothing is deactivated for first mode
		listener.setCanAdd( true );
		assertSame( mode, test.addMovementMode( mode ));
		assertSame( mode, listener.getAddedMovementMode() );
		assertSame( mode, yesListener.getAddedMovementMode() );
		assertNull( listener.getDeactivatedMovementMode() );
		assertSame( mode, listener.getActivatedMovementMode() );
		assertNull( yesListener.getDeactivatedMovementMode() );
		assertSame( mode, listener.getActivatedMovementMode() );
		
		listener.clear();
		yesListener.clear();
		listener.setMovementMode( disabledMode );
		yesListener.setMovementMode( disabledMode );
		
		// disabled mode is added, but it doesn't become active and nothing is deactivated
		assertSame( disabledMode, test.addMovementMode( disabledMode ));
		assertSame( disabledMode, listener.getAddedMovementMode() );
		assertSame( disabledMode, yesListener.getAddedMovementMode() );
		assertNull( listener.getDeactivatedMovementMode() );
		assertNull( listener.getActivatedMovementMode() );
		assertNull( yesListener.getDeactivatedMovementMode() );
		assertNull( yesListener.getActivatedMovementMode() );
		
		test.setMovementModeDisabled( disabledMode, false );
		test.removeMovementMode( disabledMode );
		listener.clear();
		yesListener.clear();
		listener.setDisableOnAdd( 1 );
		
		// try adding an enabled mode and disabling it in add mode event
		test.addMovementMode( disabledMode );
		assertSame( disabledMode, listener.getAddedMovementMode() );
		assertSame( disabledMode, yesListener.getAddedMovementMode() );
		assertSame( disabledMode, listener.getActivatedMovementMode() );
		assertSame( disabledMode, yesListener.getActivatedMovementMode() );
		assertSame( mode, listener.getDeactivatedMovementMode() );
		assertSame( mode, yesListener.getDeactivatedMovementMode() );
		
		test.removeMovementMode( disabledMode );
		listener.setDisableOnAdd( -1 );
		listener.clear();
		yesListener.clear();
		
		// try disabling by a privacy-violating method
		test.addMovementMode( disabledMode );
		assertSame( disabledMode, listener.getAddedMovementMode() );
		assertSame( disabledMode, yesListener.getAddedMovementMode() );
		assertNull( listener.getActivatedMovementMode() );
		assertNull( yesListener.getActivatedMovementMode() );
		assertNull( listener.getDeactivatedMovementMode() );
		assertNull( yesListener.getDeactivatedMovementMode() );
		
		listener.setDisableOnAdd( 0 );
		
		// movement modes are returned in a copy array
		assertSame( mode, test.getMovementMode() );
		MovementMode[] modes = test.getMovementModes();
		assertArrayEquals( new MovementMode[] { mode, disabledMode }, modes );
		modes[0] = null;
		assertArrayEquals( new MovementMode[] { mode, disabledMode }, test.getMovementModes() );
		
		listener.clear();
		yesListener.clear();
		listener.setMovementMode( mode );
		yesListener.setMovementMode( mode );
		
		// removed mode is deactivated, and nothing becomes active since other mode is disabled
		assertSame( mode, test.removeMovementMode( mode ));
		assertNull( test.getMovementMode() );
		assertSame( mode, listener.getRemovedMovementMode() );
		assertSame( mode, yesListener.getRemovedMovementMode() );
		assertSame( mode, listener.getDeactivatedMovementMode() );
		assertNull( listener.getActivatedMovementMode() );
		assertSame( mode, yesListener.getDeactivatedMovementMode() );
		assertNull( yesListener.getActivatedMovementMode() );
		
		listener.clear();
		yesListener.clear();
		
		// disable again, then enable, nothing happens
		test.setMovementModeDisabled( disabledMode, true );
		test.setMovementModeDisabled( disabledMode, false );
		assertNull( test.getMovementMode() );
		assertNull( listener.getDeactivatedMovementMode() );
		assertNull( listener.getActivatedMovementMode() );
		assertNull( yesListener.getDeactivatedMovementMode() );
		assertNull( yesListener.getActivatedMovementMode() );
		
		listener.clear();
		yesListener.clear();
		
		// enabling the disabled mode makes it active, but there's nothing to deactivate
		test.setMovementModeDisabled( disabledMode, false );
		assertSame( disabledMode, test.getMovementMode() );
		assertNull( listener.getDeactivatedMovementMode() );
		assertSame( disabledMode, listener.getActivatedMovementMode() );
		assertNull( yesListener.getDeactivatedMovementMode() );
		assertSame( disabledMode, yesListener.getActivatedMovementMode() );
		
		// newer mode becomes active
		listener.setMovementMode( mode );
		yesListener.setMovementMode( mode );
		assertSame( mode, test.addMovementMode( mode ));
		assertSame( mode, test.getMovementMode() );
		
		listener.clear();
		yesListener.clear();
		
		// remove last mode, it's deactivated and next one becomes active
		assertSame( mode, test.removeMovementMode( mode ));
		assertSame( disabledMode, test.getMovementMode() );
		assertSame( mode, listener.getDeactivatedMovementMode() );
		assertSame( disabledMode, listener.getActivatedMovementMode() );
		assertSame( mode, yesListener.getDeactivatedMovementMode() );
		assertSame( disabledMode, yesListener.getActivatedMovementMode() );
		
		listener.clear();
		yesListener.clear();
		
		// enabling a mode that hasn't been added has no effect
		test.setMovementModeDisabled( mode, false );
		assertSame( disabledMode, test.getMovementMode() );
		assertNull( listener.getDeactivatedMovementMode() );
		assertNull( listener.getActivatedMovementMode() );
		assertNull( yesListener.getDeactivatedMovementMode() );
		assertNull( yesListener.getActivatedMovementMode() );
		
		// enabling a mode that's already enabled has no effect
		test.setMovementModeDisabled( disabledMode, false );
		assertSame( disabledMode, test.getMovementMode() );
		assertNull( listener.getDeactivatedMovementMode() );
		assertNull( listener.getActivatedMovementMode() );
		assertNull( yesListener.getDeactivatedMovementMode() );
		assertNull( yesListener.getActivatedMovementMode() );
		
		// after being enabled twice, must be disabled twice
		test.setMovementModeDisabled( disabledMode, true );
		assertSame( disabledMode, test.getMovementMode() );
		assertNull( listener.getDeactivatedMovementMode() );
		assertNull( listener.getActivatedMovementMode() );
		assertNull( yesListener.getDeactivatedMovementMode() );
		assertNull( yesListener.getActivatedMovementMode() );
		
		// disable only movement mode, and it's deactivated
		test.setMovementModeDisabled( disabledMode, true );
		assertNull( test.getMovementMode() );
		assertSame( disabledMode, listener.getDeactivatedMovementMode() );
		assertNull( listener.getActivatedMovementMode() );
		assertSame( disabledMode, yesListener.getDeactivatedMovementMode() );
		assertNull( yesListener.getActivatedMovementMode() );
		
		assertSame( mode, test.addMovementMode( mode ));
		assertSame( mode, test.getMovementMode() );
		listener.clear();
		yesListener.clear();

		// repeat same with two modes
		test.setMovementModeDisabled( mode, true );
		assertNull( test.getMovementMode() );
		assertSame( mode, listener.getDeactivatedMovementMode() );
		assertNull( listener.getActivatedMovementMode() );
		assertSame( mode, yesListener.getDeactivatedMovementMode() );
		assertNull( yesListener.getActivatedMovementMode() );
		
		listener.clear();
		yesListener.clear();
		
		// enable a movement mode, becomes active
		test.setMovementModeDisabled( disabledMode, false );
		assertSame( disabledMode, test.getMovementMode() );
		assertNull( listener.getDeactivatedMovementMode() );
		assertSame( disabledMode, listener.getActivatedMovementMode() );
		assertNull( yesListener.getDeactivatedMovementMode() );
		assertSame( disabledMode, yesListener.getActivatedMovementMode() );

		listener.clear();
		yesListener.clear();
		
		// enable last mode, becomes active, other mode is deactivated
		test.setMovementModeDisabled( mode, false );
		assertSame( mode, test.getMovementMode() );
		assertSame( disabledMode, listener.getDeactivatedMovementMode() );
		assertSame( mode, listener.getActivatedMovementMode() );
		assertSame( disabledMode, yesListener.getDeactivatedMovementMode() );
		assertSame( mode, yesListener.getActivatedMovementMode() );

		listener.clear();
		yesListener.clear();
		
		// disable last mode, it's deactivated, other mode is activated
		test.setMovementModeDisabled( mode, true );
		assertSame( disabledMode, test.getMovementMode() );
		assertSame( mode, listener.getDeactivatedMovementMode() );
		assertSame( disabledMode, listener.getActivatedMovementMode() );
		assertSame( mode, yesListener.getDeactivatedMovementMode() );
		assertSame( disabledMode, yesListener.getActivatedMovementMode() );
		
		// trying to remove a listener that's not added has no effect
		assertSame( yesListener, test.removeMovementModeListener( yesListener ));
		assertNull( test.removeMovementModeListener( yesListener ));
		
		// trying to add null motion listeners has no effect
		assertNull( test.removeMotionListener( null ));
		assertArrayEquals( new MotionListener[0], test.getMotionListeners() );
		test.addMotionListener( null );
		assertArrayEquals( new MotionListener[0], test.getMotionListeners() );
		
		// test adding a motion listener
		MotionListener motionListener = new TestMotionListener();
		test.addMotionListener( motionListener );
		assertArrayEquals( new MotionListener[] { motionListener }, test.getMotionListeners() );
		
		// motion listener can be added more than once
		test.addMotionListener( motionListener );
		assertArrayEquals( new MotionListener[] { motionListener, motionListener },
				test.getMotionListeners() );
		
		// trying to remove null has no effect, after adding twice, can be removed twice
		assertNull( test.removeMotionListener( null ));
		assertSame( motionListener, test.removeMotionListener( motionListener ));
		assertArrayEquals( new MotionListener[] { motionListener }, test.getMotionListeners() );
		assertSame( motionListener, test.removeMotionListener( motionListener ));
		assertArrayEquals( new MotionListener[0], test.getMotionListeners() );

		// re-test adding after data structure has been emptied
		test.addMotionListener( motionListener );
		assertArrayEquals( new MotionListener[] { motionListener }, test.getMotionListeners() );
	}
}