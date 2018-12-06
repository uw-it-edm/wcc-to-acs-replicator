package edu.uw.edm.wcctoacsreplicator.clients.contentapi.exception;

/**
 * @author Maxime Deravet Date: 10/9/18
 */
public class NoMappingForIdException extends Throwable {
    public NoMappingForIdException(String contentId) {
        super("No mapping for wcc id " + contentId);
    }
}
