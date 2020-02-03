package classloading.enhancers;

import java.lang.reflect.Modifier;

import javax.persistence.PostLoad;

import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import play.Logger;
import play.Play;
import play.classloading.ApplicationClasses.ApplicationClass;
import play.classloading.enhancers.Enhancer;
import play.classloading.enhancers.PropertiesEnhancer.PlayPropertyAccessor;
import play.db.jpa.JPA;
import play.exceptions.UnexpectedException;

/**
 * Enhance model classes.
 * 
 * 
 * @author joel
 *
 */
public class ModelEnhancer extends Enhancer{

	public void enhanceThisClass(ApplicationClass applicationClass) throws Exception {

		CtClass ctClass = makeClass(applicationClass);

		if (!ctClass.subtypeOf(classPool.get("models.ModelBase"))) {
			return;
		}

		if (!hasAnnotation(ctClass, "javax.persistence.Entity")) {
			return;
		}

		final String entityName = ctClass.getName();
		
		for (CtField ctField : ctClass.getDeclaredFields()) {
            try {

                if (isProperty(ctField)) {

                    // Property name
                    String propertyName = ctField.getName().substring(0, 1).toUpperCase() + ctField.getName().substring(1);
                    String setter = "set" + propertyName;

                    CtMethod originalSetter = ctClass.getDeclaredMethod(setter);
                    
                    if(!hasAnnotation(originalSetter,PlayPropertyAccessor.class.getName())){
                    	continue;
                    }
                    
                    boolean hasAccessAnnotation = null != getAnnotations(ctField).getAnnotation("models.annotations.Access");
                    
                    ctClass.removeMethod(originalSetter);
                    
                    
                    String setterBody = "{";
                    
                    
                    String prefix = "";
                    String suffix = "";
                    if(ctField.getType().isPrimitive()){
                    	String name = ctField.getType().getName();
                    	if(name.equals("int")){
                    		prefix = "new Integer(";
                    		suffix = ")";
                    	}else if(name.equals("long")){
                    		prefix = "new Long(";
                    		suffix = ")";
                    	} else if(name.equals("boolean")){
                    		prefix = "new Boolean(";
                    		suffix = ")";
                    	}else if(name.equals("float")){
                    		prefix = "new Float(";
                    		suffix = ")";
                    	}else if(name.equals("double")){
                    		prefix = "new Double(";
                    		suffix = ")";
                    	}else if(name.equals("short")){
                    		prefix = "new Short(";
                    		suffix = ")";
                    	}else if(name.equals("byte")){
                    		prefix = "new Byte(";
                    		suffix = ")";
                    	}else{
                    		Logger.error("Unhandled primitive type %s", name);
                    	}
                    }
                    
                    if(hasAccessAnnotation){
                    	setterBody += "checkWriteAccess(\"" + ctField.getName() + "\");";
                    }
                    
                    
                    setterBody += "Object oldValue = " + prefix + "this." + ctField.getName() +
                    		suffix + "; Object newValue = " + prefix + "value" + suffix + ";";

                    boolean isCollection = false;
                    for (CtClass clazz : ctField.getType().getInterfaces()){
                    	if (clazz.getName().equals("java.util.Collection")){
                    		isCollection = true;
                    		break;
                    	}
                    }
                    
                    String impl = null;
                    if(ctField.getType().getName().equals("java.util.List")){
                    	impl = "java.util.ArrayList";
                    } else if(ctField.getType().getName().equals("java.util.SortedSet")){
                    	impl = "java.util.TreeSet";
                    }
                    
                    if(isCollection && (impl != null)){
                    	setterBody +=  "if (this." + ctField.getName() + " != null) {";
                    	setterBody +=  "this." + ctField.getName() + ".clear();";
                    	setterBody +=  "} else {";
                    	setterBody +=  "this." + ctField.getName() + " = new " + impl + "();}";

                    	setterBody +=  "if (value != null) {";
                    	setterBody +=  "this." + ctField.getName() + ".addAll(value);}";
                    }else{
                    	setterBody +=  "this." + ctField.getName() + " = value;";
                    }

                    setterBody +=  "propertyUpdated (" +
                    		ctClass.getName() + ".class, \"" + ctField.getName() + "\", oldValue, newValue);";

                    
                    setterBody += "}";
                    
                    CtMethod setMethod = CtMethod.make("public void " + setter + "(" + ctField.getType().getName() + " value)" +
                    		setterBody, ctClass);
                    setMethod.setModifiers(setMethod.getModifiers() | AccessFlag.SYNTHETIC);
                    
                    Annotation as = getAnnotations(ctField).getAnnotation("play.data.binding.As");
                    if(as != null){
                    ConstPool constPool = setMethod.getMethodInfo().getConstPool();
						AnnotationsAttribute attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
						attr.addAnnotation(as);
						setMethod.getMethodInfo().addAttribute(attr);
                    }
                    
                    ctClass.addMethod(setMethod);
                    createAnnotation(getAnnotations(setMethod), PlayPropertyAccessor.class);                    
                }

            } catch (Exception e) {
                Logger.error(e, "Error in PropertiesEnhancer");
                throw new UnexpectedException("Error in PropertiesEnhancer", e);
            }

        }
		
		try{
			ctClass.getDeclaredMethod("getEntityClass");
			Logger.error("Error: The getEntityClass method should not be implemented.");
			System.exit(1);
		}catch(javassist.NotFoundException nfe){
			ctClass.addMethod(CtMethod.make("public Class getEntityClass() { return " + entityName + ".class; }", ctClass));
		}



		//      count::()J
		//      count::(Ljava/lang/String;[Ljava/lang/Object;)J
		//      findAll::()Ljava/util/List;
		//      findById::(Ljava/lang/Object;)Lplay/db/jpa/JPABase;
		//      find::(Ljava/lang/String;[Ljava/lang/Object;)Lplay/db/jpa/GenericModel$JPAQuery;
		//      find::()Lplay/db/jpa/GenericModel$JPAQuery;
		//      all::()Lplay/db/jpa/GenericModel$JPAQuery;
		//      delete::(Ljava/lang/String;[Ljava/lang/Object;)I
		//      deleteAll::()I
		//      findOneBy::(Ljava/lang/String;[Ljava/lang/Object;)Lplay/db/jpa/JPABase;
		//      create::(Ljava/lang/String;Lplay/mvc/Scope$Params;)Lplay/db/jpa/JPABase;


		//Replace the count() method
		CtMethod count = ctClass.getDeclaredMethod("count", null);
		ctClass.removeMethod(count);

		ctClass.addMethod(CtMethod.make("public static long count() { return models.query.EntityQueries.instance.count(\"" + entityName + "\"); }", ctClass));


		//Replace the findAll() method
		CtMethod findAll = ctClass.getDeclaredMethod("findAll");
		ctClass.removeMethod(findAll);

		ctClass.addMethod(CtMethod.make("public static java.util.List findAll() { return models.query.EntityQueries.instance.findAll(\"" + entityName + "\"); }", ctClass));

		

		//Replace the findById() method
		CtMethod findById = ctClass.getDeclaredMethod("findById");
		ctClass.removeMethod(findById);
		
		boolean canOptimize = true;
		outer: for (CtMethod m : ctClass.getMethods()){
			for(Annotation a : getAnnotations(m).getAnnotations()){
				String name = a.getTypeName();
				if(m.getDeclaringClass().equals(ctClass) && (name.equals("javax.persistence.PostLoad") ||
						name.equals("javax.persistence.PreLoad"))){
					canOptimize = false;
					break outer;
				}
			}
		}
		ctClass.addMethod(CtMethod.make("public static play.db.jpa.JPABase findById(Object id) { return models.query.EntityQueries.instance.findById(\"" + entityName + "\", id, " + canOptimize + "); }", ctClass));
		
		
        CtMethod findFirst = CtMethod.make("public static play.db.jpa.JPABase findFirst(String query, Object[] params) { return models.query.EntityQueries.instance.findFirst(\"" + entityName + "\", query, params); }", ctClass);
        ctClass.addMethod(findFirst);
		
        applicationClass.enhancedByteCode = ctClass.toBytecode();
        ctClass.defrost();
        
    }
	
	/**
     * Is this field a valid javabean property ?
     */
    boolean isProperty(CtField ctField) {
        if (ctField.getName().equals(ctField.getName().toUpperCase()) || ctField.getName().substring(0, 1).equals(ctField.getName().substring(0, 1).toUpperCase())) {
            return false;
        }
        return Modifier.isPublic(ctField.getModifiers())
                && !Modifier.isFinal(ctField.getModifiers())
                && !Modifier.isStatic(ctField.getModifiers());
    }
}
