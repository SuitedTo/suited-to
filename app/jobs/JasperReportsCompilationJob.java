package jobs;

import play.jobs.Job;
import play.jobs.OnApplicationStart;
import utils.FontLoader;
import utils.ReportUtil;

/**
 * Runs on start-up, but asynchronously, to compile the Jasper Report files.  Can also be called run from the views/admin.html page.
 * <p>
 * Also loads custom fonts to make them available to the JVM for use in the reports.
 * 
 * @author reesbyars
 * 
 * @see {@link controllers.Admin#recompileReports()}
 *
 */
@OnApplicationStart(async=true)
public class JasperReportsCompilationJob extends Job {

	public void doJob() {
		FontLoader.loadFromDefaultDir();
		ReportUtil.compileReports();
	}
	
}
