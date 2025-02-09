package com.groovith.groovith.dto;

public interface ResponseMessage {
    String SUCCESS = "Success.";
    String VALIDATION_FAIL = "Validation fail.";
    String CERTIFICATION_FAIL = "Certification fail.";
    String DUPLICATE_ID = "Duplicate ID.";
    String DATABASE_ERROR = "Database error.";
    String MAIL_FAIL = "Mail send fail.";
    String WRONG_PASSWORD = "Wrong password.";
    String NO_SUCH_USER = "No such user.";
    String NOT_MASTER_USER = "Not master user.";
}
