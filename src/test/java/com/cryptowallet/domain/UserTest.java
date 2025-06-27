package com.cryptowallet.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    void testUserProperties() {
        User user = new User("uid001", "alice");
        assertEquals("uid001", user.getUserId());
        assertEquals("alice", user.getUsername());

        user.setUsername("bob");
        assertEquals("bob", user.getUsername());
    }

    @Test
    void testAddWallet() {
        User user = new User("uid002", "charlie");
        Wallet wallet = new Wallet("wid001", "pub", "enc", 0.0);
        user.addWallet(wallet);

        assertEquals(1, user.getWallets().size());
        assertEquals("wid001", user.getWallets().get(0).getWalletId());
    }
}