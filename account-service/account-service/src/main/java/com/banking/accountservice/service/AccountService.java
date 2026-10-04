package com.banking.accountservice.service;

import com.banking.accountservice.dto.AccountResponse;
import com.banking.accountservice.dto.CreateAccountRequest;
import com.banking.accountservice.entity.Account;
import com.banking.accountservice.entity.AccountStatus;
import com.banking.accountservice.entity.AccountType;
import com.banking.accountservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountResponse createAccount(CreateAccountRequest request){
        log.info("Creating account for: {}",request.getEmail());

        if(accountRepository.existsByEmail(request.getEmail()))
        {
            throw new RuntimeException("account already exists for email: "+request.getEmail());
        }
        Account account = new Account();
        account.setAccountHolderName(request.getAccountHolderName());
        account.setEmail(request.getEmail());
        account.setPhone(request.getPhone());
        account.setStatus(AccountStatus.ACTIVE);
        account.setAccountType(request.getAccountType());
        account.setBalance(request.getInitialDeposit());
        account.setAccountNumber(generateAccountNumber());
        account.setDailyTransactionLimit(
                request.getAccountType() == AccountType.SAVING
                        ?new BigDecimal("100000")
                        : new BigDecimal("500000")
        );

        Account savedAccount = accountRepository.save(account);
        log.info("Account created for: {}",savedAccount.getAccountNumber());
        return mapToResponse(savedAccount);

    }
    /**
     * get account by account number
     * @param accountNumber
     */
    public AccountResponse getAccount(String accountNumber){
        Account account = accountRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(
                        () -> new RuntimeException("account not found for accountNumber: "+accountNumber)
                );
        return mapToResponse(account);
    }

    /**
     * get Balance
     * @param accountNumber
     */
    public BigDecimal getBalance(String accountNumber){
        Account account = accountRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(
                        () -> new RuntimeException("account not found for accountNumber: "+accountNumber)
                );
        return account.getBalance();
    }

    /**
     * Block Account - called by fraud detection service via kafka
     * @param accountNumber
     */
    public void blockAccount(String accountNumber){
        log.info("Blocking account : {}",accountNumber);
        Account account = accountRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(
                        () -> new RuntimeException("account not found for accountNumber: "+accountNumber)
                );
        account.setStatus(AccountStatus.BLOCKED);
        accountRepository.save(account);
        log.info("Account blocked : {}",accountNumber);
    }

    /**
     *deduct balance from the sender's account
     * Called by Transaction Service
     * @param accountNumber
     * @param amount
     */
    public void deductBalance(String accountNumber, BigDecimal amount){
        log.info("Deducting balance : {} from account {}",amount,accountNumber);
        Account account = accountRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(
                        () -> new RuntimeException("account not found for accountNumber: "+accountNumber)
                );
        if(account.getStatus() != AccountStatus.ACTIVE)
        {
            throw new RuntimeException("Account is not active for accountNumber: "+accountNumber);
        }
        if(account.getBalance().compareTo(amount) <0){
            throw new RuntimeException("Insufficient funds in the account: "+accountNumber);
        }
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        log.info("Balance updated, new balance : {}",account.getBalance());

    }

    /**
     *credit balance
     * Called by Transaction Service via kafka
     * @param accountNumber
     * @param amount
     */
    public void creditBalance(String accountNumber, BigDecimal amount) {
        log.info("Credit balance : {} to account {}",amount,accountNumber);
        Account account = accountRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(
                        () -> new RuntimeException("account not found for accountNumber: "+accountNumber)
                );

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
        log.info("Credited balance, new balance : {}",account.getBalance());
    }


    //generate unique 12 dig account number
    private String generateAccountNumber() {
        String accountNumber = "";
        SecureRandom secureRandom = new SecureRandom();

        do{
            long number = secureRandom.nextLong(10000000000000L);
            accountNumber = String.format("%012d",number);
        }while(accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }



    //response dto
    private AccountResponse mapToResponse(Account savedAccount) {
        AccountResponse accountResponse = new AccountResponse();
        accountResponse.setId(savedAccount.getId());
        accountResponse.setAccountHolderName(savedAccount.getAccountHolderName());
        accountResponse.setEmail(savedAccount.getEmail());
        accountResponse.setPhone(savedAccount.getPhone());
        accountResponse.setBalance(savedAccount.getBalance());
        accountResponse.setAccountNumber(savedAccount.getAccountNumber());
        accountResponse.setDailyTransactionLimit(savedAccount.getDailyTransactionLimit());
        accountResponse.setAccountType(savedAccount.getAccountType());
        return accountResponse;

    }


}
