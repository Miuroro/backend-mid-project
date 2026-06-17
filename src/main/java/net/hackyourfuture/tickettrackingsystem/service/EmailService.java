package net.hackyourfuture.tickettrackingsystem.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final RestClient restClient;

    @Value("${resend.api.key}")
    private String apiKey;

    public EmailService() {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.resend.com")
                .build();
    }

    /**
     * Sends a notification email to a list of recipients when a ticket is updated.
     * Implements error handling for network failure
     */
    public void sendTicketUpdateEmail(List<String> recipients, Long ticketId, String title, String status, List<String> assigneeNames) {
        if (recipients == null || recipients.isEmpty()) {
            logger.info("No assignees found for Ticket #{}. Skipping email notification.", ticketId);
            return;
        }

        // Convert the list of names into a clean, comma-separated string
        String assigneesText = (assigneeNames == null || assigneeNames.isEmpty())
                ? "None"
                : String.join(", ", assigneeNames);

        String htmlBody = """
            <html>
            <body style="font-family: Arial, sans-serif; color: #333; line-height: 1.6;">
                <div style="max-width: 600px; margin: 0 auto; border: 1px solid #e0e0e0; padding: 20px; border-radius: 8px;">
                    <h2 style="color: #2b6cb0; margin-top: 0;">Ticket #%d Updated</h2>
                    <p>Hello,</p>
                    <p>The following ticket that you are assigned to has been modified:</p>
                    <table style="width: 100%%; border-collapse: collapse; margin: 20px 0;">
                        <tr>
                            <td style="padding: 8px; font-weight: bold; background: #f7fafc; width: 30%%; border: 1px solid #edf2f7;">Title:</td>
                            <td style="padding: 8px; border: 1px solid #edf2f7;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding: 8px; font-weight: bold; background: #f7fafc; border: 1px solid #edf2f7;">Status:</td>
                            <td style="padding: 8px; border: 1px solid #edf2f7;"><span style="background: #feebc8; color: #c05621; padding: 2px 6px; border-radius: 4px; font-size: 0.9em;">%s</span></td>
                        </tr>
                        <tr>
                            <td style="padding: 8px; font-weight: bold; background: #f7fafc; border: 1px solid #edf2f7;">Assignees:</td>
                            <td style="padding: 8px; border: 1px solid #edf2f7;">%s</td>
                        </tr>
                    </table>
                    <p style="font-size: 0.9em; color: #718096; margin-bottom: 0;">This is an automated notification tracker. Please do not reply directly to this email.</p>
                </div>
            </body>
            </html>
            """.formatted(ticketId, title, status, assigneesText); // <-- Added assigneesText here

        // Prepare the JSON body payload required by Resend's API specification
        Map<String, Object> payload = Map.of(
                "from", "TicketTracker <onboarding@resend.dev>",
                "to", recipients,
                "subject", "Notification: Ticket #" + ticketId + " has been updated",
                "html", htmlBody
        );

        try {
            logger.info("Attempting to send update emails to {} for Ticket #{}", recipients, ticketId);

            restClient.post()
                    .uri("/emails")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            logger.info("Successfully transmitted update alerts via Resend for Ticket #{}", ticketId);

        } catch (Exception e) {
            logger.error("CRITICAL: Failed to dispatch alert email to Resend for Ticket #{}. Reason: {}", ticketId, e.getMessage());
        }
    }
}