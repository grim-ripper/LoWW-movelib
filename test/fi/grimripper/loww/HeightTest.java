package fi.grimripper.loww;

import static fi.grimripper.loww.Height.*;
import static org.junit.Assert.*;

import org.junit.Test;

import fi.grimripper.loww.Height;

public class HeightTest {

	@Test
	public void testHeight() {
		Height[] heights = Height.values();
		String[] heightNames = {
			"DEEP", "SHALLOW", "FLAT", "LOW", "HIGH", "VERY_HIGH", "BLOCKING" };
		
		assertEquals( heights.length, heightNames.length );
		for (int i = 0; i < heights.length; i++)
			assertSame( heights[i], Height.valueOf( heightNames[i] ));
		
		assertSame( DEEP, Height.getHeight( -1 ));
		assertSame( BLOCKING, Height.getHeight( heights.length ));
		
		for (int i = 0; i < heights.length; i++)
			assertSame( heights[i], Height.getHeight( i ));
		
		assertEquals( 0, Height.compareHeights( null, null ));
		assertTrue( Height.compareHeights( null, DEEP ) < 0 );
		assertTrue( Height.compareHeights( DEEP, null ) > 0 );
		
		for (int i = 0; i < heights.length - 1; i++)
			assertTrue( Height.compareHeights( heights[i], heights[i + 1] ) < 0 );
		
		for (int i = 1; i < heights.length; i++)
			assertTrue( Height.compareHeights( heights[i], heights[i - 1] ) > 0 );
		
		for (Height h : heights)
			assertEquals( 0, Height.compareHeights( h, h ));
		
		assertNull( Height.max( null, null ));
		assertSame( DEEP, Height.max( DEEP, null ));
		assertSame( DEEP, Height.max( null, DEEP ));
		
		for (int i = 1; i < heights.length; i++) {
			assertSame( heights[i], Height.max( heights[i], heights[i - 1] ));
			assertSame( heights[i], Height.max( heights[i - 1], heights[i] ));
		}
		
		assertNull( Height.min( null, null ));
		assertSame( BLOCKING, Height.min( BLOCKING, null ));
		assertSame( BLOCKING, Height.min( null, BLOCKING ));
		
		for (int i = 0; i < heights.length - 1; i++) {
			assertSame( heights[i], Height.min( heights[i], heights[i + 1] ));
			assertSame( heights[i], Height.min( heights[i + 1], heights[i] ));
		}
		
		for (Height h : heights)
			assertSame( Height.max( h, h ), Height.min( h, h ));
	}
}
