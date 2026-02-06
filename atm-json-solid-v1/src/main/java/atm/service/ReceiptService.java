package atm.service;

import atm.domain.ATMState;
import atm.domain.TransactionLog;
import atm.domain.TransactionType;
import atm.repo.AtmStateRepository;

import java.time.LocalDateTime;

public class ReceiptService {
    private final AtmStateRepository atmRepo;

    public ReceiptService(AtmStateRepository atmRepo) {
        this.atmRepo = atmRepo;
    }

    public boolean canPrint(ATMState state) {
        return state.canPrintReceipt();
    }

    public void printAndConsume(ATMState state, String receiptText, int maxLogs) {
        state.consumeReceiptResources();
        atmRepo.saveState(state);
        atmRepo.appendLog(new TransactionLog(LocalDateTime.now().toString(), "SYSTEM", TransactionType.BALANCE_INQUIRY,
                "Receipt printed."), maxLogs);
        atmRepo.saveAll();

        System.out.println("\n--- RECEIPT ---");
        System.out.println(receiptText);
        System.out.println("--------------\n");
    }
    public String buildBalanceReceipt(String customerId) {
        return "Balance inquiry completed for customer: " + customerId;
    }
    public String formatBalance(long totalCents) {
        return "Current balance: " + Money.formatCents(totalCents);
    }
}
