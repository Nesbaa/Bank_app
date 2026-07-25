package service.impl;

import domain.Account;
import domain.Customer;
import domain.Transaction;
import domain.Type;
import exceptions.AccountNotFoundException;
import exceptions.InsufficientFundException;
import exceptions.ValidationException;
import repository.AccountRepository;
import repository.CustomerRepository;
import repository.TransactionRepository;
import service.BankService;
import util.Validation;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;



public class BankServiceImpl implements BankService {

    private final AccountRepository accountRepository = new AccountRepository();
    private final TransactionRepository transactionRepository = new TransactionRepository();
    private final CustomerRepository customerRepository = new CustomerRepository();

    private final Validation<String> validationName = name -> {
        if (name == null || name.isBlank()) throw new ValidationException("Name is required");
    };

    private final Validation<String> validationEmail = email -> {
        if (email == null || !email.contains("@")) throw new ValidationException("email is required");
    };

    private final Validation<String> validationType = type -> {
        if (type == null || !(type.equalsIgnoreCase("SAVINGS") || type.contains("CURRENT")))
            throw new ValidationException("Type is required SAVINGS or CURRENT");
    };

    private final Validation<Double> validationAmountPositive = amount -> {
        if (amount == null || amount < 0)
            throw new ValidationException("please enter valid amount");
    };

    @Override
    public String openAccount(String name, String email, String accountType) {
        validationName.Validate(name);
        validationEmail.Validate(email);
        validationType.Validate(accountType);

        String customerId = UUID.randomUUID().toString();

        // create customer
        Customer c = new Customer(email, customerId, name);
        customerRepository.save(c);

        //change later --> 10 + 1 = AC11
        //String accountNumber = UUID.randomUUID().toString();
        String accountNumber = getAccountNumber();
        Account account = new Account(accountNumber,accountType,(double) 0,customerId);
        // save
        accountRepository.save(account);
        return accountNumber;
    }

    @Override
    public List<Account> listAccounts() {
        return accountRepository.findAll().stream()
                .sorted(Comparator.comparing(Account::getAccountNumber))
                .collect(Collectors.toList());
    }

    @Override
    public void deposit(String accountNumber, Double amount, String note) {
        validationAmountPositive.Validate(amount);
        Account account = accountRepository.findByNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));
        account.setBalance(account.getBalance() + amount);
        Transaction transaction = new Transaction(account.getAccountNumber(),
                amount, UUID.randomUUID().toString(), note, LocalDateTime.now(), Type.Deposit);
        transactionRepository.add(transaction);

    }

    @Override
    public void withdraw(String accountNumber, Double amount, String note) {
        validationAmountPositive.Validate(amount);
        Account account = accountRepository.findByNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));
        if (account.getBalance().compareTo(amount) < 0)
            throw new InsufficientFundException("Insufficient Balance");
        account.setBalance(account.getBalance() - amount);
        Transaction transaction = new Transaction(account.getAccountNumber(),
                amount, UUID.randomUUID().toString(), note, LocalDateTime.now(), Type.Withdraw);
        transactionRepository.add(transaction);
    }

    @Override
    public void transfer(String fromAccount, String toAccount, Double amount, String note) {
        validationAmountPositive.Validate(amount);
        if (fromAccount.equals(toAccount))
            throw new ValidationException("cannot transfer to you own account");
        Account from = accountRepository.findByNumber(fromAccount)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + fromAccount));

        Account to = accountRepository.findByNumber(toAccount)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + toAccount));
        if (from.getBalance().compareTo(amount) < 0)
            throw new InsufficientFundException("Insufficient Balance");

        from.setBalance(from.getBalance() - amount);
        to.setBalance(to.getBalance() + amount);

        Transaction fromTransaction = new Transaction(from.getAccountNumber(),
                amount, UUID.randomUUID().toString(), note, LocalDateTime.now(), Type.Transfer_OUT);
        transactionRepository.add(fromTransaction);

        Transaction toTransaction = new Transaction(to.getAccountNumber(),
                amount, UUID.randomUUID().toString(), note, LocalDateTime.now(), Type.Transfer_IN);
        transactionRepository.add(toTransaction);
    }

    @Override
    public List<Transaction> getStatement(String account) {
        return transactionRepository.findByAccount(account).stream()
                .sorted(Comparator.comparing(Transaction::getTimestamp))
                .collect(Collectors.toList());
    }

    @Override
    public List<Account> searchAccountsByCustomerName(String q) {
        String query = (q == null) ? "" : q.toLowerCase();
//        List<Account> result = new ArrayList<>();
//        for (Customer c : customerRepository.findAll()){
//            if (c.getName().toLowerCase().contains(query))
//                result.addAll(accountRepository.findByCustomerId(c.getId()));
//
//
//        }
//        result.sort(Comparator.comparing(Account::getAccountNumber));
//        return result;

       return customerRepository.findAll().stream()
               .filter(c -> c.getName().toLowerCase().contains(query))
               .flatMap(c -> accountRepository.findByCustomerId(c.getId()).stream())
               .sorted(Comparator.comparing(Account::getAccountNumber))
               .collect(Collectors.toList());
    }


    private String getAccountNumber() {
        int size = accountRepository.findAll().size() + 1;
        return String.format("AC%06d", size);
    }
}
