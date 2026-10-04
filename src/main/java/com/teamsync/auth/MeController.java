package com.teamsync.auth;

import com.teamsync.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class MeController {

    private final UserRepository userRepository;

    public MeController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        return userRepository.findById(userId)
                .map(user -> ResponseEntity.ok(new UserResponse(user)))
                .orElse(ResponseEntity.status(401).build());
    }

    @PatchMapping("/me")
    public ResponseEntity<?> updateMe(Authentication authentication,
                                      @Valid @RequestBody UpdateProfileRequest request) {
        String userId = (String) authentication.getPrincipal();
        return userRepository.findById(userId)
                .map(user -> {
                    user.setName(request.name().trim());
                    userRepository.save(user);
                    return ResponseEntity.ok(new UserResponse(user));
                })
                .orElse(ResponseEntity.status(401).build());
    }
}