package atm.tests;

import atm.domain.Account;
import atm.domain.AccountType;

public class UnitTestTransferLogic {

    public static void run() {
        Account checking = new Account("A1", "C1", AccountType.CHECKING, 50000);
        Account savings  = new Account("A2", "C1", AccountType.SAVINGS,  100000);

        checking.withdraw(2000);
        savings.deposit(2000);

        TestRunner.assertEquals(48000, checking.getBalanceCents(), "Checking should decrease by 20€");
        TestRunner.assertEquals(102000, savings.getBalanceCents(), "Savings should increase by 20€");

        System.out.println("PASS: UnitTestTransferLogic");
    }
}
