package models;

import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import org.drools.RuleBase;
import org.drools.WorkingMemory;
import org.drools.compiler.RuleBaseLoader;
import org.drools.spi.*;

import models.Question;
import models.User;
import play.Logger;
import play.Play;
import play.templates.TemplateLoader;
import play.test.Fixtures;
import play.vfs.VirtualFile;


/**
 * Provides access objects loaded from yml
 * 
 * @author joel
 *
 */
public class TestCache{

	public static User getUser(String key){
		try{
			return User.findById(getCachedId(key));
		}catch(Exception e){
			Logger.error("Unable to get user [%s]", key);
			return null;
		}
	}

	public static Question getQuestion(String key){
		try{
			return Question.findById(getCachedId(key));
		}catch(Exception e){
			Logger.error("Unable to get question [%s]", key);
			return null;
		}
	}

	public static Category getCategory(String key){
		try{
			return Category.findById(getCachedId(key));
		}catch(Exception e){
			Logger.error("Unable to get category [%s]", key);
			return null;
		}
	}

	public static Long getCachedId(String key){
		Iterator<String> it = Fixtures.idCache.keySet().iterator();
		while(it.hasNext()){
			String bKey = it.next();        		
			if(bKey.endsWith(key)){
				return (Long)Fixtures.idCache.get(bKey);
			}
		}
		return null;
	}

	public static String getCachedKey(Object id){
		Iterator<String> it = Fixtures.idCache.keySet().iterator();
		while(it.hasNext()){
			String bKey = it.next();        		
			if(Fixtures.idCache.get(bKey).equals(id)){
				int dash = bKey.indexOf('-');
				if(dash != -1){
					bKey = bKey.substring(dash + 1);
				}
				return bKey;
			}
		}
		return String.valueOf(id);
	}
}
