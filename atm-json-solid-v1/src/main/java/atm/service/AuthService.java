package atm.service;

import atm.domain.Card;
import atm.domain.Customer;
import atm.repo.BankRepository;

import java.util.Optional;

public class AuthService {
    private final BankRepository bankRepository;

    public AuthService(BankRepository bankRepository) {
        this.bankRepository = bankRepository;
    }

    public Optional<CustomerSession> authenticateCustomer(String cardNumber, String pin) {
        Optional<Card> cardOpt = bankRepository.findCardByNumber(cardNumber);
        if (cardOpt.isEmpty()) return Optional.empty();

        Card card = cardOpt.get();
        if (!card.getPin().equals(pin)) return Optional.empty();

        Optional<Customer> customerOpt = bankRepository.findCustomerById(card.getCustomerId());
        return customerOpt.map(c -> new CustomerSession(c.getId(), c.getName(), card.getCardNumber()));
    }
}
