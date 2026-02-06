package atm.service;

public final class Money {
    private Money(){}

    public static long eurosToCents(long euros) {
        return euros * 100;
    }

    public static String formatCents(long cents) {
        long euros = cents / 100;
        long rem = Math.abs(cents % 100);
        return euros + "." + (rem < 10 ? "0" + rem : rem) + "€";
    }
}
