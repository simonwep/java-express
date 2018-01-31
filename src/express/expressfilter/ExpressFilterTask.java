package express.expressfilter;


public interface ExpressFilterTask {

  long getDelay();

  void onUpdate();

  void onStart();

  void onStop();
}
