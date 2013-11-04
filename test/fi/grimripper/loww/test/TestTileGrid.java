package fi.grimripper.loww.test;

import fi.grimripper.loww.tiles.Tile;
import fi.grimripper.loww.tiles.TileGrid;

public class TestTileGrid extends TileGrid <Tile> {

	public TestTileGrid( double width, double height ) {
		super( width, height );
	}

	@Override
	public int getTotalWidth() {
		return 0;
	}

	@Override
	public int getTotalHeight() {
		return 0;
	}

	@Override
	public int getTileCount() {
		return 0;
	}

	@Override
	public Tile getTileAtXY( double x, double y ) {
		return null;
	}

	@Override
	public Tile getTileAtRC( int row, int col ) {
		return null;
	}

	@Override
	public Tile[][] getTiles() {
		return null;
	}

	@Override
	public fi.grimripper.loww.tiles.TileGrid.LineHelper <Tile> createLineHelper( Tile from,
			Tile to ) {
		return null;
	}
}
