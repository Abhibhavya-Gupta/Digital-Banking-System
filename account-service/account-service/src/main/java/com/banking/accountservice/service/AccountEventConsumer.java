package com.banking.accountservice.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountEventConsumer {
    AccountService accountService;

    /**
     * Consume transaction.completed event from kafka
     * Credits receiver account
     * @param payload
     */
    @KafkaListener(topics="transaction.completed")
    public void consumeTransactionCompleted(
            @Payload Map<String,Object> payload
            ) {
        try{
            String receiverAccount = (String)payload.get("receiverAccount");
            BigDecimal amount = new BigDecimal(payload.get("amount").toString());

            log.info("crediting amount {} to account {}",amount,receiverAccount);
            accountService.creditBalance(receiverAccount,amount);
        }catch(Exception ex){
            log.error("Error crediting account: {}", ex.getMessage());
        }
    }

    /**
     * Consume fraud.detected event from kafka
     * Blocks the flagged account
     * @param payload
     */
    @KafkaListener(topics="fraud.detected")
    public void consumeFraudDetected(@Payload Map<String,Object> payload) {
        try{
            String accountNumber = (String)payload.get("accountNumber");
            log.info("fraud Detected account - blocking the account : {}",accountNumber);
            accountService.blockAccount(accountNumber);
        } catch (Exception e) {
            log.error("Error blocking account: {}", e.getMessage());
        }
    }
}
