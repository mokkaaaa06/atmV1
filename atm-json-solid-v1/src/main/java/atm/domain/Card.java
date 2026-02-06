package atm.domain;

public class Card {
    private final String cardNumber;
    private final String customerId;
    private final String pin;

    public Card(String cardNumber, String customerId, String pin) {
        this.cardNumber = cardNumber;
        this.customerId = customerId;
        this.pin = pin;
    }

    public String getCardNumber() { return cardNumber; }
    public String getCustomerId() { return customerId; }
    public String getPin() { return pin; }
}
