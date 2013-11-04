package fi.grimripper.loww;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fi.grimripper.loww.AdditionalProperties.Properties;
import fi.grimripper.loww.AdditionalProperties.Property;

public class AdditionalPropertiesTest {

	@Test
	public void testAdditionalProperties() {
		// two parent properties with one child property each
		Property prop1 = new Property();
		Property prop2 = new Property( prop1 );
		Property prop3 = new Property();
		Property prop4 = new Property( prop3 );
		
		// properties object without the second parent property
		Properties props = new Properties( prop1, prop2, prop4 );
		
		// second child counts as its parent, so the parent is also included
		assertTrue( props.hasProperty( prop1 ));
		assertTrue( props.hasProperty( prop2 ));
		assertTrue( props.hasProperty( prop3 ));
		assertTrue( props.hasProperty( prop4 ));
		assertFalse( props.hasProperty( null ));	// null properties are "never has"
		
		props = new Properties();
		
		// empty properties object, but not null
		assertNotNull( props.getProperties() );
		
		// constructor with null creates empty properties object
		props = new Properties( (Property[])null );
		assertNotNull( props.getProperties() );
		
		// null property can't have children, it's treated as no parent property
		Property childOfNull = new Property( null );
		assertFalse( childOfNull.isSubProperty( null ));
	}
	
	@Test( expected = NullPointerException.class )
	public void testNullProperty() {
		new Properties( (Property)null ).hasProperty( null );
	}
}
