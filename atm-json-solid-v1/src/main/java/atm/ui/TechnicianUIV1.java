package atm.ui;

import atm.domain.ATMState;
import atm.service.Money;
import atm.service.TechnicianAuthService;
import atm.service.TechnicianService;

import java.util.Map;
import java.util.Scanner;

public class TechnicianUIV1 extends BaseConsoleUI implements TechnicianUI {

    protected final TechnicianAuthService auth;
    protected final TechnicianService techService;

    public TechnicianUIV1(Scanner sc, TechnicianAuthService auth, TechnicianService techService) {
        super(sc);
        this.auth = auth;
        this.techService = techService;
    }

    @Override
    public void start() {
        header("Technician Login (V1)");
        String user = readLine("Username: ");
        String pass = readLine("Password: ");
        if (!auth.authenticate(user, pass)) {
            System.out.println("Invalid technician credentials.");
            return;
        }

        while (true) {
            header("Technician Menu (V1 - View Only)");
            System.out.println("1) View ATM status");
            System.out.println("2) Exit");
            int choice = readInt("Choose: ");
            switch (choice) {
                case 1 -> showStatus();
                case 2 -> { System.out.println("Logged out."); return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    protected void showStatus() {
        ATMState s = techService.getState();
        header("ATM Status");
        System.out.println("Firmware: " + s.getFirmwareVersion());
        System.out.println("Paper: " + s.getPaper());
        System.out.println("Ink: " + s.getInk());
        System.out.println("Total cash: " + Money.formatCents(s.totalCashCents()));
        System.out.println("Cash breakdown:");
        for (Map.Entry<Integer,Integer> e : s.getCashNotes().entrySet()) {
            System.out.println(" - " + (e.getKey()/100) + "€ x " + e.getValue());
        }
    }
}
