package at.technikum.orm.exceptions;

public class ReflectiveAccesException extends RuntimeException {
  public ReflectiveAccesException(String message) {
    super(message);
  }

  public ReflectiveAccesException(String message, Throwable cause) {
    super(message, cause);
  }
}
