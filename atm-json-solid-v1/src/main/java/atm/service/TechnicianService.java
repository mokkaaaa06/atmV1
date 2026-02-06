package atm.service;

import atm.domain.ATMState;
import atm.domain.TransactionLog;
import atm.domain.TransactionType;
import atm.repo.AtmStateRepository;

import java.time.LocalDateTime;
import java.util.Map;

public class TechnicianService {
    private final AtmStateRepository atmRepo;
    private final int maxLogs;

    public TechnicianService(AtmStateRepository atmRepo, int maxLogs) {
        this.atmRepo = atmRepo;
        this.maxLogs = maxLogs;
    }

    public ATMState getState() {
        return atmRepo.loadState();
    }

    public void addCash(int denomCents, int count) {
        ATMState s = atmRepo.loadState();
        s.addNotes(denomCents, count);
        atmRepo.saveState(s);
        atmRepo.appendLog(new TransactionLog(LocalDateTime.now().toString(), "TECH", TransactionType.TECH_ADD_CASH,
                "Technician added " + count + " notes of " + (denomCents/100) + "€"), maxLogs);
        atmRepo.saveAll();
    }

    public void refillPaper(int amount) {
        ATMState s = atmRepo.loadState();
        s.addPaper(amount);
        atmRepo.saveState(s);
        atmRepo.appendLog(new TransactionLog(LocalDateTime.now().toString(), "TECH", TransactionType.TECH_REFILL_PAPER,
                "Technician refilled paper by " + amount), maxLogs);
        atmRepo.saveAll();
    }

    public void refillInk(int amount) {
        ATMState s = atmRepo.loadState();
        s.addInk(amount);
        atmRepo.saveState(s);
        atmRepo.appendLog(new TransactionLog(LocalDateTime.now().toString(), "TECH", TransactionType.TECH_REFILL_INK,
                "Technician refilled ink by " + amount), maxLogs);
        atmRepo.saveAll();
    }

        public void updateFirmware(String newVersion) {
        ATMState s = atmRepo.loadState();
        s.updateFirmware(newVersion);
        atmRepo.saveState(s);
        atmRepo.appendLog(new TransactionLog(LocalDateTime.now().toString(), "TECH", TransactionType.BALANCE_INQUIRY,
                "Technician updated firmware to " + newVersion), maxLogs);
        atmRepo.saveAll();
    }

    public String diagnostics() {
        ATMState s = atmRepo.loadState();
        StringBuilder sb = new StringBuilder();
        sb.append("ATM Diagnostics\n");
        sb.append("Firmware: ").append(s.getFirmwareVersion()).append("\n");
        sb.append("Paper: ").append(s.getPaper()).append("\n");
        sb.append("Ink: ").append(s.getInk()).append("\n");
        sb.append("Total cash: ").append(Money.formatCents(s.totalCashCents())).append("\n");
        sb.append("Cash breakdown: ");
        for (Map.Entry<Integer,Integer> e : s.getCashNotes().entrySet()) {
            sb.append(e.getKey()/100).append("€x").append(e.getValue()).append(" ");
        }
        return sb.toString().trim();
    }
}
