package data.binding;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NoResultException;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Query;

import beans.BeanWrapper;

import play.Play;
import play.data.binding.ParamNode;
import play.data.binding.RootParamNode;
import play.data.validation.Validation;
import play.db.jpa.JPA;
import play.db.jpa.JPABase;
import play.db.jpa.Model;
import play.exceptions.UnexpectedException;

public class CustomBinding {

	public static void bindBean(RootParamNode rootParamNode, String name, Object bean){
		edit(rootParamNode, name, bean, null);
	}

	private static <T extends JPABase> T edit(ParamNode rootParamNode, String name, Object o, Annotation[] annotations) {
		ParamNode paramNode = rootParamNode.getChild(name, true);
		// #1195 - Needs to keep track of whick keys we remove so that we can restore it before
		// returning from this method.
		List<ParamNode.RemovedNode> removedNodesList = new ArrayList<ParamNode.RemovedNode>();
		try {
			BeanWrapper bw = new BeanWrapper(o.getClass());
			// Start with relations
			Set<Field> fields = new HashSet<Field>();
			Class clazz = o.getClass();
			while (!clazz.equals(Object.class)) {
				Collections.addAll(fields, clazz.getDeclaredFields());
				clazz = clazz.getSuperclass();
			}
			for (Field field : fields) {
				boolean isEntity = false;
				String relation = null;
				boolean multiple = false;
				//
				if (field.isAnnotationPresent(OneToOne.class) || field.isAnnotationPresent(ManyToOne.class)) {
					isEntity = true;
					relation = field.getType().getName();
				}
				if (field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(ManyToMany.class)) {
					Class fieldType = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
					isEntity = true;
					relation = fieldType.getName();
					multiple = true;
				}

				if (isEntity) {

					ParamNode fieldParamNode = paramNode.getChild(field.getName(), true);

					Class<Model> c = (Class<Model>) Play.classloader.loadClass(relation);
					if (JPABase.class.isAssignableFrom(c)) {
						String keyName = Model.Manager.factoryFor(c).keyName();
						if (multiple && Collection.class.isAssignableFrom(field.getType())) {
							Collection l = new ArrayList();
							if (SortedSet.class.isAssignableFrom(field.getType())) {
								l = new TreeSet();
							} else if (Set.class.isAssignableFrom(field.getType())) {
								l = new HashSet();
							}
							String[] ids = fieldParamNode.getChild(keyName, true).getValues();
							if (ids != null) {
								// Remove it to prevent us from finding it again later
								fieldParamNode.removeChild(keyName, removedNodesList);
								for (String _id : ids) {
									if (_id.equals("")) {
										continue;
									}

									Query q = JPA.em().createQuery("from " + relation + " where " + keyName + " = ?1");
									q.setParameter(1, Binder.directBind(rootParamNode.getOriginalKey(), annotations,_id, Model.Manager.factoryFor((Class<Model>) Play.classloader.loadClass(relation)).keyType(), null));
									try {
										l.add(q.getSingleResult());

									} catch (NoResultException e) {
										Validation.addError(name + "." + field.getName(), "validation.notFound", _id);
									}
								}
								bw.set(field.getName(), o, l);
							}
						} else {
							String[] ids = fieldParamNode.getChild(keyName, true).getValues();
							if (ids != null && ids.length > 0 && !ids[0].equals("")) {

								Query q = JPA.em().createQuery("from " + relation + " where " + keyName + " = ?1");
								q.setParameter(1, Binder.directBind(rootParamNode.getOriginalKey(), annotations, ids[0], Model.Manager.factoryFor((Class<Model>) Play.classloader.loadClass(relation)).keyType(), null));
								try {
									Object to = q.getSingleResult();
									edit(paramNode, field.getName(), to, field.getAnnotations());
									// Remove it to prevent us from finding it again later
									paramNode.removeChild( field.getName(), removedNodesList);
									bw.set(field.getName(), o, to);
								} catch (NoResultException e) {
									Validation.addError(fieldParamNode.getOriginalKey(), "validation.notFound", ids[0]);
									// Remove only the key to prevent us from finding it again later
									// This how the old impl does it..
									fieldParamNode.removeChild(keyName, removedNodesList);
									if (fieldParamNode.getAllChildren().size()==0) {
										// remove the whole node..
										paramNode.removeChild( field.getName(), removedNodesList);
									}

								}

							} else if (ids != null && ids.length > 0 && ids[0].equals("")) {
								bw.set(field.getName(), o, null);
								// Remove the key to prevent us from finding it again later
								fieldParamNode.removeChild(keyName, removedNodesList);
							}
						}
					}
				}
			}
			ParamNode beanNode = rootParamNode.getChild(name, true);
			Binder.bindBean(beanNode, o, annotations);
			return (T) o;
		} catch (Exception e) {
			throw new UnexpectedException(e);
		}
		finally {
			// restoring changes to paramNode
			ParamNode.restoreRemovedChildren( removedNodesList );
		}
	}
}