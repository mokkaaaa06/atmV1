package atm.repo.json;

import atm.domain.*;
import atm.repo.AtmStateRepository;
import atm.util.SimpleJson;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

public class JsonAtmStateRepository implements AtmStateRepository {

    private ATMState state;
    private final List<TransactionLog> logs = new ArrayList<>();
    private String technicianUsername;
    private String technicianPassword;

    public JsonAtmStateRepository() {
        loadOrSeed();
    }

    public String getTechnicianUsername() { return technicianUsername; }
    public String getTechnicianPassword() { return technicianPassword; }

    @Override
    public ATMState loadState() {
        return state;
    }

    @Override
    public List<TransactionLog> loadLogs() {
        return new ArrayList<>(logs);
    }

    @Override
    public void saveState(ATMState state) {
        this.state = state;
    }

    @Override
    public void appendLog(TransactionLog log, int maxLogs) {
        logs.add(log);
        while (logs.size() > maxLogs) logs.remove(0);
    }

    @Override
    public void saveAll() {
        persist();
    }

    private void loadOrSeed() {
        Path p = Paths.get(JsonFiles.ATM_FILE);
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

            technicianUsername = (String) root.get("technicianUsername");
            technicianPassword = (String) root.get("technicianPassword");

                        String firmwareVersion = (String) root.getOrDefault("firmwareVersion", "1.0.0");
            int paper = ((Number)root.getOrDefault("paper", 0)).intValue();
            int ink = ((Number)root.getOrDefault("ink", 0)).intValue();

            Map<Integer,Integer> cash = new LinkedHashMap<>();
            @SuppressWarnings("unchecked")
            Map<String,Object> cashObj = (Map<String,Object>) root.get("cashNotes");
            for (Map.Entry<String,Object> e : cashObj.entrySet()) {
                int denom = Integer.parseInt(e.getKey());
                int count = ((Number)e.getValue()).intValue();
                cash.put(denom, count);
            }
            state = new ATMState(firmwareVersion, paper, ink, cash);

            List<Object> logArr = asList(root.get("logs"));
            for (Object o : logArr) {
                Map<String,Object> m = asMap(o);
                logs.add(new TransactionLog(
                        (String)m.get("time"),
                        (String)m.get("actor"),
                        TransactionType.valueOf((String)m.get("type")),
                        (String)m.get("message")
                ));
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to load atm_state.json. Delete it to reset. Cause: " + e.getMessage(), e);
        }
    }

    private void seedDefaults() {
        technicianUsername = DefaultSeed.technicianUsername();
        technicianPassword = DefaultSeed.technicianPassword();
        state = new ATMState("1.0.0", DefaultSeed.defaultPaper(), DefaultSeed.defaultInk(), DefaultSeed.defaultEuroNotes());

        appendLog(new TransactionLog(LocalDateTime.now().toString(), "SYSTEM", TransactionType.BALANCE_INQUIRY,
                "ATM initialized with default cash, paper, and ink."), 50);
    }

    private void persist() {
        Map<String,Object> root = new LinkedHashMap<>();
        root.put("technicianUsername", technicianUsername);
        root.put("technicianPassword", technicianPassword);
        root.put("firmwareVersion", state.getFirmwareVersion());
        root.put("paper", state.getPaper());
        root.put("ink", state.getInk());

        Map<String,Object> cash = new LinkedHashMap<>();
        for (Map.Entry<Integer,Integer> e : state.getCashNotes().entrySet()) {
            cash.put(String.valueOf(e.getKey()), e.getValue());
        }
        root.put("cashNotes", cash);

        List<Object> logArr = new ArrayList<>();
        for (TransactionLog tl : logs) {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("time", tl.getTime());
            m.put("actor", tl.getActor());
            m.put("type", tl.getType().name());
            m.put("message", tl.getMessage());
            logArr.add(m);
        }
        root.put("logs", logArr);

        try {
            Files.writeString(Paths.get(JsonFiles.ATM_FILE), SimpleJson.stringify(root), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write atm_state.json: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String,Object> asMap(Object o) { return (Map<String,Object>) o; }

    @SuppressWarnings("unchecked")
    private static List<Object> asList(Object o) { return o == null ? new ArrayList<>() : (List<Object>) o; }
}
