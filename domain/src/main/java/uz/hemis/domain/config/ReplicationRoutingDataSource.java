package uz.hemis.domain.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Routing DataSource that switches between Master and Replica
 *
 * Routing Logic:
 * - @Transactional(readOnly = true) → REPLICA database
 * - @Transactional (default) → MASTER database
 * - No transaction → MASTER database (safe default)
 *
 * How it works:
 * 1. Spring opens transaction with readOnly flag
 * 2. TransactionSynchronizationManager stores this flag in ThreadLocal
 * 3. determineCurrentLookupKey() is called to select datasource
 * 4. Returns REPLICA if readOnly=true, else MASTER
 *
 * CRITICAL:
 * - Must use LazyConnectionDataSourceProxy wrapper
 * - Routing decision happens AFTER transaction starts
 * - Thread-safe (uses ThreadLocal)
 */
@Slf4j
public class ReplicationRoutingDataSource extends AbstractRoutingDataSource {

    /**
     * Determine which datasource to use based on transaction readOnly flag
     *
     * @return DataSourceType.MASTER for writes, DataSourceType.REPLICA for reads
     */
    @Override
    protected Object determineCurrentLookupKey() {
        boolean isReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();

        if (isReadOnly) {
            log.trace("Routing to REPLICA database (read-only transaction)");
            return DataSourceType.REPLICA;
        } else {
            log.trace("Routing to MASTER database (write transaction or no transaction)");
            return DataSourceType.MASTER;
        }
    }
}
