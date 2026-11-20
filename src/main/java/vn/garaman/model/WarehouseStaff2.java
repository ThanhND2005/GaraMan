package vn.garaman.model;

import java.time.LocalDate;

public final class WarehouseStaff2 extends Member2 {
    private final String position;

    public WarehouseStaff2(Long id, String username, String passwordHash, String fullName, String address, LocalDate birthDate, String email, String phone, Boolean active,
            String position) {
        super(id, username, passwordHash, fullName, address, birthDate, email, phone, active);
        this.position = position;
    }

    public String position() { return position; }
}
