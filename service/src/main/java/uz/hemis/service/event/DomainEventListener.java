package uz.hemis.service.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uz.hemis.domain.event.*;

/**
 * Domain Event Listener - Example Implementation
 *
 * <p>Demonstrates how to listen to and react to domain events</p>
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Async processing (@Async)</li>
 *   <li>Decoupled from event publishers</li>
 *   <li>Can have multiple listeners for same event</li>
 *   <li>Easy to test and maintain</li>
 * </ul>
 *
 * <p><strong>Real Implementation Tasks:</strong></p>
 * <ul>
 *   <li>Send notifications (email, SMS, push)</li>
 *   <li>Update statistics and reports</li>
 *   <li>Integrate with external systems</li>
 *   <li>Audit logging</li>
 *   <li>Trigger workflows</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Component
@Slf4j
public class DomainEventListener {

    /**
     * Handle Student Enrolled Event
     */
    @EventListener
    @Async
    public void handleStudentEnrolled(StudentEnrolledEvent event) {
        log.info("📧 [Event Listener] Student enrolled: {} ({})",
            event.getFullName(), event.getStudentCode());

        // TODO: Implement actual business logic
        // - Send welcome email
        // - Create user account
        // - Notify department head
        // - Update statistics
        
        log.debug("StudentEnrolledEvent processed: studentId={}", event.getStudentId());
    }

    /**
     * Handle Diploma Issued Event
     */
    @EventListener
    @Async
    public void handleDiplomaIssued(DiplomaIssuedEvent event) {
        log.info("🎓 [Event Listener] Diploma issued: {} - {}",
            event.getDiplomaNumber(), event.getStudentFullName());

        // TODO: Implement actual business logic
        // - Report to Ministry of Education
        // - Send notification to student
        // - Queue diploma for printing
        // - Update diploma registry
        
        log.debug("DiplomaIssuedEvent processed: diplomaId={}", event.getDiplomaId());
    }

    /**
     * Handle Contract Signed Event
     */
    @EventListener
    @Async
    public void handleContractSigned(ContractSignedEvent event) {
        log.info("📄 [Event Listener] Contract signed: {} - {} - {}",
            event.getContractNumber(), event.getStudentFullName(),
            event.getTotalAmount() + " " + event.getCurrency());

        // TODO: Implement actual business logic
        // - Generate invoice
        // - Send contract copy to student
        // - Notify finance department
        // - Record in accounting system
        
        log.debug("ContractSignedEvent processed: contractId={}", event.getContractId());
    }

    /**
     * Handle Grade Submitted Event
     */
    @EventListener
    @Async
    public void handleGradeSubmitted(GradeSubmittedEvent event) {
        log.info("📝 [Event Listener] Grade submitted: {} - {} - Grade: {}",
            event.getStudentFullName(), event.getCourseName(), event.getGradeValue());

        // TODO: Implement actual business logic
        // - Notify student about new grade
        // - Update transcript
        // - Recalculate GPA
        // - Check academic warnings
        
        log.debug("GradeSubmittedEvent processed: gradeId={}", event.getGradeId());
    }
}
