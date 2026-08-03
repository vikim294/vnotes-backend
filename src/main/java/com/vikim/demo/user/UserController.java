package com.vikim.demo.user;


import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/users")
public class UserController {


    private final UserService userService;


    public UserController(UserService userService){

        this.userService = userService;

    }



    @GetMapping
    public List<UserResponse> findAll(){
        return userService.findAll();
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerAccount(@Valid @RequestBody RegisterAccountRequest request) {
        userService.registerAccount(request);
    }
}