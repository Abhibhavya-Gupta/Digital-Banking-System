package com.banking.transactionservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferRequest {
    @NotBlank(message = "Please enter Sender's Account Number")
    private String senderAccountNumber;

    @NotBlank(message = "Please enter Recipient's Account Number")
    private String recipientAccountNumber;

    @NotNull(message = "Please enter an amount")
    @Positive(message = "Please enter a valid amount")
    private BigDecimal amount;

    private String description;
}
