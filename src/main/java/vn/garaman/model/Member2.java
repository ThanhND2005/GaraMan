package vn.garaman.model;

import java.time.LocalDate;

public abstract class Member2 {
    private final Long id;
    private final String username;
    private final String passwordHash;
    private final String fullName;
    private final String address;
    private final LocalDate birthDate;
    private final String email;
    private final String phone;
    private final Boolean active;

    protected Member2(Long id,
            String username,
            String passwordHash,
            String fullName,
            String address,
            LocalDate birthDate,
            String email,
            String phone,
            Boolean active) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.address = address;
        this.birthDate = birthDate;
        this.email = email;
        this.phone = phone;
        this.active = active;
    }

    public Long id() { return id; }

    public String username() { return username; }

    public String passwordHash() { return passwordHash; }

    public String fullName() { return fullName; }

    public String address() { return address; }

    public LocalDate birthDate() { return birthDate; }

    public String email() { return email; }

    public String phone() { return phone; }

    public Boolean active() { return active; }
}
