package org.kiennguyenfpt.datingapp.controllers;

import org.kiennguyenfpt.datingapp.dtos.mapper.UserMapper;
import org.kiennguyenfpt.datingapp.dtos.requests.ChangePasswordRequest;
import org.kiennguyenfpt.datingapp.dtos.requests.ForgotPasswordRequest;
import org.kiennguyenfpt.datingapp.dtos.requests.UserLoginRequest;
import org.kiennguyenfpt.datingapp.dtos.requests.UserRegistrationRequest;
import org.kiennguyenfpt.datingapp.dtos.responses.UserResponse;
import org.kiennguyenfpt.datingapp.entities.User;
import org.kiennguyenfpt.datingapp.responses.CommonResponse;
import org.kiennguyenfpt.datingapp.services.AuthService;
import org.kiennguyenfpt.datingapp.services.EmailService;
import org.kiennguyenfpt.datingapp.services.UserService;
import org.kiennguyenfpt.datingapp.services.impl.AuthServiceImpl;
import org.kiennguyenfpt.datingapp.utils.JwtUtil;
import org.kiennguyenfpt.datingapp.utils.PasswordUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequestMapping("api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, EmailService emailService, PasswordEncoder passwordEncoder, UserMapper userMapper, UserService userService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<CommonResponse<UserResponse>> register(@RequestBody UserRegistrationRequest userReq) {
        CommonResponse<UserResponse> response = new CommonResponse<>();
        try {
            AuthServiceImpl.UserWithPassword userWithPassword = authService.register(userReq.getEmail(), userReq.getName());
            if (userWithPassword != null) {
                UserResponse userResponse = userMapper.userToUserResponse(userWithPassword.getUser(), userWithPassword.getRawPassword());
                response.setStatus(HttpStatus.OK.value());
                response.setMessage("User registered successfully!");
                response.setData(userResponse);
                return ResponseEntity.ok(response);
            }
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage("Error registering user.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Error during registration: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @PostMapping("/login")
    public ResponseEntity<CommonResponse<String>> login(@RequestBody UserLoginRequest loginRequest) {
        CommonResponse<String> response = new CommonResponse<>();
        try {
            User user = userService.findByEmail(loginRequest.getEmail());
            if (user != null && passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
                if (user.isFirstLogin()) {
                    response.setStatus(HttpStatus.OK.value());
                    response.setMessage("Please change your password.");
                    return ResponseEntity.ok(response);
                } else {
                    String token = jwtUtil.generateToken(new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPasswordHash(), new ArrayList<>()));
                    response.setStatus(HttpStatus.OK.value());
                    response.setMessage("Login successful.");
                    response.setData(token);
                    return ResponseEntity.ok(response);
                }
            }
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage("Invalid email or password.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Error during login: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<CommonResponse<String>> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        CommonResponse<String> response = new CommonResponse<>();
        try {
            User user = userService.findByEmail(changePasswordRequest.getEmail());
            if (user != null && passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPasswordHash())) {
                user.setPasswordHash(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
                user.setFirstLogin(false);
                userService.save(user);
                response.setStatus(HttpStatus.OK.value());
                response.setMessage("Password changed successfully!");
                return ResponseEntity.ok(response);
            }
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage("Invalid email or password.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Error during password change: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<CommonResponse<User>> forgotPassword(@RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        CommonResponse<User> response = new CommonResponse<>();
        try {
            User user = userService.findByEmail(forgotPasswordRequest.getEmail());
            if (user != null) {
                String newPassword = PasswordUtil.generateRandomPassword();
                user.setPasswordHash(passwordEncoder.encode(newPassword)); // Cập nhật mật khẩu mới
                userService.save(user); // Lưu user với mật khẩu mới
                response.setStatus(HttpStatus.OK.value());
                response.setMessage("Password reset successfully. Please check your email for the new password.");
                response.setData(user);
                emailService.sendEmail(user.getEmail(), "Your New Password", "Your new password is: " + newPassword); // Gửi email với mật khẩu mới
                return ResponseEntity.ok(response);
            } else {
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                response.setMessage("Invalid email");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Error resetting password: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<CommonResponse<String>> logout(@RequestBody String token) {
        CommonResponse<String> response = new CommonResponse<>();
        try {
            jwtUtil.invalidateToken(token);
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Logout successful.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Error during logout: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
