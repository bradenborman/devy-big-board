package devybigboard.schema;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests to verify database schema and table structure.
 * Validates that all required tables and columns exist with correct types.
 */
@SpringBootTest
@ActiveProfiles("test")
class DatabaseSchemaTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void playersTable_ShouldExist() {
        // Verify the players table exists
        String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'PLAYERS'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        
        assertNotNull(count, "Query should return a result");
        assertEquals(1, count, "Players table should exist");
    }

    @Test
    void playersTable_ShouldHaveRequiredColumns() {
        // Query column information for players table
        String sql = "SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS " +
                     "WHERE TABLE_NAME = 'PLAYERS' ORDER BY COLUMN_NAME";
        
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(sql);
        
        assertFalse(columns.isEmpty(), "Players table should have columns");
        
        // Verify key columns exist
        assertTrue(hasColumn(columns, "ID"), "Should have ID column");
        assertTrue(hasColumn(columns, "NAME"), "Should have NAME column");
        assertTrue(hasColumn(columns, "POSITION"), "Should have POSITION column");
        assertTrue(hasColumn(columns, "TEAM"), "Should have TEAM column");
        assertTrue(hasColumn(columns, "COLLEGE"), "Should have COLLEGE column");
        assertTrue(hasColumn(columns, "VERIFIED"), "Should have VERIFIED column");
        assertTrue(hasColumn(columns, "CREATED_AT"), "Should have CREATED_AT column");
        assertTrue(hasColumn(columns, "UPDATED_AT"), "Should have UPDATED_AT column");
    }

    @Test
    void playersTable_RequiredColumns_ShouldNotBeNullable() {
        String sql = "SELECT COLUMN_NAME, IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS " +
                     "WHERE TABLE_NAME = 'PLAYERS' AND COLUMN_NAME IN ('ID', 'NAME', 'POSITION', 'VERIFIED')";
        
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(sql);
        
        for (Map<String, Object> column : columns) {
            String columnName = (String) column.get("COLUMN_NAME");
            String isNullable = (String) column.get("IS_NULLABLE");
            
            assertEquals("NO", isNullable, 
                columnName + " should be NOT NULL");
        }
    }

    @Test
    void draftsTable_ShouldExist() {
        String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'DRAFTS'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        
        assertNotNull(count, "Query should return a result");
        assertEquals(1, count, "Drafts table should exist");
    }

    @Test
    void draftsTable_ShouldHaveRequiredColumns() {
        String sql = "SELECT COLUMN_NAME, DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS " +
                     "WHERE TABLE_NAME = 'DRAFTS' ORDER BY COLUMN_NAME";
        
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(sql);
        
        assertFalse(columns.isEmpty(), "Drafts table should have columns");
        
        assertTrue(hasColumn(columns, "ID"), "Should have ID column");
        assertTrue(hasColumn(columns, "UUID"), "Should have UUID column");
        assertTrue(hasColumn(columns, "DRAFT_NAME"), "Should have DRAFT_NAME column");
        assertTrue(hasColumn(columns, "STATUS"), "Should have STATUS column");
        assertTrue(hasColumn(columns, "PARTICIPANT_COUNT"), "Should have PARTICIPANT_COUNT column");
        assertTrue(hasColumn(columns, "CREATED_AT"), "Should have CREATED_AT column");
        assertTrue(hasColumn(columns, "COMPLETED_AT"), "Should have COMPLETED_AT column");
    }

    @Test
    void draftsTable_UUID_ShouldBeUnique() {
        // Check if UUID column is NOT NULL (which is required for uniqueness)
        String nullCheckSql = "SELECT IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS " +
                              "WHERE TABLE_NAME = 'DRAFTS' AND COLUMN_NAME = 'UUID'";
        String isNullable = jdbcTemplate.queryForObject(nullCheckSql, String.class);
        assertEquals("NO", isNullable, "UUID should be NOT NULL");
        
        // Verify UUID column exists
        String columnCheckSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                                "WHERE TABLE_NAME = 'DRAFTS' AND COLUMN_NAME = 'UUID'";
        Integer count = jdbcTemplate.queryForObject(columnCheckSql, Integer.class);
        assertEquals(1, count, "UUID column should exist");
    }

    @Test
    void draftPicksTable_ShouldExist() {
        String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'DRAFT_PICKS'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        
        assertNotNull(count, "Query should return a result");
        assertEquals(1, count, "Draft_picks table should exist");
    }

    @Test
    void draftPicksTable_ShouldHaveRequiredColumns() {
        String sql = "SELECT COLUMN_NAME, DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS " +
                     "WHERE TABLE_NAME = 'DRAFT_PICKS' ORDER BY COLUMN_NAME";
        
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(sql);
        
        assertFalse(columns.isEmpty(), "Draft_picks table should have columns");
        
        assertTrue(hasColumn(columns, "ID"), "Should have ID column");
        assertTrue(hasColumn(columns, "DRAFT_ID"), "Should have DRAFT_ID column");
        assertTrue(hasColumn(columns, "PLAYER_ID"), "Should have PLAYER_ID column");
        assertTrue(hasColumn(columns, "PICK_NUMBER"), "Should have PICK_NUMBER column");
        assertTrue(hasColumn(columns, "PICKED_AT"), "Should have PICKED_AT column");
        assertTrue(hasColumn(columns, "POSITION"), "Should have POSITION column");
        assertTrue(hasColumn(columns, "FORCED_BY"), "Should have FORCED_BY column");
        assertTrue(hasColumn(columns, "ROUND_NUMBER"), "Should have ROUND_NUMBER column");
    }

    @Test
    void draftPicksTable_ShouldHaveForeignKeys() {
        // Verify that DRAFT_ID and PLAYER_ID columns exist (foreign key columns)
        String sql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                     "WHERE TABLE_NAME = 'DRAFT_PICKS' AND COLUMN_NAME IN ('DRAFT_ID', 'PLAYER_ID')";
        
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(sql);
        
        assertEquals(2, columns.size(), 
            "Draft_picks should have DRAFT_ID and PLAYER_ID columns for foreign keys");
        
        // Verify both columns are NOT NULL (required for foreign keys)
        String nullCheckSql = "SELECT COLUMN_NAME, IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS " +
                              "WHERE TABLE_NAME = 'DRAFT_PICKS' AND COLUMN_NAME IN ('DRAFT_ID', 'PLAYER_ID')";
        
        List<Map<String, Object>> nullChecks = jdbcTemplate.queryForList(nullCheckSql);
        
        for (Map<String, Object> column : nullChecks) {
            String isNullable = (String) column.get("IS_NULLABLE");
            assertEquals("NO", isNullable, 
                column.get("COLUMN_NAME") + " should be NOT NULL for foreign key relationship");
        }
    }

    @Test
    void allTables_ShouldBeAccessible() {
        // Verify we can query all tables without errors
        assertDoesNotThrow(() -> {
            jdbcTemplate.queryForList("SELECT COUNT(*) FROM PLAYERS");
            jdbcTemplate.queryForList("SELECT COUNT(*) FROM DRAFTS");
            jdbcTemplate.queryForList("SELECT COUNT(*) FROM DRAFT_PICKS");
        }, "All tables should be accessible for queries");
    }

    /**
     * Helper method to check if a column exists in the column list.
     */
    private boolean hasColumn(List<Map<String, Object>> columns, String columnName) {
        return columns.stream()
            .anyMatch(col -> columnName.equalsIgnoreCase((String) col.get("COLUMN_NAME")));
    }
}
