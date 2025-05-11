package model;

public class BloodInventory {
    private String bloodGroup;
    private int quantity;
    private String status;

    public BloodInventory(String bloodGroup, int quantity, String status) {
        this.bloodGroup = bloodGroup;
        this.quantity = quantity;
        this.status = status;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getStatus() {
        return status;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}