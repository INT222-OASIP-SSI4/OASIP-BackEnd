package jag.oasipbackend.controllers;

import jag.oasipbackend.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email")
public class EmailController {
    @Autowired
    private EmailService service;

    @CrossOrigin(origins = "*")
    @GetMapping("/test")
    public void  sendEmail() {
        service.sendSimpleMessage("death.sites123@gmail.com", "test", "test");
    }
}
