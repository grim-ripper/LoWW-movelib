package fi.grimripper.loww.test;

import fi.grimripper.loww.Height;
import fi.grimripper.loww.movement.MobileObject;
import fi.grimripper.loww.templates.MovementTemplate;

public class TestMobileObject extends MobileObject {

	public TestMobileObject( Height height, MovementTemplate template ) {
		super( height, template );
	}

	public TestMobileObject( Height height, MovementTemplate template, Properties properties ) {
		super( height, template, properties );
	}
	
	@Override
	public float modifyMoveCost( float cost, int impassable ) {
		return impassable;
	}
}
