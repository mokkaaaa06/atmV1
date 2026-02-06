package atm.repo;

import atm.domain.*;

import java.util.List;
import java.util.Optional;

public interface BankRepository {
    Optional<Card> findCardByNumber(String cardNumber);
    Optional<Customer> findCustomerById(String customerId);
    List<Account> findAccountsByCustomer(String customerId);
    Optional<Account> findAccount(String customerId, AccountType type);

    void saveCard(Card card);
    void saveCustomer(Customer customer);
    void saveAccount(Account account);

    void saveAll();
}
