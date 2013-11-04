package fi.grimripper.loww;

/**
 * Enumeration for categorized heights.
 * 
 * @author Marko Tuominen
 */
public enum Height {

	/**
	 * A deep object is a pit or something like it, descending far below the surface represented by
	 * a tile grid. An example of a deep object is an ocean.
	 */
	DEEP,
	
	/**
	 * A shallow object is a hollow with its bottom below the surface represented by the tile grid.
	 * It's not deep enough to cause any danger to a mobile object, but is likely to hinder
	 * movement anyway.
	 */
	SHALLOW,

	/**
	 * Flat is on the level with the surface represented by the tile grid, or fairly close.
	 */
	FLAT,

	/**
	 * Low is something a bit higher than the surface represented by the tile grid. A point of
	 * comparison is a human.
	 */
	LOW,

	/**
	 * Somewhat higher than low. There can be for example high trees, high rocks and so on.
	 */
	HIGH,

	/**
	 * The highest obstacles which still fit into the space above the tile grid.
	 */
	VERY_HIGH,

	/**
	 * Blocking obstacles reach far above the tile grid, or possibly connect with its ceiling.
	 */
	BLOCKING;
	
	private static Height[] values = values();

	/**
	 * Gets a height object for an ordinal.
	 * 
	 * @param ordinal		ordinal for height
	 * @return				height for ordinal
	 */
	public static Height getHeight( int ordinal ) {
		return ordinal >= values.length ? BLOCKING : ordinal < 0 ? DEEP : values[ ordinal ];
	}

	/**
	 * Compares two height objects, accepting <code>null</code> as parameter. It's considered lower
	 * than any actual height.
	 * 
	 * @param first			compare this height to the other
	 * @param second		compare this height to the other
	 * @return				negative if the first height is less than the second, zero if they are
	 * 						equal or positive if the first height is greater than the second
	 */
	public static int compareHeights( Height first, Height second ) {
		return first == null && second == null ? 0 :
			first == null ? -1 : second == null ? 1 : first.compareTo( second );
	}

	/**
	 * Gets the higher of two heights. Handles <code>null</code>s as well, and treats them as lower
	 * than any actual height.
	 * 
	 * @param h1			first height
	 * @param h2			second height
	 * @return				the higher of the two heights
	 */
	public static Height max( Height h1, Height h2 ) {
		return h1 == null ? h2 : h2 == null || h1.compareTo( h2 ) >= 0 ? h1 : h2;
	}
	
	/**
	 * Gets the lower of two heights. Handles <code>null</code>s as well, and treats them as higher
	 * than any actual height.
	 * 
	 * @param h1			first height
	 * @param h2			second height
	 * @return				the lower of the two heights
	 */
	public static Height min( Height h1, Height h2 ) {
		return h1 == null ? h2 : h2 == null || h1.compareTo( h2 ) <= 0 ? h1 : h2;
	}
}
