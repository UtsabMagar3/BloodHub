package model;

public class User {
    private int id;
    private String fullName;
    private String email;
    private String bloodGroup;
    private String role;

    public User(int id, String fullName, String email, String bloodGroup, String role) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.bloodGroup = bloodGroup;
        this.role = role;
    }

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
