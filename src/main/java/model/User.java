package model;

public class User {
    private int id;
    private String fullName;
    private String email;
    private String bloodGroup;
    private String role;

    // Constructor
    public User(int id, String fullName, String email, String bloodGroup, String role) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.bloodGroup = bloodGroup;
        this.role = role;
    }

    // Getters and setters (required for TableView)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}