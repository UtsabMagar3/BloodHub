package model;

import java.time.LocalDate;

public class BloodRequest {
    private int id;
    private String requesterName;
    private String bloodGroup;
    private int unitsNeeded;
    private LocalDate requiredDate;
    private String hospital;
    private String emergencyLevel;
    private String status;

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

    // Getters and Setters
    public int getId() { return id; }
    public String getRequesterName() { return requesterName; }
    public String getBloodGroup() { return bloodGroup; }
    public int getUnitsNeeded() { return unitsNeeded; }
    public LocalDate getRequiredDate() { return requiredDate; }
    public String getHospital() { return hospital; }
    public String getEmergencyLevel() { return emergencyLevel; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}