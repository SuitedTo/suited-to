package jobs;

import play.jobs.Job;

/**
 * @deprecated - This is likely not necessary now that we're moving all file storage to S3 where we can set retention
 * policies, etc.  The User delete and Accounts.deletePicture methods have been updated to clean up the files themselves
 * Its possible that there will be some straggling files out there but that risk appears to have been mitigated to the
 * point where I'm comfortable leaving this is a NOOP state
 *
 * Delete blob files that aren't in use. Eventually we probably want to
 * schedule this to run nightly.
 * 
 * @author joel
 *
 */
public class CleanAttachments extends Job {

    @Override
    public void doJob() throws Exception {
//    	List<String> keepers = new ArrayList<String>();
//    	List<User> users = User.findAll();
//    	for(User user : users){
//    		S3Blob blob = user.picture;
//    		if(blob != null){
//    			//keepers.add();
//    		}
//    	}
//
//    	File bStore = Blob.getStore();
//    	File[] files = bStore.listFiles();
//    	for(File file : files){
//    		String name = file.getName();
//    		if(!keepers.contains(name)){
//    			file.delete();
//    		}
//    	}
    }
}
