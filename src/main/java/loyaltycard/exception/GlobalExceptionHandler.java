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

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorDto> handleCustomerNotFoundException(CustomerNotFoundException e) {

        log.error(e.getMessage());

        ErrorDto errorDto = new ErrorDto(e.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDto);
    }

    @ExceptionHandler(CustomerAlreadyExistsException.class)
    public ResponseEntity<ErrorDto> handleCustomerAlreadyExistsException(CustomerAlreadyExistsException e) {

        log.error(e.getMessage());

        ErrorDto errorDto = new ErrorDto(e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorDto> handleInvalidCredentialsException(InvalidCredentialsException e) {

        log.error(e.getMessage());

        ErrorDto errorDto = new ErrorDto(e.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorDto);
    }

    @ExceptionHandler(AccountIsBlockedException.class)
    public ResponseEntity<ErrorDto> handleAccountIsBlockedException(AccountIsBlockedException e) {

        log.error(e.getMessage());

        ErrorDto errorDto = new ErrorDto(e.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorDto);
    }

    @ExceptionHandler(CardNotFoundException.class)
    public ResponseEntity<ErrorDto> handleCardNotFoundException(CardNotFoundException e) {

        log.error(e.getMessage());

        ErrorDto errorDto = new ErrorDto(e.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDto);
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<ErrorDto> handleTransactionNotFoundException(TransactionNotFoundException e) {

        log.error(e.getMessage());

        ErrorDto errorDto = new ErrorDto(e.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDto);
    }

    @ExceptionHandler(CardIsBlockedException.class)
    public ResponseEntity<ErrorDto> handleCardIsBlockedException(CardIsBlockedException e) {

        log.error(e.getMessage());

        ErrorDto errorDto = new ErrorDto(e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
    }

    @ExceptionHandler(TransactionAlreadyCanceledException.class)
    public ResponseEntity<ErrorDto> handleTransactionAlreadyCanceledException(TransactionAlreadyCanceledException e) {

        log.error(e.getMessage());

        ErrorDto errorDto = new ErrorDto(e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
    }
}

