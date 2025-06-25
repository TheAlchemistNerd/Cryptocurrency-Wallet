package com.cryptowallet.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class UserDocument {

    @Id
    private String id;
    private String userName;
    private String password;

    public UserDocument() {}

    public UserDocument(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUserName(String username) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
