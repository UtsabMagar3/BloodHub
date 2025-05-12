package model;

import java.time.LocalDate;

public class BloodRequest {
    private int id; // Unique identifier for the blood request
    private String requesterName; // Name of the person requesting the blood
    private String bloodGroup; // Blood group required for the request
    private int unitsNeeded; // Number of units of blood requested
    private LocalDate requiredDate; // Date when the blood is needed
    private String hospital; // Hospital where the blood is needed
    private String emergencyLevel; // Emergency level of the request (e.g., High, Medium, Low)
    private String status; // Status of the request (e.g., Pending, Approved, Rejected)

    // Constructor to initialize the blood request details
    public BloodRequest(int id, String requesterName, String bloodGroup, int unitsNeeded,
                        LocalDate requiredDate, String hospital, String emergencyLevel, String status) {
        this.id = id;
        this.requesterName = requesterName;
        this.bloodGroup = bloodGroup;
        this.unitsNeeded = unitsNeeded;
        this.requiredDate = requiredDate;
        this.hospital = hospital;
        this.emergencyLevel = emergencyLevel;
        this.status = status;
    }

    // Getters and Setters for each field
    public int getId() {
        return id;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public int getUnitsNeeded() {
        return unitsNeeded;
    }

    public LocalDate getRequiredDate() {
        return requiredDate;
    }

    public String getHospital() {
        return hospital;
    }

    public String getEmergencyLevel() {
        return emergencyLevel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

