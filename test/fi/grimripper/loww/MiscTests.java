package fi.grimripper.loww;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import fi.grimripper.loww.Point;

@RunWith(Suite.class)
@SuiteClasses({ AdditionalPropertiesTest.class, ArrayUtilitiesTest.class, DirectionTest.class,
	HeightTest.class })
public class MiscTests {

	@BeforeClass
	public static void testPointDistance() {
		try {
			Class.forName( "fi.grimripper.loww.AwtPoint" );
		} catch (ClassNotFoundException cnfx) {
			cnfx.printStackTrace();
		}
		
		assertEquals( Math.sqrt( 5 ), Point.distance( 1, 4, 3, 5 ), 0.001 );
	}
}
