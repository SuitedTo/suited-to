package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * Company specific notes about a Question.
 */
@Entity
@Table(name = "QUESTION_NOTE")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class QuestionNote extends ModelBase {

    /**
     * The Question that this note pertains to.
     */
    @ManyToOne
    @Fetch(FetchMode.SELECT)
    public Question question;

    /**
     * The Company that this note pertains to.
     */
    @ManyToOne
    @Fetch(FetchMode.SELECT)
    public Company company;

    /**
     * Text value of the note.
     */
    @Column(columnDefinition = "LONGTEXT")
    public String text;
}
