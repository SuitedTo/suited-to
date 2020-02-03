package classloading.enhancers;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.ClassMemberValue;
import javassist.bytecode.annotation.MemberValue;
import models.annotations.PropertyChangeListeners;
import play.classloading.ApplicationClasses.ApplicationClass;
import play.classloading.enhancers.Enhancer;

/**
 * Enhance property change listeners.
 * 
 * 
 * @author joel
 *
 */
public class PropertyChangeListenerEnhancer extends Enhancer{

	public void enhanceThisClass(ApplicationClass applicationClass) throws Exception {

		CtClass ctClass = makeClass(applicationClass);

		if(!ctClass.isAnnotation()){
			Annotation a = getAnnotations(ctClass).getAnnotation(PropertyChangeListeners.class.getName());
			if(a != null){
				MemberValue[] listeners = ((ArrayMemberValue)a.getMemberValue("value")).getValue();

				if((listeners != null) && (listeners.length != 0)){
					StringBuilder body = new StringBuilder();
					for(MemberValue listener : listeners){
						String listenerClassName = ((ClassMemberValue)listener).getValue();
						body.append("addPropertyChangeListener(" +

						"((java.beans.PropertyChangeListener)" +
						listenerClassName + ".class.newInstance())" +
								");");
					}
					
					body.append("listenersEnabled = true;");

					CtMethod m = CtMethod.make("public void ___registerListeners () {" + body + "}", ctClass);
					ctClass.addMethod(m);

					if (hasAnnotation(ctClass,"javax.persistence.Entity")) {
						ConstPool constPool = m.getMethodInfo().getConstPool();
						AnnotationsAttribute attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
						Annotation annot = new Annotation("javax.persistence.PostLoad", constPool);
						attr.addAnnotation(annot);
						m.getMethodInfo().addAttribute(attr);
					}else{
						throw new RuntimeException("Property support class needs to be an entity: " + ctClass.getName());
					}
				}

			}
		}

		applicationClass.enhancedByteCode = ctClass.toBytecode();
		ctClass.defrost();

	}
}
