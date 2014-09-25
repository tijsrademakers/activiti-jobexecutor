package org.activiti.job;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.runtime.Job;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobTest {
  
  private static final Logger logger = LoggerFactory.getLogger(JobTest.class);
  
  protected ProcessEngine processEngine;
  
  @Before
  public void initEngine() {
    processEngine = ProcessEngines.getDefaultProcessEngine();
    processEngine.getRepositoryService().createDeployment().name("job").addClasspathResource("job.bpmn20.xml").deploy();
  }
  
  @Test
  public void testSingleJob() throws Exception {
    processEngine.getRuntimeService().startProcessInstanceByKey("job");
    List<Job> jobs = processEngine.getManagementService().createJobQuery().list();
    System.out.println("jobs " + jobs);
    Thread.sleep(2000);
    List<HistoricProcessInstance> processes = processEngine.getHistoryService().createHistoricProcessInstanceQuery().list();
    System.out.println("process " + processes.get(0).getEndTime());
  }

  @Test
  public void test() throws Exception {
    ExecutorService executor = Executors.newFixedThreadPool(5);
    long nrOfJobs = 500L;
    for (int i = 0; i < nrOfJobs; i++) {
      Runnable worker = new StartThread();
      executor.execute(worker);
    }
    executor.shutdown();
    System.out.println("Finished all start threads");
    
    boolean finishedAllInstances = false;
    long lastPrintTime = 0;
    long start = System.currentTimeMillis();
    while (finishedAllInstances == false) {
      long finishedCount = processEngine.getHistoryService().createHistoricProcessInstanceQuery().finished().count();
      if (finishedCount == nrOfJobs) {
        finishedAllInstances = true;
      } else {
      	if (System.currentTimeMillis() - lastPrintTime > 5000L) {
      		lastPrintTime = System.currentTimeMillis();
      		logger.error("finished " + finishedCount);
      	}
        Thread.sleep(50);
      }
    }
    long end = System.currentTimeMillis();
    
    long time = end - start;
    System.out.println("Took " + time + " ms");
    double perSecond = ( (double) nrOfJobs / (double) time) * 1000;
    System.out.println("Which is " + perSecond + " processes/second");
    
  }
  
  public class StartThread implements Runnable {

    public void run() {
      processEngine.getRuntimeService().startProcessInstanceByKey("job");
    }
    
  }

}
