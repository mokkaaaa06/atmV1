package atm.domain;

public class Account {
    private final String id;
    private final String customerId;
    private final AccountType type;
    private long balanceCents;

    public Account(String id, String customerId, AccountType type, long balanceCents) {
        this.id = id;
        this.customerId = customerId;
        this.type = type;
        this.balanceCents = balanceCents;
    }

    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public AccountType getType() { return type; }
    public long getBalanceCents() { return balanceCents; }

    public void deposit(long cents) {
        if (cents <= 0) throw new IllegalArgumentException("Deposit must be positive.");
        balanceCents += cents;
    }

    public void withdraw(long cents) {
        if (cents <= 0) throw new IllegalArgumentException("Withdraw must be positive.");
        if (balanceCents < cents) throw new IllegalArgumentException("Insufficient funds.");
        balanceCents -= cents;
    }
}
