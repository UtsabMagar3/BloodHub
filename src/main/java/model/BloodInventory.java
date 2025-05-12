package model;

public class BloodInventory {
    private String bloodGroup; // Blood group for the inventory item
    private int quantity; // Quantity of blood available for the specified blood group
    private String status; // Status of the blood inventory (e.g., Available, Expired, etc.)

    // Constructor to initialize the blood group, quantity, and status
    public BloodInventory(String bloodGroup, int quantity, String status) {
        this.bloodGroup = bloodGroup;
        this.quantity = quantity;
        this.status = status;
    }

    // Getter for blood group
    public String getBloodGroup() {
        return bloodGroup;
    }

    // Getter for the quantity of blood available
    public int getQuantity() {
        return quantity;
    }

    // Getter for the status of the blood inventory
    public String getStatus() {
        return status;
    }

    // Setter for blood group
    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    // Setter for the quantity of blood available
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // Setter for the status of the blood inventory
    public void setStatus(String status) {
        this.status = status;
    }
}
