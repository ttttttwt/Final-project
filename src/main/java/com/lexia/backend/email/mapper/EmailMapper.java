package com.lexia.backend.email.mapper;

import com.lexia.backend.email.dto.EmailLogDTO;
import com.lexia.backend.email.dto.EmailQueueDTO;
import com.lexia.backend.email.dto.EmailRequest;
import com.lexia.backend.email.entity.EmailLog;
import com.lexia.backend.email.entity.EmailQueue;
import com.lexia.backend.entity.User;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Email entities and DTOs.
 *
 * @author LEXIA Development Team
 * @since 1.0.0
 */
public final class EmailMapper {

    private EmailMapper() {
        // Utility class, prevent instantiation
    }

    // ==================== EmailQueue Mappings ====================

    /**
     * Converts an EmailRequest to an EmailQueue entity.
     *
     * @param request the email request
     * @param user    the recipient user (can be null)
     * @param subject the resolved subject line
     * @return the email queue entity
     */
    public static EmailQueue toEntity(EmailRequest request, User user, String subject) {
        return EmailQueue.builder()
                .recipient(user)
                .recipientEmail(request.getRecipientEmail())
                .recipientName(request.getRecipientName())
                .emailType(request.getEmailType())
                .subject(subject)
                .templateName(request.getEffectiveTemplateName())
                .templateData(request.getTemplateData())
                .priority(request.getEffectivePriority())
                .maxAttempts(request.getEffectivePriority().getMaxAttempts())
                .locale(request.getLocale())
                .build();
    }

    /**
     * Converts an EmailQueue entity to DTO.
     *
     * @param entity the email queue entity
     * @return the DTO
     */
    public static EmailQueueDTO toDTO(EmailQueue entity) {
        if (entity == null) {
            return null;
        }

        return EmailQueueDTO.builder()
                .id(entity.getId())
                .recipientId(entity.getRecipient() != null ? entity.getRecipient().getId() : null)
                .recipientEmail(entity.getRecipientEmail())
                .recipientName(entity.getRecipientName())
                .emailType(entity.getEmailType())
                .subject(entity.getSubject())
                .templateName(entity.getTemplateName())
                .status(entity.getStatus())
                .priority(entity.getPriority().name())
                .attempts(entity.getAttempts())
                .maxAttempts(entity.getMaxAttempts())
                .nextRetryAt(entity.getNextRetryAt())
                .lastError(entity.getLastError())
                .createdAt(entity.getCreatedAt())
                .sentAt(entity.getSentAt())
                .deliveredAt(entity.getDeliveredAt())
                .build();
    }

    /**
     * Converts a list of EmailQueue entities to DTOs.
     *
     * @param entities the entities
     * @return list of DTOs
     */
    public static List<EmailQueueDTO> toQueueDTOList(List<EmailQueue> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(EmailMapper::toDTO)
                .collect(Collectors.toList());
    }

    // ==================== EmailLog Mappings ====================

    /**
     * Converts an EmailLog entity to DTO.
     *
     * @param entity the email log entity
     * @return the DTO
     */
    public static EmailLogDTO toDTO(EmailLog entity) {
        if (entity == null) {
            return null;
        }

        return EmailLogDTO.builder()
                .id(entity.getId())
                .queueId(entity.getEmailQueue() != null ? entity.getEmailQueue().getId() : null)
                .recipientId(entity.getRecipient() != null ? entity.getRecipient().getId() : null)
                .recipientEmail(entity.getRecipientEmail())
                .emailType(entity.getEmailType())
                .subject(entity.getSubject())
                .provider(entity.getProvider())
                .providerMessageId(entity.getProviderMessageId())
                .status(entity.getStatus())
                .errorMessage(entity.getErrorMessage())
                .openedAt(entity.getOpenedAt())
                .clickedAt(entity.getClickedAt())
                .unsubscribedAt(entity.getUnsubscribedAt())
                .bouncedAt(entity.getBouncedAt())
                .complainedAt(entity.getComplainedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    /**
     * Converts a list of EmailLog entities to DTOs.
     *
     * @param entities the entities
     * @return list of DTOs
     */
    public static List<EmailLogDTO> toLogDTOList(List<EmailLog> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(EmailMapper::toDTO)
                .collect(Collectors.toList());
    }
}
