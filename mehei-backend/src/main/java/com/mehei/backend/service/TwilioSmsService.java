package com.mehei.backend.service;

import com.twilio.Twilio;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TwilioSmsService {

    private static final Logger log = LoggerFactory.getLogger(TwilioSmsService.class);

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.verify-service-sid}")
    private String verifyServiceSid;

    @PostConstruct
    public void init() {
        if (accountSid == null || accountSid.contains("YOUR_TWILIO") || authToken == null || authToken.contains("YOUR_TWILIO")) {
            log.warn("Twilio credentials are not properly configured. OTPs will not be sent.");
        } else {
            Twilio.init(accountSid, authToken);
            log.info("Twilio Verify initialized successfully with Service SID: {}", verifyServiceSid);
        }
    }

    public void sendVerificationCode(String toPhoneNumber) {
        if (accountSid == null || accountSid.contains("YOUR_TWILIO")) {
            log.error("Cannot send OTP. Twilio is not configured. Please update application.properties.");
            return;
        }

        try {
            Verification verification = Verification.creator(
                    verifyServiceSid,
                    toPhoneNumber,
                    "sms"
            ).create();
            
            log.info("Twilio Verification sent to {}. Status: {}", toPhoneNumber, verification.getStatus());
        } catch (Exception e) {
            log.error("Failed to send verification code to {}. Error: {}", toPhoneNumber, e.getMessage());
        }
    }

    public boolean checkVerificationCode(String toPhoneNumber, String code) {
        if (accountSid == null || accountSid.contains("YOUR_TWILIO")) {
            log.warn("Twilio not configured. Accepting all OTPs for testing purposes.");
            return true; // Mock success if Twilio isn't set up yet
        }

        try {
            VerificationCheck verificationCheck = VerificationCheck.creator(verifyServiceSid)
                    .setTo(toPhoneNumber)
                    .setCode(code)
                    .create();

            boolean isApproved = "approved".equals(verificationCheck.getStatus());
            log.info("Twilio Verification check for {}. Status: {}", toPhoneNumber, verificationCheck.getStatus());
            return isApproved;
        } catch (Exception e) {
            log.error("Failed to check verification code for {}. Error: {}", toPhoneNumber, e.getMessage());
            return false;
        }
    }
}
