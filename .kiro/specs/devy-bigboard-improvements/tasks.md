# Implementation Plan: Devy BigBoard Improvements

## Overview

This implementation plan breaks down the Devy BigBoard enhancements into discrete, incremental tasks. The approach prioritizes database integration first, then builds player management, draft features, and UI improvements on top of that foundation. Each task builds on previous work, with checkpoints to ensure stability before proceeding.

## Tasks

- [x] 1. Infrastructure and project restructuring
  - Rename devybigboard-client directory to app-client
  - Rename devybigboard-server directory to app-server
  - Update all build file references (build.gradle, package.json, webpack.config.js)
  - Create railway.json with build and deployment configuration
  - Create nixpacks.toml with Java 21 and Node.js build settings
  - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5_

- [x] 2. Database setup and entity models
  - [x] 2.1 Configure MySQL connection in application.properties
    - Make a application-local.yml (git ignored that will have hardcoded values like creds)
    - Make a application-cloud.yml that will inject the values from ENV vars ${}
    - Add Railway MySQL connection string, username, password
    - Configure JPA/Hibernate settings (ddl-auto, dialect)
    - Add HikariCP connection pool configuration
    - _Requirements: 1.1_
  
  - [x] 2.2 Create Player entity with JPA annotations
    - Add fields: id, name, position, team, college, verified, totalSelections, sumDraftPositions, averageDraftPosition
    - Add timestamps (createdAt, updatedAt)
    - Add indexes on verified, position, averageDraftPosition
    - _Requirements: 1.2, 12.1_
  
  - [x] 2.3 Create Draft entity with JPA annotations
    - Add fields: id, uuid, draftName, status, participantCount, createdAt, completedAt
    - Add index on uuid and createdAt
    - _Requirements: 1.3, 12.1, 12.2_
  
  - [x] 2.4 Create DraftPick entity with JPA annotations
    - Add fields: id, draft (ManyToOne), player (ManyToOne), pickNumber, pickedAt
    - Add indexes on draft_id and player_id
    - Configure cascade delete for draft relationship
    - _Requirements: 1.3, 12.3_
  
  - [ ]* 2.5 Write unit tests for entity relationships
    - Test Player-DraftPick relationship
    - Test Draft-DraftPick cascade delete
    - Test timestamp auto-population
    - _Requirements: 1.2, 1.3_

- [x] 3. Repository layer
  - [x] 3.1 Create PlayerRepository interface
    - Add method: findByVerifiedTrue()
    - Extend JpaRepository<Player, Long>
    - _Requirements: 2.2_
  
  - [x] 3.2 Create DraftRepository interface
    - Add method: findByUuid(String uuid)
    - Add method: findTopNByOrderByCreatedAtDesc(int limit)
    - Extend JpaRepository<Draft, Long>
    - _Requirements: 4.4_
  
  - [ ]* 3.3 Write integration tests for repositories
    - Use Testcontainers with MySQL
    - Test findByVerifiedTrue filters correctly
    - Test findByUuid retrieval
    - _Requirements: 2.2, 4.4_

- [ ] 4. Player management service and API
  - [ ] 4.1 Create PlayerDTO with validation annotations
    - Add @NotBlank for name and position
    - Add @Size constraints for all string fields
    - _Requirements: 2.3, 10.4_
  
  - [ ] 4.2 Implement PlayerService
    - Implement createPlayer() with validation
    - Implement getVerifiedPlayers()
    - Implement verifyPlayer()
    - Implement updateADP() with calculation logic
    - _Requirements: 2.1, 2.2, 2.3, 1.4_
  
  - [ ]* 4.3 Write property test for ADP calculation
    - **Property 1: ADP Calculation Accuracy**
    - Generate random existing selections, sums, and new pick numbers
    - Verify ADP = sum / total after update
    - **Validates: Requirements 1.4, 4.3**
  
  - [ ]* 4.4 Write property test for unverified player default state
    - **Property 2: Unverified Player Default State**
    - Generate random valid player data
    - Verify created player has verified=false
    - **Validates: Requirements 2.1**
  
  - [ ]* 4.5 Write property test for verified player pool filtering
    - **Property 3: Verified Player Pool Filtering**
    - Create mix of verified and unverified players
    - Verify getVerifiedPlayers() returns only verified=true
    - **Validates: Requirements 2.2**
  
  - [ ] 4.6 Create PlayerController with REST endpoints
    - POST /api/players - create player
    - GET /api/players - get verified players
    - POST /api/players/{id}/verify - verify player with code
    - _Requirements: 2.1, 2.2, 3.2_
  
  - [ ]* 4.7 Write property test for required field validation
    - **Property 4: Required Field Validation**
    - Generate player submissions with missing fields
    - Verify HTTP 400 response with field errors
    - **Validates: Requirements 2.3, 2.4**
  
  - [ ]* 4.8 Write unit tests for PlayerController
    - Test successful player creation returns 201
    - Test validation errors return 400
    - Test get verified players returns 200
    - _Requirements: 2.1, 2.2, 2.5_


- [ ] 5. Verification service
  - [ ] 5.1 Add verification secret to application.properties
    - Add property: app.verification.secret
    - Document in README how to set for production
    - _Requirements: 3.1_
  
  - [ ] 5.2 Implement VerificationService
    - Inject verification secret from properties
    - Implement verifyPlayer() with secret validation
    - Throw UnauthorizedException for incorrect code
    - _Requirements: 3.2, 3.3_
  
  - [ ]* 5.3 Write property test for verification state transition
    - **Property 5: Verification State Transition**
    - Generate random unverified players
    - Verify calling verify with correct code sets verified=true
    - **Validates: Requirements 3.2, 3.5**
  
  - [ ]* 5.4 Write property test for verification authentication
    - **Property 6: Verification Authentication**
    - Generate random incorrect codes
    - Verify all return HTTP 403
    - **Validates: Requirements 3.3**
  
  - [ ]* 5.5 Write unit tests for VerificationService
    - Test correct code verifies player
    - Test incorrect code throws UnauthorizedException
    - Test non-existent player throws PlayerNotFoundException
    - _Requirements: 3.2, 3.3, 3.4_

- [ ] 6. Checkpoint - Ensure player management works
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 7. Draft service and persistence
  - [ ] 7.1 Create DraftDTO and PickDTO with validation
    - Add validation annotations for required fields
    - Add @Min constraints for numeric fields
    - _Requirements: 4.2, 10.5_
  
  - [ ] 7.2 Implement DraftService
    - Implement saveDraft() with UUID generation
    - Implement getDraftByUuid()
    - Implement getRecentDrafts()
    - Call playerService.updateADP() for each pick
    - _Requirements: 4.1, 4.2, 4.3, 4.4_
  
  - [ ]* 7.3 Write property test for draft UUID uniqueness
    - **Property 7: Draft UUID Uniqueness**
    - Generate multiple drafts
    - Verify all UUIDs are distinct and valid format
    - **Validates: Requirements 4.1**
  
  - [ ]* 7.4 Write property test for draft persistence round-trip
    - **Property 8: Draft Persistence Round-Trip**
    - Generate random draft data
    - Save draft, retrieve by UUID
    - Verify retrieved data equals original
    - **Validates: Requirements 4.2, 4.4**
  
  - [ ] 7.5 Create DraftController with REST endpoints
    - POST /api/drafts - save draft
    - GET /api/drafts/{uuid} - get draft by UUID
    - _Requirements: 4.2, 4.4_
  
  - [ ]* 7.6 Write unit tests for DraftController
    - Test successful draft save returns 201 with UUID
    - Test draft retrieval returns 200
    - Test non-existent UUID returns 404
    - _Requirements: 4.2, 4.4, 4.5_

- [ ] 8. Export functionality
  - [ ] 8.1 Add export dependencies to build.gradle
    - Add OpenCSV for CSV export
    - Add Apache PDFBox for PDF export
    - Add Jackson for JSON (already included)
    - _Requirements: 5.1, 5.2, 5.3_
  
  - [ ] 8.2 Implement ExportService
    - Implement exportToCSV() with proper formatting
    - Implement exportToJSON() with complete data
    - Implement exportToPDF() with printable layout
    - _Requirements: 5.1, 5.2, 5.3_
  
  - [ ]* 8.3 Write property test for CSV export completeness
    - **Property 9: CSV Export Completeness**
    - Generate random drafts
    - Verify CSV has correct rows and columns
    - **Validates: Requirements 5.1**
  
  - [ ]* 8.4 Write property test for JSON export completeness
    - **Property 10: JSON Export Completeness**
    - Generate random drafts
    - Verify JSON is valid and contains all fields
    - **Validates: Requirements 5.2**
  
  - [ ] 8.5 Add export endpoints to DraftController
    - GET /api/drafts/{uuid}/export/csv
    - GET /api/drafts/{uuid}/export/json
    - GET /api/drafts/{uuid}/export/pdf
    - Set appropriate Content-Type headers
    - _Requirements: 5.1, 5.2, 5.3, 5.4_
  
  - [ ]* 8.6 Write property test for export content-type headers
    - **Property 11: Export Content-Type Headers**
    - Test each export format
    - Verify Content-Type matches format
    - **Validates: Requirements 5.4**
  
  - [ ]* 8.7 Write unit tests for export endpoints
    - Test CSV export returns text/csv
    - Test JSON export returns application/json
    - Test PDF export returns application/pdf
    - Test export failure returns 500
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_

- [ ] 9. Shareable links and draft viewing
  - [ ] 9.1 Update DraftController to return shareable URL
    - Modify POST /api/drafts response to include shareable URL
    - Format: {baseUrl}/drafts/{uuid}
    - _Requirements: 6.1_
  
  - [ ]* 9.2 Write property test for shareable URL format
    - **Property 12: Shareable URL Format**
    - Generate random drafts
    - Verify returned URL contains UUID and is valid
    - **Validates: Requirements 6.1**
  
  - [ ]* 9.3 Write property test for unauthenticated draft access
    - **Property 14: Unauthenticated Draft Access**
    - Test draft retrieval without auth headers
    - Verify successful response
    - **Validates: Requirements 6.5**

- [ ] 10. Checkpoint - Ensure backend functionality complete
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 11. Global error handling
  - [ ] 11.1 Create custom exception classes
    - Create PlayerNotFoundException
    - Create DraftNotFoundException
    - Create UnauthorizedException
    - Create ValidationException
    - _Requirements: 9.4, 9.5_
  
  - [ ] 11.2 Create ErrorResponse model
    - Add fields: timestamp, status, error, message, path, fieldErrors
    - _Requirements: 9.5_
  
  - [ ] 11.3 Implement GlobalExceptionHandler
    - Handle PlayerNotFoundException → 404
    - Handle DraftNotFoundException → 404
    - Handle UnauthorizedException → 403
    - Handle ValidationException → 400
    - Handle MethodArgumentNotValidException → 400
    - Handle DataAccessException → 503
    - Handle generic Exception → 500
    - Log all errors with stack traces
    - _Requirements: 1.5, 9.4, 9.5_
  
  - [ ]* 11.4 Write property test for error response structure consistency
    - **Property 20: Error Response Structure Consistency**
    - Generate various error conditions
    - Verify all error responses have required fields
    - **Validates: Requirements 9.5**
  
  - [ ]* 11.5 Write property test for error logging completeness
    - **Property 21: Error Logging Completeness**
    - Trigger various errors
    - Verify log entries contain timestamp, message, stack trace
    - **Validates: Requirements 9.4**
  
  - [ ]* 11.6 Write unit tests for GlobalExceptionHandler
    - Test each exception type returns correct status code
    - Test error response format
    - Test field errors are included for validation failures
    - _Requirements: 9.4, 9.5_

- [ ] 12. Input validation and security
  - [ ] 12.1 Add validation annotations to all DTOs
    - Ensure @NotBlank, @NotNull, @Size on all fields
    - Add @Min, @Max for numeric fields
    - _Requirements: 10.1, 10.4, 10.5_
  
  - [ ] 12.2 Configure JPA to use parameterized queries
    - Verify Hibernate uses prepared statements
    - Add SQL logging for development
    - _Requirements: 10.3_
  
  - [ ]* 12.3 Write property test for input type validation
    - **Property 22: Input Type Validation**
    - Send requests with type mismatches
    - Verify HTTP 400 responses
    - **Validates: Requirements 10.1, 10.2**
  
  - [ ]* 12.4 Write property test for SQL injection prevention
    - **Property 23: SQL Injection Prevention**
    - Generate inputs with SQL injection patterns
    - Verify no SQL commands execute
    - **Validates: Requirements 10.3**
  
  - [ ]* 12.5 Write property test for text field length constraints
    - **Property 24: Text Field Length Constraints**
    - Generate inputs exceeding max length
    - Verify HTTP 400 responses
    - **Validates: Requirements 10.4**
  
  - [ ]* 12.6 Write property test for numeric range validation
    - **Property 25: Numeric Range Validation**
    - Generate out-of-range numeric values
    - Verify HTTP 400 responses
    - **Validates: Requirements 10.5**


- [ ] 13. Frontend API client and error handling
  - [ ] 13.1 Create TypeScript interfaces for all data models
    - Create Player, Draft, DraftPick, PlayerSubmission interfaces
    - Create ApiError interface
    - _Requirements: 9.5_
  
  - [ ] 13.2 Implement ApiClient class with error handling
    - Implement request() method with try-catch
    - Parse error responses into ApiError objects
    - Handle network errors
    - Implement methods: getVerifiedPlayers(), createPlayer(), saveDraft(), getDraft()
    - _Requirements: 9.2, 9.5_
  
  - [ ] 13.3 Create ErrorDisplay component
    - Display error message with icon
    - Show field errors if present
    - Include dismiss button
    - Add ARIA live region for screen readers
    - _Requirements: 9.2, 7.1_
  
  - [ ]* 13.4 Write unit tests for ApiClient
    - Test successful requests
    - Test error response parsing
    - Test network error handling
    - _Requirements: 9.2, 9.5_
  
  - [ ]* 13.5 Write unit tests for ErrorDisplay component
    - Test error message rendering
    - Test field errors rendering
    - Test dismiss functionality
    - _Requirements: 9.2_

- [ ] 14. Player submission form component
  - [ ] 14.1 Create PlayerSubmissionForm component
    - Add form fields: name, position, team, college
    - Add client-side validation
    - Display loading state during submission
    - Display validation errors from API
    - Add ARIA labels and required attributes
    - _Requirements: 2.1, 7.1, 9.1, 9.3_
  
  - [ ]* 14.2 Write property test for validation error highlighting
    - **Property 19: Validation Error Highlighting**
    - Submit forms with various invalid fields
    - Verify errors appear on correct fields
    - **Validates: Requirements 9.3**
  
  - [ ]* 14.3 Write unit tests for PlayerSubmissionForm
    - Test form submission with valid data
    - Test validation error display
    - Test loading state
    - Test ARIA attributes
    - _Requirements: 2.1, 7.1, 9.1, 9.3_

- [ ] 15. Enhanced draft board component
  - [ ] 15.1 Update DraftBoard component with accessibility
    - Add ARIA labels to all interactive elements
    - Add role="application" to draft board
    - Add role="list" and role="listitem" to player lists
    - Add keyboard event handlers (Enter, Space for selection)
    - Add tabIndex to draggable items
    - _Requirements: 7.1, 7.2_
  
  - [ ] 15.2 Add responsive design to DraftBoard
    - Detect viewport width and set mobile/desktop mode
    - Add resize event listener
    - Apply mobile-optimized layout for width < 768px
    - _Requirements: 8.1_
  
  - [ ] 15.3 Enhance drag-and-drop for touch devices
    - Add touch event handlers
    - Add visual feedback during drag
    - Ensure smooth touch interactions
    - _Requirements: 8.2, 8.3_
  
  - [ ]* 15.4 Write property test for ARIA label presence
    - **Property 15: ARIA Label Presence**
    - Render draft board with various player counts
    - Verify all interactive elements have ARIA labels
    - **Validates: Requirements 7.1**
  
  - [ ]* 15.5 Write property test for touch target minimum size
    - **Property 16: Touch Target Minimum Size**
    - Render mobile layout
    - Measure all interactive elements
    - Verify dimensions >= 44x44px
    - **Validates: Requirements 8.5**
  
  - [ ]* 15.6 Write unit tests for DraftBoard
    - Test drag-and-drop functionality
    - Test keyboard selection
    - Test mobile layout switching
    - Test touch event handling
    - _Requirements: 7.1, 7.2, 8.1, 8.2, 8.3_

- [ ] 16. Export controls component
  - [ ] 16.1 Create ExportControls component
    - Add buttons for CSV, JSON, PDF export
    - Implement download functionality
    - Display loading state during export
    - Display error messages if export fails
    - Add ARIA labels to buttons
    - _Requirements: 5.1, 5.2, 5.3, 9.1, 9.2, 7.1_
  
  - [ ]* 16.2 Write property test for loading state indication
    - **Property 17: Loading State Indication**
    - Trigger various API requests
    - Verify loading indicator appears
    - **Validates: Requirements 9.1**
  
  - [ ]* 16.3 Write property test for error message display
    - **Property 18: Error Message Display**
    - Trigger API failures
    - Verify error messages appear within 100ms
    - **Validates: Requirements 9.2**
  
  - [ ]* 16.4 Write unit tests for ExportControls
    - Test each export format download
    - Test loading state
    - Test error handling
    - _Requirements: 5.1, 5.2, 5.3, 9.1, 9.2_

- [ ] 17. Shareable link component
  - [ ] 17.1 Create ShareableLink component
    - Display shareable URL in read-only input
    - Add copy-to-clipboard button
    - Show "Copied!" feedback
    - Add ARIA labels
    - _Requirements: 6.1, 7.1_
  
  - [ ] 17.2 Create DraftView page for shareable links
    - Create route: /drafts/:uuid
    - Fetch draft data on mount
    - Display draft in read-only mode
    - Display draft metadata (name, timestamp, participants)
    - Handle invalid UUID with error message
    - _Requirements: 6.2, 6.3, 6.4_
  
  - [ ]* 17.3 Write property test for draft metadata display
    - **Property 13: Draft Metadata Display**
    - Render draft view with various drafts
    - Verify all metadata fields are displayed
    - **Validates: Requirements 6.3**
  
  - [ ]* 17.4 Write unit tests for ShareableLink and DraftView
    - Test URL display
    - Test copy functionality
    - Test draft data fetching
    - Test error handling for invalid UUID
    - _Requirements: 6.1, 6.2, 6.3, 6.4_

- [ ] 18. Accessibility enhancements
  - [ ] 18.1 Add focus management to modals
    - Trap focus within modal when open
    - Return focus to trigger element on close
    - Add Escape key handler to close
    - _Requirements: 7.4_
  
  - [ ] 18.2 Add keyboard navigation to context menus
    - Add arrow key navigation
    - Add Enter/Space to select
    - Add Escape to close
    - _Requirements: 7.3_
  
  - [ ] 18.3 Add visible focus indicators with proper contrast
    - Add CSS focus styles to all interactive elements
    - Ensure 3:1 contrast ratio minimum
    - Test with keyboard navigation
    - _Requirements: 7.5_
  
  - [ ]* 18.4 Write accessibility tests with jest-axe
    - Test all major components for accessibility violations
    - Test keyboard navigation flows
    - Test focus management
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5_

- [ ] 19. Checkpoint - Ensure frontend functionality complete
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 20. Styling and theme improvements
  - [ ] 20.1 Create modern color theme
    - Define color palette with good contrast
    - Create CSS variables for theme colors
    - Ensure WCAG AA compliance (4.5:1 for text)
    - _Requirements: 7.5_
  
  - [ ] 20.2 Apply theme to all components
    - Update DraftBoard styling
    - Update form styling
    - Update button and control styling
    - Ensure consistent spacing and typography
    - _Requirements: 7.5_
  
  - [ ] 20.3 Add responsive breakpoints
    - Define breakpoints for mobile, tablet, desktop
    - Apply responsive styles to all components
    - Test on various screen sizes
    - _Requirements: 8.1, 8.4_

- [ ] 21. Integration and final wiring
  - [ ] 21.1 Wire PlayerSubmissionForm to API
    - Connect form submission to ApiClient.createPlayer()
    - Handle success and error states
    - Refresh player pool after successful submission
    - _Requirements: 2.1, 9.1, 9.2_
  
  - [ ] 21.2 Wire DraftBoard to API
    - Connect draft completion to ApiClient.saveDraft()
    - Display shareable link after save
    - Handle errors during save
    - _Requirements: 4.2, 6.1, 9.1, 9.2_
  
  - [ ] 21.3 Wire ExportControls to API
    - Connect export buttons to export endpoints
    - Handle download responses
    - Display errors if export fails
    - _Requirements: 5.1, 5.2, 5.3, 9.2_
  
  - [ ] 21.4 Add routing for DraftView page
    - Configure React Router for /drafts/:uuid
    - Add navigation from draft completion
    - _Requirements: 6.2_
  
  - [ ]* 21.5 Write end-to-end integration tests
    - Test complete draft flow: create → save → share → view
    - Test player submission → verification → appears in pool
    - Test export flow for all formats
    - _Requirements: All_

- [ ] 22. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties (minimum 100 iterations each)
- Unit tests validate specific examples and edge cases
- Backend uses jqwik for property-based testing
- Frontend uses Jest + React Testing Library
- All property tests must include comment tag: `// Feature: devy-bigboard-improvements, Property {number}: {property_text}`
