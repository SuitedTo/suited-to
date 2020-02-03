package utils;

import enums.EventType;
import models.Event;
import models.Story;
import models.User;
import play.Play;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewsFeed {
    public static final Map<String, Double> affinityMap = new HashMap<String, Double>();
    public static final Map<String, Double> weightMap = new HashMap<String, Double>();
    public static final String AFFINITY_USER = "affinity.user";
    public static final String AFFINITY_COMPANY = "affinity.company";
    public static final String AFFINITY_ALL = "affinity.all";
    public static final String DEGRADATION_FACTOR = "degradation.factor";
    public static final String WEIGHT_DEFAULT = "weight.default";
    private static final int MAX_STORIES_DEFAULT = Integer.MAX_VALUE;//10;
    private static final String USER_QUERY_PART = "%s.user is not null and %s.user = :user";
    private static final String COMPANY_QUERY_PART = "%s.company is not null and %s.company = :company";
    private static final String COMMUNITY_ALL_QUERY_PART = "%s.allUsers = true";
    private static final String NON_COMMUNITY_ALL_QUERY_PART = "(%s.allUsers = true and %s.event is null)";




    static {
        affinityMap.put(AFFINITY_USER, Double.valueOf(Play.configuration.getProperty(AFFINITY_USER)));
        affinityMap.put(AFFINITY_COMPANY, Double.valueOf(Play.configuration.getProperty(AFFINITY_COMPANY)));
        affinityMap.put(AFFINITY_ALL, Double.valueOf(Play.configuration.getProperty(AFFINITY_ALL)));

        weightMap.put(WEIGHT_DEFAULT, Double.valueOf(Play.configuration.getProperty(WEIGHT_DEFAULT)));
        for (EventType eventType : EventType.values()) {
            weightMap.put(eventType.name(), Double.valueOf(Play.configuration.getProperty("weight."+eventType.name())));
        }
    }



    public static final long millisecondsPerSecond = 1000;
    public static final Double timeDegradationFactor = Double.valueOf(Play.configuration.getProperty(DEGRADATION_FACTOR));


    /**
     * Gets an ordered list of Stories, otherwise known as the news feed, for the given user restricted by the given
     * page and max values.  The ordering of the stories is defines by w * a * t where:
     * w = weight factor for the story
     * a = affinity of the story
     * t = function of the time since the story was created
     * stories with a higher result of the w * a * t will appear first in the list.  If multiple stories are found
     * that are associated with the same event only the story with the higher score will be returned
     * @param user User to build the newsfeed for
     * @param page the Page number for the results
     * @param max the maximum number of results per page
     * @return ordered List of Stories
     */
    public static List<Story> getNewsFeed(User user, int page, int max){
        return getNewsFeed(user, page, max, false);
    }

    public static List<Story> getNewsFeed(User user, int page, int max, boolean onlyStoriesForUser){
        String queryString = "select s from Story s ";
        String whereBase;
        boolean isUserCommunityMember = user.isCommunityMember();
        if(onlyStoriesForUser){
            whereBase = "where " + getUserQueryPart("s") + " ";
        } else {
            whereBase = "where ((" + getUserQueryPart("s") + ") or (" + getCompanyQueryPart("s") + ") or " + getAllQueryPart("s", isUserCommunityMember) + ") ";
        }

        queryString = queryString + whereBase +
                "and (s.event is null or ((s.weight * s.affinity * POWER(" + timeDegradationFactor + ", (:now - s.creationTimeInMillis)/:millisInSecond)) = " +
                "(select max(ss.weight * ss.affinity * POWER(" + timeDegradationFactor + ", (:now - ss.creationTimeInMillis)/:millisInSecond)) " +
                "from Story ss where ss.event = s.event and (("+ getUserQueryPart("ss") + ") or (" + getCompanyQueryPart("ss") + ") or " + getAllQueryPart("ss", isUserCommunityMember) + ")))) " +
                "order by s.weight * s.affinity * POWER(" + timeDegradationFactor + ", (:now - s.creationTimeInMillis)/:millisInSecond) desc";

        return Story.find(queryString)
                .setParameter("user", user)
                .setParameter("company", user.company)
                .setParameter("now", new Date().getTime())
                .setParameter("millisInSecond", millisecondsPerSecond)
                .fetch(page, max);
    }

    private static String getUserQueryPart(String name){
        return USER_QUERY_PART.replace("%s", name);
    }

    private static String getCompanyQueryPart(String name){
        return COMPANY_QUERY_PART.replace("%s", name);
    }

    private static String getAllQueryPart(String name, boolean isCommunityMember){
        if(isCommunityMember){
            return COMMUNITY_ALL_QUERY_PART.replace("%s", name);
        } else {
            return NON_COMMUNITY_ALL_QUERY_PART.replace("%s", name);
        }
    }

    /**
     * Overloaded version of getNewsFeed that uses default values for page and max of 1 and whatever is defined as the
     * MAX_STORIES_DEFAULT. so if MAX_STORIES_DEFAULT is 10 this will return up to the first 10 values for the user's
     * news feed
     * @param user User to build the newsfeed for
     * @return ordered List of Stories
     */
    public static List<Story> getNewsFeed(User user){
        return getNewsFeed(user, 1, MAX_STORIES_DEFAULT);
    }

    /**
     * Calculates the affinity score for this story.
     * @param story
     * @return
     */
    public static Double determineAffinity(Story story){
        if(story == null){
            throw new IllegalArgumentException("NewsFeed.determineAffinity argument story cannot be null");
        }

        if(story.user != null){
            return affinityMap.get(AFFINITY_USER);
        }

        if(story.company != null){
            return affinityMap.get(AFFINITY_COMPANY);
        }

        if(story.allUsers){
            return affinityMap.get(AFFINITY_ALL);
        }

        throw new IllegalStateException("NewsFeed.determineAffinity unable to determine affinity for story " + story +
                " no user or company defined and allUsers is not set to true");
    }

    public static Double determineWeight(Event event){
        //in the case that the event is null just return the default value;
        if(event == null){
            return weightMap.get(WEIGHT_DEFAULT);
        }

        String eventTypeName = event.eventType != null ? event.eventType.name() : null;
        if(eventTypeName == null){
            return weightMap.get(WEIGHT_DEFAULT);
        }

        Double value = weightMap.get(eventTypeName);
        if(value != null){
            return value;
        }

        throw new IllegalStateException("NewsFeed.determineWeight unable to determine weight for event " + event);

    }
}
