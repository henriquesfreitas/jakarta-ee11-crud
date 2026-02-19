package com.library.util;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.OptimisticLockException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class ExceptionUtil {

    @Inject
    private MessageUtil messageUtil;

    public void handleException(Exception e) {
        if (isOptimisticLockException(e)) {
            log.error("Optimistic lock exception detected", e);
            messageUtil.addErrorMessage("Conflict", "The record was modified by another user. Please reload and try again.");
        } else {
            log.error("Unexpected error", e);
            messageUtil.addErrorMessage("Error", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred.");
        }
    }

    private boolean isOptimisticLockException(Throwable e) {
        if (e instanceof OptimisticLockException) {
            return true;
        }
        if (e.getCause() != null && e.getCause() != e) {
            return isOptimisticLockException(e.getCause());
        }
        return false;
    }
}
