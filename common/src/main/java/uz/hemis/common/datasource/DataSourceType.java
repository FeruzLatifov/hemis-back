package uz.hemis.common.datasource;

/**
 * Data Source Type Enum
 *
 * <p><strong>CQRS Pattern - Command Query Responsibility Segregation</strong></p>
 *
 * <p><strong>Usage:</strong></p>
 * <ul>
 *   <li>MASTER - Write operations (CREATE, UPDATE, DELETE)</li>
 *   <li>REPLICA - Read operations (SELECT queries)</li>
 * </ul>
 *
 * <p><strong>Benefits:</strong></p>
 * <ul>
 *   <li>Performance: 90% reads go to REPLICA (faster, no locks)</li>
 *   <li>Scalability: Multiple REPLICA servers possible</li>
 *   <li>High Availability: READs work even if MASTER down</li>
 * </ul>
 *
 * @since 1.0.0
 */
public enum DataSourceType {
    /**
     * Master Database - Write Operations
     *
     * <p>Used for:</p>
     * <ul>
     *   <li>CREATE operations (INSERT)</li>
     *   <li>UPDATE operations</li>
     *   <li>DELETE operations</li>
     * </ul>
     */
    MASTER,

    /**
     * Replica Database - Read Operations
     *
     * <p>Used for:</p>
     * <ul>
     *   <li>SELECT queries</li>
     *   <li>Read-only operations</li>
     *   <li>Reports and analytics</li>
     * </ul>
     */
    REPLICA
}
