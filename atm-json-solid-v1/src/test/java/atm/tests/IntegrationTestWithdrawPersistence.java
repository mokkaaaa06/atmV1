package atm.tests;

import atm.repo.json.JsonAtmStateRepository;
import atm.repo.json.JsonBankRepository;
import atm.service.AuthService;
import atm.service.CustomerTransactionService;
import atm.service.ReceiptService;
import atm.domain.AccountType;

import java.nio.file.Files;
import java.nio.file.Path;

public class IntegrationTestWithdrawPersistence {

    public static void run() throws Exception {
        Files.deleteIfExists(Path.of("bank.json"));
        Files.deleteIfExists(Path.of("atm_state.json"));

        var bankRepo = new JsonBankRepository();
        var atmRepo = new JsonAtmStateRepository();

        var auth = new AuthService(bankRepo);
        var receipt = new ReceiptService(atmRepo);
        var tx = new CustomerTransactionService(bankRepo, atmRepo, receipt, 50);

        var session = auth.authenticateCustomer("11112222", "1234").orElseThrow();

        long before = bankRepo.findAccount(session.getCustomerId(), AccountType.CHECKING).orElseThrow().getBalanceCents();
        tx.withdraw(session.getCustomerId(), AccountType.CHECKING, 5000);
        long after = bankRepo.findAccount(session.getCustomerId(), AccountType.CHECKING).orElseThrow().getBalanceCents();

        TestRunner.assertEquals(before - 5000, after, "Balance should persist after withdraw");

        var bankRepo2 = new JsonBankRepository();
        long afterReload = bankRepo2.findAccount(session.getCustomerId(), AccountType.CHECKING).orElseThrow().getBalanceCents();
        TestRunner.assertEquals(after, afterReload, "Balance should remain after reload from JSON");

        System.out.println("PASS: IntegrationTestWithdrawPersistence");
    }
}
