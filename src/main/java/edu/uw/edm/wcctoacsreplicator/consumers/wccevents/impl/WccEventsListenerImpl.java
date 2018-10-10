package edu.uw.edm.wcctoacsreplicator.consumers.wccevents.impl;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

import edu.uw.edm.wcctoacsreplicator.clients.contentapi.ContentApisClient;
import edu.uw.edm.wcctoacsreplicator.clients.contentapi.exception.NoMappingForIdException;
import edu.uw.edm.wcctoacsreplicator.consumers.wccevents.WccEventsListener;
import edu.uw.edm.wcctoacsreplicator.consumers.wccevents.model.Document;
import edu.uw.edm.wcctoacsreplicator.consumers.wccevents.model.DocumentIndexingMessage;
import edu.uw.edm.wcctoacsreplicator.properties.ReplicatorProperties;
import lombok.extern.slf4j.Slf4j;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Maxime Deravet Date: 10/5/18
 */

@Service
@Slf4j
public class WccEventsListenerImpl implements WccEventsListener {

    private final ContentApisClient contentApisClient;
    private final ReplicatorProperties replicatorProperties;

    @Autowired
    public WccEventsListenerImpl(ContentApisClient contentApisClient, ReplicatorProperties replicatorProperties) {
        this.contentApisClient = contentApisClient;
        this.replicatorProperties = replicatorProperties;
    }

    @Override
    public void execute(DocumentIndexingMessage documentIndexingMessage) throws IOException, NoMappingForIdException {
        //TODO handle prereqs error
        checkNotNull(documentIndexingMessage, "documentIndexingMessage is required");
        checkNotNull(documentIndexingMessage.getDocument(), "document is required");
        checkNotNull(documentIndexingMessage.getDocument().getId(), "documentId is required");

        log.trace("received : {} for {}", documentIndexingMessage.getIndexingType(), documentIndexingMessage.getDocument().getId());

        checkIfIdShouldBeProcessed(documentIndexingMessage);
        checkIfProfileShouldBeProcessed(documentIndexingMessage);

        switch (documentIndexingMessage.getIndexingType()) {
            case create:
                addToACS(documentIndexingMessage.getDocument());
                break;
            case update:
                updateACS(documentIndexingMessage.getDocument());
                break;
            case delete:
                deleteFromACS(documentIndexingMessage.getDocument());
                break;
            default:
                throw new AmqpRejectAndDontRequeueException(new UnsupportedOperationException("This method only allow create/update/delete , received " + documentIndexingMessage.getIndexingType().name()));
        }

    }

    private void deleteFromACS(Document document) {
        checkNotNull(document, "Document cannot be null");
        checkNotNull(document.getId(), "Document cannot be null");

        if (replicatorProperties.disableDelete()) {
            throw new AmqpRejectAndDontRequeueException("Not deleting docId " + document.getId() + " by app rule");
        }

        contentApisClient.delete(document.getId());
    }

    private void updateACS(Document document) throws IOException, NoMappingForIdException {
        checkPreReqsForCreateUpdate(document);
        try {
            contentApisClient.update(document.getId(), document);
        } catch (IOException | NoMappingForIdException e) {
            log.error("Cannot update new document", e);
            throw e;
        }
    }


    private void addToACS(Document document) throws IOException {
        checkPreReqsForCreateUpdate(document);

        try {
            contentApisClient.createNewOrNewRevision(document.getId(), document);
        } catch (IOException e) {
            log.error("Cannot create new document", e);
            throw e;
        }

    }


    private void checkIfProfileShouldBeProcessed(DocumentIndexingMessage documentIndexingMessage) {
        if (replicatorProperties.isLimitedToProfiles()) {
            String profileId = (String) documentIndexingMessage.getDocument().getMetadata().get(ContentApisClient.CONTENT_API_FIELD_PROFILE_ID);
            if (!replicatorProperties.getProfilesToProcess().contains(profileId)) {
                throw new AmqpRejectAndDontRequeueException("Not Processing profile " + profileId + " by app rule");
            }
        }
    }

    private void checkIfIdShouldBeProcessed(DocumentIndexingMessage documentIndexingMessage) {
        if (replicatorProperties.isLimitedToIds()) {
            String docId = documentIndexingMessage.getDocument().getId();
            if (!replicatorProperties.getIdsToProcess().contains(docId)) {
                throw new AmqpRejectAndDontRequeueException("Not Processing docId " + docId + " by app rule");
            }
        }
    }

    private void checkPreReqsForCreateUpdate(Document document) {
        checkNotNull(document, "Document cannot be null");
        checkNotNull(document.getId(), "Document id cannot be null");
        checkNotNull(document.getLabel(), "Document label cannot be null");
        checkNotNull(document.getMetadata(), "Document metadata cannot be null");
    }
}
