package com.example.student_management;

public class LoginHistory {
    String datetime;
    String device;
    String system;

    public LoginHistory(String datetime, String device, String system) {
        this.datetime = datetime;
        this.device = device;
        this.system = system;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }
}
