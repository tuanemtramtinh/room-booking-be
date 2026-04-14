package com.hcmut.roombookingbe.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin
@RequiredArgsConstructor
public class RoomController {

    @GetMapping("")
    public String test(){
        return "OK";
    }
}
