package com.cryptowallet.dto;

import java.util.Set;

public record UserDTO(
    String id,
    String userName,
    String email,
    Set<String> roles
) {}
