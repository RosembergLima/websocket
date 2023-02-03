package com.websocket.chat.services;

import com.websocket.chat.data.User;
import com.websocket.chat.data.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> findChatUsers(){
        return userRepository.findAll();
    }

}
