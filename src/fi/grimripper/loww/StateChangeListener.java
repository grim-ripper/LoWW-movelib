package fi.grimripper.loww;

/**
 * A generic interface for listening when an object's state changes. The reasons for triggering a
 * state change event vary between classes.

 * @author Marko Tuominen
 * 
 * @param <T>		type of object whose state is listened
 */
public interface StateChangeListener <T> {

	/**
	 * Notified when the state of an object changes.
	 * 
	 * @param changed			the object whose state changed
	 */
	public void stateChanged( T changed );
}
