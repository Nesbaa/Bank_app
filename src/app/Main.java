package app;




import service.BankService;
import service.impl.BankServiceImpl;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        BankService bankService = new BankServiceImpl();
        boolean running = true;
        System.out.println("Welcome to Console Bank");
        while (running){
            System.out.println(""" 
                1) Open Account
                2) Deposit
                3) Withdraw
                4) Transfer
                5) Account Statement
                6) List Account
                7) Search Account by Customer Name
                0) Exit
            """);
            System.out.println("Press: ");
            String Choice = input.nextLine().trim();
            System.out.println("Choice: " +Choice);

            switch (Choice){
                case "1" -> openAccount(input,bankService);
                case "2" -> deposit(input,bankService);
                case "3" -> withdraw(input,bankService);
                case "4" -> transfer(input,bankService);
                case "5" -> statement(input,bankService);
                case "6" -> listAccount(input,bankService);
                case "7" -> searchAccounts(input,bankService);
                case "0" -> running = false;
            }

        }

    }

    private static void openAccount(Scanner input,BankService bankService) {
        System.out.println("Customer name: ");
        String name = input.nextLine().trim();
        System.out.println("Customer email: ");
        String email = input.nextLine().trim();
        System.out.println("Account Type (SAVINGS/CURRENT): ");
        String type = input.nextLine().trim();
        System.out.println("Initial Deposit (optional, blank for 0): ");
        String amountStr = input.nextLine().trim();
        double initial = amountStr.isEmpty() ? 0.0 : Double.parseDouble(amountStr);
        String accountNumber = bankService.openAccount(name, email, type);
        if(initial > 0)
            bankService.deposit(accountNumber,initial,"Initial Deposit");
        System.out.println("Account opened: " + accountNumber);
    }

    private static void deposit(Scanner input, BankService bankService) {
        System.out.println("Account number: ");
        String accountNumber = input.nextLine().trim();
        System.out.println("Amount: ");
        Double amount = Double.valueOf(input.nextLine().trim());
        bankService.deposit(accountNumber,amount,"Deposit");
        System.out.println("Deposited: ");
    }

    private static void withdraw(Scanner input, BankService bankService) {
        System.out.println("Account number: ");
        String accountNumber = input.nextLine().trim();
        System.out.println("Amount: ");
        Double amount = Double.valueOf(input.nextLine().trim());
        bankService.withdraw(accountNumber,amount,"withdraw");
        System.out.println("Withdrawn: ");
    }

    private static void transfer(Scanner input, BankService bankService) {
        System.out.println("From Account: ");
        String fromAccount = input.nextLine().trim();
        System.out.println("To Account: ");
        String toAccount = input.nextLine().trim();
        System.out.println("Amount: ");
        Double amount = Double.valueOf(input.nextLine().trim());
        bankService.transfer(fromAccount, toAccount, amount, "Transfer");
    }

    private static void statement(Scanner input,BankService bankService) {
        System.out.println("Account Number: ");
        String account = input.nextLine().trim();
        bankService.getStatement(account).forEach(t -> {
            System.out.println(t.getTimestamp() + " | " + t.getType() + " | " + t.getAmount() + " | " + t.getNote());
        });
     }

    private static void listAccount(Scanner input,BankService bankService) {
        bankService.listAccounts().forEach(a -> {
            System.out.println(a.getAccountNumber() + " | " + a.getAccountType() + " | " + a.getBalance() + " | " + a.getCustomerId());
        });

    }

    private static void searchAccounts(Scanner input, BankService bankService) {
        System.out.println("Customer name contains: ");
        String q = input.nextLine().trim();
        bankService.searchAccountsByCustomerName(q).forEach(account -> {
            System.out.println(account.getAccountNumber() + " | " + account.getAccountType()+ " | " + account.getBalance());
        });
    }


}


