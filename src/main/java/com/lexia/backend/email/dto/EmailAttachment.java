package com.lexia.backend.email.dto;

import jakarta.activation.DataSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * DTO representing an email attachment.
 * 
 * <p>
 * <strong>Memory Warning:</strong> When using byte[] content, the entire file
 * is loaded into memory. For large files (>5MB), prefer using DataSource for
 * streaming to avoid OutOfMemoryError when sending bulk emails.
 * </p>
 *
 * @author LEXIA Development Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class EmailAttachment {

    /**
     * Maximum recommended size for in-memory attachments (5MB).
     * Larger files should use DataSource for streaming.
     */
    public static final int MAX_RECOMMENDED_SIZE_BYTES = 5 * 1024 * 1024; // 5MB

    /**
     * Filename for the attachment.
     */
    private String filename;

    /**
     * MIME content type (e.g., "application/pdf").
     */
    private String contentType;

    /**
     * Attachment content as bytes.
     * <p>
     * <strong>Warning:</strong> For files larger than 5MB, use dataSource instead
     * to avoid memory issues when processing bulk emails.
     * </p>
     */
    private byte[] content;

    /**
     * Alternative: DataSource for streaming large files.
     * Recommended for attachments larger than 5MB.
     */
    private DataSource dataSource;

    /**
     * Whether this is an inline attachment (for HTML images).
     */
    @Builder.Default
    private boolean inline = false;

    /**
     * Content ID for inline attachments (used in HTML src="cid:contentId").
     */
    private String contentId;

    /**
     * Checks if the attachment size exceeds the recommended limit.
     *
     * @return true if content is larger than MAX_RECOMMENDED_SIZE_BYTES
     */
    public boolean isOversized() {
        return content != null && content.length > MAX_RECOMMENDED_SIZE_BYTES;
    }

    /**
     * Gets the size of the attachment in bytes.
     *
     * @return size in bytes, or -1 if size cannot be determined
     */
    public long getSizeBytes() {
        if (content != null) {
            return content.length;
        }
        return -1; // Size unknown for DataSource
    }

    /**
     * Logs a warning if the attachment is oversized.
     * Call this before adding attachments to emails.
     */
    public void warnIfOversized() {
        if (isOversized()) {
            log.warn("Attachment '{}' is {}MB which exceeds the recommended {}MB limit. " +
                    "Consider using DataSource for better memory efficiency.",
                    filename,
                    content.length / (1024 * 1024),
                    MAX_RECOMMENDED_SIZE_BYTES / (1024 * 1024));
        }
    }

    /**
     * Creates a simple file attachment.
     * Logs a warning if the file exceeds the recommended size limit.
     *
     * @param filename    the filename
     * @param contentType the MIME type
     * @param content     the file content
     * @return the attachment
     */
    public static EmailAttachment file(String filename, String contentType, byte[] content) {
        EmailAttachment attachment = EmailAttachment.builder()
                .filename(filename)
                .contentType(contentType)
                .content(content)
                .inline(false)
                .build();
        attachment.warnIfOversized();
        return attachment;
    }

    /**
     * Creates an inline image attachment for HTML emails.
     *
     * @param contentId   the content ID to reference in HTML
     * @param filename    the filename
     * @param contentType the MIME type
     * @param content     the image content
     * @return the attachment
     */
    public static EmailAttachment inlineImage(String contentId, String filename,
            String contentType, byte[] content) {
        EmailAttachment attachment = EmailAttachment.builder()
                .contentId(contentId)
                .filename(filename)
                .contentType(contentType)
                .content(content)
                .inline(true)
                .build();
        attachment.warnIfOversized();
        return attachment;
    }

    /**
     * Creates a PDF attachment.
     * Logs a warning if the file exceeds the recommended size limit.
     *
     * @param filename the filename
     * @param content  the PDF content
     * @return the attachment
     */
    public static EmailAttachment pdf(String filename, byte[] content) {
        return file(filename, "application/pdf", content);
    }

    /**
     * Creates an attachment from a DataSource (recommended for large files).
     * This approach uses streaming and is more memory-efficient.
     *
     * @param filename   the filename
     * @param dataSource the data source for streaming
     * @return the attachment
     */
    public static EmailAttachment fromDataSource(String filename, DataSource dataSource) {
        return EmailAttachment.builder()
                .filename(filename)
                .dataSource(dataSource)
                .inline(false)
                .build();
    }
}
