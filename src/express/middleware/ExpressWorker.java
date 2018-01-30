package express.middleware;


import express.utils.ExtendetTimer;

public abstract class ExpressWorker {

  private final ExtendetTimer TIMER = new ExtendetTimer();

  public void start() {
    if (!TIMER.isActive()) {
      TIMER.scheduleAtFixedRate(this::update, 0, getDelay());
    }
  }

  public void stop() {
    TIMER.cancel();
  }

  abstract long getDelay();

  abstract void update();
}
