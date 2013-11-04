package fi.grimripper.loww.tiles;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ TerrainTest.class, ObstacleTest.class,
	TileTest.class, HexTest.class, SquareTest.class,
	TileGridTest.class, FilledRowHexGridTest.class, FilledSquareGridTest.class })
public class TileTests {

}
