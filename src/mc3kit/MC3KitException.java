package mc3kit;

@SuppressWarnings("serial")
public class MC3KitException extends Exception {

  public MC3KitException(String fmt, Object... args) {
    super(String.format(fmt, args));
  }

  public MC3KitException(Throwable cause, String fmt, Object... args) {
    super(String.format(fmt, args), cause);
  }

}
