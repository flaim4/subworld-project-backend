package net.flaim.service;

import lombok.Data;
import lombok.Builder;
import net.flaim.model.EmailVerification;

@Data
@Builder
public class VerificationResult {
    private boolean valid;
    private String errorMessage;

    public static VerificationResult success() {
        return VerificationResult.builder().valid(true).build();
    }

    public static VerificationResult failure(String errorMessage) {
        return VerificationResult.builder().valid(false).errorMessage(errorMessage).build();
    }
}