package models;

import javax.persistence.*;

import enums.QuestionRating;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.List;


@Entity(name = "INTERVIEW_QUESTION")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class InterviewQuestion extends ModelBase implements Comparable {

    @ManyToOne
    @JoinColumn(name = "question_id")
    public Question question;

    @ManyToOne
    @JoinColumn(name = "interview_id")
    public Interview interview;

    @Enumerated(EnumType.STRING)
    public QuestionRating rating;

    public String comment = "";

    public Integer sortOrder;

    public int compareTo(Object o) {
        if (this == o) {
            return 0;
        }

        return this.sortOrder.compareTo(((InterviewQuestion) o).sortOrder);

    }

    public void setRating(QuestionRating rating) {
        this.rating = rating;

        if (interview instanceof ActiveInterview) {
            ActiveInterview activeInterview = (ActiveInterview) interview;
            List<InterviewQuestion> interviewQuestions = activeInterview.interviewQuestions;

            if (interviewQuestions != null) {
                int sum = 0;
                int count = 0;
                for (InterviewQuestion interviewQuestion : interviewQuestions) {
                    if (interviewQuestion.rating != null) {
                        sum += interviewQuestion.rating.toInteger();
                        count++;
                    }
                }

                activeInterview.averageQuestionRating = count > 0 ? (double)sum/count : 0;
                activeInterview.save();
            }
        }
    }
}
