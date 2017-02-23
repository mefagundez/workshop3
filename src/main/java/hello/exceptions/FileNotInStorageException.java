package hello.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class FileNotInStorageException extends RuntimeException {
    public FileNotInStorageException(String message) {
        super(message);
    }
}