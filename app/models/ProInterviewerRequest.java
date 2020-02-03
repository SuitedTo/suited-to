package models;

import controllers.Categories;
import enums.ProInterviewerRequestStatus;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import play.data.binding.As;
import play.data.validation.CheckWith;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ProInterviewerRequest extends ModelBase {
    public ProInterviewerRequest(User user,
                                 List<Category> categories,
                                 String phone,
                                 String yearsCategoryExperience,
                                 String yearsInterviewerExperience,
                                 String linkedInProfile) {
        this.user = user;
        this.categories.addAll(categories);
        this.phone = phone;
        this.yearsCategoryExperience = yearsCategoryExperience;
        this.yearsInterviewerExperience = yearsInterviewerExperience;
        this.linkedInProfile = linkedInProfile;
        this.status = ProInterviewerRequestStatus.SUBMITTED;
    }

    @ManyToOne
    public User user;

    @ManyToMany
    @JoinTable(name = "prointerviewerrequest_category",
            joinColumns = @JoinColumn(name = "prointerviewerrequest_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    @CheckWith(User.CategoriesCheck.class)
    @As(value={","}, binder=User.CategoryBinder.class)
    public List<Category> categories = new ArrayList<Category>();

    public String phone;

    public String yearsCategoryExperience;

    public String yearsInterviewerExperience;

    public String linkedInProfile;

    @OneToOne(mappedBy="proInterviewerRequest", cascade = CascadeType.ALL, fetch=FetchType.LAZY, orphanRemoval = true)
    public ProInterviewerRequestFile supportingDocument;

    @Enumerated(EnumType.STRING)
    public ProInterviewerRequestStatus status;

    public String getCommaSeparatedCategoryNames() {
        return Categories.getCommaSeparatedCategoryNames(categories);
    }
}
