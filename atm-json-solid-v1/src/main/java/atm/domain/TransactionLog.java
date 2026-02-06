package atm.domain;

public class TransactionLog {
    private final String time;
    private final String actor;
    private final TransactionType type;
    private final String message;

    public TransactionLog(String time, String actor, TransactionType type, String message) {
        this.time = time;
        this.actor = actor;
        this.type = type;
        this.message = message;
    }

    public String getTime() { return time; }
    public String getActor() { return actor; }
    public TransactionType getType() { return type; }
    public String getMessage() { return message; }
}
