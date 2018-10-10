package edu.uw.edm.wcctoacsreplicator.config;

import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.buffered.AmazonSQSBufferedAsyncClient;

import org.springframework.cloud.aws.core.config.AmazonWebserviceClientFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Maxime Deravet Date: 10/5/18
 */
@Configuration
public class SQSConfiguration {


/*
    @Bean
    public SimpleMessageListenerContainerFactory simpleMessageListenerContainerFactory(AmazonSQSAsync amazonSqs) {
        SimpleMessageListenerContainerFactory factory = new SimpleMessageListenerContainerFactory();
        factory.setAmazonSqs(amazonSqs);
        factory.setAutoStartup(true);
        factory.setMaxNumberOfMessages(10);
        factory.setTaskExecutor(createDefaultTaskExecutor())
        return factory;
    }
*/

}
