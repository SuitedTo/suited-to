package models;

import enums.QuestionStatus;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.matchers.JUnitMatchers.hasItem;

public class CategoryTest extends UnitTest {
    private Company testCompany;
    private User user;

    @Before
    public void setup() {
        Fixtures.deleteDatabase();
        Fixtures.loadModels("data.yml");
        testCompany = Company.all().first();
        user = User.all().first();
    }

    @Test
    public void testQuestionCountUpdatesForDeletes() {
        Category category1 = new Category("test");
        category1.questionCount = 1;
        category1.save();
        Question question = new Question();
        question.user = user;
        question.category = category1;
        question.categoryAsLoaded = category1;
        question.save();

        category1.updateQuestionCount();

        assertThat(category1.questionCount, is(0L));
    }

    @Test
    public void testQuestionCountUpdatesForModification() {
        Category category1 = new Category("test");
        category1.questionCount = 1;
        category1.save();
        Category category2 = new Category("test2");
        category2.save();
        Question question = new Question();
        question.user = user;
        question.active = true;
        question.activeAsLoaded = true;
        question.category = category2;
        question.categoryAsLoaded = category1;
        question.status = QuestionStatus.ACCEPTED;
        question.statusAsLoaded = QuestionStatus.ACCEPTED;
        question.save();
        category1.updateQuestionCount();
        category2.updateQuestionCount();

        assertThat(category1.questionCount, is(0L));
        assertThat(category2.questionCount, is(1L));
    }

    @Test
    public void testQuestionCountUpdatesForDeactivate() {
        Category category1 = new Category("test");
        category1.questionCount = 1;
        category1.save();
        Question question = new Question();
        question.user = user;
        question.category = category1;
        question.categoryAsLoaded = category1;
        question.activeAsLoaded = true;
        question.active = false;
        question.save();
        category1.updateQuestionCount();

        assertThat(category1.questionCount, is(0L));
    }

    @Test
    public void testCategoryConsolidation() {
        Category category1 = new Category("cat1");
        category1.questionCount = 1;
        category1.save();
        Category category2 = new Category("cat2").save();
        category2.save();

        Question question1 = new Question("test1", user);
        question1.categoryAsLoaded = category1;
        question1.category = category1;
        question1.active = true;
        question1.activeAsLoaded = true;
        question1.status = QuestionStatus.ACCEPTED;
        question1.statusAsLoaded = QuestionStatus.ACCEPTED;

        question1.save();

        user.reviewCategories.add(category1);
        user.save();

        Category.consolidateCategories(category2, category1);
        category1.updateQuestionCount();
        category2.updateQuestionCount();
        assertThat(category1.questionCount, is(0L));
        assertThat(category2.questionCount, is(1L));
        assertThat(question1.category, is(category2));
        assertThat(question1.category, not(category1));
        assertThat(user.reviewCategories, hasItem(category2));
        assertThat(user.reviewCategories, not(hasItem(category1)));
    }
}
