package com.binomiaux.archimedes.db.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "activities")
public class Activity {
    @Id
    /*@GeneratedValue(strategy = GenerationType.AUTO)*/
    @Column(name = "activityid")
    private int activityId;

    @Column(name = "name")
    private String name;

    @Column(name = "type")
    private String type;

    @Column(name = "attribute1")
    private String attribute1;

    @Column(name = "attribute2")
    private String attribute2;

    @Column(name = "attribute3")
    private String attribute3;
}
