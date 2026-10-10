package com.TheEmperorPR.LibraryBackend.common.domain.scheduler;

import com.TheEmperorPR.LibraryBackend.common.domain.service.FineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class OverdueFineScheduler {

    private final FineService fineService;

    /**
     * Executes daily at 00:00:00 (Midnight) to assess active loans, transition overdue records,
     * calculate daily accrued fines, and apply active holds when thresholds are exceeded.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void runMidnightOverdueAudit() {
        log.info("🕛 Starting scheduled midnight overdue loan audit and fine calculation batch job...");
        try {
            int evaluated = fineService.assessOverdueLoansAndApplyFines();
            log.info("✅ Midnight batch job completed successfully. Evaluated and processed {} overdue loans.", evaluated);
        } catch (Exception e) {
            log.error("❌ Error running scheduled midnight overdue loan audit: {}", e.getMessage(), e);
        }
    }
}
