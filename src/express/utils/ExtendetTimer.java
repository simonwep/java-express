package express.utils;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class ExtendetTimer extends Timer {

  private ExtendetTimerTask extendetTimerTask;
  private LambdaTimerTask lambdaTimerTask;
  private boolean isActive;

  public void schedule(LambdaTimerTask task, long delay) {
    init(task);
    super.schedule(extendetTimerTask, delay);
  }

  public void schedule(LambdaTimerTask task, Date time) {
    init(task);
    super.schedule(extendetTimerTask, time);
  }

  public void schedule(LambdaTimerTask task, long delay, long period) {
    init(task);
    super.schedule(extendetTimerTask, delay, period);
  }

  public void schedule(LambdaTimerTask task, Date firstTime, long period) {
    init(task);
    super.schedule(extendetTimerTask, firstTime, period);
  }

  public void scheduleAtFixedRate(LambdaTimerTask task, long delay, long period) {
    init(task);
    super.scheduleAtFixedRate(extendetTimerTask, delay, period);
  }

  public void scheduleAtFixedRate(LambdaTimerTask task, Date firstTime, long period) {
    init(task);
    super.scheduleAtFixedRate(extendetTimerTask, firstTime, period);
  }

  private void init(LambdaTimerTask task){
    extendetTimerTask = new ExtendetTimerTask();
    isActive = true;
    this.lambdaTimerTask = task;
  }

  public void cancel() {
    isActive = false;
    super.cancel();
  }

  public boolean isActive() {
    return isActive;
  }

  private class ExtendetTimerTask extends TimerTask {
    @Override
    public void run() {
      lambdaTimerTask.run();
    }
  }

  @FunctionalInterface
  public interface LambdaTimerTask {
    void run();
  }

}