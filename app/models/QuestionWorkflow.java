package models;

import javax.persistence.*;

import enums.QuestionStatus;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class QuestionWorkflow extends Workflow implements Comparable {

    @ManyToOne
    @Fetch(FetchMode.SELECT)
    public User user;

    @ManyToOne
    @Fetch(FetchMode.SELECT)
    public Question question;

    @Enumerated(EnumType.STRING)
    public QuestionStatus statusFrom;

    @Enumerated(EnumType.STRING)
    public QuestionStatus status;

    @Column(columnDefinition = "LONGTEXT")
    public String comment;


    public QuestionWorkflow(Question question, User user, QuestionStatus statusFrom,
                            QuestionStatus status, String comment) {
        this.question = question;
        this.user = user;
        this.statusFrom = statusFrom;
        this.status = status;
        this.comment = comment;
    }

    @Override
    public int compareTo(Object o) {
        if (this == o) {
            return 0;
        }

        return this.created.compareTo(((QuestionWorkflow) o).created);
    }
}
