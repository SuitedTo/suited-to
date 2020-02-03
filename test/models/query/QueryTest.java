package models.query;

import models.Category;
import models.ModelBase;
import models.Question;
import models.RuleRunner;
import models.TestCache;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.Logger;
import play.test.Fixtures;
import play.test.UnitTest;
import play.utils.Java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class QueryTest<M extends ModelBase> extends UnitTest implements EntityMatchHandler<M> {
    private QueryBase<M> query;

    protected List<M> shouldSelect;

    protected List<M> shouldNotSelect;
    
    private String rulesFile;

    @Before
    public void setup() {
        setup("data.yml");
    }
    public void setup(String dataFile) {
    	setup(dataFile, null);
    }
    
    public void setup(String dataFile, String rulesFile) {
    	Fixtures.deleteDatabase();
    	Fixtures.loadModels(dataFile);
        this.rulesFile = rulesFile;
        shouldSelect = new ArrayList<M>();
        shouldNotSelect = new ArrayList<M>();
    }

    protected void reset(QueryBase<M> query) {
        this.query = query;
        shouldSelect.clear();
        shouldNotSelect.clear();
    }

    protected void shouldSelect(M entity) {
        shouldSelect.add(entity);
    }

    protected void shouldNotSelect(M entity) {
        shouldNotSelect.add(entity);
    }
    
    protected abstract Class<M> getEntityClass();
    
    protected void evaluateResultList(List<M> resultList){
    	for (M entity : resultList) {
            assertFalse("Should not have selected " + TestCache.getCachedKey(entity.id), shouldNotSelect.contains(entity));
        }

        for (M entity : shouldSelect) {
            assertTrue("Failed to select " + TestCache.getCachedKey(entity.id), resultList.contains(entity));
        }
    }

    protected void testExecution() {
    	
    	evaluateResultList(query.executeQuery().getList());
    	
    }
    
    protected void testExecution(String agenda){
    	testExecution(agenda, null);
    }
    
    protected void testExecution(String agenda, Map<String, Object> args){
    	if(rulesFile != null){
        	RuleRunner<M> rRunner = new RuleRunner<M>(rulesFile, "handler", this, args);
        	rRunner.load(getAll());
        	rRunner.runAgenda(agenda);
        	syncUnselected();
        	evaluateResultList(query.executeQuery().getList());
        }
    }
    
    protected void syncUnselected(){
    	List<M> all = getAll();
    	all.removeAll(shouldSelect);
    	shouldNotSelect = all;
    }
    
    protected List<M> getAll(){
    	Class<M> clazz = getEntityClass();
    	try {
			return (List<M>) Java.invokeStatic(clazz, "findAll");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
    }
    
    protected User getUser(String key){
    	return TestCache.getUser(key);
    }
    
    protected Question getQuestion(String key){
    	return TestCache.getQuestion(key);
    }

    protected Category getCategory(String key){
    	return TestCache.getCategory(key);
    }
    
    public void handleMatch(M entity){
    	shouldSelect(entity);
    }
    
}
