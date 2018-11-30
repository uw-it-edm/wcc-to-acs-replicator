package edu.uw.edm.wcctoacsreplicator.clients.contentapi;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Optional;

import edu.uw.edm.wcctoacsreplicator.clients.contentapi.exception.NoMappingForIdException;
import edu.uw.edm.wcctoacsreplicator.consumers.wccevents.model.Document;
import edu.uw.edm.wcctoacsreplicator.properties.ReplicatorProperties;
import edu.uw.edm.wcctoacsreplicator.wccmapping.WCCToACSMapping;
import edu.uw.edm.wcctoacsreplicator.wccmapping.WCCToACSMappingService;

import static edu.uw.edm.wcctoacsreplicator.clients.contentapi.ContentApisClient.CONTENT_API2_DELETE_V_3_ITEM;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ContentApisClientTest {
    private WCCToACSMappingService wccToACSMappingService = mock(WCCToACSMappingService.class);
    private RestTemplate restTemplate = mock(RestTemplate.class);
    private ReplicatorProperties replicatorProperties;
    private ContentApisClient contentApisClient;

    @Before
    public void setup() {
        replicatorProperties = getTestReplicatorProperties();
        contentApisClient = new ContentApisClient(wccToACSMappingService, restTemplate, replicatorProperties);
    }
    private ReplicatorProperties getTestReplicatorProperties(){
        final ReplicatorProperties testReplicatorProperties = new ReplicatorProperties();
        final ReplicatorProperties.ContentApi2 contentApi2 = new ReplicatorProperties.ContentApi2();
        contentApi2.setAuthenticationHeader("testHeader");
        contentApi2.setHostAndContext("testHostAndContext");
        testReplicatorProperties.setContentApi2(contentApi2);
        return testReplicatorProperties;
    }

    @Test
    public void delete() throws NoMappingForIdException {
        final String expectedDeleteUrl = replicatorProperties.getContentApi2().getHostAndContext() + CONTENT_API2_DELETE_V_3_ITEM + "testAcsId";

        final Document doc = new Document();
        doc.setId("testId");
        doc.setLabel("testLabel");

        final WCCToACSMapping wccToACSMapping = new WCCToACSMapping();
        wccToACSMapping.setAcsId("testAcsId");

        when(wccToACSMappingService.getMappingForWCCId("testId")).thenReturn(Optional.of(wccToACSMapping));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.DELETE), any(), eq(String.class), any(HashMap.class))).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        contentApisClient.delete(doc);

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate, times(1)).exchange(captor.capture(), eq(HttpMethod.DELETE), any(), eq(String.class), any(HashMap.class));
        assertEquals(expectedDeleteUrl, captor.getValue());
    }

    @Test(expected = NoMappingForIdException.class)
    public void whenWccIdIsNotInMappingTableThrowNoMappingForIdException() throws NoMappingForIdException {
        when(wccToACSMappingService.getMappingForWCCId("testId")).thenReturn(Optional.empty());
        final Document doc = new Document();
        doc.setId("testId");
        doc.setLabel("testLabel");

        contentApisClient.delete(doc);
    }
}