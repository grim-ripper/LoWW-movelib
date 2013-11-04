package fi.grimripper.loww;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import fi.grimripper.loww.movement.MovementTests;
import fi.grimripper.loww.templates.TemplateTests;
import fi.grimripper.loww.tiles.TileTests;

@RunWith(Suite.class)
@SuiteClasses({ MiscTests.class, TemplateTests.class, TileTests.class, MovementTests.class })
public class AllTests {

}
