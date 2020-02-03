package jobs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import models.Feedback;

import play.jobs.Job;
import utils.ReportUtil;

/**
 * 
 * Used to wrap an asynchronous call around the pdf generation to free up the request thread pool
 * 
 * @author reesbyars
 *
 */
public class FeedbackPdfJob extends Job<byte[]> {
	
	private List<Feedback> feedbacks;

	public FeedbackPdfJob(List<Feedback> feedbacks) {
		this.feedbacks = feedbacks;
	}
	
	public byte[] doJobWithResult() {
		return ReportUtil.getCandidateFeedBackPdf(feedbacks);
	}
	
}
