package com.waitless.user.exception;

import java.time.LocalDateTime;

public class UserSuspendedException extends RuntimeException {

    private final LocalDateTime suspensionEndDate;

    public UserSuspendedException(String message, LocalDateTime suspensionEndDate) {
        super(message);
        this.suspensionEndDate = suspensionEndDate;
    }

    public LocalDateTime getSuspensionEndDate() {
        return suspensionEndDate;
    }

}
