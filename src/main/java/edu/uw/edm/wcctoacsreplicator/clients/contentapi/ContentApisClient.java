package edu.uw.edm.wcctoacsreplicator.clients.contentapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Optional;

import edu.uw.edm.wcctoacsreplicator.clients.contentapi.exception.NoMappingForIdException;
import edu.uw.edm.wcctoacsreplicator.consumers.wccevents.model.Document;
import edu.uw.edm.wcctoacsreplicator.properties.ReplicatorProperties;
import edu.uw.edm.wcctoacsreplicator.wccmapping.WCCToACSMapping;
import edu.uw.edm.wcctoacsreplicator.wccmapping.WCCToACSMappingService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Maxime Deravet Date: 10/8/18
 */
@Service
@Slf4j
public class ContentApisClient {

    public static final String CONTENT_API_FIELD_PROFILE_ID = "ProfileId";
    public static final String CONTENT_API_FIELD_ORIGINAL_FILE_NAME = "OriginalFileName";
    private static final String CONTENT_API2_POST_V_3_ITEM = "/content/v3/item";
    private static final String CONTENT_API_GET_V_3_FILE = "/securid/v3/file/";
    private static final String CONTENT_API_GET_PRIMARY_RENDITION_PARAMS = "?rendition=Primary&forcePDF=false&useChannel=true";
    public static final String CONTENT_API_FIELD_LAST_MODIFIER = "LastModifier";


    private final WCCToACSMappingService wccToACSMappingService;
    private final RestTemplate restTemplate;

    private final ReplicatorProperties replicatorProperties;


    private final ObjectMapper objectMapper = new ObjectMapper();


    @Autowired
    public ContentApisClient(WCCToACSMappingService wccToACSMappingService, RestTemplate restTemplate, ReplicatorProperties replicatorProperties) {
        this.wccToACSMappingService = wccToACSMappingService;
        this.restTemplate = restTemplate;
        this.replicatorProperties = replicatorProperties;
    }

    public void update(String contentId, Document document) throws RestClientException, IOException, NoMappingForIdException {

        log.trace("received create for {}", contentId);

        Optional<WCCToACSMapping> mappingForWCCId = wccToACSMappingService.getMappingForWCCId(contentId);

        if (!mappingForWCCId.isPresent()) {
            log.error("Document {} should be in wcc ", document.getId());
            throw new NoMappingForIdException(contentId);
        }

        String lastModifier = getLastModifier(document);

        //TODO this method as a side effect, we should rename
        String updateURL = getContentApi2CreateOrUpdateURL(document, mappingForWCCId);

        MultiValueMap<String, Object> multipartRequest = new LinkedMultiValueMap<>();
        addDocumentToMultipartRequest(document, multipartRequest);


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        headers.add(replicatorProperties.getContentApi().getAuthenticationHeader(), lastModifier);
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(multipartRequest, headers);


        createOrUpdateDocumentInContentApi2(updateURL, request);


    }

    private String getLastModifier(Document document) {
        return (String) document.getMetadata().get(CONTENT_API_FIELD_LAST_MODIFIER);
    }

    public void createNewOrNewRevision(String contentId, Document document) throws RestClientException, IOException {

        log.trace("received create for {}", contentId);

        //This will allows us to know if this is a new document or a new revision
        Optional<WCCToACSMapping> mappingForWCCId = wccToACSMappingService.getMappingForWCCId(contentId);

        String lastModifier = getLastModifier(document);

        InputStreamResource inputStreamResource = getContentApiFileStream(contentId, document, lastModifier);


        String serverUrl = getContentApi2CreateOrUpdateURL(document, mappingForWCCId);


        MultiValueMap<String, Object> multipartRequest = new LinkedMultiValueMap<>();

        addDocumentToMultipartRequest(document, multipartRequest);
        addFileToMultipartRequest(inputStreamResource, multipartRequest);


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        headers.add(replicatorProperties.getContentApi().getAuthenticationHeader(), lastModifier);
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(multipartRequest, headers);


        ResponseEntity<Document> response = createOrUpdateDocumentInContentApi2(serverUrl, request);

        String acsId = response.getBody().getId();


        /**
         * //TODO CAB-3256 This should be removed once ACS starts sending the create events to SNS
         */
        if (replicatorProperties.isWccEventListenerUpdatesMapping() && !mappingForWCCId.isPresent()) {
            wccToACSMappingService.createEntry(contentId, acsId, (String) document.getMetadata().get(CONTENT_API_FIELD_PROFILE_ID));
        }


    }

    private ResponseEntity<Document> createOrUpdateDocumentInContentApi2(String serverUrl, HttpEntity<MultiValueMap<String, Object>> request) throws RestClientException {
        ResponseEntity<Document> response = null;
        try {
            response = restTemplate.exchange(serverUrl, HttpMethod.POST, request, Document.class, new HashMap<>());


            log.trace("got response {}", response.getStatusCode());
            log.trace("{}" , response.getBody());


        } catch (HttpServerErrorException e) {
            log.error(e.getResponseBodyAsString());
            throw e;
        } catch (RestClientException e) {
            log.error(e.getMessage());
            throw e;
        }
        return response;
    }

    private String getContentApi2CreateOrUpdateURL(Document document, Optional<WCCToACSMapping> mappingForWCCId) {
        String serverUrl = replicatorProperties.getContentApi2().getHostAndContext() + CONTENT_API2_POST_V_3_ITEM;

        if (mappingForWCCId.isPresent()) {
            // Create is actually a create new revision
            String acsId = mappingForWCCId.get().getAcsId();
            document.setId(acsId);
            serverUrl = serverUrl + "/" + acsId;
        }
        return serverUrl;
    }

    private InputStreamResource getContentApiFileStream(String contentId, Document document, String lastModifier) throws IOException {
        URL url = new URL(replicatorProperties.getContentApi().getHostAndContext() + CONTENT_API_GET_V_3_FILE + contentId + CONTENT_API_GET_PRIMARY_RENDITION_PARAMS);

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        urlConnection.setRequestMethod(HttpMethod.GET.name());
        urlConnection.setRequestProperty(replicatorProperties.getContentApi2().getAuthenticationHeader(), lastModifier);

        //TODO Not sure OriginalFileName is always available
        return new MultipartInputStreamFileResource(urlConnection.getInputStream(), (String) document.getMetadata().get(CONTENT_API_FIELD_ORIGINAL_FILE_NAME));
    }

    private void addFileToMultipartRequest(InputStreamResource inputStreamResource, MultiValueMap<String, Object> multipartRequest) {
        HttpHeaders fileHeader = new HttpHeaders();
        HttpEntity fileEntity = new HttpEntity<>(inputStreamResource, fileHeader);

        multipartRequest.add("attachment", fileEntity);
    }

    private void addDocumentToMultipartRequest(Document document, MultiValueMap<String, Object> multipartRequest) throws JsonProcessingException {
        HttpHeaders jsonHeader = new HttpHeaders();
        jsonHeader.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity jsonHttpEntity = new HttpEntity<>(objectMapper.writeValueAsString(document), jsonHeader);

        multipartRequest.add("document", jsonHttpEntity);
    }


    public void delete(String id) {
        //TODO
    }
}
