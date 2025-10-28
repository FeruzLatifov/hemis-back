package uz.hemis.common.datasource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Write-Only Database Annotation
 *
 * <p><strong>CQRS Pattern - WRITE Operations</strong></p>
 *
 * <p>Methods annotated with @WriteOnly will use MASTER database</p>
 *
 * <p><strong>Usage:</strong></p>
 * <pre>
 * {@literal @}WriteOnly
 * {@literal @}Transactional
 * public Map&lt;String, Object&gt; update(Map&lt;String, Object&gt; data) {
 *     // This will write to MASTER database
 *     Student student = studentRepository.findByPinfl(pinfl);
 *     student.setFirstName(data.get("first_name"));
 *     return studentRepository.save(student);
 * }
 * </pre>
 *
 * <p><strong>Benefits:</strong></p>
 * <ul>
 *   <li>Consistent writes to single MASTER</li>
 *   <li>Proper replication to REPLICA servers</li>
 *   <li>Transaction safety</li>
 * </ul>
 *
 * <p><strong>Use For:</strong></p>
 * <ul>
 *   <li>INSERT operations (CREATE)</li>
 *   <li>UPDATE operations</li>
 *   <li>DELETE operations</li>
 *   <li>POST/PUT/DELETE endpoints</li>
 * </ul>
 *
 * @since 1.0.0
 * @see ReadOnly
 * @see DataSourceType#MASTER
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface WriteOnly {
}
