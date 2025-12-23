package com.example.fingerprint_api.controllers;

import com.example.fingerprint_api.models.Usuario.UsuarioModel;
import com.example.fingerprint_api.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Permite peticiones desde tu Frontend
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UsuarioModel loginRequest) {
        Optional<UsuarioModel> user = usuarioRepository.findByUsername(loginRequest.getUsername());

        if (user.isPresent() && user.get().getPassword().equals(loginRequest.getPassword())) {
            // En un entorno real usaríamos BCrypt y JWT, pero para este paso usaremos texto plano
            return ResponseEntity.ok(user.get());
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario o contraseña incorrectos");
    }
}
