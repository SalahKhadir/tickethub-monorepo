package com.tickethub.service;

import com.tickethub.model.Ticket;
import com.tickethub.model.TicketStatus;
import com.tickethub.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlaMonitoringService {

    /**
     * Javadoc.
     */
    private static final long SLA_CHECK_INTERVAL_MS = 60_000L;
    /**
     * Javadoc.
     */
    private static final long SLA_WARNING_MINUTES = 15L;

    /**
     * Repository for ticket operations.
     */
    private final TicketRepository ticketRepository;

    /**
     * Monitors tickets for SLA breaches and logs warnings.
     */
    @Scheduled(fixedRate = SLA_CHECK_INTERVAL_MS)
    @Transactional(readOnly = true)
    public void monitorSla() {
        log.info("Starting SLA monitoring check...");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusMinutes(SLA_WARNING_MINUTES);
        List<TicketStatus> excludedStatuses = List.of(TicketStatus.RESOLVED,
            TicketStatus.CLOSED);

        List<Ticket> nearingSlaTickets =
                ticketRepository.findTicketsNearingSla(
                        now,
                        threshold,
                        excludedStatuses);

        if (!nearingSlaTickets.isEmpty()) {
            for (Ticket ticket : nearingSlaTickets) {
                log.warn(
                        "CRITICAL SLA WARNING: Ticket ID {} is nearing its "
                                + "SLA deadline at {}",
                        ticket.getId(),
                        ticket.getSlaDeadline());
            }
        } else {
            log.info("No tickets nearing SLA deadline.");
        }
    }
}
