package edu.uw.edm.wcctoacsreplicator.consumer.acsevents.model;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;

import java.util.Map;

import lombok.Data;

/**
 * @author Maxime Deravet Date: 10/5/18
 */
@Data
public class ACSEventSnsMessage implements Message<DocumentChangedEvent> {

    private DocumentChangedEvent payload;
    private MessageHeaders headers;

    public ACSEventSnsMessage() {
    }
}
