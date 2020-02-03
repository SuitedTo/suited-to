package integration.taleo;

import integration.taleo.generated.ByteArr;
import integration.taleo.generated.CandidateBean;
import integration.taleo.generated.MapItem;
import integration.taleo.generated.SearchResultArr;
import integration.taleo.generated.SearchResultBean;
import integration.taleo.generated.UserBean;
import integration.taleo.generated.WebServicesException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import controllers.Security;

import db.jpa.S3Blob;
import enums.EmailType;
import enums.PhoneType;

import play.Logger;
import play.libs.MimeTypes;

import models.ActiveInterview;
import models.Candidate;
import models.CandidateFile;
import models.Feedback;
import models.User;
import models.embeddable.Email;
import models.embeddable.PhoneNumber;

public class TaleoHelper {

	/**
	 * Returns a list of Taleo candidate ids that match the given name. The list will
	 * be sorted by match relevance - the best match will be first.
	 *
	 * @param name The entire name in one space delimited string.
	 * @param limit The maximum number of results expected. If this value is zero or less then
	 * all results are returned.
	 *
	 * @return A list of Taleo IDs.
	 */
	public static List<Long> findMatchingTaleoCandidates(TaleoService taleo, String name, int limit){
		List<Long> rtn = new ArrayList<Long>();
		integration.taleo.generated.Map args = new integration.taleo.generated.Map();
		List<SearchResultBean> results = new ArrayList<SearchResultBean>();
		if((name != null) && (name.length() > 0)){
			String[] parts = name.trim().split("\\s+");

			if(parts.length == 0){
				return rtn;
			}

			MapItem ln = new MapItem();
			ln.setKey("lastName");
			ln.setValue(parts[parts.length-1]);
			args.getItem().add(ln);

			if(parts.length > 1){
				MapItem fn = new MapItem();
				fn.setKey("firstName");
				fn.setValue(parts[0]);
				args.getItem().add(fn);

				try {
					results = taleo.searchCandidate(args).getArray().getItem();
				} catch (WebServicesException e) {}

			}else{
				try {
					results = taleo.searchCandidate(args).getArray().getItem();

					args = new integration.taleo.generated.Map();
					MapItem fn = new MapItem();
					fn.setKey("firstName");
					fn.setValue(parts[0]);
					args.getItem().add(fn);

					results.addAll(taleo.searchCandidate(args).getArray().getItem());
				} catch (WebServicesException e) {}
			}
		}

		if(results.size() > 0){

			/*
			 * I don't know if Taleo sorts results by relevance or not - Seems that they
			 * would be sorted but I don't see anything in the docs to suggest that they are sorted.
			 */
			Collections.sort(results, new Comparator(){

				@Override
				public int compare(Object arg0, Object arg1) {
					SearchResultBean a = (SearchResultBean)arg0;
					SearchResultBean b = (SearchResultBean)arg1;
					return (int) (100*(b.getRelevance() - a.getRelevance()));
				}

			});


			for(SearchResultBean result : results){
				if((limit > 0) && (rtn.size() >= limit)){
					break;
				}
				rtn.add(result.getId());
			}
		}


		return rtn;
	}

	public static List<Long> findMatchingTaleoCandidates(String name, int limit) throws WebServicesException{
		return findMatchingTaleoCandidates(TaleoService.INSTANCE(), name, limit);
	}

	/**
	 * Returns a list of Taleo candidate ids that match the given candidate. The list will
	 * be sorted by match relevance - the best match will be first.
	 *
	 * @param candidate The candidate to match.
	 * @param limit The maximum number of results expected. If this value is zero or less then
	 * all results are returned.
	 *
	 * @return A list of Taleo IDs.
	 */
	public static List<Long> findMatchingTaleoCandidates(Candidate candidate, int limit){
		try {
			TaleoService taleo = TaleoService.INSTANCE();
			return findMatchingTaleoCandidates(taleo, candidate.name, limit);
		} catch (WebServicesException e) {
			return new ArrayList<Long>();
		}
	}
	
	public static Candidate asCandidate(long taleoCandidateId){

		try {
			return asCandidate(TaleoService.INSTANCE(), taleoCandidateId);
		} catch (WebServicesException e) {
			return null;
		}
	}

	public static Candidate asCandidate(TaleoService taleo, long taleoCandidateId){
		try {
			CandidateBean taleoCandidate = taleo.getCandidateById(taleoCandidateId);
			Candidate candidate = new Candidate();
			if(taleoCandidate != null){
				candidate.taleoCandId = taleoCandidate.getId();
				candidate.name = taleoCandidate.getFirstName() + " " + taleoCandidate.getLastName();
				candidate.address = taleoCandidate.getAddress();
				candidate.feedbackList = new LinkedList<Feedback>();
				candidate.feedbackHidden = true;
				candidate.files = new ArrayList<CandidateFile>();
				candidate.activeInterviews = new ArrayList<ActiveInterview>();
		        PhoneNumber cellPhoneNumber = null;

		        SortedSet<Email> emails = new TreeSet<Email>();
		        if(taleoCandidate.getEmail() !=null && !taleoCandidate.getEmail().isEmpty()) {
		        Email email = new Email(EmailType.HOME, taleoCandidate.getEmail());
		        emails.add(email);
		        }
		        candidate.emails = emails;

		        SortedSet<PhoneNumber> phoneNumbers = new TreeSet<PhoneNumber>();

		        if((taleoCandidate.getCellPhone() != null) && !taleoCandidate.getCellPhone().isEmpty()) {
		            cellPhoneNumber = new PhoneNumber(PhoneType.MOBILE, taleoCandidate.getCellPhone());
		            phoneNumbers.add(cellPhoneNumber);
		        }
		        if(taleoCandidate.getPhone() !=null && !taleoCandidate.getPhone().isEmpty()) {
		            PhoneNumber homePhoneNumber = new PhoneNumber(PhoneType.HOME, taleoCandidate.getPhone());
		            phoneNumbers.add(homePhoneNumber);

		        }
		        candidate.phoneNumbers = phoneNumbers;

		        return candidate;
			}
		} catch (WebServicesException e) {
			Logger.error("Unable to convert Taleo candidate to SuitedTo candidate: %s", e.getMessage());
		}

		return null;
	}

    public static CandidateFile importResumeFromTaleo(long taleoCandidateId){

		try {
			 TaleoService taleo = TaleoService.INSTANCE();
			return importResumeFromTaleo(taleo, taleoCandidateId);
		} catch (WebServicesException e) {
			return null;
		}

    }
    
    public static CandidateFile importResumeFromTaleo(TaleoService taleo, long taleoCandidateId){
			return importResumeFromTaleo(taleo, Security.connectedUser(), taleoCandidateId);

    }

    public static CandidateFile importResumeFromTaleo(TaleoService taleo, User creator, long taleoCandidateId){

        try {
            CandidateBean taleoCandidate = taleo.getCandidateById(taleoCandidateId);
            ByteArr ba = taleo.getBinaryResume(taleoCandidateId);
            if(ba != null){
                String fileName = taleoCandidate.getResumeFileName();
                //MS Word docs are coming over with a fileName of whatever.doc.gz however, taleo is not actually
                // compressing the file, (this is a bug on their end)- remove the gzip extension
                //so that file names and types are set to the underlying .doc or .docx file type.
                if(fileName.substring(fileName.length()-3).equals(".gz") && fileName != null) {
                    fileName = fileName.replace(".gz", "");
                }
                String fileType = MimeTypes.getContentType(fileName);

                InputStream in = new ByteArrayInputStream(ba.getArray());
                S3Blob blob = new S3Blob();
                blob.set(in, fileType);

                return new CandidateFile(fileName,
                        fileType, blob, Security.connectedUser());
            }

        } catch (WebServicesException e) {
            Logger.error("Unable to import resume from Taleo: %s", e.getMessage());
        }
        return null;
    }
    
    public static boolean isAdmin(TaleoService taleo, String taleoUserName){
		try {
			integration.taleo.generated.Map args = new integration.taleo.generated.Map();
			MapItem fn = new MapItem();
			fn.setKey("role");
			fn.setValue("A");
			args.getItem().add(fn);
			
			SearchResultArr results = taleo.searchUser(args);
			for(SearchResultBean result : results.getArray().getItem()){
				UserBean adminUser = taleo.getUserById(result.getId());
				if((adminUser != null) &&
						adminUser.getLoginName().equals(taleoUserName)){
					return true;
				}
			}
		} catch (WebServicesException e) {
			Logger.error("Unable to determine if connected user is admin: %s", e.getMessage());
		}
		return false;
    	
    }

}
