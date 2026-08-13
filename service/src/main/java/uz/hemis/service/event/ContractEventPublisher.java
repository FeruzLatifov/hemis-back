package uz.hemis.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uz.hemis.domain.event.ContractSignedEvent;

/**
 * Contract Event Publisher
 *
 * <p>Publishes contract-related domain events</p>
 *
 * @since 2.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ContractEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * Publish contract signed event
     */
    public void publishContractSigned(ContractSignedEvent event) {
        // PII-safe: student full name intentionally omitted from the log line.
        log.info("Publishing ContractSignedEvent: contractId={}, number={}, amount={}",
            event.getContractId(), event.getContractNumber(), event.getTotalAmount());

        eventPublisher.publishEvent(event);

        log.debug("ContractSignedEvent published successfully");
    }
}
