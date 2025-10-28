package uz.hemis.common.datasource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Read-Only Database Annotation
 *
 * <p><strong>CQRS Pattern - READ Operations</strong></p>
 *
 * <p>Methods annotated with @ReadOnly will use REPLICA database</p>
 *
 * <p><strong>Usage:</strong></p>
 * <pre>
 * {@literal @}ReadOnly
 * {@literal @}Transactional(readOnly = true)
 * public Map&lt;String, Object&gt; verify(String pinfl) {
 *     // This will query REPLICA database
 *     return studentRepository.findByPinfl(pinfl);
 * }
 * </pre>
 *
 * <p><strong>Benefits:</strong></p>
 * <ul>
 *   <li>Faster queries (no write locks on REPLICA)</li>
 *   <li>Load balancing (90% of queries go to REPLICA)</li>
 *   <li>High availability (READs work if MASTER down)</li>
 * </ul>
 *
 * <p><strong>Use For:</strong></p>
 * <ul>
 *   <li>SELECT queries</li>
 *   <li>GET endpoints</li>
 *   <li>List/search operations</li>
 *   <li>Reports</li>
 * </ul>
 *
 * @since 1.0.0
 * @see WriteOnly
 * @see DataSourceType#REPLICA
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ReadOnly {
}
