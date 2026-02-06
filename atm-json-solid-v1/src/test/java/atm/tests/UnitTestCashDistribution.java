package atm.tests;

import atm.domain.ATMState;
import atm.repo.json.DefaultSeed;
import atm.service.CashDispenser;

import java.util.Map;

public class UnitTestCashDistribution {

    public static void run() {
        Map<Integer,Integer> notes = DefaultSeed.defaultEuroNotes();
        ATMState state = new ATMState("1.0.0", 5, 5, notes);

        CashDispenser d = new CashDispenser();
        var plan = d.planDispense(state, 13500);

        TestRunner.assertEqualsInt(1, plan.getOrDefault(10000, 0), "Should use one 100€ note");
        TestRunner.assertEqualsInt(1, plan.getOrDefault(2000, 0), "Should use one 20€ note");
        TestRunner.assertEqualsInt(1, plan.getOrDefault(1000, 0), "Should use one 10€ note");
        TestRunner.assertEqualsInt(1, plan.getOrDefault(500, 0), "Should use one 5€ note");

        System.out.println("PASS: UnitTestCashDistribution");
    }
}
