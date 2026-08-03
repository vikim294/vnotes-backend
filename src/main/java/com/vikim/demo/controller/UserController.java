package com.vikim.demo.controller;


import com.vikim.demo.entity.User;
import com.vikim.demo.service.UserService;
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
    public List<User> findAll(){

        return userService.findAll();

    }

}