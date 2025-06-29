package com.cryptowallet.mapper;

import com.cryptowallet.dto.UserDTO;
import com.cryptowallet.model.UserDocument;

public class UserMapper {
    public static UserDTO toDTO(UserDocument doc) {
        return new UserDTO(doc.getId(), doc.getUserName(), doc.getEmail());
    }

    public static UserDocument fromDTO(UserDTO dto) {
        UserDocument user = new UserDocument();
        user.setId(dto.id());
        user.setUserName(dto.userName());
        user.setEmail(dto.email());
        return user;
    }
}
