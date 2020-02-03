package models;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import enums.QuestionStatus;
import enums.RoleValue;


public class UserTest extends UnitTest {

	private Company testCompany;

	@Before
	public void setup() {
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		testCompany = Company.all().first();
	}

	@Test
	public void createAndRetrieveUser() {
		new User("tom@gmail.com", "testpassword", "Bob", testCompany).save();

		User bob = User.find("byEmail", "tom@gmail.com").first();

		assertThat(bob, is(notNullValue()));
		assertThat(bob.fullName, is("Bob"));
	}

	@Test
	public void createUserWithoutCompany() {
		new User("tom@gmail.com", "testPassword", "Bob Test").save();
		User bob = User.find("byEmail", "tom@gmail.com").first();

		assertThat(bob, is(notNullValue()));
	}


	@Test
	public void testRoles() {
		User user = new User("bob@roleTest.com", "testPassword", "Bob Test");

		user.roles.add(RoleValue.APP_ADMIN);
		user.save();

		User result = User.find("byEmail", "bob@roleTest.com").first();
		assertThat(result.roles, is(notNullValue()));
		assertThat(result.roles.get(0), is(RoleValue.APP_ADMIN));
	}

	@Test
	public void testPublicQuestionAcceptedCount() {
		User user = new User("bob@roleTest.com", "testPassword", "Bob Test");
		user.save();

		//public accepted question
		Question q1 = new Question("dummy", user);
		q1.status = QuestionStatus.ACCEPTED;
		q1.save();

		//public out for review question
		Question q2 = new Question("dummy2", user);
		q2.status = QuestionStatus.OUT_FOR_REVIEW;
		q2.save();

		//private question
		Question pQ = new Question("dummy3", user);
		pQ.status = QuestionStatus.PRIVATE;
		pQ.save();

		assertThat(user.getAcceptedPublicQuestionCount(), is(1L));
	}

	@Test
	public void testPublicQuestionSubmittedCount() {
		new InlineJob(){
			public void doStuff(){
				User user = new User("bob@anotherTest.com", "testPassword", "Bob Test");
				user.save();

				//public out for review question
				Question q1 = new Question("dummy", user).save();
				q1.updateStatus(user, QuestionStatus.OUT_FOR_REVIEW, null);
				q1.save();

				//public question with no workflow should not be included
				Question q2 = new Question("dummy2", user).save();
				q2.status = QuestionStatus.OUT_FOR_REVIEW;
				q2.save();

				//accepted question should be included
				Question q3 = new Question("dummy3", user).save();
				q3.updateStatus(user, QuestionStatus.OUT_FOR_REVIEW, null);
				q3.updateStatus(user, QuestionStatus.ACCEPTED, null);
				q3.save();
			}
		}.invokeAndWait();

		User user = User.findByUsername("bob@anotherTest.com");
		assertThat(user.getPublicQuestionSubmissionCount(), is(2L));
	}

	@Test
	public void calculateStreetCredTest(){
		new InlineJob(){
			public void doStuff(){
				User user = new User("tesguy@test.com", "testPassword", "a guy");
				user.save();

				User user2 = new User("anotherguy@test.com", "testPassword", "another guy");
				user2.save();

				//question with score of 3 created by user
				Question q1 = new Question("dummy", user).save();
				q1.updateStatus(user2, QuestionStatus.ACCEPTED, null);
				q1.totalRating = 3;
				q1.save();

				//question with score of -1 created by user
				Question q2 = new Question("dummy2", user).save();
				q2.updateStatus(user2, QuestionStatus.ACCEPTED, null);
				q2.save();

				//question with score of 8 in which user has participated in review
				Question q3 = new Question("dummy3", user2).save();
				q3.updateStatus(user, QuestionStatus.RETURNED_TO_SUBMITTER, null);
				q3.updateStatus(user2, QuestionStatus.OUT_FOR_REVIEW, null);
				q3.updateStatus(user, QuestionStatus.ACCEPTED, null);
				q3.save();

				//question with score of 10 in which user is the creator and participated in review
				Question q4 = new Question("dummy4", user).save();
				q4.updateStatus(user, QuestionStatus.ACCEPTED, null);
				q4.save();

				//question with score of 10 in which user is the creator and participated in review
				Question q5 = new Question("dummy5", user).save();
				q5.updateStatus(user2, QuestionStatus.ACCEPTED, null);
				q5.flaggedAsInappropriate = true;
				q5.save();

				boolean down = false, up = true;
				rateQuestion(q1, up, 3);
				rateQuestion(q2, down, 1);
				rateQuestion(q3, up, 8);
				rateQuestion(q4, up, 10);
				rateQuestion(q5, up, 10);
			}
		}.invokeAndWait();

		User user = User.findByUsername("tesguy@test.com");
		user.updateStreetCred();
		user.save();
		assertThat(user.streetCred, is(-37L));
	}
	
	private static void rateQuestion(Question q, boolean up, int times){
		for(int i = 0; i < times; ++i){
			String random = UUID.randomUUID().toString();
			User user = new User(random + "@blah.com", "password", random).save();
			QuestionMetadata md = new QuestionMetadata(q, user);
			md.rating = up?1:-1;
			md.save();
			q.updateStandardScore();
		}
	}

	@Test
	public void testStreetCredInitialization(){
		User user = new User("bob@mytest.com", "asdfasdf", "just a guy");
		user.save();

		User result = User.find("byEmail", "bob@mytest.com").first();
		assertThat(result.streetCred, is(0L));
	}

	@Test
	public void testDelete(){
		Fixtures.deleteDatabase();
		Fixtures.loadModels("model-relationship-data.yml");
		User user1 = User.find("byEmail", "admin@suitedto.com").first();
		user1.delete();

		Fixtures.deleteDatabase();
		Fixtures.loadModels("model-relationship-data.yml");
		User user2 = User.find("byEmail", "user2@test.com").first();
		user2.delete();

		Fixtures.deleteDatabase();
		Fixtures.loadModels("model-relationship-data.yml");
		User user3 = User.find("byEmail", "user3@test.com").first();
		user3.delete();


	}
}
