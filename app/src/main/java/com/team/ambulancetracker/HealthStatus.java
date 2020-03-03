package com.team.ambulancetracker;

public class HealthStatus {
    private String height;
    private String weight;
    private String age;
    private String hereditary;
    private String chronic;
    private String allergy;
    private String remarks;

    public HealthStatus() {

    }

    public HealthStatus(String height, String weight, String age, String hereditary, String chronic, String allergy, String remarks) {
        this.height = height;
        this.weight = weight;
        this.age = age;
        this.hereditary = hereditary;
        this.chronic = chronic;
        this.allergy = allergy;
        this.remarks = remarks;
    }

    public String getHeight() {
        return height;
    }

    public String getWeight() {
        return weight;
    }

    public String getAge() {
        return age;
    }

    public String getHereditary() {
        return hereditary;
    }

    public String getChronic() {
        return chronic;
    }

    public String getAllergy() {
        return allergy;
    }

    public String getRemarks() {
        return remarks;
    }
}
