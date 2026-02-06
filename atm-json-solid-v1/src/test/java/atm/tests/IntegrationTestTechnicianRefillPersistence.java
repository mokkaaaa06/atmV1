package atm.tests;

import atm.repo.json.JsonAtmStateRepository;
import atm.service.TechnicianService;

import java.nio.file.Files;
import java.nio.file.Path;

public class IntegrationTestTechnicianRefillPersistence {

    public static void run() throws Exception {
        Files.deleteIfExists(Path.of("atm_state.json"));

        var atmRepo = new JsonAtmStateRepository();
        var tech = new TechnicianService(atmRepo, 50);

        int beforePaper = atmRepo.loadState().getPaper();
        tech.refillPaper(3);
        int afterPaper = atmRepo.loadState().getPaper();

        TestRunner.assertEqualsInt(beforePaper + 3, afterPaper, "Paper should increase");

        var atmRepo2 = new JsonAtmStateRepository();
        int afterReload = atmRepo2.loadState().getPaper();
        TestRunner.assertEqualsInt(afterPaper, afterReload, "Paper should remain after reload from JSON");

        System.out.println("PASS: IntegrationTestTechnicianRefillPersistence");
    }
}
