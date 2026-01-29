package com.example.fingerprint_api.controllers;

import com.example.fingerprint_api.models.Usuario.UsuarioModel;
import com.example.fingerprint_api.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // --- LOGIN EXISTENTE ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UsuarioModel loginRequest) {
        Optional<UsuarioModel> user = usuarioRepository.findByUsername(loginRequest.getUsername());
        if (user.isPresent() &&
                user.get().getUsername().equals(loginRequest.getUsername()) &&
                user.get().getPassword().equals(loginRequest.getPassword())) {

            return ResponseEntity.ok(user.get());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario o contraseña incorrectos");
    }

    // --- NUEVOS ENDPOINTS PARA GESTIÓN DE USUARIOS ---

    // 1. Obtener todos los usuarios
    @GetMapping("/users")
    public List<UsuarioModel> getAllUsers() {
        return usuarioRepository.findAll();
    }

    // 2. Registrar nuevo usuario
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UsuarioModel newUser) {
        if (usuarioRepository.findByUsername(newUser.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("El nombre de usuario ya existe");
        }
        UsuarioModel savedUser = usuarioRepository.save(newUser);
        return ResponseEntity.ok(savedUser);
    }

    // 3. Editar usuario
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Integer id, @RequestBody UsuarioModel userDetails) {
        Optional<UsuarioModel> optionalUser = usuarioRepository.findById(id);

        if (optionalUser.isPresent()) {
            UsuarioModel user = optionalUser.get();
            user.setUsername(userDetails.getUsername());
            user.setPassword(userDetails.getPassword());
            user.setRol(userDetails.getRol());
            usuarioRepository.save(user);
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 4. Eliminar usuario
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
            return ResponseEntity.ok("Usuario eliminado correctamente");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}