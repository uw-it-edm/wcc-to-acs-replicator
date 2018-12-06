package edu.uw.edm.wcctoacsreplicator.consumer.acsevents;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Date;

import edu.uw.edm.wcctoacsreplicator.consumer.acsevents.model.ACSEventSnsMessage;
import edu.uw.edm.wcctoacsreplicator.consumer.acsevents.model.DocumentChangedEvent;
import edu.uw.edm.wcctoacsreplicator.consumer.acsevents.model.DocumentChangedType;
import edu.uw.edm.wcctoacsreplicator.consumer.acsevents.services.SnsToACSEventConverter;
import edu.uw.edm.wcctoacsreplicator.wccmapping.WCCToACSMappingService;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Maxime Deravet Date: 10/10/18
 */
@RunWith(MockitoJUnitRunner.class)
public class ACSEventsListenerTest {

    @Mock
    private SnsToACSEventConverter snsToACSEventConverter;

    @Mock
    private WCCToACSMappingService wccToACSMappingService;


    private ACSEventsListener acsEventsListener;

    @Before
    public void setUp() throws Exception {
        acsEventsListener = new ACSEventsListener(snsToACSEventConverter, wccToACSMappingService);
    }

    @Test
    public void whenNoWCCIdThenNOOPTest() throws IOException {

        ACSEventSnsMessage acsEventSnsMessage = new ACSEventSnsMessage();

        acsEventSnsMessage.setPayload(new DocumentChangedEvent(DocumentChangedType.create, "123", null, "profile", new Date().getTime()));

        when(snsToACSEventConverter.parseMessage(anyString())).thenReturn(acsEventSnsMessage);

        acsEventsListener.read("mock");

        verify(snsToACSEventConverter, times(1)).parseMessage(eq("mock"));
        verify(wccToACSMappingService, times(0)).createEntry(anyString(), anyString(), anyString());
    }

    @Test
    public void whenEmptyWCCIdThenNOOPTest() throws IOException {

        ACSEventSnsMessage acsEventSnsMessage = new ACSEventSnsMessage();

        acsEventSnsMessage.setPayload(new DocumentChangedEvent(DocumentChangedType.create, "123", "", "profile", new Date().getTime()));

        when(snsToACSEventConverter.parseMessage(anyString())).thenReturn(acsEventSnsMessage);

        acsEventsListener.read("mock");

        verify(snsToACSEventConverter, times(1)).parseMessage(eq("mock"));
        verify(wccToACSMappingService, times(0)).createEntry(anyString(), anyString(), anyString());
    }

    @Test
    public void whenMessageValidThenCreateEntryTest() throws IOException {

        ACSEventSnsMessage acsEventSnsMessage = new ACSEventSnsMessage();

        acsEventSnsMessage.setPayload(new DocumentChangedEvent(DocumentChangedType.create, "123", "234", "profile", new Date().getTime()));

        when(snsToACSEventConverter.parseMessage(anyString())).thenReturn(acsEventSnsMessage);

        acsEventsListener.read("mock");

        verify(snsToACSEventConverter, times(1)).parseMessage(eq("mock"));
        verify(wccToACSMappingService, times(1)).createEntry(eq("234"), eq("123"), eq("profile"));
    }
}