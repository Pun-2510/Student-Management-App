package com.example.student_management;

import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;

public class Student implements Serializable {
    String fullname;
    String dob;
    String student_id;
    String class_id;
    String gender;
    String department;
    String intake;

    public Student() {
    }

    public Student(String fullname, String dob, String student_id, String class_id, String gender, String department, String intake) {
        this.fullname = fullname;
        this.dob = dob;
        this.student_id = student_id;
        this.class_id = class_id;
        this.gender = gender;
        this.department = department;
        this.intake = intake;
    }

    @PropertyName("fullname")
    public String getFullname() {
        return fullname;
    }

    @PropertyName("fullname")
    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    @PropertyName("dob")
    public String getDob() {
        return dob;
    }

    @PropertyName("dob")
    public void setDob(String dob) {
        this.dob = dob;
    }

    @PropertyName("student_id")
    public String getStudent_id() {
        return student_id;
    }

    @PropertyName("student_id")
    public void setStudent_id(String student_id) {
        this.student_id = student_id;
    }

    @PropertyName("class")
    public String getClass_id() {
        return class_id;
    }

    @PropertyName("class")
    public void setClass_id(String class_id) {
        this.class_id = class_id;
    }

    @PropertyName("gender")
    public String getGender() {
        return gender;
    }

    @PropertyName("gender")
    public void setGender(String gender) {
        this.gender = gender;
    }

    @PropertyName("department")
    public String getDepartment() {
        return department;
    }

    @PropertyName("department")
    public void setDepartment(String department) {
        this.department = department;
    }

    @PropertyName("intake")
    public String getIntake() {
        return intake;
    }

    @PropertyName("intake")
    public void setIntake(String intake) {
        this.intake = intake;
    }
}
