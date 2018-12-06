package edu.uw.edm.wcctoacsreplicator.consumers.wccevents;

import java.io.IOException;

import edu.uw.edm.wcctoacsreplicator.clients.contentapi.exception.NoMappingForIdException;
import edu.uw.edm.wcctoacsreplicator.consumers.wccevents.model.DocumentIndexingMessage;

/**
 * @author Maxime Deravet Date: 10/5/18
 */
public interface WccEventsListener {
    String INTAKE_METHOD_NAME = "execute";

    void execute(DocumentIndexingMessage documentIndexingMessage) throws IOException, NoMappingForIdException;

}
