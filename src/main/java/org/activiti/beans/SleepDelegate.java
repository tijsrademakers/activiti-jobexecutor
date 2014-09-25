package org.activiti.beans;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;

public class SleepDelegate implements JavaDelegate {

  protected Expression sleepTime;

  public void execute(DelegateExecution execution) throws Exception {
    if (sleepTime != null) {
      Object value = sleepTime.getValue(execution);
      if (value != null) {
        Long sleepTime = Long.valueOf(value.toString());
        Thread.sleep(sleepTime);
      }
    }
  }

  public void setSleepTime(Expression sleepTime) {
    this.sleepTime = sleepTime;
  }
}
