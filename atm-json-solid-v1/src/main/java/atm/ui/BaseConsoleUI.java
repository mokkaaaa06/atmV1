package atm.ui;

import java.util.Scanner;

public abstract class BaseConsoleUI {
    protected final Scanner sc;

    protected BaseConsoleUI(Scanner sc) {
        this.sc = sc;
    }

    protected String readLine(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    protected int readInt(String prompt) {
        while (true) {
            String s = readLine(prompt);
            try {
                return Integer.parseInt(s);
            } catch (Exception e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    protected long readLong(String prompt) {
        while (true) {
            String s = readLine(prompt);
            try {
                return Long.parseLong(s);
            } catch (Exception e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    protected void header(String title) {
        System.out.println("\n==============================");
        System.out.println(title);
        System.out.println("==============================");
    }
}
