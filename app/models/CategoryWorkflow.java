package models;

import javax.persistence.*;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.ArrayList;
import java.util.List;

@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class CategoryWorkflow extends Workflow implements Comparable {

    @ManyToOne
    @Fetch(FetchMode.SELECT)
    public Category category;

    @Enumerated(EnumType.STRING)
    public Category.CategoryStatus statusFrom;

    @Enumerated(EnumType.STRING)
    public Category.CategoryStatus status;

    public CategoryWorkflow(Category category) {
        this.category = category;
    }

    @Override
    public int compareTo(Object o) {
        if (this == o) {
            return 0;
        }

        return this.created.compareTo(((CategoryWorkflow) o).created);
    }
}
