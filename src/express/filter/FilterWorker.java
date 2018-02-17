package express.filter;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Simon Reinisch
 * Worker modul for FilterTasks.
 */
public class FilterWorker extends TimerTask {

  private final FilterTask MW;
  private Timer timer;

  public FilterWorker(FilterTask middlewareWorker) {
    this.MW = middlewareWorker;
  }

  public void start() {
    if (this.timer == null) {
      MW.onStart();
      this.timer = new Timer();
      this.timer.scheduleAtFixedRate(this, 0, MW.getDelay());
    }
  }

  public void stop() {
    if (timer != null) {
      MW.onStop();
      this.timer.cancel();
      this.timer = null;
    }
  }

  public boolean isActive() {
    return timer != null;
  }

  @Override
  public void run() {
    MW.onUpdate();
  }
}
