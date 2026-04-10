package main.service;

import java.util.List;

public record EmailTask(String to, String subject, String htmlContent, List<InlineAttachment> inlineAttachments) {
    /** Factory method per email semplici senza allegati inline. */
    public static EmailTask simple(String to, String subject, String htmlContent) {
        return new EmailTask(to, subject, htmlContent, List.of());
    }
}
