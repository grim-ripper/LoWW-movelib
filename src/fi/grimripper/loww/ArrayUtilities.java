package fi.grimripper.loww;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * A collection of utilities for array operations.
 * 
 * @author Marko Tuominen
 */
public abstract class ArrayUtilities {

	/**
	 * Appends an object to the end of an array.
	 * 
	 * @param <O>			type of objects in the array
	 * @param <S>			type of the new object, which must extend the array's component type
	 * @param array			append to this array (create a new size one array if <code>null</code>,
	 * 						using the appended object's type)
	 * @param newObject		append this object
	 * @return				new array with the given object
	 */
	@SuppressWarnings( "unchecked" )
	public static <O, S extends O> O[] appendObject( O[] array, S newObject ) {
		if (array == null && newObject == null)
			return (O[])new Object[] { null };
		
		Class <O> type = (Class <O>)(array != null ? array.getClass().getComponentType() :
			newObject.getClass());
		O[] objects = null;
		
		if (array == null)
			objects = (O[])Array.newInstance( type, 1 );
		
		else
			objects = Arrays.copyOf( array, array.length + 1 );
		
		objects[ objects.length - 1 ] = newObject;
		return objects;
	}
	
	/**
	 * Copies a 2D array. The copy array is a new object, and each array within it is a new object.
	 * The content arrays will have references to the same objects as in the original array. The
	 * content objects are not copied.
	 * 
	 * @param <O>			type of objects
	 * @param array			copy this array
	 * @return				copy of the array
	 */
	public static <O> O[][] copy2D( O[][] array ) {
		if (array == null)
			return null;
		
		O[][] objects = Arrays.copyOf( array, array.length );
		
		for (int i = 0; i < array.length; i++)
			objects[i] = array[i] == null ? null : Arrays.copyOf( array[i], array[i].length );
		
		return objects;
	}

	/**
	 * Finds an object in an array.
	 * 
	 * @param array			search for object in this array (not <code>null</code>)
	 * @param search		search for this object (not <code>null</code>)
	 * @return				object's index, or negative if not found
	 */
	public static int linearSearch( Object[] array, Object search ) {
		for (int i = 0; i < array.length; i++)
			if (search.equals( array[i] ))
				return i;
		return -1;
	}

	/**
	 * Finds an object from an array, and returns a new array without the object.
	 * 
	 * @param <O>			type of objects
	 * @param array			search for object in this array (not <code>null</code>)
	 * @param index		remove object at this index
	 * @return				a new array, without the object that was removed
	 */
	@SuppressWarnings( "unchecked" )
	public static <O> O[] removeObject( O[] array, int index ) {
		Class <O> type = (Class <O>)array.getClass().getComponentType();
		
		if (array.length == 0)
			return (O[])Array.newInstance( type, 0 );
		
		O[] objects = (O[])Array.newInstance( type, array.length - 1 );
		System.arraycopy( array, 0, objects, 0, index );
		System.arraycopy( array, index + 1, objects, index, objects.length - index );
		
		return objects;
	}
}
