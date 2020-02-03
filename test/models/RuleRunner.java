package models;

import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.drools.RuleBase;
import org.drools.WorkingMemory;
import org.drools.compiler.RuleBaseLoader;
import org.drools.spi.Activation;
import org.drools.spi.AgendaFilter;

import play.Play;
import play.templates.TemplateLoader;
import play.vfs.VirtualFile;

/**
 * You need to load working memory (call the load method) before running any rules.
 * 
 * @author joel
 *
 * @param <M>
 */
public class RuleRunner<M> {

	protected WorkingMemory workingMemory;

	public RuleRunner (final String name, final String handlerName, final Object handler, final Map<String, Object> args){
		VirtualFile rulesFile = null;
		try {
			for (VirtualFile vf : Play.javaPath) {
				rulesFile = vf.child(name);
				if (rulesFile != null && rulesFile.exists()) {
					break;
				}
			}
			if (rulesFile == null) {
				throw new RuntimeException("Cannot load rules " + name + ", the file was not found");
			}

			String renderedRules = TemplateLoader.load(rulesFile).render();

			Reader reader = new StringReader(renderedRules);

			RuleBase ruleBase = RuleBaseLoader.getInstance().loadFromReader(reader);

			workingMemory = ruleBase.newStatefulSession();

			workingMemory.setGlobal(handlerName, handler);

			if(args != null){
				Iterator<String> it = args.keySet().iterator();
				while(it.hasNext()){
					String key = it.next();
					workingMemory.setGlobal(key, args.get(key));
				}
			}

		} catch (Throwable e) {
			e.printStackTrace();
			throw new RuntimeException("Cannot load rules " + name + ": " + e.getMessage(), e);
		}
	}

	public void load(final List<M> initialObjects){
		for(Object o : initialObjects){
			workingMemory.insert(o);
		}
	}

	public void runAll(){
		workingMemory.fireAllRules();
	}

	public void runAgenda(final String agenda){
		if(agenda != null){
			AgendaFilter filter = new AgendaFilter()
			{
				public boolean accept(Activation activation)
				{
					if (!activation.getRule().getName().equals(agenda)){
						return false;
					}

					return true;
				}
			};

			workingMemory.fireAllRules(filter);
		}
	}
}
