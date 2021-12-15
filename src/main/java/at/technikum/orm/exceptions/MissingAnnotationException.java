package at.technikum.orm.exceptions;

public class MissingAnnotationException extends RuntimeException {
  public MissingAnnotationException(String annotation) {
    super(annotation);
  }
}
