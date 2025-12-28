package com.example.fingerprint_api.dto;

/**
 * DTO para las solicitudes de gestión de lectores que incluye readerId e instanceId.
 * 
 * @param readerId ID del lector de huellas a gestionar
 * @param instanceId ID único de la instancia/pestaña del navegador
 */
public record ReaderRequest(String readerId, String instanceId) {
}