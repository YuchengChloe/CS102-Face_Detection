package src.repository.exceptions;

public class DuplicateStudentIDException extends RuntimeException{
    public DuplicateStudentIDException(String message) {
        super(message);
    }
}
