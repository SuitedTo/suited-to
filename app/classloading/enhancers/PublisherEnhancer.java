package classloading.enhancers;

import java.lang.reflect.Modifier;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.ClassMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import utils.publishSubcribe.Subscribers;
import play.classloading.ApplicationClasses.ApplicationClass;
import play.classloading.enhancers.Enhancer;

public class PublisherEnhancer extends Enhancer{

	public void enhanceThisClass(ApplicationClass applicationClass) throws Exception {

		CtClass ctClass = makeClass(applicationClass);

		if(!ctClass.isAnnotation()){
			Annotation a = getAnnotations(ctClass).getAnnotation(Subscribers.class.getName());
			if(a != null){
				MemberValue[] subscribers = ((ArrayMemberValue)a.getMemberValue("value")).getValue();

				if((subscribers != null) && (subscribers.length != 0)){
					StringBuilder body = new StringBuilder();
					for(MemberValue subscriber : subscribers){
						String subscriberClassName = ((ClassMemberValue)subscriber).getValue();
						body.append("utils.publishSubcribe.PublicationSupport" + ".addSubscriber(" +
								ctClass.getName() + ".class," +
								"((utils.publishSubcribe.Subscriber)" +
								subscriberClassName + ".class.newInstance())" +
								");");
					}
					CtConstructor init = ctClass.makeClassInitializer();
					if(init.isEmpty()){
						init.setBody("{" + body + "}");
					}else{
						init.insertAt(0, body.toString());
					}

					for (CtMethod m : ctClass.getDeclaredMethods()) {
						m.instrument(new ExprEditor() {

							@Override
							public void edit(MethodCall m) throws CannotCompileException {
								try {
									if (m.getMethodName().equals("publish") && m.getSignature().equals("(Ljava/lang/Object;)V")) {
										m.replace("{utils.publishSubcribe.PublicationSupport.publish(this.getClass(),$$);}");
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						});
					}
				}
			}

		}
		applicationClass.enhancedByteCode = ctClass.toBytecode();
		ctClass.defrost();
	}

}
