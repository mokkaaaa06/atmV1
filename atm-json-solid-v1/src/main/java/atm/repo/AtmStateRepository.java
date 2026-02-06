package atm.repo;

import atm.domain.ATMState;
import atm.domain.TransactionLog;

import java.util.List;

public interface AtmStateRepository {
    ATMState loadState();
    List<TransactionLog> loadLogs();

    void saveState(ATMState state);
    void appendLog(TransactionLog log, int maxLogs);

    void saveAll();
}
