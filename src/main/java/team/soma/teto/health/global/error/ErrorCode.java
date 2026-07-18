package team.soma.teto.health.global.error;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

    String code();

    String message();

    HttpStatus status();
}
