package uz.hemis.domain.config;

/**
 * DataSource types for routing
 */
public enum DataSourceType {
    MASTER,   // Write operations (INSERT, UPDATE, DELETE)
    REPLICA   // Read operations (SELECT)
}
