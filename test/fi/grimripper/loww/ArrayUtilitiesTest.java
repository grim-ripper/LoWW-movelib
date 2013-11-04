package fi.grimripper.loww;

import static org.junit.Assert.*;

import org.junit.Test;

import fi.grimripper.loww.ArrayUtilities;

public class ArrayUtilitiesTest {

	@Test
	public void testAppendObject() {
		String[] numbers = { "one", "two", "three" };
		String[] moreNumbers = ArrayUtilities.appendObject( numbers, "four" );
		
		// creates a new array with the extra object added to it
		assertArrayEquals( new String[] { "one", "two", "three", "four" }, moreNumbers );
		assertNotSame( numbers, moreNumbers );
		
		// null array is treated as an empty array, but null values can be added to an array
		assertArrayEquals( new Integer[] { 1 }, ArrayUtilities.appendObject( null, 1 ));
		assertArrayEquals( new Byte[] { null }, ArrayUtilities.appendObject( null, null ));
	}
	
	@Test
	public void testCopy2D() {
		Object a = new Object();
		Object b = new Object();
		Object c = new Object();
		
		// copying accepts null values and null arrays
		Object[][] original = {{ a, b, c }, { a, b, null }, { null }, null };
		Object[][] copy = ArrayUtilities.copy2D( original );
		
		assertNotNull( copy );
		assertNotSame( original, copy );
		assertEquals( original.length, copy.length );
		
		// assert original copy have equal content but not the same, and nulls are copied as nulls
		for (int i = 0; i < original.length; i++)
			if (original[i] == null)
				assertNull( copy[i] );
			else
				assertNotSame( original[i], copy[i] );
		
		assertArrayEquals( original, copy );
		assertNull( ArrayUtilities.copy2D( null ));		// copy of null is null
	}

	@Test
	public void testLinearSearch() {
		Object a = new Object();
		Object b = new Object();
		Object c = new Object();
		
		// finds index of first occurrence if there are more than one
		Object[] objects = { a, null, a, b };
		assertEquals( 0, ArrayUtilities.linearSearch( objects, a ));
		assertEquals( 3, ArrayUtilities.linearSearch( objects, b ));
		
		// doesn't find object that's not in the array, and finds nothing in empty array
		assertTrue( ArrayUtilities.linearSearch( objects, c ) < 0 );
		assertTrue( ArrayUtilities.linearSearch( new Object[0], new Object() ) < 0 );
	}

	@Test( expected = NullPointerException.class )
	public void testLinearSearchNullPointer1() {
		ArrayUtilities.linearSearch( null, new Object() );
	}
	
	@Test( expected = NullPointerException.class )
	public void testLinearSearchNullPointer2() {
		ArrayUtilities.linearSearch( new Object[1], null );
	}

	@Test
	public void testRemoveObject() {
		Object a = new Object();
		Object b = new Object();
		Object c = new Object();
		
		Object[] objects = { a, b, null, c, b };
		Object[] noA = ArrayUtilities.removeObject( objects, 0 );
		Object[] noNull = ArrayUtilities.removeObject( objects, 2 );
		
		// new array without removed object
		assertArrayEquals( new Object[] { b, null, c, b }, noA );
		assertArrayEquals( new Object[] { a, b, c, b }, noNull );
		
		// just new empty array if parameter array is empty
		Object[] empty = new Object[0];
		Object[] nothingToRemove = ArrayUtilities.removeObject( empty, 1 );
		assertArrayEquals( new Object[0], nothingToRemove );
		assertNotSame( empty, nothingToRemove );
	}

	@Test( expected = NullPointerException.class )
	public void testRemoveObjectNullPointer() {
		ArrayUtilities.removeObject( null, 0 );
	}
	
	@Test( expected = ArrayIndexOutOfBoundsException.class )
	public void testRemoveObjectArrayIndexOutOfBounds() {
		ArrayUtilities.removeObject( new Object[1], 1 );
	}

	@Test
	public void testForCompleteCoverage() {
		new ArrayUtilities() {
			
		};
	}
}
