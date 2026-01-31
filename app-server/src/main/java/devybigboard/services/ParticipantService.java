package devybigboard.services;

import devybigboard.dao.DraftParticipantRepository;
import devybigboard.dao.DraftRepository;
import devybigboard.exceptions.DraftNotFoundException;
import devybigboard.exceptions.ValidationException;
import devybigboard.models.Draft;
import devybigboard.models.DraftParticipant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing participants in live draft lobbies.
 * Handles joining, leaving, ready status, and validation of participant data.
 */
@Service
public class ParticipantService {

    private final DraftParticipantRepository participantRepository;
    private final DraftRepository draftRepository;

    public ParticipantService(DraftParticipantRepository participantRepository, DraftRepository draftRepository) {
        this.participantRepository = participantRepository;
        this.draftRepository = draftRepository;
    }

    /**
     * Add a participant to a draft lobby.
     * Validates that the position and nickname are available.
     * 
     * @param draftId the ID of the draft
     * @param nickname the participant's nickname (2-50 characters)
     * @param position the participant's position (A-Z letter)
     * @return the created participant entity
     * @throws DraftNotFoundException if draft does not exist
     * @throws ValidationException if position or nickname is already taken
     * @throws IllegalStateException if draft is not in LOBBY status or lobby is full
     */
    @Transactional
    public DraftParticipant joinDraft(Long draftId, String nickname, String position) {
        // Validate draft exists
        Draft draft = draftRepository.findById(draftId)
            .orElseThrow(() -> new DraftNotFoundException("Draft not found with ID: " + draftId));
        
        // Validate draft is in LOBBY status
        if (!"LOBBY".equals(draft.getStatus())) {
            throw new IllegalStateException("Cannot join draft: draft is not in LOBBY status");
        }
        
        // Validate lobby is not full
        long currentParticipantCount = participantRepository.findByDraftId(draftId).size();
        if (currentParticipantCount >= draft.getParticipantCount()) {
            throw new IllegalStateException("Cannot join draft: lobby is full");
        }
        
        // Validate position is available
        if (!isPositionAvailable(draftId, position)) {
            throw new ValidationException("Position " + position + " is already taken");
        }
        
        // Validate nickname is available
        if (!isNicknameAvailable(draftId, nickname)) {
            throw new ValidationException("Nickname '" + nickname + "' is already taken");
        }
        
        // Validate position format (single uppercase letter A-Z)
        if (!position.matches("^[A-Z]$")) {
            throw new ValidationException("Position must be a single uppercase letter (A-Z)");
        }
        
        // Validate position is within participant count
        int positionIndex = position.charAt(0) - 'A';
        if (positionIndex >= draft.getParticipantCount()) {
            throw new ValidationException("Position " + position + " is not valid for a draft with " + 
                draft.getParticipantCount() + " participants");
        }
        
        // Validate nickname length
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new ValidationException("Nickname cannot be empty");
        }
        if (nickname.length() < 2 || nickname.length() > 50) {
            throw new ValidationException("Nickname must be between 2 and 50 characters");
        }
        
        // Create and save participant
        DraftParticipant participant = new DraftParticipant(draft, position, nickname.trim());
        participant.setJoinedAt(LocalDateTime.now());
        
        // Auto-verify and ready if participant is the creator
        if (nickname.trim().equals(draft.getCreatedBy())) {
            participant.setIsVerified(true);
            participant.setIsReady(true);
        } else {
            // Non-creators start as not ready and not verified
            participant.setIsReady(false);
            participant.setIsVerified(false);
        }
        
        return participantRepository.save(participant);
    }

    /**
     * Toggle the ready status of a participant.
     * For non-creators, requires PIN verification before setting ready to true.
     * 
     * @param draftId the ID of the draft
     * @param position the participant's position (A-Z letter)
     * @param isReady the new ready status
     * @param pin the PIN for verification (required for non-creators when setting ready to true)
     * @return the updated participant entity
     * @throws ValidationException if participant not found or PIN is invalid
     */
    @Transactional
    public DraftParticipant setReady(Long draftId, String position, boolean isReady, String pin) {
        DraftParticipant participant = participantRepository.findByDraftIdAndPosition(draftId, position)
            .orElseThrow(() -> new ValidationException("Participant not found at position " + position));
        
        Draft draft = draftRepository.findById(draftId)
            .orElseThrow(() -> new DraftNotFoundException("Draft not found with ID: " + draftId));
        
        // If setting ready to true and participant is not verified
        if (isReady && !participant.getIsVerified()) {
            // Check if participant is the creator (auto-verified)
            if (participant.getNickname().equals(draft.getCreatedBy())) {
                participant.setIsVerified(true);
            } else {
                // Non-creator must provide valid PIN
                if (pin == null || pin.trim().isEmpty()) {
                    throw new ValidationException("PIN is required to ready up");
                }
                
                if (!pin.equals(draft.getPin())) {
                    throw new ValidationException("Invalid PIN");
                }
                
                // Mark as verified
                participant.setIsVerified(true);
            }
        }
        
        participant.setIsReady(isReady);
        return participantRepository.save(participant);
    }

    /**
     * Remove a participant from a draft lobby.
     * 
     * @param draftId the ID of the draft
     * @param position the participant's position (A-Z letter)
     * @throws ValidationException if participant not found
     */
    @Transactional
    public void leaveDraft(Long draftId, String position) {
        DraftParticipant participant = participantRepository.findByDraftIdAndPosition(draftId, position)
            .orElseThrow(() -> new ValidationException("Participant not found at position " + position));
        
        participantRepository.delete(participant);
    }

    /**
     * Get all participants for a specific draft.
     * 
     * @param draftId the ID of the draft
     * @return list of all participants in the draft
     */
    @Transactional(readOnly = true)
    public List<DraftParticipant> getParticipants(Long draftId) {
        return participantRepository.findByDraftId(draftId);
    }

    /**
     * Check if a position is available in a draft.
     * 
     * @param draftId the ID of the draft
     * @param position the position (A-Z letter) to check
     * @return true if the position is available, false if taken
     */
    @Transactional(readOnly = true)
    public boolean isPositionAvailable(Long draftId, String position) {
        Optional<DraftParticipant> existing = participantRepository.findByDraftIdAndPosition(draftId, position);
        return existing.isEmpty();
    }

    /**
     * Check if a nickname is available in a draft.
     * 
     * @param draftId the ID of the draft
     * @param nickname the nickname to check
     * @return true if the nickname is available, false if taken
     */
    @Transactional(readOnly = true)
    public boolean isNicknameAvailable(Long draftId, String nickname) {
        List<DraftParticipant> participants = participantRepository.findByDraftId(draftId);
        return participants.stream()
            .noneMatch(p -> p.getNickname().equalsIgnoreCase(nickname.trim()));
    }
}
