package models;

import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

public class ActiveInterviewStateChangeTest extends UnitTest {

    @Test
    public void testDelete(){
        Fixtures.deleteDatabase();
        Fixtures.loadModels("model-relationship-data.yml");
        ActiveInterviewStateChange testEntity = ActiveInterviewStateChange.all().first();
        testEntity.delete();
    }
}
