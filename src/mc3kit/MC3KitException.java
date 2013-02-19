package mc3kit;

@SuppressWarnings("serial")
public class MC3KitException extends Exception {
  public MC3KitException() {
    super();
  }
  
  public MC3KitException(Throwable cause) {
    super(cause);
  }
  
  public MC3KitException(String msg) {
    super(msg);
  }

  public MC3KitException(String msg, Throwable cause) {
    super(msg, cause);
  }

}
