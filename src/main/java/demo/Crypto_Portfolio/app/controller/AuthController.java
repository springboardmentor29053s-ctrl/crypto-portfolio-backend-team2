package demo.Crypto_Portfolio.app.controller;

import demo.Crypto_Portfolio.app.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;


import demo.Crypto_Portfolio.app.model.User;
import demo.Crypto_Portfolio.app.service.UserService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")

public class AuthController {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;
    // REGISTER
    @PostMapping("/register")
    public String register(@RequestBody User user) {
        System.out.println("Register API Hit");
        return userService.register(user);
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {

        boolean success = userService.login(
                user.getEmail(),
                user.getPassword()
        );

        if (!success) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid Email or Password");
        }

        return ResponseEntity.ok("Login Successful");
    }

}
