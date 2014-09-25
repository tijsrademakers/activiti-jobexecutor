package org.activiti.beans;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.AcquiredJobs;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.activiti.engine.impl.persistence.entity.JobEntity;

public class CustomAcquireJobsCmdV1 implements Command<AcquiredJobs> {

  private final JobExecutor jobExecutor;

  public CustomAcquireJobsCmdV1(JobExecutor jobExecutor) {
    this.jobExecutor = jobExecutor;
  }
  
  public AcquiredJobs execute(CommandContext commandContext) {
  	
    String lockOwner = jobExecutor.getLockOwner();
    int lockTimeInMillis = jobExecutor.getLockTimeInMillis();
    int maxNonExclusiveJobsPerAcquisition = jobExecutor.getMaxJobsPerAcquisition();
    
    AcquiredJobs acquiredJobs = new AcquiredJobs();
    List<JobEntity> jobs = commandContext
      .getJobEntityManager()
      .findNextJobsToExecute(new Page(0, maxNonExclusiveJobsPerAcquisition));

    List<String> jobIds = new ArrayList<String>();
    for (JobEntity job: jobs) {
      if (job != null && !acquiredJobs.contains(job.getId())) {
        if (job.isExclusive() && job.getProcessInstanceId() != null) {
          // wait to get exclusive jobs within 100ms
          /*try {
            Thread.sleep(100);
          } catch (InterruptedException e) {}*/
          
          // acquire all exclusive jobs in the same process instance
          // (includes the current job)
          /*List<JobEntity> exclusiveJobs = commandContext.getJobEntityManager()
            .findExclusiveJobsToExecute(job.getProcessInstanceId());
          for (JobEntity exclusiveJob : exclusiveJobs) {
            if (exclusiveJob != null) {
              lockJob(commandContext, exclusiveJob, lockOwner, lockTimeInMillis);
              jobIds.add(exclusiveJob.getId());
            }
          }*/
          lockJob(commandContext, job, lockOwner, lockTimeInMillis);
          jobIds.add(job.getId());
          
        } else {
          lockJob(commandContext, job, lockOwner, lockTimeInMillis);
          jobIds.add(job.getId());
        }
        
      }
    }
    
    acquiredJobs.addJobIdBatch(jobIds);

    return acquiredJobs;
  }

  protected void lockJob(CommandContext commandContext, JobEntity job, String lockOwner, int lockTimeInMillis) {    
    job.setLockOwner(lockOwner);
    GregorianCalendar gregorianCalendar = new GregorianCalendar();
    gregorianCalendar.setTime(commandContext.getProcessEngineConfiguration().getClock().getCurrentTime());
    gregorianCalendar.add(Calendar.MILLISECOND, lockTimeInMillis);
    job.setLockExpirationTime(gregorianCalendar.getTime());    
  }
}