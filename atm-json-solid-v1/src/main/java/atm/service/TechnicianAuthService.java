package atm.service;

import atm.repo.json.JsonAtmStateRepository;

public class TechnicianAuthService {
    private final JsonAtmStateRepository atmRepo;

    public TechnicianAuthService(JsonAtmStateRepository atmRepo) {
        this.atmRepo = atmRepo;
    }

    public boolean authenticate(String username, String password) {
        return atmRepo.getTechnicianUsername().equals(username) && atmRepo.getTechnicianPassword().equals(password);
    }
}
