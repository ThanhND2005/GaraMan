package vn.garaman.model;

import java.util.List;
import java.util.ArrayList;

public final class Customer2 {
    private final Long id;
    private final String fullName;
    private final String phone;
    private final String address;
    private final String email;
    private final List<Vehicle2> vehicles = new ArrayList<>();

    public Customer2(Long id,
            String fullName,
            String phone,
            String address,
            String email) {
        this.id = id;
        this.fullName = fullName;
        this.phone = phone;
        this.address = address;
        this.email = email;
    }

    public Long id() { return id; }

    public String fullName() { return fullName; }

    public String phone() { return phone; }

    public String address() { return address; }

    public String email() { return email; }

    public List<Vehicle2> vehicles() { return List.copyOf(vehicles); }

    public void addVehicle(Vehicle2 value) { vehicles.add(value); }

    public String licensePlates() { return String.join(", ", vehicles.stream().map(Vehicle2::licensePlate).toList()); }
}
