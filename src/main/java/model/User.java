package model;

public class User {
    private int id; // Unique identifier for the user
    private String fullName; // Full name of the user
    private String email; // Email address of the user
    private String bloodGroup; // Blood group of the user
    private String role; // Role of the user (e.g., Admin, Donor, Requester)

    // Constructor to initialize the user details
    public User(int id, String fullName, String email, String bloodGroup, String role) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.bloodGroup = bloodGroup;
        this.role = role;
    }

    // Getters for each field
    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public String getRole() {
        return role;
    }
}
