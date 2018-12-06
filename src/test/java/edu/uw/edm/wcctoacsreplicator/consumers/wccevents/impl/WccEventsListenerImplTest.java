package edu.uw.edm.wcctoacsreplicator.consumers.wccevents.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import java.io.IOException;

import edu.uw.edm.wcctoacsreplicator.clients.contentapi.ContentApisClient;
import edu.uw.edm.wcctoacsreplicator.clients.contentapi.exception.NoMappingForIdException;
import edu.uw.edm.wcctoacsreplicator.consumers.wccevents.model.Document;
import edu.uw.edm.wcctoacsreplicator.consumers.wccevents.model.DocumentIndexingMessage;
import edu.uw.edm.wcctoacsreplicator.properties.ReplicatorProperties;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Maxime Deravet Date: 10/9/18
 */
@RunWith(MockitoJUnitRunner.class)
public class WccEventsListenerImplTest {

    private ReplicatorProperties replicatorProperties;

    @Mock
    private ContentApisClient contentApisClient;

    private WccEventsListenerImpl wccEventsListener;

    @Before
    public void setUp() throws Exception {

        replicatorProperties = new ReplicatorProperties();

        wccEventsListener = new WccEventsListenerImpl(contentApisClient, replicatorProperties);
    }


    @Test(expected = AmqpRejectAndDontRequeueException.class)
    public void whenLimitedByIdThenSyncIsDisabledTest() throws IOException, NoMappingForIdException {
        Document document = new Document();
        document.setId("1234");
        document.setLabel("doc 1234");
        document.getMetadata().put("ProfileId", "a-disabled-profile");

        replicatorProperties.setLimitedToIds(true);
        replicatorProperties.getIdsToProcess().add("324");

        DocumentIndexingMessage documentIndexingMessage = new DocumentIndexingMessage(document, DocumentIndexingMessage.IndexingType.update);

        wccEventsListener.execute(documentIndexingMessage);

        verify(contentApisClient, times(0)).update(eq("1234"), eq(document));

    }

    @Test(expected = AmqpRejectAndDontRequeueException.class)
    public void whenLimitedByProfileThenSyncIsDisabledTest() throws IOException, NoMappingForIdException {

        Document document = new Document();
        document.setId("1234");
        document.setLabel("doc 1234");
        document.getMetadata().put("ProfileId", "a-disabled-profile");

        replicatorProperties.setLimitedToProfiles(true);
        replicatorProperties.getProfilesToProcess().add("non-disabled-profile");

        DocumentIndexingMessage documentIndexingMessage = new DocumentIndexingMessage(document, DocumentIndexingMessage.IndexingType.update);

        wccEventsListener.execute(documentIndexingMessage);

        verify(contentApisClient, times(0)).update(eq("1234"), eq(document));
    }

    @Test
    public void whenUpdateEventThenCallUpdateTest() throws IOException, NoMappingForIdException {

        Document document = new Document();
        document.setId("1234");
        document.setLabel("doc 1234");

        DocumentIndexingMessage documentIndexingMessage = new DocumentIndexingMessage(document, DocumentIndexingMessage.IndexingType.update);

        wccEventsListener.execute(documentIndexingMessage);

        verify(contentApisClient, times(1)).update(eq("1234"), eq(document));
    }

    @Test
    public void whenCreateEventThenCallCreateTest() throws IOException, NoMappingForIdException {

        Document document = new Document();
        document.setId("1234");
        document.setLabel("doc 1234");

        DocumentIndexingMessage documentIndexingMessage = new DocumentIndexingMessage(document, DocumentIndexingMessage.IndexingType.create);

        wccEventsListener.execute(documentIndexingMessage);

        verify(contentApisClient, times(1)).createNewOrNewRevision(eq("1234"), eq(document));
    }

    @Test
    public void whenDeleteEventThenCallDeleteTest() throws IOException, NoMappingForIdException {

        Document document = new Document();
        document.setId("1234");

        DocumentIndexingMessage documentIndexingMessage = new DocumentIndexingMessage(document, DocumentIndexingMessage.IndexingType.delete);

        wccEventsListener.execute(documentIndexingMessage);

        verify(contentApisClient, times(1)).delete(eq(document));
    }


}