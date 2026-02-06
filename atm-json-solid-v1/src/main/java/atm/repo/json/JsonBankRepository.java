package atm.repo.json;

import atm.domain.*;
import atm.repo.BankRepository;
import atm.util.SimpleJson;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class JsonBankRepository implements BankRepository {

    private final Map<String, Customer> customersById = new LinkedHashMap<>();
    private final Map<String, Card> cardsByNumber = new LinkedHashMap<>();
    private final Map<String, List<Account>> accountsByCustomer = new LinkedHashMap<>();

    public JsonBankRepository() {
        loadOrSeed();
    }

    @Override
    public Optional<Card> findCardByNumber(String cardNumber) {
        return Optional.ofNullable(cardsByNumber.get(cardNumber));
    }

    @Override
    public Optional<Customer> findCustomerById(String customerId) {
        return Optional.ofNullable(customersById.get(customerId));
    }

    @Override
    public List<Account> findAccountsByCustomer(String customerId) {
        return new ArrayList<>(accountsByCustomer.getOrDefault(customerId, List.of()));
    }

    @Override
    public Optional<Account> findAccount(String customerId, AccountType type) {
        for (Account a : findAccountsByCustomer(customerId)) {
            if (a.getType() == type) return Optional.of(a);
        }
        return Optional.empty();
    }

    @Override
    public void saveCard(Card card) {
        cardsByNumber.put(card.getCardNumber(), card);
    }

    @Override
    public void saveCustomer(Customer customer) {
        customersById.put(customer.getId(), customer);
    }

    @Override
    public void saveAccount(Account account) {
        accountsByCustomer.computeIfAbsent(account.getCustomerId(), k -> new ArrayList<>());
        List<Account> list = accountsByCustomer.get(account.getCustomerId());
        for (int i=0;i<list.size();i++){
            if (list.get(i).getId().equals(account.getId())) {
                list.set(i, account);
                return;
            }
        }
        list.add(account);
    }

    @Override
    public void saveAll() {
        persist();
    }

    private void loadOrSeed() {
        Path p = Paths.get(JsonFiles.BANK_FILE);
        if (!Files.exists(p)) {
            seedDefaults();
            persist();
            return;
        }
        try {
            String raw = Files.readString(p);
            Object rootObj = SimpleJson.parse(raw);
            @SuppressWarnings("unchecked")
            Map<String,Object> root = (Map<String,Object>) rootObj;

            List<Object> custs = asList(root.get("customers"));
            for (Object o : custs) {
                Map<String,Object> m = asMap(o);
                Customer c = new Customer((String)m.get("id"), (String)m.get("name"));
                customersById.put(c.getId(), c);
            }

            List<Object> cards = asList(root.get("cards"));
            for (Object o : cards) {
                Map<String,Object> m = asMap(o);
                Card card = new Card((String)m.get("cardNumber"), (String)m.get("customerId"), (String)m.get("pin"));
                cardsByNumber.put(card.getCardNumber(), card);
            }

            List<Object> accs = asList(root.get("accounts"));
            for (Object o : accs) {
                Map<String,Object> m = asMap(o);
                String id = (String)m.get("id");
                String customerId = (String)m.get("customerId");
                AccountType type = AccountType.valueOf((String)m.get("type"));
                long bal = ((Number)m.get("balanceCents")).longValue();
                Account a = new Account(id, customerId, type, bal);
                saveAccount(a);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to load bank.json. Delete it to reset. Cause: " + e.getMessage(), e);
        }
    }

    private void seedDefaults() {
        Customer alice = new Customer("C1", "Alice");
        Customer bob = new Customer("C2", "Bob");

        saveCustomer(alice);
        saveCustomer(bob);

        saveCard(new Card("11112222", alice.getId(), "1234"));
        saveCard(new Card("33334444", bob.getId(), "4321"));

        saveAccount(new Account("A1", alice.getId(), AccountType.CHECKING, 500_00));
        saveAccount(new Account("A2", alice.getId(), AccountType.SAVINGS, 1000_00));
        saveAccount(new Account("A3", bob.getId(), AccountType.CHECKING, 300_00));
        saveAccount(new Account("A4", bob.getId(), AccountType.SAVINGS, 700_00));
    }

    private void persist() {
        Map<String,Object> root = new LinkedHashMap<>();

        List<Object> custs = new ArrayList<>();
        for (Customer c : customersById.values()) {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("id", c.getId());
            m.put("name", c.getName());
            custs.add(m);
        }

        List<Object> cards = new ArrayList<>();
        for (Card card : cardsByNumber.values()) {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("cardNumber", card.getCardNumber());
            m.put("customerId", card.getCustomerId());
            m.put("pin", card.getPin());
            cards.add(m);
        }

        List<Object> accs = new ArrayList<>();
        for (Map.Entry<String,List<Account>> e : accountsByCustomer.entrySet()) {
            for (Account a : e.getValue()) {
                Map<String,Object> m = new LinkedHashMap<>();
                m.put("id", a.getId());
                m.put("customerId", a.getCustomerId());
                m.put("type", a.getType().name());
                m.put("balanceCents", a.getBalanceCents());
                accs.add(m);
            }
        }

        root.put("customers", custs);
        root.put("cards", cards);
        root.put("accounts", accs);

        try {
            Files.writeString(Paths.get(JsonFiles.BANK_FILE), SimpleJson.stringify(root), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write bank.json: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String,Object> asMap(Object o) { return (Map<String,Object>) o; }

    @SuppressWarnings("unchecked")
    private static List<Object> asList(Object o) { return o == null ? new ArrayList<>() : (List<Object>) o; }
}
