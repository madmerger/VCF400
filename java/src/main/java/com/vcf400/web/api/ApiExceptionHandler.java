package com.vcf400.web.api;

import com.vcf400.service.AddVoteService;
import com.vcf400.service.RpgMessages;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public AddVoteService.VoteResult validation(MethodArgumentNotValidException ignored) {
        return new AddVoteService.VoteResult(false, RpgMessages.BADGE, "VOTE1");
    }
}
