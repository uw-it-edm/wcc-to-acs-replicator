package edu.uw.edm.wcctoacsreplicator.consumer.acsevents;

import com.google.common.base.Strings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

import edu.uw.edm.wcctoacsreplicator.consumer.acsevents.model.ACSEventSnsMessage;
import edu.uw.edm.wcctoacsreplicator.consumer.acsevents.model.DocumentChangedEvent;
import edu.uw.edm.wcctoacsreplicator.consumer.acsevents.services.SnsToACSEventConverter;
import edu.uw.edm.wcctoacsreplicator.wccmapping.WCCToACSMappingService;
import lombok.extern.slf4j.Slf4j;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Maxime Deravet Date: 10/5/18
 */
@Slf4j
@Service
public class ACSEventsListener {

    private SnsToACSEventConverter snsToACSEventConverter;
    private WCCToACSMappingService wccToACSMappingService;


    @Autowired
    public ACSEventsListener(SnsToACSEventConverter snsToACSEventConverter, WCCToACSMappingService wccToACSMappingService) {
        this.snsToACSEventConverter = snsToACSEventConverter;
        this.wccToACSMappingService = wccToACSMappingService;
    }


    @SqsListener(value = "${uw.replicator.sqs.eventQueueName}", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void read(String sqsMessage) throws IOException {
        checkNotNull(sqsMessage, "Message shouldn't be null");

        ACSEventSnsMessage aCSEventSnsMessage = snsToACSEventConverter.parseMessage(sqsMessage);

        checkNotNull(aCSEventSnsMessage, "aCSEventSnsMessage shouldn't be null");
        checkNotNull(aCSEventSnsMessage.getPayload(), "payload shouldn't be null");

        log.trace("received create event for {}", aCSEventSnsMessage.getPayload().getDocumentId());
        DocumentChangedEvent documentChangedEvent = aCSEventSnsMessage.getPayload();
        if (Strings.isNullOrEmpty(documentChangedEvent.getWccId())) {
            //NOOP
        }else{
            wccToACSMappingService.createEntry(documentChangedEvent.getWccId(), documentChangedEvent.getDocumentId(), documentChangedEvent.getProfile());
        }

    }

}
