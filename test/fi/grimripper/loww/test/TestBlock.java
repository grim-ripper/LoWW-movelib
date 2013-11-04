package fi.grimripper.loww.test;

import java.util.ArrayList;
import java.util.HashMap;

import fi.grimripper.loww.Height;
import fi.grimripper.loww.movement.MobileObject;
import fi.grimripper.loww.tiles.Block;
import fi.grimripper.loww.tiles.Tile;

public class TestBlock implements Block {

	private Height height = null;
	private float moveCost = 0f;
	
	private HashMap <Tile, Height> tileHeights = new HashMap <>();
	private ArrayList <Tile> blockTemplate = new ArrayList <>();
	
	private int allowTemplateTested = 0;
	
	public TestBlock() {
		this( Height.DEEP, 0f );
	}
	
	public TestBlock( Height height, float moveCost ) {
		this.height = height;
		this.moveCost = moveCost;
	}
	
	@Override
	public Properties getProperties() {
		return null;
	}

	@Override
	public Height getHeight( Tile tile ) {
		return tileHeights.containsKey( tile ) ? tileHeights.get( tile ) : height;
	}

	@Override
	public float modifyMoveCost( Tile from, Tile to, MobileObject moving, Height height,
			float cost ) {
		return cost + moveCost;
	}

	@Override
	public boolean allowsTemplate( Tile tile, MobileObject moving, Height height,
			Tile[] template ) {
		allowTemplateTested++;
		
		for (Tile t : template)
			if (blockTemplate.contains( t ))
				return false;
		
		return true;
	}
	
	public float getMoveCost() {
		return moveCost;
	}
	
	public void setMoveCost( float moveCost ) {
		this.moveCost = moveCost;
	}
	
	public int getAllowTemplateTested() {
		return allowTemplateTested;
	}
	
	public void addTileHeight( Tile tile, Height height ) {
		tileHeights.put( tile, height );
	}
	
	public void addToBlockTemplate( Tile tile ) {
		blockTemplate.add( tile );
	}
}
