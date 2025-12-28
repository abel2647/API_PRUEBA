package com.example.fingerprint_api.dto.websocket;

import com.example.fingerprint_api.dto.Empleado.EmpleadoDto;
import com.example.fingerprint_api.dto.asistencia.JornadaEstadoDto;
import java.util.List;

public class FullAttendanceStateEvent {
    private final String type = "FULL_ATTENDANCE_STATE_UPDATE";
    private String readerName;
    private EmpleadoDto employeeData;
    private List<JornadaEstadoDto> dailyWorkSessions;
    private String nextRecommendedActionBackend; // Values should be "entrada", "salida", "ALL_COMPLETE", "NO_ACTION"
    private Integer activeSessionIdBackend;
    private Integer justCompletedSessionIdBackend; // ID of a session that was just completed (for indicating recent exit)

    // Getters
    public String getType() { return type; }
    public String getReaderName() { return readerName; }
    public EmpleadoDto getEmployeeData() { return employeeData; }
    public List<JornadaEstadoDto> getDailyWorkSessions() { return dailyWorkSessions; }
    public String getNextRecommendedActionBackend() { return nextRecommendedActionBackend; }
    public Integer getActiveSessionIdBackend() { return activeSessionIdBackend; }
    public Integer getJustCompletedSessionIdBackend() { return justCompletedSessionIdBackend; }

    // Setters
    public void setReaderName(String readerName) { this.readerName = readerName; }
    public void setEmployeeData(EmpleadoDto employeeData) { this.employeeData = employeeData; }
    public void setDailyWorkSessions(List<JornadaEstadoDto> dailyWorkSessions) { this.dailyWorkSessions = dailyWorkSessions; }
    public void setNextRecommendedActionBackend(String nextRecommendedActionBackend) { this.nextRecommendedActionBackend = nextRecommendedActionBackend; }
    public void setActiveSessionIdBackend(Integer activeSessionIdBackend) { this.activeSessionIdBackend = activeSessionIdBackend; }
    public void setJustCompletedSessionIdBackend(Integer justCompletedSessionIdBackend) { this.justCompletedSessionIdBackend = justCompletedSessionIdBackend; }
} 