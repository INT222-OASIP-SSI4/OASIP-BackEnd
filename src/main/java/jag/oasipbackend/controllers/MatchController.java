package jag.oasipbackend.controllers;

import jag.oasipbackend.dtos.UserLoginDTO;
import jag.oasipbackend.services.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/match")
public class MatchController {
    @Autowired
    private MatchService matchService;

    @PostMapping("")
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseEntity userCheckMatch(@Valid @RequestBody UserLoginDTO userLoginDTO) {
        return matchService.matchCheck(userLoginDTO);
    }
}
