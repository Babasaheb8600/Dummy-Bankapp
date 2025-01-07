package com.example.bankapp.controller;

import com.example.bankapp.model.Account;
import com.example.bankapp.model.Transaction;
import com.example.bankapp.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BankController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/account")
    public ResponseEntity<?> getAccountDetails(Authentication authentication) {
        BigDecimal balance = accountService.getBalance(authentication.getName());
        return ResponseEntity.ok(Map.of(
            "username", authentication.getName(),
            "balance", balance
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerAccount(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        String email = request.get("email");

        Account account = accountService.registerAccount(username, password, email);
        return ResponseEntity.ok(Map.of(
            "message", "Account created successfully",
            "username", account.getUsername()
        ));
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(
            Authentication authentication,
            @RequestBody Map<String, BigDecimal> request) {
        BigDecimal amount = request.get("amount");
        accountService.deposit(authentication.getName(), amount);
        return ResponseEntity.ok(Map.of(
            "message", "Deposit successful",
            "balance", accountService.getBalance(authentication.getName())
        ));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(
            Authentication authentication,
            @RequestBody Map<String, BigDecimal> request) {
        BigDecimal amount = request.get("amount");
        accountService.withdraw(authentication.getName(), amount);
        return ResponseEntity.ok(Map.of(
            "message", "Withdrawal successful",
            "balance", accountService.getBalance(authentication.getName())
        ));
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(
            Authentication authentication,
            @RequestBody Map<String, Object> request) {
        String toUsername = (String) request.get("toUsername");
        BigDecimal amount = new BigDecimal(request.get("amount").toString());

        accountService.transfer(authentication.getName(), toUsername, amount);
        return ResponseEntity.ok(Map.of(
            "message", "Transfer successful",
            "balance", accountService.getBalance(authentication.getName())
        ));
    }
}
