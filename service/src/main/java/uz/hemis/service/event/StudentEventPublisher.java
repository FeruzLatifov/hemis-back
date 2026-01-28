package uz.hemis.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uz.hemis.domain.event.StudentEnrolledEvent;

/**
 * Student Event Publisher
 *
 * <p>Publishes student-related domain events</p>
 *
 * @since 2.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StudentEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * Publish student enrolled event
     */
    public void publishStudentEnrolled(StudentEnrolledEvent event) {
        log.info("Publishing StudentEnrolledEvent: studentId={}, code={}",
            event.getStudentId(), event.getStudentCode());

        eventPublisher.publishEvent(event);

        log.debug("StudentEnrolledEvent published successfully");
    }
}
