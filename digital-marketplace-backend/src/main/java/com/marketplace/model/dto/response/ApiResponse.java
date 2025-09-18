package com.marketplace.model.dto.response;

import java.time.LocalDateTime;

public class ApiResponse {
    private boolean success;
    private String message;
    private LocalDateTime timestamp;


    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
