package com.henry.orchestrator.staff.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Staff {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column
    @NotBlank(message = "Name is mandatory")
    private String name;

    @Column
    private String birthPlace;

    @Column
    @JsonFormat(pattern="yyyy-mm-dd")
    private String birthDate;

    @Column
    @NotBlank(message = "Position is mandatory")
    private String position;

    @Column
    @JsonFormat(pattern="yyyy-mm-dd")
    private String dateJoin;

    @Column
    @JsonFormat(pattern="yyyy-mm-dd")
    private String dateResign;

    @Column
    private String salary;

    @Column
    private String address;

    @Column
    private String maritalStatus;

    @Column
    private String education;

    @Column
    @NotBlank(message = "Email is mandatory")
    private String email;
}
