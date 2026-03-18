package com.waitless.notificationservice.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {

    @Value("${app.twilio.account-sid}")
    private String accountSid;

    @Value("${app.twilio.auth-token}")
    private String authToken;

    @Value("${app.twilio.phone-number}")
    private String fromPhoneNumber;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
        log.info("Twilio initialisé avec succès");
    }

    public void sendSms(String toPhoneNumber, String content) {
        try {
            Message message = Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(fromPhoneNumber),
                    content
            ).create();
            log.info("SMS envoyé avec succès à {}. SID: {}", toPhoneNumber, message.getSid());
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi du SMS à {}: {}", toPhoneNumber, e.getMessage());
        }
    }
}
