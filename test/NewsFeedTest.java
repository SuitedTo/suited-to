import enums.EventType;
import models.Company;
import models.Event;
import models.Story;
import models.User;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;
import utils.NewsFeed;

import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.matchers.JUnitMatchers.hasItem;

public class NewsFeedTest extends UnitTest {

    private List<Event> allEvents;

    @Before
    public void setup(){
        Fixtures.deleteAllModels();
        Fixtures.loadModels("newsFeedData.yml");

       allEvents = Event.findAll();
    }

//    @Test
    public void getBasicUserNewsFeed(){
        User testUser = User.find("byEmail", "user2@test.com").first();

        //create two stories at roughly the same time and ensure that the one with greater affinity is returned first
        Story story1 = new Story();
        story1.user = testUser;
        story1.affinity = .5;
        story1.weight = .5;
        story1.save();

        Story story2 = new Story();
        story2.user = testUser;
        story2.affinity = 1.0;
        story2.weight = .5;
        story2.save();

        List<Story> newsFeed = NewsFeed.getNewsFeed(testUser);
        assertThat(newsFeed, is(notNullValue()));
        assertThat(newsFeed.size(), is(2));
        assertThat(newsFeed.get(0), is(story2));
    }

//    @Test
    public void getConsolidatedNewsFeed(){
        User testUser = User.find("byEmail", "user2@test.com").first();
        Company testCompany = testUser.company;

        //create two stories at roughly the same time and ensure that the one with greater affinity is returned first
        Story story1 = new Story();
        story1.user = testUser;
        story1.save();

        Story story2 = new Story();
        story2.company = testCompany;
        story2.save();

        Story story3 = new Story();
        story3.allUsers = true;
        story3.save();

        List<Story> newsFeed = NewsFeed.getNewsFeed(testUser);
        assertThat(newsFeed, is(notNullValue()));
        assertThat(newsFeed.size(), is(3));
    }

//    @Test
    public void getConsolidatedNewsFeedForUserWithNoCompany(){
        User testUser = User.find("byEmail", "user4@test.com").first();
        Company testCompany = Company.all().first();

        //create two stories at roughly the same time and ensure that the one with greater affinity is returned first
        Story story1 = new Story();
        story1.user = testUser;
        story1.save();

        Story story2 = new Story();
        story2.company = testCompany;
        story2.save();

        Story story3 = new Story();
        story3.allUsers = true;
        story3.save();

        List<Story> newsFeed = NewsFeed.getNewsFeed(testUser);
        assertThat(newsFeed, is(notNullValue()));
        assertThat(newsFeed.size(), is(2));
        assertThat(newsFeed, hasItem(story1));
        assertThat(newsFeed, hasItem(story3));
    }

//    @Test
    public void testStoryWeights(){
        User testUser = User.find("byEmail", "user2@test.com").first();

        //create two stories at roughly the same time and affinity and ensure that the one with greater weight is returned first
        Story story1 = new Story();
        story1.user = testUser;
        story1.affinity = .5;
        story1.weight = .5;
        story1.save();

        Story story2 = new Story();
        story2.user = testUser;
        story2.affinity = .5;
        story2.weight = .99;
        story2.save();

        List<Story> newsFeed = NewsFeed.getNewsFeed(testUser);
        assertThat(newsFeed, is(notNullValue()));
        assertThat(newsFeed.size(), is(2));
        assertThat(newsFeed.get(0), is(story2));
    }

    @Test
    public void testNoDuplicates(){
        User testUser = User.find("byEmail", "user2@test.com").first();
        Event event = new Event().save();

        Story story1 = new Story();
        story1.user = testUser;
        story1.affinity = .99;
        story1.weight = .5;
        story1.event = event;
        story1.save();

        Story story2 = new Story();
        story2.user = testUser;
        story2.affinity = .5;
        story2.weight = .5;
        story2.event = event;
        story2.save();

        List<Story> newsFeed = NewsFeed.getNewsFeed(testUser);
        assertThat(newsFeed, is(notNullValue()));
        assertThat(newsFeed.size(), is(1));
        assertThat(newsFeed, hasItem(story1));

    }

//    @Test(expected = IllegalStateException.class)
    public void testDetermineAffinityException(){
        Story testStory = new Story();
        NewsFeed.determineAffinity(testStory); //user, company, and allUsers are null/false expected IllegalStateException
    }

//    @Test
    public void testDetermineAffinity(){
        Story testStory = new Story();
        testStory.user = new User();

        Double result = NewsFeed.determineAffinity(testStory);
        assertThat(result, is(NewsFeed.affinityMap.get(NewsFeed.AFFINITY_USER)));

        testStory.user = null;
        testStory.company = new Company();
        result = NewsFeed.determineAffinity(testStory);
        assertThat(result, is(NewsFeed.affinityMap.get(NewsFeed.AFFINITY_COMPANY)));

        testStory.company = null;
        testStory.allUsers = true;
        result = NewsFeed.determineAffinity(testStory);
        assertThat(result, is(NewsFeed.affinityMap.get(NewsFeed.AFFINITY_ALL)));

    }

//    @Test
    public void testDetermineWeightException(){
        Event testEvent = new Event();
        Double result = NewsFeed.determineWeight(testEvent);
        assertThat(result, is(NewsFeed.weightMap.get(NewsFeed.WEIGHT_DEFAULT)));

        testEvent.eventType = EventType.BADGE;

        result = NewsFeed.determineWeight(testEvent);
        assertThat(result, is(NewsFeed.weightMap.get(EventType.BADGE.name())));

    }


}
