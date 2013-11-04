package fi.grimripper.loww;

import java.util.Arrays;

/**
 * An interface that can be used as a common type for objects with additional properties.
 * 
 * @author Marko Tuominen
 */
public interface AdditionalProperties {

	/**
	 * Gets the object's properties.
	 * 
	 * @return			a handler for the object's additional properties (can be
	 * 					<code>null</code> if the object doesn't have additional properties)
	 */
	public Properties getProperties();

	/**
	 * A class for additional properties that can be shared by objects of different types. The
	 * properties can be hierarchical, having a parent and any number of children. This class
	 * doesn't save children, as the properties are meant to be used to check if one property is a
	 * successor of another property.
	 * 
	 * @author Marko Tuominen
	 */
	public class Property {

		private Property parent = null;

		/**
		 * Constructs a property without a parent property.
		 */
		public Property() {
			// nothing to do
		}

		/**
		 * Constructs a property with a parent property.
		 * 
		 * @param parent		the property's parent property
		 */
		public Property( Property parent ) {
			this.parent = parent;
		}

		/**
		 * Test if this property is same as a given property, or its child property.
		 * 
		 * @param property		test if this's the same or a parent property
		 * @return				this property is the same as the given property, or its successor
		 */
		public boolean isSubProperty( Property property ) {
			Property comp = this;

			while (comp != null)
				if (comp.equals( property ))
					return true;
				else
					comp = comp.parent;

			return false;
		}
	}

	/**
	 * A handler object for an object's properties.
	 * 
	 * @author Marko Tuominen
	 */
	public class Properties {

		private Property[] properties = null;

		/**
		 * Constructs a new property handler with zero or more properties.
		 * 
		 * @param properties	the object's properties
		 */
		public Properties( Property... properties ) {
			this.properties = properties == null ? new Property[0] : properties;
		}

		/**
		 * Gets all of an object's properties.
		 * 
		 * @return				the object's properties
		 */
		public Property[] getProperties() {
			return Arrays.copyOf( properties, properties.length );
		}

		/**
		 * Checks if the object has a certain property, including its predecessors.
		 * 
		 * @param property		check if the object has this property
		 * @return				the object has the given property or one of its predecessors
		 */
		public boolean hasProperty( Property property ) {
			for (Property p : properties)
				if (p.isSubProperty( property ))
					return true;

			return false;
		}
	}
}