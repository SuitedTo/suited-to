package controllers;

import com.google.gson.*;
import controllers.deadbolt.*;
import db.jpa.S3Blob;
import enums.*;
import integration.taleo.TaleoHelper;
import integration.taleo.TaleoService;
import integration.taleo.generated.CandidateBean;
import integration.taleo.generated.WebServicesException;
import jobs.ClearFeedbackInTaleo;
import jobs.FeedbackToTaleo;
import models.*;
import models.embeddable.Email;
import models.embeddable.ExternalLink;
import models.embeddable.PhoneNumber;
import models.query.feedback.AccessibleFeedbackHelper;
import notifiers.Mails;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.Blob;
import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
import play.exceptions.MailException;
import play.i18n.Messages;
import play.libs.MimeTypes;
import play.mvc.Util;
import play.mvc.With;
import utils.JobUtil;
import utils.SafeStringArrayList;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.io.FileInputStream;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@With(Deadbolt.class)
@RoleHolderPresent
@RestrictedResource(name="")
public class Candidates extends ControllerBase {

    private static final CandidateNameSerializer NAME_SERIALIZER =
            new CandidateNameSerializer();

    public static void list() {
        User connectedUser = Security.connectedUser();
        String jsonJobNames = new Gson().toJson(Job.getJobNamesByCompany(connectedUser.company.id));
        String jsonJobStatusNames = new Gson().toJson(JobStatus.names());

        render(jsonJobNames, jsonJobStatusNames);
    }

    /**
    *
    * @param id Id of a Candidate in the SuitedTo system
    * @param userCount
    * @param userErrors
    * @param feedbackAdded
    */
   @RestrictedResource(name = {"models.Candidate"}, staticFallback = true)
   public static void show(Long id, Integer userCount, Integer emailCount,
                           List<String> userErrors, boolean feedbackAdded, List<String> emailErrors) {

       User connectedUser = Security.connectedUser();

       Company company = connectedUser.company;

       int numFeedback = 0;
       // All jobs belonging to this company
       List<CandidateJobStatus> statuses = null;
       List<CandidateJobStatus> candidateJobStatuses = null;

       List<CompanyJobStatus> companyJobStatuses = CompanyJobStatus.find("byCompany", company).fetch();
       List<Jobs> companyJobs = Job.find("byCompany", company).fetch();


       Candidate candidate = null;
       if(id != null){
           // Query for the candidate's feedback count
           candidate = Candidate.findById(id);
           AccessibleFeedbackHelper afh = new AccessibleFeedbackHelper(connectedUser);
           // TODO: see if Candidate.byCaniddateId filter can be used
           models.filter.feedback.ByCandidateId feedbackByCandidate = new models.filter.feedback.ByCandidateId();
           feedbackByCandidate.include(id.toString());
           afh.addFilter(feedbackByCandidate);
           CriteriaQuery cq = afh.finish();
           TypedQuery<Feedback> feedbackQuery = JPA.em().createQuery(cq);
           numFeedback = feedbackQuery.getResultList().size();

           candidateJobStatuses = CandidateJobStatus.find("byCandidate", candidate).fetch();
       }

       boolean loggedInToTaleo = Taleo.getCredentialsForConnectedUser() != null;

       //TODO : I strongly suspect this is being completely ignored in the 
       //       template.  We should confirm that and remove this if that's true
       List activeInterviews = (candidate == null)?null:
               ActiveInterview.find("byIntervieweeAndActive", candidate,
                       true).fetch();

       //hitting "End Interview" on the Live Interview will set the value of the id of the just completed Active Interview
       //into flash scope before redirecting to the candidates page
       String justCompletedInterviewId = flash.get("justCompletedInterviewId");
       //candidate = ((candidate == null) ? new Candidate():candidate);
       render(company, candidate, userCount, emailCount, userErrors, emailErrors,
               feedbackAdded, activeInterviews, loggedInToTaleo, justCompletedInterviewId,
               numFeedback, companyJobs, candidateJobStatuses, companyJobStatuses);
   }

    /**
    * @param id The (optional) id of the corresponding SuitedTo candidate
    * @param taleoId Id of a Candidate in the Taleo system
    */
   @RestrictedResource(name = {"models.Candidate"}, staticFallback = true)
   public static void showTaleoCandidate(final Long id, @Required final Long taleoId) {

	   Candidate candidate = null;
	   User connectedUser = Security.connectedUser();
	   Company company = connectedUser.company;

	   String newlyUploadedFiles = null;
	   boolean loggedInToTaleo = false;
       	try{
       		final TaleoService taleo = TaleoService.INSTANCE();

       		loggedInToTaleo = true;

       		candidate = TaleoHelper.asCandidate(taleo, taleoId);

       		if(candidate != null){
       			CandidateFile file = null;
       			try {
       				file = await(JobUtil.asJobInContext(new Callable<CandidateFile>(){

       					@Override
       					public CandidateFile call() throws Exception {
       						return TaleoHelper.importResumeFromTaleo(taleo, taleoId);
       					}

       				}).now());

       			} catch (Exception e) {
       				Logger.error("Unable to import resume: %s", e.getMessage());
       			}
       			
       			if(file != null){
       				file.save();
       				newlyUploadedFiles = file.id + ",";
       				candidate.files.add(file);
       			}
       			
       		}
       	}catch(WebServicesException wse){

       	}
       	
       	if(id != null){
       		candidate.id = id;
       	}
       	
       	renderTemplate("@show",connectedUser, candidate,
					loggedInToTaleo, taleoId, company, newlyUploadedFiles);
   }

    public static void cancelEdit(Long id){
        show(id, null, null, null, false, null);
    }

    /**
     * Asynchronously accessed action to toggle the visibility of candidate feedback, should only be accessed by
     * SuitedTo admins and company admins
     * @param id Candidate Id
     * @param feedbackHidden indicates whether feedback should be hidden
     */
    @RestrictedResource(name = {"models.Candidate"}, staticFallback = true)
    @Restrictions({@Restrict({RoleValue.APP_ADMIN_STRING}), @Restrict({RoleValue.COMPANY_ADMIN_STRING})})
    public static void toggleFeedbackDisplay(Long id, boolean feedbackHidden){
        Candidate candidate = Candidate.findById(id);
        candidate.feedbackHidden = feedbackHidden;
        candidate.save();
        renderText(feedbackHidden ? "hide" : "show");
    }

    public static boolean exportFeedback(long candidateId){

    	try {
			new FeedbackToTaleo(TaleoService.INSTANCE(), candidateId).now();
		} catch (Exception e) {
			return false;
		}
    	return true;
    }

    public static void getCandidateIDForUserCompanyByName(
            @Required String name) {

        String result;
        User user = Security.connectedUser();

        if (user == null) {
            result = "[false, \"No user logged in.\"]";
        }
        else {

            Company company = user.company;

            if (company == null) {
                result = "[false, \"User is not associated with a " +
                        "company.\"]";
            }
            else {
                Candidate candidate = Candidate.find(
                        "byCompanyAndNameIlike", user.company,
                        name).first();

                if (candidate == null) {
                    result = "[true, -1]";
                }
                else {
                    result = "[true, " + candidate.id + "]";
                }
            }
        }

        renderJSON(result);
    }

    public static void getUserCompanyCandidateList(
            String partialCandidateName) {

        User user = Security.connectedUser();

        List<Candidate> candidates;
        final Company company = user.company;
        if (company != null) {

            if (partialCandidateName == null) {
                candidates = company.candidates;
            }
            else {
                candidates = Candidate.find("byCompanyAndNameIlike",
                        company, "%" + partialCandidateName + "%").fetch();
            }
        }
        else {
            candidates = new LinkedList<Candidate>();
        }

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Candidate.class, NAME_SERIALIZER);

        renderJSON(builder.create().toJson(candidates));
    }

    /**
     * @param id Candidate id
     * @param solicitEmailsAndUsernames A json string of emails and/or usernames generated by the
     *                           jquery tag/autocomplete plugin. The string is an array
     *                           of objects, each of which contain a string named Label whose
     *                           value is either an email or username to request feedback from.
     * @param regardingMagicID   The candidate's id an a or c prepended to it. These characters
     *                           represent active and candidate interviews, respectively.
     */
    @RestrictedResource(name = {"models.Candidate"}, staticFallback = true)
    public static void solicitFeedback(@Required Long id,
                @Required String solicitEmailsAndUsernames,
                @Required String regardingMagicID) {

        try {
            Interview.getCandidateInterviewAssociationFromMagicID(
                    regardingMagicID);
        }
        catch (NoSuchElementException nsee) {
            validationError("No such candidate interview.");
                Validation.keep();
                show(id, null, null, null, false, null);
        }

        Candidate candidate = Candidate.findById(id);

        if (candidate == null) {
            validationError("No such candidate.");
            Validation.keep();
            list();
        }

        List<String> emailList = new LinkedList<String>();

        /** I could not get gson.fromJson() to deserialize
         *  emailsAndUsernames in the name of not spinning my
         *  wheels for any longer, I threw in this regex to get
         *  this method working, which is a completely hackish
         *  and inflexible solution. I have filed the chore
         *  https://www.pivotaltracker.com/story/show/35465049
         *  to find a fix.
         */
        Pattern pattern = Pattern.compile("\":\"[^\"]+\"");
        Matcher matcher = pattern.matcher(solicitEmailsAndUsernames);

        String emailWithQuotes;
        String emailQuotesRemoved;
        User user;

        while (matcher.find()) {
            emailWithQuotes = matcher.group();
            // This line felt so wrong to write.
            emailQuotesRemoved = emailWithQuotes.substring(3,emailWithQuotes.length()-1);

            user = User.findByFullName(emailQuotesRemoved);
            if(user == null) {
                emailList.add(emailQuotesRemoved);
            }
            else {
                emailList.add(user.email);
            }
        }

        User requestingUser = Security.connectedUser();

        List<String> emailErrors = new ArrayList<String>();
        int userCount = 0;
        for (String email : emailList) {
                try {
                    String requestReason = requestingUser.getPrivateDisplayName() +
                            " has requested your feedback on " +
                            candidate.name + ".";

                    String subject = "Feedback request for " + candidate.name;

                    Mails.getFeedback(email, candidate, regardingMagicID,
                            subject, requestReason);
                    userCount++;
                } catch (MailException e) {
                    emailErrors.add(email);
                }
            }

        show(id, userCount, null, null, false, emailErrors);
    }

    /**
     * @param id Candidate id
     * @param emailFeedbackEmailsAndUsernames A json string of emails and/or usernames generated by the
     *                           jquery tag/autocomplete plugin. The string is an array
     *                           of objects, each of which contain a string named Label whose
     *                           value is either an email or username to request feedback from.
     * @param regardingMagicID   The candidate's id an a or c prepended to it. These characters
     *                           represent active and candidate interviews, respectively.
     */

    /** TODO: There is some code duplication between this method and solicitFeedback involving
     *  the parsing of email addresses and and usernames generated by textext.
     */
    @RestrictedResource(name = {"models.Candidate"}, staticFallback = true)
    public static void emailFeedback(@Required Long id,
                                       @Required String emailFeedbackEmailsAndUsernames,
                                       @Required String regardingMagicID) {

        try {
            Interview.getCandidateInterviewAssociationFromMagicID(
                    regardingMagicID);
        }
        catch (NoSuchElementException nsee) {
            validationError("No such candidate interview.");
            Validation.keep();
            show(id, null, null, null, false, null);
        }

        Candidate candidate = Candidate.findById(id);

        if (candidate == null) {
            validationError("No such candidate.");
            Validation.keep();
            list();
        }

        List<String> emailList = new LinkedList<String>();
        List<User> userList = new LinkedList<User>();

        /** I could not get gson.fromJson() to deserialize
         *  emailsAndUsernames in the name of not spinning my
         *  wheels for any longer, I threw in this regex to get
         *  this method working, which is a completely hackish
         *  and inflexible solution. I have filed the chore
         *  https://www.pivotaltracker.com/story/show/35465049
         *  to find a fix.
         */
        Pattern pattern = Pattern.compile("\":\"[^\"]+\"");
        Matcher matcher = pattern.matcher(emailFeedbackEmailsAndUsernames);

        String emailWithQuotes;
        String emailQuotesRemoved;
        User user;

        while (matcher.find()) {
            emailWithQuotes = matcher.group();
            // This line felt so wrong to write.
            emailQuotesRemoved = emailWithQuotes.substring(3,emailWithQuotes.length()-1);

            user = User.findByFullName(emailQuotesRemoved);
            if(user == null) {
                emailList.add(emailQuotesRemoved);
            }
            else {
                userList.add(user);
            }
        }

        //Figure out which users have submitted feedback
        List<Feedback> feedback = Feedback.find("byCandidate", candidate).fetch();
        List<TemporaryFeedbackAuthorization> temporaryFeedbackAuthorizations = TemporaryFeedbackAuthorization.find("byCandidate", candidate).fetch();

        Hashtable submittedFeedbackEmails = new Hashtable();

        //List each user only once, even if they have multiple feedbacks
        for(Feedback fb: feedback ) {
            if(fb.feedbackSource != null) {
                submittedFeedbackEmails.put(fb.feedbackSource.email, true);
            }
            else {
                submittedFeedbackEmails.put(fb.feedbackEmail, true);
            }
        }


        int awaitingFeedbackCount = 0;
        //Determine the number of future, active, not yet finished interviews for this candidate
        List<ActiveInterview> futureUnfinished = new LinkedList<ActiveInterview>();
        for(ActiveInterview ai : candidate.activeInterviews) {
        	if( ai.active ){
        		if(ai.getStatus() != ActiveInterviewState.FINISHED &&
        				(ai.anticipatedDate == null || ai.anticipatedDate.after(new Date()))) {
        			futureUnfinished.add(ai);
        		}else if((ai.getStatus() == ActiveInterviewState.FINISHED) &&
        					!ai.hasFeedback()){
        			awaitingFeedbackCount++;
        		}
        	}
        }

        User sendingUser = Security.connectedUser();

        List<String> emailErrors = new ArrayList<String>();
        int userCount = 0;
        for (String email : emailList) {
            try {
                Mails.emailFeedback(email, candidate, awaitingFeedbackCount, submittedFeedbackEmails.size(),
                    futureUnfinished.size(), sendingUser, null );
                userCount++;
            } catch (MailException e) {
                emailErrors.add(email);
            }
        }

        for (User u : userList) {
            try {
                Mails.emailFeedback(u.email, candidate, awaitingFeedbackCount, submittedFeedbackEmails.size(),
                        futureUnfinished.size(), sendingUser, u);
                userCount++;
            } catch (MailException e) {
                emailErrors.add(u.email);
            }
        }

        show(id, null, userCount, null, false, emailErrors);
    }


    @RestrictedResource(name = {"models.Candidate"}, staticFallback = true)
    public static void save(Long id, @Required String candidateName, String address, String uploadedFiles, Long taleoCandId) throws Exception {
        User user = Security.connectedUser();
        Candidate candidate;
        if (id == null) {  // Create candidate

            if (!enums.AccountLimitedAction.CREATE_CANDIDATE.canPerform(user)) {

                validationError(
                        Messages.get("limits.candidates.cannot_create"));
                Validation.keep();
                list();
            }

            candidate = new Candidate(user.company, user);

        } else { // Retrieve candidate
            candidate = Candidate.findById(id);
        }

        candidate.name = candidateName;

        final List<play.jobs.Job> jobs = new ArrayList<play.jobs.Job>();
        boolean changedTaleoCandidateAssociation = false;
        if((taleoCandId != null) && (candidate.taleoCandId != null) && (candidate.taleoCandId != taleoCandId)){
        	changedTaleoCandidateAssociation = true;
        	jobs.add(new ClearFeedbackInTaleo(candidate.taleoCandId));
        }
        candidate.taleoCandId = taleoCandId;

        if(changedTaleoCandidateAssociation){
        	jobs.add(new FeedbackToTaleo(id));
        }
        
        new play.jobs.Job(){
        	public void doJob(){
        		for(play.jobs.Job job : jobs){
        			try {
						job.doJob();
					} catch (Exception e) {
						Logger.error("%s failed %s", job.getClass().getSimpleName(),e.getMessage());
					}
        		}
        	}
        }.now();


        candidate.phoneNumbers.clear();
        candidate.emails.clear();
        candidate.externalLinks.clear();



        //pull out the phoneNumberParams
        for (String key : params.allSimple().keySet()) {
            String value = params.get(key);
            if(key.contains("phoneNumberType") && !StringUtils.isEmpty(value)){

                String correspondingValue = params.get(key.replace("Type", "Value"));
                if(!StringUtils.isEmpty(correspondingValue)){
                    validation.phone(correspondingValue);
                    candidate.phoneNumbers.add(new PhoneNumber(PhoneType.valueOf(value), correspondingValue));
                }
            }
            if(key.contains("emailType") && !StringUtils.isEmpty(value)){
                String correspondingValue = params.get(key.replace("Type", "Address"));
                if(!StringUtils.isEmpty(correspondingValue)){
                    validation.email(correspondingValue);
                    candidate.emails.add(new Email(EmailType.valueOf(value), correspondingValue));
                }
            }

            if(key.contains("socialProfileType") && !StringUtils.isEmpty(value)){
                String correspondingValue = params.get(key.replace("Type", "Value"));
                if(!StringUtils.isEmpty(correspondingValue)){

                    candidate.externalLinks.add(new ExternalLink(ExternalLinkType.valueOf(value), correspondingValue));
                }
            }
        }

        candidate.address = address;

        if (user.hasRole(RoleValue.APP_ADMIN) ||
                user.hasRole(RoleValue.COMPANY_ADMIN)) {

            candidate.feedbackHidden =
                    ("on".equals(params.get("feedbackHidden")));
        }

        if (validation.hasErrors()) {
            Company company = user.company;
            boolean loggedInToTaleo = Taleo.getCredentialsForConnectedUser() != null;
            render("@show", candidate, company, loggedInToTaleo);
        }


        List<CandidateFile> modifiedFiles = new ArrayList<CandidateFile>();
        if(StringUtils.isNotEmpty(uploadedFiles)){
            String[] uploadedFileIdArray = uploadedFiles.split(",");
            for (String fileId : uploadedFileIdArray) {
                if(StringUtils.isNotEmpty(fileId)){
                    CandidateFile file = CandidateFile.findById(Long.valueOf(fileId));
                    /*it's possible to have an id value submitted for a file that no longer exists if the user adds and
                    deletes a file before ever saving the candidate*/
                    if(file != null) {
                        file.candidate = candidate;
                        modifiedFiles.add(file);
                    }
                }
            }
        }

        candidate.save();
        for (CandidateFile modifiedFile : modifiedFiles) {
            modifiedFile.save();
        }

        show(candidate.id, null, null, null, false, null);
    }


    @RestrictedResource(name = {"models.Candidate"}, staticFallback = true)
    public static void delete(Long id) {
        Candidate candidate = Candidate.findById(id);
        candidate.delete();
        list();
    }

    /**
     * Handles both the addition of new job statuses as well as changing
     * the status of existing jobs.
     * @param companyJobStatId
     * @param jobId
     * @param candidateId
     */
    @RestrictedResource(name = {"models.CompanyJobStatus"}, staticFallback = true)
    public static void updateCandidateJobStatuses(Long companyJobStatId, Long jobId,
                                      Long candidateId) {

        Candidate candidate = Candidate.findById(candidateId);

        if(jobId != null && companyJobStatId != null) {
            Job job = Job.findById(jobId);
            CompanyJobStatus companyJobStat = CompanyJobStatus.findById(companyJobStatId);

            CandidateJobStatus candidateJobStat = new CandidateJobStatus();
            candidateJobStat.job = job;
            candidateJobStat.candidate = candidate;
            candidateJobStat.companyJobStatus = companyJobStat;
            candidateJobStat.save();
        }

        HashMap<CandidateJobStatus, CompanyJobStatus> currentStatuses = new HashMap<CandidateJobStatus, CompanyJobStatus>();
        for(CandidateJobStatus stat : candidate.candidateJobStatuses) {
            currentStatuses.put(stat, stat.companyJobStatus);
        }

        // Run through submitted params to grab any candidateJobStatuses in need of update
        for (String key : params.allSimple().keySet()) {
            String value = params.get(key);
            if(key.contains("statusId") && !StringUtils.isEmpty(value)){

                String companyStatusId = params.get(key.replace("statusId", "companyStatusId"));
                if(!StringUtils.isEmpty(companyStatusId)){
                    CandidateJobStatus candidatejs = CandidateJobStatus.findById(Long.parseLong(value));
                    CompanyJobStatus companyjs = CompanyJobStatus.findById(Long.parseLong(companyStatusId));
                    if(!candidatejs.companyJobStatus.equals(currentStatuses.get(companyStatusId))) {

                        //Find and update the appropriate candidateJobStatus for this candidate
                        for(CandidateJobStatus candidateStatus : candidate.candidateJobStatuses) {
                            if(candidateStatus.equals(candidatejs)) {
                                candidateStatus.companyJobStatus =  CompanyJobStatus.findById(Long.parseLong(companyStatusId));
                                candidateStatus.save();
                            }
                        }
                    }
                }
            }
        }

        candidate.save();

        show(candidate.id, null, null, null, false, null);
    }

    @RestrictedResource(name = {"models.CandidateJobStatus"}, staticFallback = true)
    public static void updateCandidateJobStatus (Long companyJobStatId,
                                                 Long id) {
        CompanyJobStatus companyStatus = CompanyJobStatus.findById(companyJobStatId);
        CandidateJobStatus candidateStatus = CandidateJobStatus.findById(id);

        if(companyStatus != null && id != null) {
            candidateStatus.companyJobStatus = companyStatus;
            candidateStatus.save();
        }
    }

    @RestrictedResource(name = {"models.CandidateJobStatus"}, staticFallback = true)
    public static void deleteCandidateJobStatus(Long id) {
        CandidateJobStatus status = CandidateJobStatus.findById(id);
        if(status != null) {
            status.delete();
        }

        renderText("success");
    }


    private static final String[] listColumns = {"id", "name", "created"};

    /**
     *
     * @param id Optional Id of the candidate that the file should belong to.  When uploading files for a new candidate
     *           then the id will be null and we'll match the file up to the newly created candidate if and when the user
     *           hits save.  For an existing candidate go ahead and associate the file.
     * @param candidateFileName
     * @param candidateFile
     */
    @RestrictedResource(name = {"models.Candidate"}, staticFallback = true)
    public static void uploadCandidateFile(
            Long id,
    		String candidateFileName,
    		@Required CandidateDocType docType,
    		@data.validation.CandidateFile Blob candidateFile){

        CandidateFile file = createCandidateFile(id, candidateFileName, candidateFile, docType);

        Map result = new HashMap();
        result.put("id", file.id);
        result.put("displayType", file.getDisplayType());
        result.put("docTypeName", file.docType.name());
        result.put("docTypeString", file.docType.toString());
        result.put("name", candidateFileName);

        renderJSON(new Gson().toJson(result));
    }

    /**
     * Creates a Candidate File
     * @param id optional Id of the Candidate that the file is being created for
     * @param candidateFileName
     * @param candidateFile
     * @return
     */
    @Util
    private static CandidateFile createCandidateFile(Long id,
    		String candidateFileName,
    		Blob candidateFile,
    		CandidateDocType docType) {
        CandidateFile file = null;
        try {
			S3Blob s3Blob = new S3Blob();
			await(s3Blob.setAsJob(new FileInputStream(candidateFile.getFile()),
					MimeTypes.getContentType(candidateFile.getFile().getName())).now());


			file = new CandidateFile(candidateFileName, candidateFile.type(), docType, s3Blob, Security.connectedUser());
            if (id != null){
                file.candidate = Candidate.findById(id);
            }
			file.save();

        } catch(Exception e){
			Logger.error("Unable to upload file. %s", e.getMessage());
        }
        return file;
    }
    
    @RestrictedResource(name = {"models.CandidateFile"}, staticFallback = true)
    public static void temporaryCandidateFileRedirect(@Required Long id){
    	CandidateFile file = CandidateFile.findById(id);
    	
        if (file == null) {
            throw new RuntimeException("No such file.");
        }
        
        redirect(file.getTemporaryURL());
    }
    
    @RestrictedResource(name = {"models.CandidateFile"}, staticFallback = true)
    public static void getCandidateFile(@Required Long id, Boolean inline, Boolean convert){
    	CandidateFile file = CandidateFile.findById(id);
    	
    	//Don't really want to keep a transaction open while reading a stream from Amazon
    	//so we close it first.
    	JPAPlugin.closeTx(false);
    	
    	if(file != null){
    		if (inline == null) {
    			inline = true;
    		}
            if (convert == null) {
                convert = false;
            }
            if (inline == false) {
                convert = false;
            }
    		renderBinary(file.contents.get(), file.name, file.type, inline, convert);
    	}
    }

    @RestrictedResource(name = {"models.CandidateFile"}, staticFallback = true)
    public static void deleteCandidateFile(@Required Long id){
        CandidateFile candidateFile = CandidateFile.findById(id);
        if(candidateFile != null){
            Candidate candidate = candidateFile.candidate;
            if(candidate != null){
                candidate.deleteFile(id);
                candidate.save();
            } else {
                candidateFile.delete();
            }
        }
        renderText("success");
    }

    private static class CandidateNameSerializer implements JsonSerializer<Candidate> {

        public JsonElement serialize(Candidate t, Type type,
                    JsonSerializationContext jsc) {
            return new JsonPrimitive(t.name);
        }

    }

    public static void searchTaleoCandidates(Long id, String taleoCandidateName) {
    	List<CandidateBean> candidates = new ArrayList<CandidateBean>();
    	try {
    		List<Long> taleoIDs = TaleoHelper.findMatchingTaleoCandidates(taleoCandidateName, 0);
    		if((taleoIDs != null) && (taleoIDs.size() > 0)) {
    			TaleoService taleo = TaleoService.INSTANCE();
    			for(long taleoId : taleoIDs){
    				CandidateBean cb = taleo.getCandidateById(taleoId);
    				if(cb != null){
    					candidates.add(cb);
    				}
    			}
    		}
    	} catch (WebServicesException e) {
    		Logger.error("Taleo candidate search failed: %s", e.getMessage());
    	}
    	renderJSON(candidates);
    }

    /**
     * For the given ActiveInterview and the state of that interview progresses to the next applicable state in the
     * workflow and renders that new state (ActiveInterviewState.name()) as text
     * @param id key of the ActiveInterview
     * @param currentState ActiveInterviewState of the given interview
     */
    @RestrictedResource(name = {"models.ActiveInterview"}, staticFallback = true)
    public static void processInterviewStatusChange(@Required Long id, @Required ActiveInterviewState currentState){
        ActiveInterview interview = ActiveInterview.findById(id);

        /*make sure the user is actually allowed to change the status of the interview.  The @RestrictedResource check
        will just verify that the user can view the interview*/
        final User user = Security.connectedUser();
        if(!interview.canEdit(user)){
            unauthorized();
        }

        ActiveInterviewState newState = currentState.getDefaultNextState();
        if(newState != null){
            //change status which triggers a save
            interview.changeStatus(user, newState);
        }

        renderText(newState != null ? newState : "");
    }

    @RestrictedResource(name = {"models.ActiveInterview"}, staticFallback = true)
    public static void updateInterviewStatus(@Required Long interviewId, @Required ActiveInterviewState newState){
        ActiveInterview interview = ActiveInterview.findById(interviewId);
        interview.changeStatus(Security.connectedUser(), newState);
        show(interview.interviewee.id, null, null, null, false, null);
    }

    /**
     * Adds feedback pertaining to the given ActiveInterview
     * @param activeInterviewId
     */
    @RestrictedResource(name = {"models.ActiveInterview"}, staticFallback = true)
    public static void quickFeedback(@Required Long activeInterviewId, FeedbackSummary summary, String comments){
        ActiveInterview interview = ActiveInterview.findById(activeInterviewId);
        Candidate candidate = interview.interviewee;
        candidate.addFeedback(Security.connectedUser(), interview, summary, comments);
        candidate.save();

    }
}

