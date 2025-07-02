package com.cryptowallet.event;

import com.cryptowallet.dto.UserDTO;
import com.cryptowallet.service.UserService;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a new user is successfully registered & saved.
 * This is a POJO containing details about the user.
 */
public class UserRegisteredEvent extends ApplicationEvent {

    private final UserDTO userDTO;

    /**
     * Create a new UserRegisteredEvent.
     * @param source The service on which the user registration initially occured.
     * @param userDTO The Data Transfer Object for the newly registered user
     */
    public UserRegisteredEvent(Object source, UserDTO userDTO) {
        super(source);
        this.userDTO = userDTO;
    }

    /**
     * Returns the details of the registered user
     * @return The UserDTO associated with this event
     */
    public UserDTO getUserDTO() {
        return userDTO;
    }
}
