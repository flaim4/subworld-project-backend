package net.flaim.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.flaim.dto.auth.register.RegisterRequest;
import net.flaim.dto.auth.register.RegisterResponse;
import net.flaim.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.validation.BindingResult;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String error = bindingResult.getFieldError().getDefaultMessage();
            return RegisterResponse.error(error);
        }

        try {
            authService.register(request);
            return RegisterResponse.success("Registered successfully");
        } catch (RuntimeException e) {
            return RegisterResponse.error(e.getMessage());
        }
    }

}
