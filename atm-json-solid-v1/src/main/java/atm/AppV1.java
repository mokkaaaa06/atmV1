package atm;

import atm.repo.BankRepository;
import atm.repo.json.JsonAtmStateRepository;
import atm.repo.json.JsonBankRepository;
import atm.service.*;
import atm.ui.*;

import java.util.Scanner;

public class AppV1 {
    public static void main(String[] args) {
        BankRepository bankRepo = new JsonBankRepository();
        JsonAtmStateRepository atmRepo = new JsonAtmStateRepository();

        AuthService authService = new AuthService(bankRepo);
        ReceiptService receiptService = new ReceiptService(atmRepo);
        CustomerTransactionService txService = new CustomerTransactionService(bankRepo, atmRepo, receiptService, 50);

        TechnicianAuthService techAuth = new TechnicianAuthService(atmRepo);
        TechnicianService techService = new TechnicianService(atmRepo, 50);

        Scanner sc = new Scanner(System.in);

        Runnable customerFlow = () -> new CustomerUI(sc, authService, txService, atmRepo).start();
        Runnable techFlow = () -> {
            TechnicianUI ui = new TechnicianUIV1(sc, techAuth, techService);
            ui.start();
        };

        new MainMenuUI(sc, customerFlow, techFlow).start();
    }
}
