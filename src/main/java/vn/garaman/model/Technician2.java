package vn.garaman.model;

import java.time.LocalDate;

public final class Technician2 extends Member2 {
    private final String specialty;
    private final String grade;

    public Technician2(Long id, String username, String passwordHash, String fullName, String address, LocalDate birthDate, String email, String phone, Boolean active,
            String specialty,
            String grade) {
        super(id, username, passwordHash, fullName, address, birthDate, email, phone, active);
        this.specialty = specialty;
        this.grade = grade;
    }

    public String specialty() { return specialty; }

    public String grade() { return grade; }
}
