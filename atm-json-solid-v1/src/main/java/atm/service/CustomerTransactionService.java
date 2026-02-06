package atm.service;

import atm.domain.*;
import atm.repo.AtmStateRepository;
import atm.repo.BankRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class CustomerTransactionService {

    private final BankRepository bankRepo;
    private final AtmStateRepository atmRepo;
    private final ReceiptService receiptService;
    private final CashDispenser dispenser = new CashDispenser();
    private final int maxLogs;

    public CustomerTransactionService(BankRepository bankRepo, AtmStateRepository atmRepo, ReceiptService receiptService, int maxLogs) {
        this.bankRepo = bankRepo;
        this.atmRepo = atmRepo;
        this.receiptService = receiptService;
        this.maxLogs = maxLogs;
    }

    public List<Account> getAccounts(String customerId) {
        return bankRepo.findAccountsByCustomer(customerId);
    }

    public void deposit(String customerId, AccountType type, long amountCents) {
        Account acc = bankRepo.findAccount(customerId, type).orElseThrow();
        acc.deposit(amountCents);
        bankRepo.saveAccount(acc);
        bankRepo.saveAll();
        logCustomer(customerId, TransactionType.DEPOSIT, "Deposit " + Money.formatCents(amountCents) + " to " + type);
    }

    public Map<Integer,Integer> withdraw(String customerId, AccountType type, long amountCents) {
        ATMState state = atmRepo.loadState();

        Account acc = bankRepo.findAccount(customerId, type).orElseThrow();

        Map<Integer,Integer> plan = dispenser.planDispense(state, amountCents);
        acc.withdraw(amountCents);
        dispenser.applyDispense(state, plan);

        bankRepo.saveAccount(acc);
        bankRepo.saveAll();

        atmRepo.saveState(state);
        atmRepo.appendLog(new TransactionLog(LocalDateTime.now().toString(), "CUSTOMER", TransactionType.WITHDRAW,
                "Customer " + customerId + " withdrew " + Money.formatCents(amountCents) + " from " + type), maxLogs);
        atmRepo.saveAll();

        return plan;
    }

    public void transfer(String customerId, AccountType from, AccountType to, long amountCents) {
        if (from == to) throw new IllegalArgumentException("Cannot transfer to same account type.");

        Account aFrom = bankRepo.findAccount(customerId, from).orElseThrow();
        Account aTo = bankRepo.findAccount(customerId, to).orElseThrow();

        aFrom.withdraw(amountCents);
        aTo.deposit(amountCents);

        bankRepo.saveAccount(aFrom);
        bankRepo.saveAccount(aTo);
        bankRepo.saveAll();

        logCustomer(customerId, TransactionType.TRANSFER,
                "Transfer " + Money.formatCents(amountCents) + " from " + from + " to " + to);
    }

    public String buildReceiptText(String customerName, String action, String details) {
        return "ATM RECEIPT\n" +
                "Customer: " + customerName + "\n" +
                "Action: " + action + "\n" +
                "Details: " + details + "\n" +
                "Time: " + LocalDateTime.now();
    }

    public boolean printReceiptIfWanted(ATMState state, String receiptText, boolean wantsReceipt, boolean proceedWithoutReceiptIfNoResources) {
        if (!wantsReceipt) return false;

        if (!receiptService.canPrint(state)) {
            if (proceedWithoutReceiptIfNoResources) return false;
            throw new IllegalStateException("Out of paper/ink and customer chose to cancel.");
        }

        receiptService.printAndConsume(state, receiptText, maxLogs);
        return true;
    }

    private void logCustomer(String customerId, TransactionType type, String message) {
        atmRepo.appendLog(new TransactionLog(LocalDateTime.now().toString(), "CUSTOMER", type,
                "Customer " + customerId + ": " + message), maxLogs);
        atmRepo.saveAll();
    }
    public String getBalance(String customerId) {
        long totalCents = getAccounts(customerId)
                .stream()
                .mapToLong(Account::getBalanceCents)
                .sum();

        return receiptService.formatBalance(totalCents);
    }

}