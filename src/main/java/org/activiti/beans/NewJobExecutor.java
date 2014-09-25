package org.activiti.beans;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.activiti.engine.impl.jobexecutor.DefaultJobExecutor;

public class NewJobExecutor extends DefaultJobExecutor {

  protected int myCorePoolSize = 50;
  protected int myMaxPoolSize = 100;
//  protected int myMaxJobsPerAcquisition = 100;
  
  @Override
  protected void startExecutingJobs() {
    System.out.println("******************");
    System.out.println("my job executor");
    System.out.println("******************");
    if (threadPoolQueue == null) {
      threadPoolQueue = new ArrayBlockingQueue<Runnable>(queueSize);
    }
    if (threadPoolExecutor == null) {
      threadPoolExecutor = new ThreadPoolExecutor(myCorePoolSize, myMaxPoolSize, 0L, TimeUnit.MILLISECONDS, threadPoolQueue);      
      threadPoolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
    }
    startJobAcquisitionThread(); 
  }
  
//  @Override
//  public int getMaxJobsPerAcquisition() {
//    System.out.println("******************");
//    System.out.println("my getMaxJobsPerAcquisition");
//    System.out.println("******************");
//    return myMaxJobsPerAcquisition;
//  }
}
