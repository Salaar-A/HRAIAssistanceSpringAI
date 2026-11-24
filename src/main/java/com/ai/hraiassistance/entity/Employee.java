package com.ai.hraiassistance.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Employee {

    @Id
    private Integer id;
    private String name;
    private String designation;
    private String employmentType;

    protected Employee() {}   // JPA needs an empty constructor

    public Employee(Integer id, String name, String designation, String employmentType) {
        this.id = id;
        this.name = name;
        this.designation = designation;
        this.employmentType = employmentType;
    }
}