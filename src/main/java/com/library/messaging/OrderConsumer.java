package com.library.messaging;

import com.library.dto.BookOrderDTO;
import com.library.service.BookService;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.inject.Inject;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.ObjectMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "java:/jms/queue/OrderQueue"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue")
})
public class OrderConsumer implements MessageListener {

    @Inject
    private BookService bookService;

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof ObjectMessage) {
                ObjectMessage objectMessage = (ObjectMessage) message;
                BookOrderDTO order = (BookOrderDTO) objectMessage.getObject();
                log.info("Received order for book ID: {}", order.bookId());

                // Process the purchase
                bookService.processPurchase(order.bookId());

                log.info("Order processed successfully");
            } else {
                log.warn("Received unknown message type: {}", message.getClass().getName());
            }
        } catch (JMSException e) {
            log.error("Error processing JMS message", e);
            // In a real app, you might want to throw RuntimeException to trigger redelivery
        } catch (Exception e) {
            log.error("Error processing order", e);
        }
    }
}
