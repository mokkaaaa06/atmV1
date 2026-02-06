package atm.service;

import atm.domain.ATMState;

import java.util.*;

public class CashDispenser {
    public static final List<Integer> DENOMS = List.of(10000, 5000, 2000, 1000, 500);

    public Map<Integer,Integer> planDispense(ATMState state, long amountCents) {
        if (amountCents <= 0) throw new IllegalArgumentException("Amount must be positive.");
        if (amountCents % 500 != 0) throw new IllegalArgumentException("Amount must be multiple of 5€.");

        long remaining = amountCents;
        Map<Integer,Integer> plan = new LinkedHashMap<>();

        for (int denom : DENOMS) {
            int available = state.getNoteCount(denom);
            long needed = remaining / denom;
            int take = (int)Math.min(available, needed);
            if (take > 0) {
                plan.put(denom, take);
                remaining -= (long)denom * take;
            }
        }

        if (remaining == 0) return plan;
        return backtrack(state, amountCents);
    }

    private Map<Integer,Integer> backtrack(ATMState state, long amountCents) {
        Map<Integer,Integer> result = new LinkedHashMap<>();
        boolean ok = dfs(state, 0, amountCents, new LinkedHashMap<>(), result);
        if (!ok) throw new IllegalArgumentException("ATM cannot dispense this amount with available notes.");
        return result;
    }

    private boolean dfs(ATMState state, int idx, long remaining, Map<Integer,Integer> current, Map<Integer,Integer> result) {
        if (remaining == 0) {
            result.clear();
            result.putAll(current);
            return true;
        }
        if (idx >= DENOMS.size()) return false;

        int denom = DENOMS.get(idx);
        int available = state.getNoteCount(denom);
        int maxTake = (int)Math.min(available, remaining / denom);

        for (int take = maxTake; take >= 0; take--) {
            if (take > 0) current.put(denom, take);
            else current.remove(denom);

            long newRemaining = remaining - (long)denom * take;
            if (dfs(state, idx+1, newRemaining, current, result)) return true;
        }
        current.remove(denom);
        return false;
    }

    public void applyDispense(ATMState state, Map<Integer,Integer> plan) {
        for (Map.Entry<Integer,Integer> e : plan.entrySet()) {
            state.removeNotes(e.getKey(), e.getValue());
        }
    }
}
