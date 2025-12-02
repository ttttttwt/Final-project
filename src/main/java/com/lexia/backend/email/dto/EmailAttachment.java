package com.lexia.backend.email.dto;

import jakarta.activation.DataSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing an email attachment.
 *
 * @author LEXIA Development Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailAttachment {

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
     */
    private byte[] content;

    /**
     * Alternative: DataSource for streaming large files.
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
     * Creates a simple file attachment.
     *
     * @param filename    the filename
     * @param contentType the MIME type
     * @param content     the file content
     * @return the attachment
     */
    public static EmailAttachment file(String filename, String contentType, byte[] content) {
        return EmailAttachment.builder()
                .filename(filename)
                .contentType(contentType)
                .content(content)
                .inline(false)
                .build();
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
        return EmailAttachment.builder()
                .contentId(contentId)
                .filename(filename)
                .contentType(contentType)
                .content(content)
                .inline(true)
                .build();
    }

    /**
     * Creates a PDF attachment.
     *
     * @param filename the filename
     * @param content  the PDF content
     * @return the attachment
     */
    public static EmailAttachment pdf(String filename, byte[] content) {
        return file(filename, "application/pdf", content);
    }
}
