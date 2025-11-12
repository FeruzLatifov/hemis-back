package uz.hemis.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uz.hemis.domain.event.GradeSubmittedEvent;

/**
 * Grade Event Publisher
 *
 * <p>Publishes grade-related domain events</p>
 *
 * @since 2.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GradeEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * Publish grade submitted event
     */
    public void publishGradeSubmitted(GradeSubmittedEvent event) {
        log.info("Publishing GradeSubmittedEvent: gradeId={}, student={}, course={}, grade={}",
            event.getGradeId(), event.getStudentFullName(),
            event.getCourseName(), event.getGradeValue());

        eventPublisher.publishEvent(event);

        log.debug("GradeSubmittedEvent published successfully");
    }
}
