package fi.grimripper.loww.tiles;

import static fi.grimripper.loww.Height.HIGH;
import static fi.grimripper.loww.Height.VERY_HIGH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import fi.grimripper.loww.AdditionalProperties.Properties;
import fi.grimripper.loww.tiles.Hex;
import fi.grimripper.loww.tiles.Terrain;
import fi.grimripper.loww.tiles.Tile;

public class TerrainTest {

	@Test
	public void testTerrain() {
		// terrain has a move cost, height and extra properties
		Terrain first = new Terrain( 100f );
		assertEquals( 100f, first.getCost(), 0f );
		
		Terrain second = new Terrain( 50f, HIGH );
		assertEquals( 50f, second.getCost(), 0f );
		assertSame( HIGH, second.getHeight() );
		
		Properties props = new Properties();
		Terrain third = new Terrain( 25f, VERY_HIGH, props );
		assertEquals( 25f, third.getCost(), 0f );
		assertSame( VERY_HIGH, third.getHeight() );
		assertSame( props, third.getProperties() );
		
		// add and remove terrain to a hex, although this does nothing
		Tile tile = new Hex( 0, 0, 0, 0, 0, 0 );
		first.setToTile( tile );
		first.removeFromTile( tile );
	}
}
