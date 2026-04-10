package main.web.rest.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;
import tech.jhipster.web.rest.errors.ProblemDetailWithCause;
import tech.jhipster.web.rest.errors.ProblemDetailWithCause.ProblemDetailWithCauseBuilder;

public class EventoPienoException extends ErrorResponseException {

    private static final long serialVersionUID = 1L;

    public EventoPienoException() {
        super(
            HttpStatus.CONFLICT,
            ProblemDetailWithCauseBuilder.instance()
                .withStatus(HttpStatus.CONFLICT.value())
                .withType(ErrorConstants.DEFAULT_TYPE)
                .withTitle("Evento al completo")
                .withProperty("message", "error.eventoPieno")
                .build(),
            null
        );
    }
}
