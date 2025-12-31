package com.mentalhealth.backend.controller;

import com.mentalhealth.backend.model.User;
import com.mentalhealth.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {


        System.out.println("REGISTER API HIT");
        System.out.println("Username: " + user.getUsername());

        return ResponseEntity.ok(userService.register(user));
    }
}



