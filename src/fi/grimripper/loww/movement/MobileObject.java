package fi.grimripper.loww.movement;

import java.util.ArrayList;
import java.util.LinkedList;

import fi.grimripper.loww.Height;
import fi.grimripper.loww.templates.MovementTemplate;
import fi.grimripper.loww.tiles.Obstacle;

/**
 * A mobile object is an obstacle that can move. It has movement modes, which determine how it
 * moves. A mobile object can have multiple movement modes, but only one is active at any time. A
 * movement mode can't be selected directly. The movement modes have an order of priority, based on
 * the order in which they were added. The latest addition, ignoring any disabled modes, is active.
 * Mobile objects can also receive movement mode listeners which are notified when movement modes
 * are added or removed, and when the active mode changes.
 * 
 * @author Marko Tuominen
 * @see MovementMode
 * @see MoveModeListener
 */
public abstract class MobileObject extends Obstacle {

	private ArrayList <MovementMode> movement = new ArrayList <>();
	private LinkedList <MoveModeListener> modeListeners = null;
	private LinkedList <MotionListener> motionListeners = null;
	
	/**
	 * Constructs a basic mobile object with height and template.
	 * 
	 * @param height		the mobile object's height
	 * @param template		the mobile object's template
	 */
	protected MobileObject( Height height, MovementTemplate template ) {
		super( height, template );
	}

	/**
	 * Constructs a mobile object with height, template and additional properties.
	 * 
	 * @param height		the mobile object's height
	 * @param template		the mobile object's template
	 * @param properties	the mobile object's additional properties
	 */
	protected MobileObject( Height height, MovementTemplate template, Properties properties ) {
		super( height, template, properties );
	}
	
	/**
	 * Gets a movement template instead of the obstacle template returned by superclass.
	 */
	@Override
	public MovementTemplate getTemplate() {
		return (MovementTemplate)super.getTemplate();
	}

	/**
	 * Checks if this mobile object has a specific movement mode.
	 * 
	 * @param mode		check if this mobile object has this movement mode
	 * @return			this mobile object has the given movement mode
	 */
	public boolean hasMovementMode( MovementMode mode ) {
		return movement.contains( mode );
	}
	
    /**
     * Gets the mobile object's active movement mode. This is <code>null</code> for mobile objects
     * that can't move: ones with no movement modes or all movement modes deactivated.
     * 
     * @return			the mobile object's active movement mode
     */
    public MovementMode getMovementMode() {
    	MovementMode iMode = null;
    	
    	// active movement mode is the last enabled mode in the selection
    	for (int i = movement.size() - 1; i >= 0; i--)
    		if (!(iMode = movement.get( i )).isDisabled())
    			return iMode;

    	return null;
    }

    /**
     * Gets all of the mobile object's movement modes. Disabled movement modes are included.
     * 
     * @return			the mobile object's current movement modes
     */
    public MovementMode[] getMovementModes() {
    	return movement.toArray( new MovementMode[ movement.size() ]);
    }

    /**
     * Checks if a new movement mode can be added. Movement mode listeners can prevent this, an
     * existing movement mode can't be added again and a <code>null</code> can't be added.
     * 
     * @param mode		check if this mode can be added
     * @return			the mode can be added
     */
    public boolean canAddMovementMode( MovementMode mode ) {
    	if (mode == null || movement.contains( mode ))
    		return false;
    	
    	if (modeListeners != null)
    		for (MoveModeListener listener : modeListeners)
    			if (!listener.canAddMovementMode( this, mode ))
    				return false;
    	
    	return true;
    }
    
    /**
     * Adds a new movement mode for the mobile object. The latest addition will become the active
     * movement mode, unless it's already disabled before it's added. This method does nothing if
     * the parameter is <code>null</code>, movement mode listeners prevent the addition, or the
     * mobile object already has the movement mode being added.
     * 
     * @param mode			add to the mobile object's available movement modes
     * @return				the movement mode that was added
     */
    public MovementMode addMovementMode( MovementMode mode ) {
    	if (!canAddMovementMode( mode ))
    		return null;	// can't add null modes or duplicate instances, or listeners prevent

    	MovementMode deactivated = getMovementMode();		// previously active
    	
    	if (modeListeners != null)		// notify listeners
			for (MoveModeListener listener : modeListeners)
				listener.movementModeAdded( this, mode );

    	// add after the events so that it can't be immediately disabled by normal means
    	movement.add( mode );
    	
    	if (!mode.isDisabled() && modeListeners != null)		// notify of active mode changing
    		for (MoveModeListener listener : modeListeners)
    			listener.activeModeChanged( this, deactivated, mode );
    	
    	return mode;
    }

    /**
     * Removes a movement mode from the mobile object. Removing the active movement mode will
     * activate the next one by order of precedence. A mobile object which has its last active
     * movement mode removed will not be able to move.
     * 
     * @param mode			remove this movement mode
     * @return				the removed movement mode, or <code>null</code> if not found
     */
    public MovementMode removeMovementMode( MovementMode mode ) {
    	MovementMode oldActive = getMovementMode();
    	if (!movement.remove( mode ))
    		return null;
    	
    	if (modeListeners != null)		// notify listeners of removal
    		for (MoveModeListener listener : modeListeners)
    			listener.movementModeRemoved( this, mode );
    	
    	// removed active movement mode, notify listeners of change
    	if (mode.equals( oldActive ) && modeListeners != null) {
    		MovementMode newActive = getMovementMode();
    		
    		for (MoveModeListener listener : modeListeners)
    			listener.activeModeChanged( this, mode, newActive );
    	}
    	
    	return mode;
    }

    /**
     * Disables or enables one of the mobile object's movement modes. Disabling the active movement
     * mode will make the next mode in the selection active. A mobile object can also become unable
     * to move if its last remaining movement mode is disabled. Re-enabling a movement mode which
     * has precedence over the currently active one will make the newly enabled mode active. If a
     * movement mode is disabled multiple times, it must be enabled an equal number of times before
     * it becomes functional again.
     * 
     * @param mode			disable or enable this mode
     * @param disable		<code>true</code> disables the mode, <code>false</code> enables
     * @see					MovementMode#setDisabled(boolean)
     */
    public void setMovementModeDisabled( MovementMode mode, boolean disable ) {
    	if (!movement.contains( mode ))
    		return;
    	
    	// find active movement mode, change state, and check if active mode changed
    	MovementMode oldActive = getMovementMode();
    	mode.setDisabled( disable );
    	MovementMode newActive = getMovementMode();
    	
    	// if active movement mode changed, notify listeners
    	if (modeListeners != null && (newActive == null && oldActive != null ||
    			newActive != null && !newActive.equals( oldActive )))
    		for (MoveModeListener listener : modeListeners)
    			listener.activeModeChanged( this, oldActive, newActive );
    }

    /**
     * Adds a movement mode listener, ignoring <code>null</code>s.
     * 
     * @param listener			add this listener, even if there already is a similar one
     */
    public void addMovementModeListener( MoveModeListener listener ) {
    	if (listener == null)
    		return;
    	
    	if (modeListeners == null)
    		modeListeners = new LinkedList <MoveModeListener>();
    	
    	modeListeners.add( listener );
    }

    /**
     * Removes a movement mode listener, if it exists.
     * 
     * @param listener		remove this listener, or the first instance of it
     * @return				the listener that was removed, if any
     */
    public MoveModeListener removeMovementModeListener( MoveModeListener listener ) {
    	return modeListeners != null && modeListeners.remove( listener ) ? listener : null;
    }

    /**
     * Adds a motion listener, ignoring <code>null</code>s.
     * 
     * @param listener			add this listener, even if there already is a similar one
     */
    public void addMotionListener( MotionListener listener ) {
    	if (listener == null)
    		return;
    	
    	if (motionListeners == null)
    		motionListeners = new LinkedList <MotionListener>();
    	
    	motionListeners.add( listener );
    }

    /**
	 * Gets the motion listeners attached to this mobile object.
	 * 
	 * @return				motion listeners attached to this mobile object
	 */
	public MotionListener[] getMotionListeners() {
		return motionListeners == null ? new MotionListener[0] :
			motionListeners.toArray( new MotionListener[ motionListeners.size() ]);
	}

	/**
     * Removes a motion listener, if it exists.
     * 
     * @param listener			remove this listener, or the first instance of it
     * @return				the listener that was removed, if any
     */
    public MotionListener removeMotionListener( MotionListener listener ) {
    	return motionListeners != null && motionListeners.remove( listener ) ? listener : null;
    }
}
