package com.lexia.backend.service;

import com.lexia.backend.entity.AuditLog;
import com.lexia.backend.entity.User;
import com.lexia.backend.repository.AuditLogRepository;
import com.lexia.backend.repository.UserRepository;
import com.lexia.backend.service.impl.AuditLogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuditLogService.
 */
@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    private User mockUser;
    private UUID userId;
    private UUID profileId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        profileId = UUID.randomUUID();

        mockUser = User.builder()
                .id(userId)
                .email("test@lexia.com")
                .isActive(true)
                .build();
    }

    @Test
    void testLogProfileUpdate_Success() {
        // Arrange
        String changes = "{\"firstName\":\"John\",\"lastName\":\"Doe\"}";
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        auditLogService.logProfileUpdate(userId, profileId, changes, ipAddress, userAgent);

        // Assert
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());

        AuditLog savedLog = auditLogCaptor.getValue();
        assertEquals(mockUser, savedLog.getUser());
        assertEquals("PROFILE_UPDATE", savedLog.getAction());
        assertEquals("UserProfile", savedLog.getEntityType());
        assertEquals(profileId, savedLog.getEntityId());
        assertEquals(changes, savedLog.getChanges());
        assertEquals(ipAddress, savedLog.getIpAddress());
        assertEquals(userAgent, savedLog.getUserAgent());
    }

    @Test
    void testLogProfileUpdate_UserNotFound_DoesNotThrow() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> {
            auditLogService.logProfileUpdate(userId, profileId, "{}", null, null);
        });

        verify(auditLogRepository, never()).save(any(AuditLog.class));
    }

    @Test
    void testLogAvatarUpdate_Success() {
        // Arrange
        String avatarUrl = "https://example.com/avatar.jpg";
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        auditLogService.logAvatarUpdate(userId, profileId, avatarUrl, ipAddress, userAgent);

        // Assert
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());

        AuditLog savedLog = auditLogCaptor.getValue();
        assertEquals(mockUser, savedLog.getUser());
        assertEquals("AVATAR_UPDATE", savedLog.getAction());
        assertEquals("UserProfile", savedLog.getEntityType());
        assertEquals(profileId, savedLog.getEntityId());
        assertTrue(savedLog.getChanges().contains(avatarUrl));
        assertEquals(ipAddress, savedLog.getIpAddress());
        assertEquals(userAgent, savedLog.getUserAgent());
    }

    @Test
    void testLogAvatarDelete_Success() {
        // Arrange
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        auditLogService.logAvatarDelete(userId, profileId, ipAddress, userAgent);

        // Assert
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());

        AuditLog savedLog = auditLogCaptor.getValue();
        assertEquals(mockUser, savedLog.getUser());
        assertEquals("AVATAR_DELETE", savedLog.getAction());
        assertEquals("UserProfile", savedLog.getEntityType());
        assertEquals(profileId, savedLog.getEntityId());
        assertTrue(savedLog.getChanges().contains("null"));
        assertEquals(ipAddress, savedLog.getIpAddress());
        assertEquals(userAgent, savedLog.getUserAgent());
    }

    @Test
    void testLogProfileUpdate_WithNullIpAndUserAgent_Success() {
        // Arrange
        String changes = "{\"firstName\":\"John\"}";

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        auditLogService.logProfileUpdate(userId, profileId, changes, null, null);

        // Assert
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());

        AuditLog savedLog = auditLogCaptor.getValue();
        assertNull(savedLog.getIpAddress());
        assertNull(savedLog.getUserAgent());
    }

    @Test
    void testLogAvatarUpdate_RepositoryException_DoesNotThrow() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(auditLogRepository.save(any(AuditLog.class))).thenThrow(new RuntimeException("Database error"));

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> {
            auditLogService.logAvatarUpdate(userId, profileId, "https://example.com/avatar.jpg", null, null);
        });
    }
}
