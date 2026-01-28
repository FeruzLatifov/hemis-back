package uz.hemis.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uz.hemis.domain.event.DiplomaIssuedEvent;

/**
 * Diploma Event Publisher
 *
 * <p>Publishes diploma-related domain events</p>
 *
 * @since 2.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DiplomaEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * Publish diploma issued event
     */
    public void publishDiplomaIssued(DiplomaIssuedEvent event) {
        log.info("Publishing DiplomaIssuedEvent: diplomaId={}, number={}, student={}",
            event.getDiplomaId(), event.getDiplomaNumber(), event.getStudentFullName());

        eventPublisher.publishEvent(event);

        log.debug("DiplomaIssuedEvent published successfully");
    }
}
