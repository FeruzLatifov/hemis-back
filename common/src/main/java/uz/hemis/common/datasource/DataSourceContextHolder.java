package uz.hemis.common.datasource;

/**
 * Data Source Context Holder - ThreadLocal Storage
 *
 * <p><strong>CQRS Pattern Implementation</strong></p>
 *
 * <p>Stores current thread's data source type (MASTER or REPLICA)</p>
 *
 * <p><strong>Usage:</strong></p>
 * <pre>
 * // Set data source for current thread
 * DataSourceContextHolder.setDataSourceType(DataSourceType.REPLICA);
 *
 * // Get current data source
 * DataSourceType type = DataSourceContextHolder.getDataSourceType();
 *
 * // Clear after use (important!)
 * DataSourceContextHolder.clearDataSourceType();
 * </pre>
 *
 * <p><strong>Thread Safety:</strong></p>
 * <ul>
 *   <li>Uses ThreadLocal - each thread has its own value</li>
 *   <li>No synchronization needed</li>
 *   <li>Must clear after use to prevent memory leaks</li>
 * </ul>
 *
 * @since 1.0.0
 */
public class DataSourceContextHolder {

    private static final ThreadLocal<DataSourceType> contextHolder = new ThreadLocal<>();

    /**
     * Set data source type for current thread
     *
     * @param type MASTER or REPLICA
     */
    public static void setDataSourceType(DataSourceType type) {
        if (type == null) {
            throw new IllegalArgumentException("DataSourceType cannot be null");
        }
        contextHolder.set(type);
    }

    /**
     * Get data source type for current thread
     *
     * @return current data source type, or MASTER if not set
     */
    public static DataSourceType getDataSourceType() {
        DataSourceType type = contextHolder.get();
        // Default to MASTER if not set
        return type != null ? type : DataSourceType.MASTER;
    }

    /**
     * Clear data source type for current thread
     *
     * <p><strong>IMPORTANT:</strong> Always call this in finally block
     * or @After advice to prevent memory leaks!</p>
     */
    public static void clearDataSourceType() {
        contextHolder.remove();
    }
}
