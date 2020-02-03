package models.filter.category;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import models.Category.CategoryStatus;

import play.i18n.Messages;


/**
 * Filter categories by status.
 *
 * @author joel
 */
public class ByStatus extends CategoryFilter<CategoryStatus> {

    @Override
    public String getAttributeName() {
        return "status";
    }

    @Override
    protected String toString(CategoryStatus status) {
        if (status == null) {
            return null;
        }
        return status.name();
    }

    @Override
    public CategoryStatus fromString(String status) {
        try {
            return CategoryStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
        }

        return null;
    }

}
