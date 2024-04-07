package hh.forest_of_habits.service;

import hh.forest_of_habits.dto.AuthRequest;
import hh.forest_of_habits.dto.AuthResponse;
import hh.forest_of_habits.dto.ErrorDTO;
import hh.forest_of_habits.dto.RegistrationRequest;
import hh.forest_of_habits.utils.JwtTokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtTokenUtils tokenUtils;
    private final AuthenticationManager authenticationManager;

    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    authRequest.getUsername(),
                    authRequest.getPassword()
            ));
        } catch (BadCredentialsException exception) {
            return new ResponseEntity<>(
                    new ErrorDTO(
                            HttpStatus.UNAUTHORIZED.value(),
                            "Неверный логин или пароль",
                            LocalDateTime.now()
                    ),
                    HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.ok(
                authResponse((UserDetails) authentication.getPrincipal()));
    }

    public ResponseEntity<?> registration(@RequestBody RegistrationRequest request) {

        if (userService.findByName(request.getUsername()).isPresent()) {
            return new ResponseEntity<>(new ErrorDTO(
                    HttpStatus.BAD_REQUEST.value(),
                    "Пользователь уже существует",
                    LocalDateTime.now()),
                    HttpStatus.BAD_REQUEST
            );
        }

        userService.createNewUser(request);

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                request.getUsername(),
                request.getPassword(),
                List.of()
        );
        return ResponseEntity.ok(authResponse(userDetails));
    }

    private AuthResponse authResponse(UserDetails userDetails) {
        String token = tokenUtils.generateToken(userDetails);
        return new AuthResponse(token);
    }
}
