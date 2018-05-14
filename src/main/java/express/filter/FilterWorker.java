package express.filter;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Simon Reinisch
 * <p>
 * Worker modul for FilterTasks.
 */
public class FilterWorker extends TimerTask {

  private final FilterTask middlewareWorker;
  private Timer timer;

  public FilterWorker(FilterTask middlewareWorker) {
    this.middlewareWorker = middlewareWorker;
  }

  public void start() {
    if (this.timer == null) {
      middlewareWorker.onStart();
      this.timer = new Timer();
      this.timer.scheduleAtFixedRate(this, 0, middlewareWorker.getDelay());
    }
  }

  public void stop() {
    if (timer != null) {
      middlewareWorker.onStop();
      this.timer.cancel();
      this.timer = null;
    }
  }

  public boolean isActive() {
    return timer != null;
  }

  @Override
  public void run() {
    middlewareWorker.onUpdate();
  }
}
