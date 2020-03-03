package com.team.ambulancetracker;

public class User {
    private String name;
    private String phone;
    private String eMail;
    private HealthStatus healthStatus;


    public User() {

    }

    public User(String Name, String phone, String eMail) {
        this.name = Name;
        this.phone = phone;
        this.eMail = eMail;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String geteMail() {
        return eMail;
    }

    public HealthStatus getHealthStatus() {
        return healthStatus;
    }
}
