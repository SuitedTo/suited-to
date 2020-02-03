package models;

import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

public class CompanyTest extends UnitTest {

    //test that users are managed by the company
    @Before
    public void setup() {
        Fixtures.deleteDatabase();
        Fixtures.loadModels("automation-data.yml");
    }

    //@Test
    public void testUserManagement() {
        Company company = new Company("TestCompany").save();

        new User("test@gmail.com", "testPassword", "Test Guy", company).save();

        assertNotNull(User.find("byCompany", company).first());

        company.delete();
    }

    @Test
    public void testAddJob() {
        Company company = new Company("TestCompany").save();

        addJob(company);

        assertNotNull(Job.find("byCompany", company).first());

        company.delete();
    }

    //@Test
    public void testAddInterview() {
        Company company = new Company("TestCompany").save();

        addInterview(company);

        assertNotNull(Interview.find("byCompany", company).first());

        company.delete();
    }

    @Test
    public void testDelete() {
        Fixtures.deleteDatabase();
        Fixtures.loadModels("model-relationship-data.yml");
        Company testSubject = Company.all().first();
        testSubject.delete();
    }

    private static Job addJob(Company company) {

        Job testJob = new Job(company).save();

        company.jobs.add(testJob);

        company.save();

        return testJob;
    }

    private static Interview addInterview(Company company) {

        User user = new User("test@gmail.com", "testPassword", "Test Guy", company).save();

        Interview interview = new Interview(user).save();

        company.interviews.add(interview);

        company.save();

        assertNotNull(interview.find("byUser", user));

        return interview;
    }




}
