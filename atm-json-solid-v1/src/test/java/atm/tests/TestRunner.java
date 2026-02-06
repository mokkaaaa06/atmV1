package atm.tests;

public class TestRunner {

    public static void main(String[] args) {
        int passed = 0;
        int failed = 0;

        // ---------------- Unit Tests ----------------
        try {
            UnitTestCashDistribution.run();
            passed++;
        } catch (AssertionError e) {
            failed++;
            System.out.println(e.getMessage());
        }

        try {
            UnitTestTransferLogic.run();
            passed++;
        } catch (AssertionError e) {
            failed++;
            System.out.println(e.getMessage());
        }

        // -------------- Integration Tests --------------
        try {
            IntegrationTestWithdrawPersistence.run();
            passed++;
        } catch (AssertionError e) {
            failed++;
            System.out.println(e.getMessage());
        } catch (Exception e) {
            failed++;
            System.out.println("ERROR (IntegrationTestWithdrawPersistence): " + e.getMessage());
        }

        try {
            IntegrationTestTechnicianRefillPersistence.run();
            passed++;
        } catch (AssertionError e) {
            failed++;
            System.out.println(e.getMessage());
        } catch (Exception e) {
            failed++;
            System.out.println("ERROR (IntegrationTestTechnicianRefillPersistence): " + e.getMessage());
        }

        System.out.println("\n==============================");
        System.out.println("Tests finished. Passed: " + passed + " | Failed: " + failed);
        System.out.println("==============================");

        if (failed > 0) System.exit(1);
    }

    static void assertEquals(long exp, long act, String msg) {
        if (exp != act)
            throw new AssertionError("FAIL: " + msg + " (expected=" + exp + ", actual=" + act + ")");
    }

    static void assertEqualsInt(int exp, int act, String msg) {
        if (exp != act)
            throw new AssertionError("FAIL: " + msg + " (expected=" + exp + ", actual=" + act + ")");
    }
}
