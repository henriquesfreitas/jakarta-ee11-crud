package com.library.messaging;

import com.library.dto.BookOrderDTO;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.jms.JMSContext;
import jakarta.jms.Queue;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class OrderProducer {

    @Inject
    private JMSContext context;

    @Resource(mappedName = "java:/jms/queue/OrderQueue")
    private Queue orderQueue;

    public void sendOrder(BookOrderDTO order) {
        log.info("Sending order for book ID: {} to queue", order.bookId());
        try {
            context.createProducer().send(orderQueue, order);
            log.info("Order sent successfully");
        } catch (Exception e) {
            log.error("Failed to send order to queue", e);
            throw new RuntimeException("Failed to send order", e);
        }
    }
}
