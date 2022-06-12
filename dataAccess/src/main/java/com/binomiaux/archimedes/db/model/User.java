package com.binomiaux.archimedes.db.model;

import javax.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    /*@GeneratedValue(strategy = GenerationType.AUTO)*/
    @Column(name = "userid")
    private int userId;

    @Column(name = "username")
    private String userName;

    @Column(name = "password")
    private String password;

    @Column(name = "firstname")
    private String firstName;

    @Column(name = "roles")
    private String roles;

    //@CreatedBy
    //private User createdUser;

    //@CreatedDate
    //private DateTime createdDate;

    //@LastModifiedBy
    //private User lastUserModifiedBy;

    //@LastModifiedDate
    //private DateTime lastModifiedDate;


    public int getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getRoles() {
        return roles;
    }
}
