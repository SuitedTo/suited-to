/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package models;

import javax.persistence.Entity;

/**
 * <p>Represents a job to be run in the future, flattened and serialized for
 * appropriate insertion into a job queue.</p>
 */
@Entity
public class ScheduledJob extends ModelBase {
    
    /**
     * <p>We serialize the job as a string in the format: 
     * "path.to.JobName(param1, param2, param3)"</p>
     * 
     * <p>The path name is relative to jobs.QueueProcessorJob.JOB_PREFIX.</p>
     * 
     * <p>The weird, flattened-string format is mostly for consistency with our
     * first crack at scheduling jobs into a queue, which used Amazon SQS, where
     * all messages in the queue had to be pure strings.</p>
     */
    public String callString;
    
    public ScheduledJob(String callString) {
        this.callString = callString;
    }
}
