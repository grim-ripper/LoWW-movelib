package fi.grimripper.loww.test;

import static org.junit.Assert.assertFalse;
import fi.grimripper.loww.Height;
import fi.grimripper.loww.tiles.Terrain;

public class OnceOnlyTerrain extends Terrain {

	private boolean costRetrieved = false;
	
	public OnceOnlyTerrain( float cost, Height height ) {
		super( cost, height );
	}

	@Override
	public float getCost() {
		assertFalse( costRetrieved );
		costRetrieved = true;
		return super.getCost();
	}
	
	public boolean isCostRetrieved() {
		return costRetrieved;
	}
	
	public void cancelCostRetrieved() {
		costRetrieved = false;
	}
}