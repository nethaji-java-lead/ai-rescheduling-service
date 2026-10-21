package com.example.carebridge.agent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
@Component
public class RescheduleEventListener {
 private final RescheduleWorker worker;
 public RescheduleEventListener(RescheduleWorker worker) { this.worker = worker; }
 @Async
 @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
 public void onRescheduleEvent(RescheduleEvent event) { worker.process(event); }
}
