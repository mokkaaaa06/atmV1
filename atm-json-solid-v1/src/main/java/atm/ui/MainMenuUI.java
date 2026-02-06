package atm.ui;

import java.util.Scanner;

public class MainMenuUI extends BaseConsoleUI {

    private final Runnable customerFlow;
    private final Runnable technicianFlow;

    public MainMenuUI(Scanner sc, Runnable customerFlow, Runnable technicianFlow) {
        super(sc);
        this.customerFlow = customerFlow;
        this.technicianFlow = technicianFlow;
    }

    public void start() {
        while (true) {
            header("ATM Main Menu");
            System.out.println("1) Customer");
            System.out.println("2) Technician");
            System.out.println("3) Exit");
            int choice = readInt("Choose: ");
            switch (choice) {
                case 1 -> customerFlow.run();
                case 2 -> technicianFlow.run();
                case 3 -> { System.out.println("Goodbye!"); return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }
}
