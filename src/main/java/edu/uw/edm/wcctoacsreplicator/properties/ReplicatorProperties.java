package edu.uw.edm.wcctoacsreplicator.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * @author Maxime Deravet Date: 10/4/18
 */
@Component
@ConfigurationProperties(prefix = "uw.replicator")
@Data
public class ReplicatorProperties {

    @NonNull
    private String dynamoTableNamePrefix;

    private boolean wccEventListenerUpdatesMapping = false;

    private String dynamoDBEndpointOverride;
    private boolean dynamoDBCreateTable = false;

    /**
     * Useful for tests so that you can listen only specific ids
     */
    private boolean limitedToIds = false;
    private List<String> idsToProcess = new ArrayList<>();

    private boolean limitedToProfiles = false;
    private List<String> profilesToProcess = new ArrayList<>();


    @Accessors(fluent = true)
    private boolean disableDelete = false;

    private ContentApi contentApi;
    private ContentApi2 contentApi2;

    @Data
    public static class ContentApi {
        private String authenticationHeader;
        private String hostAndContext;

    }

    @Data
    public static class ContentApi2 {
        private String authenticationHeader;
        private String hostAndContext;
    }
}
