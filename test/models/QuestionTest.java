package models;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.Transient;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import play.utils.Java;
import utils.XLSUtil;
import enums.Difficulty;
import enums.QuestionStatus;
import enums.RoleValue;
import enums.Timing;

public class QuestionTest extends UnitTest {

	private Company testCompany;
	private User user;

	@Before
	public void setup() {
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		testCompany = Company.all().first();
		user = User.all().first();
	}

	/**
	 * Determines whether a field looks like a persistable entity field
	 * that belongs directly to the given class.
	 *
	 * @param field The field to be checked.
	 * @return True if the given field is public and not transient and not
	 *         static and not synthetic and not declared by a super class.
	 */
	private static boolean isEntityField(final Field field, final Class clazz) {
		boolean transientField = false;
		Annotation[] annotations = field.getDeclaredAnnotations();
		for (Annotation annotation : annotations) {
			if (annotation instanceof Transient) {
				transientField = true;
				break;
			}
		}


		int modifiers = field.getModifiers();
		return ((modifiers & Modifier.PUBLIC) == Modifier.PUBLIC) &&
				!transientField &&
				((modifiers & Modifier.STATIC) != Modifier.STATIC) &&
				!field.isSynthetic() &&
				field.getDeclaringClass().equals(clazz);
	}

	@Test
	public void testUploadFields() {
		Set<String> ignore = new LinkedHashSet<String>();
		try {
			//Here you need to explicitly ignore any field in the questions
			//class that you don't expect to pull in from a spreadsheet
			ignore.add("interviewQuestionList");
			ignore.add("status");
			ignore.add("workflows");
			ignore.add("comments");
			ignore.add("metrics");
			ignore.add("standardScore");
			ignore.add("duplication");
			ignore.add("questionNotes");
			ignore.add("active");
			ignore.add("flaggedAsInappropriate");
			ignore.add("flaggedReason");
			ignore.add("metadata");
            ignore.add("totalRating");
            ignore.add("totalInterviews");
            ignore.add("interviewPoints");
            ignore.add("prepAnswers");
            ignore.add("excludeFromPrep");


			Set<Field> questionFields = new LinkedHashSet<Field>();
			Java.findAllFields(Question.class, questionFields);
			Set<Field> xlsFields = new LinkedHashSet<Field>();
			Java.findAllFields(XLSUtil.XLSQuestionIn.class, xlsFields);

			Set<String> xlsFieldNames = new LinkedHashSet<String>();
			for (Field field : xlsFields) {
				xlsFieldNames.add(field.getName());

			}
			for (Field field : questionFields) {
				if (!ignore.contains(field.getName()) && isEntityField(field, Question.class)) {
					assertTrue("Question field [" + field.getName() + "] will not be recognized by the upload utility",
							xlsFieldNames.contains(field.getName()));

				}
			}
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	@Test
	public void testCreateAndFind() {
		new Question("This is just a sample Question", user).save();

		Question result = Question.find("byText", "This is just a sample Question").first();

		assertThat(result, is(notNullValue()));
		assertThat(result.text, is("This is just a sample Question"));
	}

	@Test
	public void testCategories() {
		Question question = new Question("This is another sample Question", user);
		Category category = new Category("test").save();
		question.category = category;
		question.save();

		Question result = Question.find("byText", "This is another sample Question").first();
		assertThat(result, is(notNullValue()));

		assertThat(result.category, is(category));
	}

	@Test
	public void hasAccessTestNullUser() {
		Question question = new Question();
		boolean result = question.hasAccess(null);
		assertThat(result, is(false));
	}

	@Test
	public void hasAccessPublicQuestion() {
		Question question = new Question();
		question.status = QuestionStatus.ACCEPTED;
		User user = new User();
		boolean result = question.hasAccess(user);
		assertThat(result, is(true));
	}

	@Test
	public void hasAccessSameUser() {
		User user = new User();
		Question question = new Question();
		question.user = user;
		question.status = QuestionStatus.PRIVATE;

		boolean result = question.hasAccess(user);
		assertThat(result, is(true));
	}

	@Test
	public void hasAccessPrivateQuestionSameCompany() {
		User user = new User();
		User user2 = new User();
		user.company = testCompany;
		user2.company = testCompany;
		Question question = new Question();
		question.user = user;
		question.status = QuestionStatus.PRIVATE;

		boolean result = question.hasAccess(user2);
		assertThat(result, is(true));
	}


	@Test
	public void canEditTestNullUser() {
		Question question = new Question();
		boolean result = question.canEdit(null);
		assertThat(result, is(false));
	}

	@Test
	public void canEditTestNoAccess() {
		User user = new User();
		User anotherUser = new User();
		Question question = new Question();
		question.status = QuestionStatus.PRIVATE;
		question.user = anotherUser;
		boolean result = question.canEdit(user);
		assertThat(result, is(false));
	}

	@Test
	public void canEditSameUser() {
		User user = new User();
		Question question = new Question();
		question.user = user;
		boolean result = question.canEdit(user);
		assertThat(result, is(true));
	}

	@Test
	public void canEditCompanyAdmin() {
		User user = new User();
		user.company = testCompany;
		User admin = new User();
		admin.company = testCompany;
		admin.roles.add(RoleValue.COMPANY_ADMIN);

		Question question = new Question();
		question.user = user;
		boolean result = question.canEdit(admin);
		assertThat(result, is(true));

	}

	@Test
	public void canEditNullCompanyForQuestionOwner() {
		User user = new User();
		User admin = new User();
		admin.company = testCompany;
		admin.roles.add(RoleValue.COMPANY_ADMIN);

		Question question = new Question();
		question.user = user;
		boolean result = question.canEdit(admin);
		assertThat(result, is(false));
	}

	@Test
	public void canEditAppAdmin() {
		User admin = new User();
		admin.roles.add(RoleValue.APP_ADMIN);

		Question question = new Question();
		question.user = user;
		boolean result = question.canEdit(admin);
		assertThat(result, is(true));
	}

	@Test
	public void canEditInterviewQuestionsForSameCompanyOnly() {
		User user = new User();
		Question question = new Question();
		question.user = user;
		user.company = testCompany;

		Interview interview = new Interview(new User());
		interview.company = testCompany;
		InterviewQuestion interviewQuestion = new InterviewQuestion();
		interviewQuestion.interview = interview;
		interview.interviewQuestions.add(interviewQuestion);
		interviewQuestion.question = question;
		question.interviewQuestionList.add(interviewQuestion);

		boolean result = question.canEdit(user);
		assertThat(result, is(true));
	}

	@Test
	public void canEditInterviewQuestionsForDifferentCompany() {
		User user = new User();
		Question question = new Question();
		question.user = user;
		user.company = testCompany;

		Interview interview = new Interview(new User());
		interview.company = new Company("test2");
		InterviewQuestion interviewQuestion = new InterviewQuestion();
		interviewQuestion.interview = interview;
		interview.interviewQuestions.add(interviewQuestion);
		interviewQuestion.question = question;
		question.interviewQuestionList.add(interviewQuestion);

		boolean result = question.canEdit(user);
		assertThat(result, is(false));
	}

	@Test
	public void canEditInterviewQuestionsOwnerIsLoner() {
		User user = new User();
		Question question = new Question();
		question.user = user;

		Interview interview = new Interview(new User());
		interview.company = testCompany;
		InterviewQuestion interviewQuestion = new InterviewQuestion();
		interviewQuestion.interview = interview;
		interview.interviewQuestions.add(interviewQuestion);
		interviewQuestion.question = question;
		question.interviewQuestionList.add(interviewQuestion);

		boolean result = question.canEdit(user);
		assertThat(result, is(false));
	}

	@Test
	public void canReviewTestNullUser() {
		Question question = new Question();
		boolean result = question.canReview(null);
		assertThat(result, is(false));
	}

	@Test
	public void canReviewNoAccess() {
		User user = new User();
		User anotherUser = new User();
		Question question = new Question();
		question.status = QuestionStatus.PRIVATE;
		question.user = anotherUser;
		boolean result = question.canReview(user);
		assertThat(result, is(false));
	}

	@Test
	public void canReviewTestWorkflowStatus() {
		User user = new User();
		Question question = new Question();
		question.user = user;
		question.status = QuestionStatus.ACCEPTED;
		boolean result = question.canReview(user);
		assertThat(result, is(false));
	}

	@Test
	public void canReviewTestSuperReviewer() {
		User user = new User();
		user.superReviewer = true;
		Question question = new Question();
		question.status = QuestionStatus.OUT_FOR_REVIEW;
		boolean result = question.canReview(user);
		assertThat(result, is(true));
	}

	@Test
	public void canReviewTestQuestionOwner() {
		User user = new User();
		user.superReviewer = false;
		Question question = new Question();
		question.user = user;
		question.status = QuestionStatus.OUT_FOR_REVIEW;
		boolean result = question.canReview(user);
		assertThat(result, is(false));
	}

	@Test
	public void canReviewTestCategories() {
		Category category1 = new Category("category1");
		Category category2 = new Category("category2");
		Category category3 = new Category("category3");
		Question question = new Question();
		question.status = QuestionStatus.OUT_FOR_REVIEW;
		question.category = category1;
		User user1 = new User();
		user1.reviewCategories.add(category1);
		assertThat(question.canReview(user1), is(true));

		User user2 = new User();
		user2.reviewCategories.add(category2);
		assertThat(question.canReview(user2), is(false));

		User user3 = new User();
		user3.reviewCategories.add(category1);
		user3.reviewCategories.add(category2);
		user3.reviewCategories.add(category3);
		assertThat(question.canReview(user3), is(true));
	}

	@Test
	public void updateWorkFlowTestNoPreviousWorkflow() {
		new InlineJob(){
			public void doStuff(){
				Question question = new Question("updateWorkFlowTestNoPreviousWorkflow", user).save();
				question.updateStatus(user, QuestionStatus.ACCEPTED, null);
				question.save();
			}
		}.invokeAndWait();


		Question question = Question.findFirst("byText", "updateWorkFlowTestNoPreviousWorkflow");
		assertThat(question.workflows.size(), is(1));
	}

	@Test
	public void testDuplication() {
		Question question1 = new Question("", user);
		question1.save();

		Question question2 = new Question("", user);
		question2.save();

		Question question3 = new Question("", user);
		question3.save();

		question1.setDuplicateOf(question2);

		assertTrue(question1.getDuplicates().contains(question2));

		question2.setDuplicateOf(question3);

		assertTrue(question1.getDuplicates().contains(question3));

		question3.duplication = null;

		question3.save();

		assertTrue(!question1.getDuplicates().contains(question3));

	}

	@Test
	public void getTimeOfSubmissionTest() {
		new InlineJob(){
			public void doStuff(){
				//make this a private question.  if it was ever a public candidate we are expecting a result
				Question question = new Question("gobbldeygook", user).save();
				question.status = QuestionStatus.OUT_FOR_REVIEW;
				question.save();
			}
		}.invokeAndWait();


		Question question = Question.findFirst("byText", "gobbldeygook");
		assertThat(question.getTimeOfSubmission(), is(notNullValue()));
		Date testDate = new Date();
		assertTrue(question.getTimeOfSubmission().before(testDate));

		new InlineJob(){
			public void doStuff(){
				final Question question = Question.findFirst("byText", "gobbldeygook");
				question.updateStatus(user, QuestionStatus.WITHDRAWN, null);
				question.save();
			}
		}.invokeAndWait();

		question = Question.findFirst("byText", "gobbldeygook");
		assertTrue(question.getTimeOfSubmission().before(testDate));
	}


	@Test
	public void testInitPublicWorkflow(){
		new InlineJob(){
			public void doStuff(){
				Question testSubject = new Question("BOO!", user).save();
				testSubject.initPublicWorkflow(null);
				testSubject.save();
			}
		}.invokeAndWait();


		Question question = Question.findFirst("byText", "BOO!");
		assertThat(question.status, is(QuestionStatus.AWAITING_CATEGORY));
		assertThat(question.workflows, is(notNullValue()));
		assertThat(question.workflows.size(), is(1));
		assertThat(question.workflows.get(0).comment, is(QuestionStatus.AWAITING_CATEGORY.toString()));
	}

	@Test
	public void testInitPublicWorkflowAwaitingCompletion(){
		new InlineJob(){
			public void doStuff(){
				Question testSubject = new Question("BAM!", user);
				testSubject.category = new Category("A_CATEGORY").save();
				testSubject.initPublicWorkflow(null);
				testSubject.save();

			}
		}.invokeAndWait();


		Question question = Question.findFirst("byText", "BAM!");
		assertThat(question.status, is(QuestionStatus.AWAITING_COMPLETION));
		assertThat(question.workflows, is(notNullValue()));
		assertThat(question.workflows.size(), is(1));
		assertThat(question.workflows.get(0).comment, is(QuestionStatus.AWAITING_COMPLETION.toString()));


	}

	@Test
	public void testInitPublicWorkflowOutForReview(){
		new InlineJob(){
			public void doStuff(){
				Question testSubject = new Question();
				testSubject.text = "SPLAT!";
				testSubject.answers = "or another";
				testSubject.time = Timing.MEDIUM;
				testSubject.difficulty = Difficulty.EASY;
				testSubject.user = user;
				testSubject.save();
				testSubject.category = new Category("A_CATEGORY").save();
				testSubject.initPublicWorkflow(null);
				testSubject.save();
			}
		}.invokeAndWait();
		Question question = Question.findFirst("byText", "SPLAT!");
		assertThat(question.status, is(QuestionStatus.OUT_FOR_REVIEW));
		assertThat(question.workflows, is(notNullValue()));
		assertThat(question.workflows.size(), is(1));
		assertThat(question.workflows.get(0).comment, is(QuestionStatus.OUT_FOR_REVIEW.toString()));
	}


}
