package edu.uw.edm.wcctoacsreplicator.consumer.acsevents.services;

import org.junit.Test;
import org.springframework.util.MimeType;

import java.io.IOException;

import edu.uw.edm.wcctoacsreplicator.consumer.acsevents.model.ACSEventSnsMessage;
import edu.uw.edm.wcctoacsreplicator.consumer.acsevents.model.DocumentChangedEvent;
import edu.uw.edm.wcctoacsreplicator.consumer.acsevents.model.DocumentChangedType;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;


/**
 * @author Maxime Deravet Date: 10/10/18
 */
public class SnsToACSEventConverterTest {

    private static String TEST_MESSAGE = "{\n" +
            "  \"Type\" : \"Notification\",\n" +
            "  \"MessageId\" : \"1234\",\n" +
            "  \"TopicArn\" : \"arn:aws:sns:dev:2134124:sns-queue-name\",\n" +
            "  \"Message\" : \"{\\\"type\\\":\\\"create\\\",\\\"documentId\\\":\\\"1234\\\",\\\"wccId\\\":\\\"789\\\",\\\"profile\\\":\\\"my-profile\\\",\\\"lastModifiedDate\\\":1539208112455}\",\n" +
            "  \"Timestamp\" : \"2018-10-10T21:48:33.368Z\",\n" +
            "  \"SignatureVersion\" : \"1\",\n" +
            "  \"Signature\" : \"Fmgu0HPXU6gzYHgGYfYmSePrhFC1x+wtOuJi/Mp1ltWT1C4PvUrRzoRyJr1kUEcEIZfdA6i5pKN/sO6NeoILTBi8R66zroxefPawn93sIXrSwti5MikAYyx0SiT3rSW6OIFEC6Csm6bUc/s1AV+s6+FObMHJsHqyGferfD3mj33Y0Fndm4+mIkPOm8GuTctH//WNDx72Zmco9g6uPIws0n9DTBh/wkLD3ld1v50CTbeNpAOQudQkJTZTy8bWzj4IKW4/iG2KKY/Hd4rNAW3V+XyMCzaUiEt8XI3kfTWbPbvOxibi3dmriakzR3NlENvB1PJIQx7ZXPjBOt1bLq9h3Q==\",\n" +
            "  \"SigningCertURL\" : \"https://sns.dev.amazonaws.com/SimpleNotificationService-ac565b8b1a6c5d002d285f9598aa1d9b.pem\",\n" +
            "  \"UnsubscribeURL\" : \"https://sns.dev.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:dev:2134124:sns-queue-name:1234\",\n" +
            "  \"MessageAttributes\" : {\n" +
            "    \"event-type\" : {\"Type\":\"String\",\"Value\":\"create\"},\n" +
            "    \"profile\" : {\"Type\":\"String\",\"Value\":\"my-profile\"},\n" +
            "    \"contentType\" : {\"Type\":\"String\",\"Value\":\"application/json\"}\n" +
            "  }\n" +
            "}";

    private SnsToACSEventConverter snsToACSEventConverter = new SnsToACSEventConverter();

    @Test
    public void whenAcsEventMessageThenConvertedTest() throws IOException {

        ACSEventSnsMessage acsEventSnsMessage = snsToACSEventConverter.parseMessage(TEST_MESSAGE);


        DocumentChangedEvent documentChangedEvent = acsEventSnsMessage.getPayload();
        assertNotNull(documentChangedEvent);
        assertThat(documentChangedEvent.getDocumentId(), is(equalTo("1234")));
        assertThat(documentChangedEvent.getWccId(), is(equalTo("789")));
        assertThat(documentChangedEvent.getProfile(), is(equalTo("my-profile")));
        assertThat(documentChangedEvent.getLastModifiedDate(), is(equalTo(1539208112455L)));
        assertThat(documentChangedEvent.getType(), is(equalTo(DocumentChangedType.create)));

        assertThat(acsEventSnsMessage.getHeaders().get("event-type"), is(equalTo("create")));
        assertThat(acsEventSnsMessage.getHeaders().get("profile"), is(equalTo("my-profile")));
        assertThat(acsEventSnsMessage.getHeaders().get("contentType"), is(equalTo(MimeType.valueOf("application/json"))));
    }
}