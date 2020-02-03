package classloading.enhancers;

import java.lang.reflect.Modifier;

import javax.persistence.PostLoad;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import play.Logger;
import play.Play;
import play.classloading.ApplicationClasses.ApplicationClass;
import play.classloading.enhancers.Enhancer;
import play.classloading.enhancers.PropertiesEnhancer.PlayPropertyAccessor;
import play.db.jpa.JPA;
import play.exceptions.UnexpectedException;

/**
 * Enhance controllers.
 * 
 * 
 * @author joel
 *
 */
public class ControllerEnhancer extends Enhancer{

	public void enhanceThisClass(ApplicationClass applicationClass) throws Exception {

		CtClass ctClass = makeClass(applicationClass);

		if (ctClass.getName().equals("controllers.deadbolt.Deadbolt")) {
			for (CtMethod m : ctClass.getDeclaredMethods()) {
	            m.instrument(new ExprEditor() {

	                @Override
	                public void edit(MethodCall m) throws CannotCompileException {
	                    try {
	                    	if (m.getMethodName().equals("getActionAnnotation")) {
	                    		
	   			             	m.replace("{$_ = ($r)controllers.ControllerBase.fastGetActionAnnotation($$);}");
	                    	}
	                    } catch (Exception e) {
	                    	e.printStackTrace();
	                    }
	                }
	            });
	        }
			applicationClass.enhancedByteCode = ctClass.toBytecode();
			ctClass.defrost();
		}
	}
}

