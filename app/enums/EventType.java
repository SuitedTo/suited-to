package enums;

import newsfeed.publicationstrategy.EverybodyPublicationStrategy;
import newsfeed.publicationstrategy.PublicationStrategy;
import newsfeed.publicationstrategy.UserOnlyPublicationStrategy;
import newsfeed.display.AcceptedQuestionStoryDisplay;
import newsfeed.display.BadgeStoryDisplay;
import newsfeed.display.NewReviewerStoryDisplay;

public enum EventType {
    /**
     * A new badge was awarded
     */
    BADGE(new EverybodyPublicationStrategy(), BadgeStoryDisplay.class),
    /**
     * Something happened within the Question workflow
     */
    QUESTION_WORKFLOW(new EverybodyPublicationStrategy(), AcceptedQuestionStoryDisplay.class),
    /**
     * A question reached some rating threshold
     */
    QUESTION_RATING(new EverybodyPublicationStrategy()),    
    /**
     * The number of questions submitted has reached some threshold.
     */
    QUESTIONS_SUBMITTED(new EverybodyPublicationStrategy()),
    /**
     * Someone just became a reviewer
     */
    REVIEWER(new EverybodyPublicationStrategy(), NewReviewerStoryDisplay.class),
    /**
     * Someone just became a pro interviewer
     */
    PRO_INTERVIEWER(new EverybodyPublicationStrategy()),
    /**
     * Someone just completed an interview
     */
    INTERVIEW_COMPLETED(new EverybodyPublicationStrategy()),
    /**
     * A category just went public
     */
    NEW_PUBLIC_CATEGORY(new UserOnlyPublicationStrategy());

    private PublicationStrategy publicationStrategy;
    private Class defaultStoryDisplay;

    public PublicationStrategy getPublicationStrategy(){
        return publicationStrategy;

    }

    public Class getDefaultStoryDisplayType() {
        return defaultStoryDisplay;
    }

    private EventType(PublicationStrategy publicationStrategy) {
        this(publicationStrategy, null);
    }

    private EventType(PublicationStrategy publicationStrategy, Class defaultStoryDisplay){
        this.publicationStrategy = publicationStrategy;
        this.defaultStoryDisplay = defaultStoryDisplay;
    }
}
