package models;

import enums.ActiveInterviewState;
import enums.ExternalLinkType;
import enums.FeedbackSummary;
import enums.PhoneType;
import models.embeddable.ExternalLink;
import models.embeddable.PhoneNumber;
import org.apache.http.impl.cookie.DateUtils;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.junit.matchers.JUnitMatchers.hasItems;

public class CandidateTest extends UnitTest {
    private Company testCompany;
    private User randomUser;

    @Before
    public void setup() {
        Fixtures.deleteDatabase();
        Fixtures.loadModels("data.yml");
        testCompany = Company.all().first();
        randomUser = User.all().first();
    }


    @Test
    public void testCreation() {
        String name = "Bob Test";
        new Candidate(name, testCompany, randomUser).save();

        Candidate result = Candidate.all().first();

        assertThat(result, is(notNullValue()));
        assertThat(result.name, is(name));

    }

    @Test
    public void testEmbedded() {
        String name = "Tom Test";
        Candidate candidate = new Candidate(name, testCompany, randomUser).save();
        String address = "125 Test St";
        candidate.address = address;


        PhoneNumber phoneNumber1 = new PhoneNumber(PhoneType.HOME, "123344555");
        PhoneNumber phoneNumber2 = new PhoneNumber(PhoneType.MOBILE, "1231212321");
        candidate.phoneNumbers.add(phoneNumber1);
        candidate.phoneNumbers.add(phoneNumber2);


        ExternalLink link = new ExternalLink(ExternalLinkType.LINKEDIN, "linkedinprofileurl");
        candidate.externalLinks.add(link);

        candidate.save();

        Candidate result = Candidate.all().first();
        assertThat(result, is(notNullValue()));
        assertThat(result.address, is(address));

        assertThat(result.phoneNumbers, hasItems(phoneNumber1, phoneNumber2));
        //validate the order is preserved
        for (PhoneNumber phoneNumber : result.phoneNumbers) {
            assertThat(phoneNumber, is(phoneNumber1));
            break;//just make sure the first one is correct order
        }

        assertThat(result.externalLinks, hasItem(link));

    }

    @Test
    public void testAddFeedBack() {
        String name = "Bob Smith";
        Date date = new Date();

        Candidate candidate = new Candidate(name, testCompany, randomUser).save();
        User user = new User("test@testcompany.com", "googlerules", "michelle", testCompany).save();
        Interview interview = new Interview(user).save();
        ActiveInterview activeInterview = new ActiveInterview(randomUser, candidate);
        activeInterview.save();
        activeInterview.changeStatus(user, ActiveInterviewState.NOT_STARTED);
        Feedback fb1 = candidate.addFeedback(user, activeInterview.getMagicID(), FeedbackSummary.MAYBE_HIRE, "he may be a good fit!");
        assertTrue(candidate.feedbackList.get(0).summaryChoice.toString().equals(fb1.summaryChoice.toString()));
        assertTrue(candidate.feedbackList.get(0).comments.toString().equals(fb1.comments.toString()));
        assertTrue(candidate.feedbackList.get(0).activeInterview.id.equals(activeInterview.id));
    }

    @Test
    public void testGetPastStartedActiveInterviews() throws Exception {
        String name = "Active Test";
        Date date = new Date();
        Candidate candidate = new Candidate(name, testCompany, randomUser).save();

        //create a couple of ActiveInterviews - the dates are irrelevant for the calculation of "Past Started" so the
        //test dates are set to something that seems contradictory to their statuses to prove that they don't factor in.
        ActiveInterview activeInterview1 = new ActiveInterview(randomUser, candidate);
        activeInterview1.anticipatedDate = DateUtils.parseDate("11/01/2099", new String[]{"MM/dd/yyyy"});
        activeInterview1.save();
        activeInterview1.changeStatus(randomUser, ActiveInterviewState.FINISHED);
        candidate.activeInterviews.add(activeInterview1);

        ActiveInterview activeInterview2 = new ActiveInterview(randomUser, candidate);
        activeInterview2.anticipatedDate = DateUtils.parseDate("11/01/2000", new String[]{"MM/dd/yyyy"});
        activeInterview2.save();
        activeInterview2.changeStatus(randomUser, ActiveInterviewState.NOT_STARTED);
        activeInterview2.name = "dummy name";  //Interview Equals is based on company and name
        candidate.activeInterviews.add(activeInterview2);

        candidate.save();

        Candidate resultCandidate = Candidate.find("byName", name).first();
        List<ActiveInterview> activeInterviewList = resultCandidate.getActiveInterviewsPastStarted();
        assertThat(activeInterviewList, hasItem(activeInterview1));
        assertThat(activeInterviewList, not(hasItem(activeInterview2)));
    }

    @Test
    public void testRemovalOfActiveInterviews(){
        String name = "Active Test";
        Candidate candidate = new Candidate(name, testCompany, randomUser);
        ActiveInterview interview = new ActiveInterview(randomUser, candidate);
        candidate.activeInterviews.add(interview);
        candidate.save();

        Candidate result = Candidate.find("byName", name).first();
        assertThat(result.activeInterviews, is(notNullValue()));
        assertThat(result.activeInterviews.size(), is(1));

        result.activeInterviews.clear();
        result.save();

        Candidate result2 = Candidate.find("byName", name).first();
        assertThat(result2.activeInterviews.size(), is(0));
    }

    @Test
    public void testDelete(){
        Fixtures.deleteDatabase();
        Fixtures.loadModels("model-relationship-data.yml");
        Candidate testSubject = Candidate.all().first();
        testSubject.delete();
    }

}
