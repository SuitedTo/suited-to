/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.filter;

import play.db.jpa.GenericModel;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 *
 * @author hamptos
 */
public interface Filter<M extends GenericModel> {
    public Predicate asPredicate(Root<M> root);
    public Class<M> getEntityClass();
}
