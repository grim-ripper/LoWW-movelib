package fi.grimripper.loww.movement;


/**
 * A listener interface to observe changes in a mobile object's collection of movement modes. The
 * listener receives notifications when movement modes are added and removed, or enabled or
 * disabled, or when the active movement mode changes. In addition, the listener is notified before
 * a movement mode is added, and can prevent that mode from being added.
 * 
 * @author Marko Tuominen
 * @see MobileObject
 * @see MovementMode
 */
public interface MoveModeListener {
	
	/**
	 * Determines whether or not a movement mode can be added. If any listener returns
	 * <code>false</code>, then the movement mode can't be given to the mobile object. The movement
	 * mode shouldn't be modified, because this method is used only to test if it's possible to add
	 * the movement mode. There's also no guarantee that all listeners will be called, because only
	 * one <code>false</code> is needed to prevent the movement mode from being added.
	 * 
	 * @param mobile			the mobile object about to receive a new movement mode
	 * @param mode				the mobile object's would-be new movement mode
	 * @return					<code>true</code> to allow adding the mode
	 */
	public boolean canAddMovementMode( MobileObject mobile, MovementMode mode );
	
	/**
	 * Notified when a new movement mode is added to the mobile object. Usually the new addition
	 * will become the active mode, since it has the highest priority. Unless it's already
	 * disabled, there's also a notification for <code>activeModeChanged</code>. This event
	 * shouldn't be used to disable the movement mode that was just added. Mobile object doesn't
	 * allow disabling the new mode between add event and activation event.
	 * 
	 * @param mobile			this mobile object received a new movement mode
	 * @param mode				the mobile object's new movement mode
	 * @see						#activeModeChanged(MobileObject, MovementMode, MovementMode)
	 */
	public void movementModeAdded( MobileObject mobile, MovementMode mode );
	
	/**
	 * Notified when a movement mode is removed from the mobile object. In case the removed mode
	 * was active, there'll also be a notification for <code>activeModeChanged</code>.
	 * 
	 * @param mobile			this mobile object lost one of its movement modes
	 * @param mode				the removed movement mode
	 * @see						#activeModeChanged(MobileObject, MovementMode, MovementMode)
	 */
	public void movementModeRemoved( MobileObject mobile, MovementMode mode );

	/**
	 * Notified when the active movement mode changes. Change of active mode will normally be
	 * triggered by adding, removing, disabling or enabling movement modes.
	 *
	 * @param mobile			this mobile object's active movement mode changed
	 * @param deactivated		the formerly active movement mode (<code>null</code> if the mobile
	 * 							object didn't have an active movement mode before)
	 * @param activated			the newly active movement mode (<code>null</code> if the mobile
	 * 							object doesn't have an active movement mode anymore)
	 */
	public void activeModeChanged( MobileObject mobile, MovementMode deactivated,
			MovementMode activated );
}
