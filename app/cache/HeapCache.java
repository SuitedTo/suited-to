package cache;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import controllers.deadbolt.Restrict;
import controllers.deadbolt.RestrictedResource;
import controllers.deadbolt.Restrictions;
import controllers.deadbolt.RoleHolderPresent;
import controllers.deadbolt.Unrestricted;

import play.Logger;
import play.Play;
import play.PlayPlugin;
import play.classloading.ApplicationClasses.ApplicationClass;
import play.utils.FastRuntimeException;
import play.vfs.VirtualFile;

/**
 * A fixed and very limited cache used only to realize performance gains and
 * only in cases where the performance gains outweigh the heap space costs.
 * 
 * 
 * @author joel
 *
 */
public class HeapCache extends PlayPlugin{

	private static Map<String, Object> cache = null;

	public void afterApplicationStart(){
		init();
	}

	private static void init(){
		if(cache != null){
			return;
		}

		cache = new Hashtable<String,Object>();

		List<ApplicationClass> classes = getAllClasses("");
		for(ApplicationClass appClass : classes){
			if(appClass == null){
				continue;
			}
			Class clazz = appClass.javaClass;

			if(appClass.getPackage().startsWith("controllers")){
				Method[] methods = clazz.getDeclaredMethods();
				for(Method m : methods){
					if (!Modifier.isPublic(m.getModifiers())){
						continue;
					}
					Annotation[] annotations = m.getAnnotations();
					List<Annotation> deadboltAnnotations = new ArrayList<Annotation>();
					for(Annotation a : annotations){
						if(isDeadboltAnnotation(a)){
							deadboltAnnotations.add(a);
						}
					}
					if(deadboltAnnotations.size() != 0){
						cache.put("A:" + clazz.getSimpleName() + "." + m.getName(), deadboltAnnotations);
					}
				}
			}else if(appClass.getPackage().equals("models")){
				cache.put("C:" + clazz.getName(), clazz);
			}
		}

	}
	
	/**
	 * Only caching model classes at this time.
	 * 
	 * @param className
	 * @return
	 */
	public static Class getClassForName(String className){
		if(cache == null){
			//I don't even think this can happen
			throw new FastRuntimeException("Attempt to get action annotation with null cache.");
		}
		return (Class) cache.get("C:" + className);
	}
	
	/**
	 * Right now we're only caching deadbolt annotations.
	 * 
	 * @param actionName
	 * @param annotationClass
	 * @return
	 */
	public static Annotation getActionAnnotation(String actionName, Class<? extends Annotation> annotationClass){
		
		if(cache == null){
			//I don't even think this can happen
			throw new FastRuntimeException("Attempt to get action annotation with null cache.");
		}
		
		if((actionName == null) || (annotationClass == null)){
			return null;
		}
		List<Annotation> deadboltAnnotations = (List)cache.get("A:" + actionName);
		if(deadboltAnnotations != null){
			for(Annotation a : deadboltAnnotations){
				if(a.annotationType().equals(annotationClass)){
					return a;
				}
			}
		}
		return null;
	}
	
	static Class[] getDeadboltAnnotations(){
		
		return new Class[]{Restrictions.class,Restrict.class,RestrictedResource.class,RoleHolderPresent.class,Unrestricted.class};
	}
	
	static boolean isDeadboltAnnotation(Annotation a){
		Class[] annots = getDeadboltAnnotations();
		for(Class annot : annots){
			if(annot.equals(a.annotationType())){
				return true;
			}
		}
		return false;
	}
	
	static List<ApplicationClass> getAllClasses(String basePackage) {
        List<ApplicationClass> res = new ArrayList<ApplicationClass>();
        for (VirtualFile virtualFile : Play.javaPath) {
            res.addAll(getAllClasses(virtualFile, basePackage));
        }
        return res;
    }

    static List<ApplicationClass> getAllClasses(VirtualFile path) {
        return getAllClasses(path, "");
    }

    static List<ApplicationClass> getAllClasses(VirtualFile path, String basePackage) {
        if (basePackage.length() > 0 && !basePackage.endsWith(".")) {
            basePackage += ".";
        }
        List<ApplicationClass> res = new ArrayList<ApplicationClass>();
        for (VirtualFile virtualFile : path.list()) {
            scan(res, basePackage, virtualFile);
        }
        return res;
    }
    
    static void scan(List<ApplicationClass> classes, String packageName, VirtualFile current) {
        if (!current.isDirectory()) {
            if (current.getName().endsWith(".java") && !current.getName().startsWith(".")) {
                String classname = packageName + current.getName().substring(0, current.getName().length() - 5);
                classes.add(Play.classes.getApplicationClass(classname));
            }
        } else {
            for (VirtualFile virtualFile : current.list()) {
                scan(classes, current.getName() + ".", virtualFile);
            }
        }
    }
	
	public String getStatus(){
		if(cache == null){
			return "Heap Cache\n~~~~~~~~~~~~~~~~~~~~~~~~~~~\ncontains 0 objects";
		}
		return "Heap Cache\n~~~~~~~~~~~~~~~~~~~~~~~~~~~\ncontains " + cache.size() + " objects";
	}
}
