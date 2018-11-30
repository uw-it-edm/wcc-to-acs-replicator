package edu.uw.edm.wcctoacsreplicator.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Maxime Deravet Date: 10/4/18
 */
@Component
@ConfigurationProperties(prefix = "uw.replicator")
@Data
@Validated
public class ReplicatorProperties {

    @NotNull
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
        private String deleteActAsUser = "admin";
    }
}
