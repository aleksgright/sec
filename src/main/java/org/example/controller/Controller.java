package org.example.controller;

import lombok.AllArgsConstructor;
import org.example.model.dto.CredentialsDto;
import org.example.model.entity.Data;
import org.example.model.entity.User;
import org.example.repository.DataRepository;
import org.example.repository.UserRepository;
import org.example.utils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.HtmlUtils;

import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
public class Controller {
    private final UserRepository userRepository;
    private final DataRepository dataRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @GetMapping("/hello")
    public String getMessage()
    {
        return "Hello sus!";
    }

    @GetMapping("/api/data")
    public List<Data> getData() {
        return dataRepository.findAll().stream().peek(
                it -> it.setValue(HtmlUtils.htmlEscape(it.getValue()))
        ).toList();
    }

    @PostMapping("/auth/register")
    public String register(@RequestBody CredentialsDto credentialsDto) {
        Optional<User> response = userRepository.findByLogin(credentialsDto.login());
        if (response.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Login is already taken");
        }
        User user = new User(
                HtmlUtils.htmlEscape(credentialsDto.login()),
                passwordEncoder.encode(credentialsDto.password())
        );
        userRepository.save(user);
        return jwtUtil.generateJwtToken(credentialsDto.login());
    }

    @PostMapping("/auth/login")
    public String login(@RequestBody CredentialsDto credentialsDto) {
        User user = userRepository.findByLogin(credentialsDto.login()).orElseThrow(() ->new ResponseStatusException(HttpStatus.CONFLICT, "User not found"));
        if (!passwordEncoder.matches(credentialsDto.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect password");
        }
        return jwtUtil.generateJwtToken(credentialsDto.login());
    }

    @PostMapping("api/data/create")
    public ResponseEntity<String> createData() {
        Data data = new Data();
        data.setValue("123");
        dataRepository.save(data);
        return new ResponseEntity<>("fine", HttpStatus.OK);
    }
}