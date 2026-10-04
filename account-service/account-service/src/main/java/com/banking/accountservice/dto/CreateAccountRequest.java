package com.banking.accountservice.dto;

import com.banking.accountservice.entity.AccountStatus;
import com.banking.accountservice.entity.AccountType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;


import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateAccountRequest {
    @NotBlank(message = "Account holder name is required")
    private String accountHolderName;

    @NotBlank(message = "email is required")
    @Email(message = "enter valid email address")
    private String email;

    @NotBlank(message = "phone number is required")
    private String phone;

    @NotNull(message = "account type is required")
    private AccountType accountType;

    @NotNull(message = "initial deposit is required")
    @Positive(message = "initial deposit must be greater than zero")
    private BigDecimal initialDeposit;

}
