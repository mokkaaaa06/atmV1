package atm.service;

public class CustomerSession {
    private final String customerId;
    private final String customerName;
    private final String cardNumber;

    public CustomerSession(String customerId, String customerName, String cardNumber) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.cardNumber = cardNumber;
    }

    public String getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getCardNumber() { return cardNumber; }
}
