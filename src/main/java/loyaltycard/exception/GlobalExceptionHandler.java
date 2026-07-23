package loyaltycard.exception;

import lombok.extern.slf4j.Slf4j;
import loyaltycard.controller.dto.ErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AdminNotFoundException.class)
    public ResponseEntity<ErrorDto> handleAdminNotFoundException(AdminNotFoundException e) {

        log.error(e.getMessage());

        ErrorDto errorDto = new ErrorDto(e.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDto);
    }

    @ExceptionHandler(AdminAlreadyExistException.class)
    public ResponseEntity<ErrorDto> handleAdminAlreadyExistException(AdminAlreadyExistException e) {

        log.error(e.getMessage());

        ErrorDto errorDto = new ErrorDto(e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
    }


}
