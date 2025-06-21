package com.cryptowallet.domain;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String userId;
    private String username;
    private List<Wallet> wallets = new ArrayList<>();

    public User(String userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Wallet> getWallets() {
        return wallets;
    }

    public void addWallet(Wallet wallet) {
        this.wallets.add(wallet);
    }
}
