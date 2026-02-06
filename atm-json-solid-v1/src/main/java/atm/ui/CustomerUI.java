package atm.ui;

import atm.domain.ATMState;
import atm.domain.AccountType;
import atm.repo.AtmStateRepository;
import atm.service.AuthService;
import atm.service.CustomerSession;
import atm.service.CustomerTransactionService;

import java.util.Optional;
import java.util.Scanner;

public class CustomerUI extends BaseConsoleUI {

    private final AuthService authService;
    private final CustomerTransactionService txService;
    private final AtmStateRepository atmRepo;

    public CustomerUI(Scanner sc, AuthService authService, CustomerTransactionService txService, AtmStateRepository atmRepo) {
        super(sc);
        this.authService = authService;
        this.txService = txService;
        this.atmRepo = atmRepo;
    }

    // Entry point used by AppV1/AppV2
    public void start() {
        header("Customer Login");
        String cardNumber = readLine("Card number: ");
        String pin = readLine("PIN: ");

        Optional<CustomerSession> sessionOpt = authService.authenticateCustomer(cardNumber, pin);
        if (sessionOpt.isEmpty()) {
            System.out.println("Invalid card number or PIN.");
            return;
        }

        startSession(sessionOpt.get());
    }

    private void startSession(CustomerSession session) {
        while (true) {
            header("Customer Menu");
            System.out.println("1) Check balance");
            System.out.println("2) Deposit");
            System.out.println("3) Withdraw");
            System.out.println("4) Transfer funds");
            System.out.println("0) Logout");

            int choice = readInt("Choose: ");

            switch (choice) {
                case 1 -> checkBalance(session);
                case 2 -> deposit(session);
                case 3 -> withdraw(session);
                case 4 -> transfer(session);
                case 0 -> {
                    System.out.println("Logged out.");
                    return;
                }
                default -> System.out.println("Invalid choice. Please select one of the menu options.");
            }
        }
    }

    private void checkBalance(CustomerSession session) {
        header("Check Balance");
        String result = txService.getBalance(session.getCustomerId());
        System.out.println(result);
    }

    private void deposit(CustomerSession session) {
        header("Deposit");

        long euros = readLong("Enter amount in euros: ");

        // STRICT validation BEFORE receipt question
        if (!isValidAmountMultipleOf5(euros)) {
            System.out.println("Invalid amount. Deposit must be > 0 and divisible by 5€.");
            return;
        }

        long cents = euros * 100;

        ReceiptDecision receipt = decideReceipt();
        if (receipt.cancelled) return;

        txService.deposit(session.getCustomerId(), AccountType.CHECKING, cents);
        System.out.println("Deposit successful.");

        String receiptText = txService.buildReceiptText(
                session.getCustomerName(),
                "DEPOSIT",
                "Amount: " + euros + "€ to CHECKING"
        );

        txService.printReceiptIfWanted(atmRepo.loadState(), receiptText, receipt.wantsReceipt, receipt.proceedWithoutReceipt);
    }

    private void withdraw(CustomerSession session) {
        header("Withdraw");

        long euros = readLong("Enter amount in euros: ");

        // STRICT validation BEFORE receipt question
        if (!isValidAmountMultipleOf5(euros)) {
            System.out.println("Invalid amount. Withdraw must be > 0 and divisible by 5€.");
            return;
        }

        long cents = euros * 100;

        ReceiptDecision receipt = decideReceipt();
        if (receipt.cancelled) return;

        txService.withdraw(session.getCustomerId(), AccountType.CHECKING, cents);
        System.out.println("Withdraw successful.");

        String receiptText = txService.buildReceiptText(
                session.getCustomerName(),
                "WITHDRAW",
                "Amount: " + euros + "€ from CHECKING"
        );

        txService.printReceiptIfWanted(atmRepo.loadState(), receiptText, receipt.wantsReceipt, receipt.proceedWithoutReceipt);
    }

    private void transfer(CustomerSession session) {
        header("Transfer Funds");
        System.out.println("1) CHECKING -> SAVINGS");
        System.out.println("2) SAVINGS -> CHECKING");

        int dir = readInt("Choose: ");

        // STRICT validation: only 1 or 2
        if (dir != 1 && dir != 2) {
            System.out.println("Invalid choice. Please select 1 or 2.");
            return;
        }

        AccountType from = (dir == 1) ? AccountType.CHECKING : AccountType.SAVINGS;
        AccountType to = (dir == 1) ? AccountType.SAVINGS : AccountType.CHECKING;

        long euros = readLong("Enter amount in euros: ");

        // STRICT validation BEFORE receipt question
        if (!isValidAmountMultipleOf5(euros)) {
            System.out.println("Invalid amount. Transfer must be > 0 and divisible by 5€.");
            return;
        }

        long cents = euros * 100;

        ReceiptDecision receipt = decideReceipt();
        if (receipt.cancelled) return;

        txService.transfer(session.getCustomerId(), from, to, cents);
        System.out.println("Transfer successful.");

        String receiptText = txService.buildReceiptText(
                session.getCustomerName(),
                "TRANSFER",
                "Amount: " + euros + "€ from " + from + " to " + to
        );

        txService.printReceiptIfWanted(atmRepo.loadState(), receiptText, receipt.wantsReceipt, receipt.proceedWithoutReceipt);
    }

    private ReceiptDecision decideReceipt() {
        boolean wantsReceipt = askYesNo("Print receipt? (y/n): ");

        if (!wantsReceipt) {
            return new ReceiptDecision(false, false, false);
        }

        ATMState state = atmRepo.loadState();
        if (state.canPrintReceipt()) {
            return new ReceiptDecision(true, false, false);
        }

        System.out.println("ATM is out of paper and/or ink.");
        boolean proceedWithoutReceipt = askYesNo("Proceed WITHOUT receipt? (y/n): ");

        if (!proceedWithoutReceipt) {
            System.out.println("Transaction cancelled.");
            return new ReceiptDecision(false, false, true);
        }

        return new ReceiptDecision(true, true, false);
    }

    private boolean askYesNo(String prompt) {
        while (true) {
            String s = readLine(prompt).trim().toLowerCase();
            if (s.equals("y") || s.equals("yes")) return true;
            if (s.equals("n") || s.equals("no")) return false;
            System.out.println("Invalid input. Please type y/n.");
        }
    }

    private boolean isValidAmountMultipleOf5(long amountEuro) {
        return amountEuro > 0 && amountEuro % 5 == 0;
    }

    private static class ReceiptDecision {
        final boolean wantsReceipt;
        final boolean proceedWithoutReceipt;
        final boolean cancelled;

        ReceiptDecision(boolean wantsReceipt, boolean proceedWithoutReceipt, boolean cancelled) {
            this.wantsReceipt = wantsReceipt;
            this.proceedWithoutReceipt = proceedWithoutReceipt;
            this.cancelled = cancelled;
        }
    }
}
