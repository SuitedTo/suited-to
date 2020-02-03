package utils;

import java.awt.GraphicsEnvironment;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import beans.BeanWrapper;

import models.Feedback;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import play.Logger;

/**
 * A utility for leveraging the JasperReports library for generating dynamic reports.  
 * <p>
 * When adding reports, be sure to include them in the {@link #ALL_REPORT_NAMES} array.
 * 
 * @author reesbyars
 *
 */
public class ReportUtil {
	
	private static final String CANDIDATE_FEEDBACK_NAME = "candidateFeedback";
	
	/**
	 * An array of all the report names sans any file extensions.  Any new report name should
	 * be added to this array to guarantee that they are compiled.
	 */
	private static final String[] ALL_REPORT_NAMES = {CANDIDATE_FEEDBACK_NAME};
	
	/**
	 * Compiles (or recompiles) all the reports.
	 * 
	 * @see jobs.JasperReportsCompilationJob
     *
	 */
	public static void compileReports() {
		try {
			for (String reportName : ALL_REPORT_NAMES) {
				JasperCompileManager.compileReportToFile(getDefinitionPath(reportName), getCompiledPath(reportName));
			}
			Logger.info("Successfully completed compilation of the Jasper Reports.");
		} catch (JRException e) {
			Logger.error("***CRITICAL ERROR***  :  Jasper Report failed in compilation.  Detailed message:  %s", e.getMessage());
		}
	}

	/**
	 * Given a candidate's ID, the candidateFeedback.jrxml file is filled with the associated candidates
	 * current feedbacks and returned as a PDF input stream that can be written to a response.
	 * 
	 * @param reportContext
	 * @return
	 */
	public static byte[] getCandidateFeedBackPdf(List<Feedback> feedbacks) {
		
		byte[] pdfBytes = null;
		
		//not currently used, but can have objects added to it that can be referenced in a report using $P{key}.someField where
		//"key" is the name of the key to the object in the map and "someField" is a public field on the object
		Map<String, Object> reportContext = new HashMap<String, Object>();

		try {
	    	pdfBytes = getPdfBytes(CANDIDATE_FEEDBACK_NAME, reportContext, new BeanCollectionDataSource(Feedback.class, feedbacks));
		} catch (Exception e) {
			Logger.error(e, "Could not generate Candidate Feedback.  Report Context:  %s.  Message:  %s", reportContext, e.getMessage());
		}
	    
	    return pdfBytes;
	}
	
	/**
	 * @param reportName
	 * @param reportContext
	 * @return
	 */
	private static byte[] getPdfBytes(String reportName, Map<String, Object> reportContext, JRDataSource dataSource) {
		
		byte[] pdfBytes = null;
		JasperPrint print = getJasperPrint(reportName, reportContext, dataSource);
		if (print != null) {
			OutputStream os = new ByteArrayOutputStream();
			JRExporter exporter = new JRPdfExporter();
			exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, os);
			exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
			try {
				exporter.exportReport();
				pdfBytes = ((ByteArrayOutputStream) os).toByteArray();
			} catch (JRException e) {
				Logger.error("Could not export Jasper Report of PDF for %s report.  Report Context:  %s.  Message:  %s", reportName, reportContext, e.getMessage());
			}
		}
		
		return pdfBytes;
	}
	
	/**
	 * 
	 * @param reportName
	 * @param reportContext
	 * @return
	 */
	private static JasperPrint getJasperPrint(String reportName, Map<String, Object> reportContext, JRDataSource dataSource) {
		
		final String compiledPath = getCompiledPath(reportName);
		
		JasperPrint print = null;
		
		try {
			print = JasperFillManager.fillReport(compiledPath, reportContext, dataSource);//DB.getConnection());
		} catch (JRException e) {
			Logger.error("Could not obtain Jasper Print for %s report.  Report Context:  %s.  Message:  %s", reportName, reportContext, e.getMessage());
		}
		
		return print;
	}
	
	/**
	 * Given the name of a report sans its extensions, returns the path to the xml definition of the report
	 * 
	 * @param reportName
	 * @return
	 */
	private static String getDefinitionPath(String reportName) {
		return "./app/reports/" + reportName + ".jrxml";
	}
	
	/**
	 * Given the name of a report sans its extensions, returns the path to the compiled version of the report
	 * 
	 * @param reportName
	 * @return
	 */
	private static String getCompiledPath(String reportName) {
		return "./tmp/" + reportName + ".jasper";
	}
	
	public static class BeanCollectionDataSource extends JRBeanCollectionDataSource{
		
		/**
		 *
		 */
		private Collection data = null;
		private Iterator iterator = null;
		private Object currentBean = null;
		
		private BeanWrapper beanAccess;

		public BeanCollectionDataSource(Class<?> clazz, Collection<?> beanCollection) {
			super(beanCollection);
			data = beanCollection;
			moveFirst();
			beanAccess = new BeanWrapper(clazz);
		}
		
		public Object getFieldValue(JRField field) throws JRException {
			return beanAccess.get(getPropertyName(field), currentBean);
		}
		
		public void moveFirst()
		{
			if (this.data != null)
			{
				this.iterator = this.data.iterator();
			}
		}
		
		public boolean next()
		{
			boolean hasNext = false;
			
			if (this.iterator != null)
			{
				hasNext = this.iterator.hasNext();
				
				if (hasNext)
				{
					this.currentBean = this.iterator.next();
				}
			}
			
			return hasNext;
		}
		
		
		
		
	}

}
