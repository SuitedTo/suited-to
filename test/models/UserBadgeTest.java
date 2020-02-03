package models;

import java.util.List;

import jobs.UpdateUserMetrics;
import models.Company;
import models.Question;
import models.TestCache;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;

import enums.RoleValue;
import enums.UserStatus;

public class UserBadgeTest extends UnitTest{

	@Before
	public void setup() {
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");

		try {
			new UpdateUserMetrics().doJob();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testReviewerBadge(){

		User nonReviewer = TestCache.getUser("nonReviewer");

		User javaReviewer = TestCache.getUser("javaReviewer");

		nonReviewer.updateBadges();
		assertTrue(nonReviewer.getEarnedBadges(10).size() == 0);

		javaReviewer.updateBadges();
		for(UserBadge badge : javaReviewer.badges){
			if(badge.name.equals("reviewer")){
				assertTrue(badge.multiplier == 1);
				return;
			}
		}
		fail();
	}
}