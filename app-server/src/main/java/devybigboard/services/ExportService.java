package devybigboard.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.opencsv.CSVWriter;
import devybigboard.models.Draft;
import devybigboard.models.DraftPick;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExportService {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Export draft to CSV format.
     * Generates a CSV file with columns: Pick, Player, Position, Team
     * 
     * @param draft the draft to export
     * @return CSV content as byte array
     */
    public byte[] exportToCSV(Draft draft) {
        try {
            StringWriter writer = new StringWriter();
            CSVWriter csvWriter = new CSVWriter(writer);
            
            // Write header
            csvWriter.writeNext(new String[]{"Pick", "Player", "Position", "Team"});
            
            // Sort picks by pick number
            List<DraftPick> sortedPicks = draft.getPicks().stream()
                .sorted(Comparator.comparing(DraftPick::getPickNumber))
                .collect(Collectors.toList());
            
            // Write picks
            for (DraftPick pick : sortedPicks) {
                csvWriter.writeNext(new String[]{
                    String.valueOf(pick.getPickNumber()),
                    pick.getPlayer().getName(),
                    pick.getPlayer().getPosition(),
                    pick.getPlayer().getTeam() != null ? pick.getPlayer().getTeam() : ""
                });
            }
            
            csvWriter.close();
            return writer.toString().getBytes(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate CSV export", e);
        }
    }
    
    /**
     * Export draft to JSON format.
     * Generates a JSON document with complete draft data including metadata.
     * 
     * @param draft the draft to export
     * @return JSON content as string
     */
    public String exportToJSON(Draft draft) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            
            // Create export DTO
            Map<String, Object> exportData = new LinkedHashMap<>();
            exportData.put("uuid", draft.getUuid());
            exportData.put("draftName", draft.getDraftName());
            exportData.put("status", draft.getStatus());
            exportData.put("participantCount", draft.getParticipantCount());
            exportData.put("createdAt", draft.getCreatedAt());
            exportData.put("completedAt", draft.getCompletedAt());
            
            // Add picks
            List<Map<String, Object>> picks = draft.getPicks().stream()
                .sorted(Comparator.comparing(DraftPick::getPickNumber))
                .map(pick -> {
                    Map<String, Object> pickData = new LinkedHashMap<>();
                    pickData.put("pickNumber", pick.getPickNumber());
                    pickData.put("playerId", pick.getPlayer().getId());
                    pickData.put("playerName", pick.getPlayer().getName());
                    pickData.put("position", pick.getPlayer().getPosition());
                    pickData.put("team", pick.getPlayer().getTeam());
                    pickData.put("college", pick.getPlayer().getCollege());
                    pickData.put("pickedAt", pick.getPickedAt());
                    return pickData;
                })
                .collect(Collectors.toList());
            
            exportData.put("picks", picks);
            
            return mapper.writeValueAsString(exportData);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate JSON export", e);
        }
    }
    
    /**
     * Export draft to PDF format.
     * Generates a printable PDF with formatted draft results.
     * 
     * @param draft the draft to export
     * @return PDF content as byte array
     */
    public byte[] exportToPDF(Draft draft) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float margin = 50;
                float yPosition = page.getMediaBox().getHeight() - margin;
                float fontSize = 12;
                float leading = 1.5f * fontSize;
                
                PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font regularFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                
                // Title
                contentStream.beginText();
                contentStream.setFont(titleFont, 16);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Draft Results");
                contentStream.endText();
                yPosition -= leading * 2;
                
                // Draft metadata
                contentStream.beginText();
                contentStream.setFont(regularFont, fontSize);
                contentStream.newLineAtOffset(margin, yPosition);
                
                if (draft.getDraftName() != null && !draft.getDraftName().isEmpty()) {
                    contentStream.showText("Draft Name: " + draft.getDraftName());
                    contentStream.newLineAtOffset(0, -leading);
                    yPosition -= leading;
                }
                
                contentStream.showText("UUID: " + draft.getUuid());
                contentStream.newLineAtOffset(0, -leading);
                yPosition -= leading;
                
                contentStream.showText("Participants: " + draft.getParticipantCount());
                contentStream.newLineAtOffset(0, -leading);
                yPosition -= leading;
                
                if (draft.getCompletedAt() != null) {
                    contentStream.showText("Completed: " + draft.getCompletedAt().format(DATE_FORMATTER));
                    contentStream.newLineAtOffset(0, -leading);
                    yPosition -= leading;
                }
                
                contentStream.endText();
                yPosition -= leading;
                
                // Picks header
                contentStream.beginText();
                contentStream.setFont(titleFont, 14);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Picks:");
                contentStream.endText();
                yPosition -= leading * 1.5f;
                
                // Sort picks by pick number
                List<DraftPick> sortedPicks = draft.getPicks().stream()
                    .sorted(Comparator.comparing(DraftPick::getPickNumber))
                    .collect(Collectors.toList());
                
                // Picks list
                contentStream.setFont(regularFont, fontSize);
                for (DraftPick pick : sortedPicks) {
                    // Check if we need a new page
                    if (yPosition < margin + leading) {
                        contentStream.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        PDPageContentStream newContentStream = new PDPageContentStream(document, page);
                        yPosition = page.getMediaBox().getHeight() - margin;
                        
                        // Continue with new content stream
                        return exportToPDFContinued(document, draft, sortedPicks, sortedPicks.indexOf(pick));
                    }
                    
                    String pickLine = String.format("%d. %s - %s (%s)",
                        pick.getPickNumber(),
                        pick.getPlayer().getName(),
                        pick.getPlayer().getPosition(),
                        pick.getPlayer().getTeam() != null ? pick.getPlayer().getTeam() : "N/A"
                    );
                    
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin + 10, yPosition);
                    contentStream.showText(pickLine);
                    contentStream.endText();
                    yPosition -= leading;
                }
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate PDF export", e);
        }
    }
    
    /**
     * Helper method to continue PDF generation on a new page.
     */
    private byte[] exportToPDFContinued(PDDocument document, Draft draft, List<DraftPick> picks, int startIndex) {
        try {
            PDPage page = document.getPage(document.getNumberOfPages() - 1);
            
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float margin = 50;
                float yPosition = page.getMediaBox().getHeight() - margin;
                float fontSize = 12;
                float leading = 1.5f * fontSize;
                
                PDType1Font regularFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                contentStream.setFont(regularFont, fontSize);
                
                for (int i = startIndex; i < picks.size(); i++) {
                    DraftPick pick = picks.get(i);
                    
                    if (yPosition < margin + leading) {
                        contentStream.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        return exportToPDFContinued(document, draft, picks, i);
                    }
                    
                    String pickLine = String.format("%d. %s - %s (%s)",
                        pick.getPickNumber(),
                        pick.getPlayer().getName(),
                        pick.getPlayer().getPosition(),
                        pick.getPlayer().getTeam() != null ? pick.getPlayer().getTeam() : "N/A"
                    );
                    
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin + 10, yPosition);
                    contentStream.showText(pickLine);
                    contentStream.endText();
                    yPosition -= leading;
                }
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate PDF export", e);
        }
    }
}
