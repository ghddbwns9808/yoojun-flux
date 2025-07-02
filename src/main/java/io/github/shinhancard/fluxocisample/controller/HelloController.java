package io.github.shinhancard.fluxocisample.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    private static int callcounter = 0;

    @GetMapping("/")
    public String hello() {
        return "this is flux test!";
    }

    @GetMapping("/callcounter")
    public int funCallCounter(){
        return ++callcounter;
    }
}