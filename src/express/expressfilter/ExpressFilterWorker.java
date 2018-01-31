package express.expressfilter;

import java.util.Timer;
import java.util.TimerTask;

public class ExpressFilterWorker extends TimerTask {

  private final ExpressFilterTask MW;
  private Timer timer;

  public ExpressFilterWorker(ExpressFilterTask middlewareWorker) {
    this.MW = middlewareWorker;
  }

  public void start() {
    if (this.timer == null) {
      this.timer = new Timer();
      this.timer.scheduleAtFixedRate(this, 0, MW.getDelay());
    }
  }

  public void stop() {
    if (timer != null) {
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
