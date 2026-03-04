package com.library.messaging;

import com.library.event.BookUpdateEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.faces.push.Push;
import jakarta.faces.push.PushContext;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class BookWebSocketObserver {

    @Inject
    @Push(channel = "bookChannel")
    private PushContext pushContext;

    public void onBookUpdate(@Observes BookUpdateEvent event) {
        log.info("Pushing update to clients: {}", event.message());
        pushContext.send(event.message());
    }
}
