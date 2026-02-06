package atm.domain;

import java.util.LinkedHashMap;
import java.util.Map;

public class ATMState {
    private String firmwareVersion;
    private int paper;
    private int ink;
    private final Map<Integer, Integer> cashNotes;

    public ATMState(String firmwareVersion, int paper, int ink, Map<Integer,Integer> cashNotes) {
        this.firmwareVersion = firmwareVersion;
        this.paper = paper;
        this.ink = ink;
        this.cashNotes = new LinkedHashMap<>(cashNotes);
    }

    public String getFirmwareVersion() { return firmwareVersion; }

    public void updateFirmware(String newVersion) {
        if (newVersion == null || newVersion.isBlank()) throw new IllegalArgumentException("Firmware version cannot be empty.");
        this.firmwareVersion = newVersion;
    }

    public int getPaper() { return paper; }
    public int getInk() { return ink; }

    public void addPaper(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Paper refill must be positive.");
        paper += amount;
    }

    public void addInk(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Ink refill must be positive.");
        ink += amount;
    }

    public boolean canPrintReceipt() {
        return paper > 0 && ink > 0;
    }

    public void consumeReceiptResources() {
        if (!canPrintReceipt()) throw new IllegalStateException("Out of paper or ink.");
        paper -= 1;
        ink -= 1;
    }

    public Map<Integer,Integer> getCashNotes() {
        return new LinkedHashMap<>(cashNotes);
    }

    public int getNoteCount(int denom) {
        return cashNotes.getOrDefault(denom, 0);
    }

    public void addNotes(int denom, int count) {
        if (count <= 0) throw new IllegalArgumentException("Count must be positive.");
        cashNotes.put(denom, getNoteCount(denom) + count);
    }

    public void removeNotes(int denom, int count) {
        int have = getNoteCount(denom);
        if (count < 0) throw new IllegalArgumentException("Count must be >= 0.");
        if (have < count) throw new IllegalArgumentException("Not enough notes for denom " + denom);
        cashNotes.put(denom, have - count);
    }

    public long totalCashCents() {
        long total=0;
        for (Map.Entry<Integer,Integer> e: cashNotes.entrySet()) {
            total += (long)e.getKey() * e.getValue();
        }
        return total;
    }
}
