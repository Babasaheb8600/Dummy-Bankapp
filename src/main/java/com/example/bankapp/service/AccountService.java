package com.example.bankapp.service;

import com.example.bankapp.model.Account;
import com.example.bankapp.model.Transaction;
import com.example.bankapp.repository.AccountRepository;
import com.example.bankapp.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Account registerAccount(String username, String password, String email) {
        if (accountRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        Account account = new Account();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(password));
        account.setEmail(email);
        account.setBalance(BigDecimal.ZERO);

        return accountRepository.save(account);
    }

    @Transactional
    public void deposit(String username, BigDecimal amount) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setAmount(amount);
        transaction.setType("DEPOSIT");
        transaction.setTimestamp(LocalDateTime.now());
        transactionRepository.save(transaction);
    }

    @Transactional
    public void withdraw(String username, BigDecimal amount) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setAmount(amount.negate());
        transaction.setType("WITHDRAWAL");
        transaction.setTimestamp(LocalDateTime.now());
        transactionRepository.save(transaction);
    }

    public BigDecimal getBalance(String username) {
        return accountRepository.findByUsername(username)
                .map(Account::getBalance)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    @Transactional
    public void transfer(String fromUsername, String toUsername, BigDecimal amount) {
        Account fromAccount = accountRepository.findByUsername(fromUsername)
                .orElseThrow(() -> new RuntimeException("Source account not found"));

        Account toAccount = accountRepository.findByUsername(toUsername)
                .orElseThrow(() -> new RuntimeException("Target account not found"));

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // Record transactions
        Transaction withdrawalTransaction = new Transaction();
        withdrawalTransaction.setAccount(fromAccount);
        withdrawalTransaction.setAmount(amount.negate());
        withdrawalTransaction.setType("TRANSFER_OUT");
        withdrawalTransaction.setTimestamp(LocalDateTime.now());
        transactionRepository.save(withdrawalTransaction);

        Transaction depositTransaction = new Transaction();
        depositTransaction.setAccount(toAccount);
        depositTransaction.setAmount(amount);
        depositTransaction.setType("TRANSFER_IN");
        depositTransaction.setTimestamp(LocalDateTime.now());
        transactionRepository.save(depositTransaction);
    }
}
