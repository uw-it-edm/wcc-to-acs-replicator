package edu.uw.edm.wcctoacsreplicator.config;

import com.google.common.base.Strings;

import org.aopalliance.aop.Advice;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.StatefulRetryOperationsInterceptorFactoryBean;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import edu.uw.edm.wcctoacsreplicator.consumers.wccevents.WccEventsListener;
import edu.uw.edm.wcctoacsreplicator.consumers.wccevents.model.DocumentIndexingMessage;
import edu.uw.edm.wcctoacsreplicator.properties.RabbitMQProperties;


@Configuration
@Profile("!disableRMQ")
public class RabbitMQConfiguration {

    private static final String ROUTING_KEY_ALL = "#";


    private static final String DEAD_LETTER_SUFFIX = "-dead-letter";
    private static final String WCC_DISPATCHER_EXCHANGE = "document-event-dispatcher";

    @Autowired
    private RabbitMQProperties rabbitMQProperties;


    @Bean
    @Qualifier("deadLetter")
    Queue deadLetterQueue() {
        return new Queue(getReplicatorDeadLetterName(), true, false, rabbitMQProperties.isAutoDeleteQueue());
    }

    @Bean
    @Qualifier("deadLetter")
    TopicExchange deadLetterExchange() {
        return new TopicExchange(getReplicatorDeadLetterName(), true, false);
    }


    @Bean
    @Qualifier("deadLetter")
    Binding deadLetterBinding(@Qualifier("deadLetter") Queue queue, @Qualifier("deadLetter") TopicExchange topicExchange) {
        return BindingBuilder.bind(queue).to(topicExchange).with("#");
    }

    @Bean
    @Qualifier("syncQueue")
    Queue indexingQueue() {
        HashMap<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", getReplicatorDeadLetterName());
        return new Queue(rabbitMQProperties.getReplicatorQueueName(), true, false, rabbitMQProperties.isAutoDeleteQueue(), arguments);
    }

    private String getReplicatorDeadLetterName() {
        return rabbitMQProperties.getReplicatorQueueName() + DEAD_LETTER_SUFFIX;
    }

    @Bean
    @Qualifier("wccDispatcherExchange")
    Exchange wccDispatcherExchange() {
        //TODO dead letter exchange
        TopicExchange topicExchange = new TopicExchange(WCC_DISPATCHER_EXCHANGE, true, false);
        topicExchange.setShouldDeclare(false);

        return topicExchange;
    }

    @Bean
    Binding binding(@Qualifier("syncQueue") Queue queue, @Qualifier("wccDispatcherExchange") Exchange wccDispatcherExchange) {
        return BindingBuilder.bind(queue).to(wccDispatcherExchange).with(ROUTING_KEY_ALL).noargs();
    }

    @Bean
    public DefaultClassMapper typeMapper() {
        DefaultClassMapper typeMapper = new DefaultClassMapper();
        Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put(DocumentIndexingMessage.class.getCanonicalName(), DocumentIndexingMessage.class);
        idClassMapping.put("content.api.v3.indexing.IndexingMessage", DocumentIndexingMessage.class);
        idClassMapping.put("DocumentIndexingMessage", DocumentIndexingMessage.class);
        idClassMapping.put("DocumentChangedBroadcastMessage", DocumentIndexingMessage.class);
        idClassMapping.put("eaiw.model.DocumentChangedBroadcastMessage", DocumentIndexingMessage.class);
        typeMapper.setIdClassMapping(idClassMapping);
        return typeMapper;
    }

    @Bean(name = "container")
    public SimpleMessageListenerContainer simpleRabbitListenerContainerFactory(@Qualifier("indexingQueue") Queue indexingQueue, ConnectionFactory connectionFactory, MessageConverter messageConverter, MessageListenerAdapter listenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setMessageConverter(messageConverter);


        container.setConcurrentConsumers(rabbitMQProperties.getConcurrentConsumers());
        container.setMaxConcurrentConsumers(rabbitMQProperties.getMaxConcurrentConsumers());

        container.setAdviceChain(new Advice[]{retryOperationsInterceptor()});

        container.setQueueNames(indexingQueue.getName());

        container.setMessageListener(listenerAdapter);
        container.setErrorHandler(new ConditionalRejectingErrorHandler() {
            @Override
            public void handleError(Throwable t) {
                final Log logger = LogFactory.getLog(getClass());
                if (this.causeChainContainsARADRE(t)) {
                    logger.info("got a AmqpRejectAndDontRequeueException : " + t.getCause());
                    if (logger.isTraceEnabled()) {
                        logger.trace(t);
                    }

                } else {
                    super.handleError(t);
                }
            }
        });

        return container;
    }

    @Bean
    MessageListenerAdapter listenerAdapter(WccEventsListener listener, MessageConverter messageConverter) {

        MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(listener, messageConverter);
        messageListenerAdapter.setDefaultListenerMethod(WccEventsListener.INTAKE_METHOD_NAME);
        return messageListenerAdapter;
    }


    @Bean
    public MessageConverter jackson2JsonMessageConverter(DefaultClassMapper typeMapper) {
        Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();

        jackson2JsonMessageConverter.setClassMapper(typeMapper);

        return jackson2JsonMessageConverter;
    }

    @Bean
    public Advice retryOperationsInterceptor() {
        StatefulRetryOperationsInterceptorFactoryBean retry = new StatefulRetryOperationsInterceptorFactoryBean();

        retry.setMessageRecoverer(new RejectAndDontRequeueRecoverer());

        retry.setRetryOperations(getRetryTemplate());
        return retry.getObject();
    }

    private RetryTemplate getRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        retryTemplate.setRetryPolicy(getRetryPolicy());

        retryTemplate.setBackOffPolicy(getBackOffPolicy());

        return retryTemplate;
    }

    private RetryPolicy getRetryPolicy() {
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(10);
        return retryPolicy;
    }

    private BackOffPolicy getBackOffPolicy() {
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(100);
        backOffPolicy.setMultiplier(2);
        backOffPolicy.setMaxInterval(300000);
        return backOffPolicy;
    }

    @Bean
    public KeyManagerCabinet keyManagerCabinet(RabbitMQProperties rabbitMQProperties) throws Exception {
        KeyManagerCabinet.Builder builder = new KeyManagerCabinet.Builder();
        if (!Strings.isNullOrEmpty(rabbitMQProperties.getKeystoreLocation())) {
            builder.setKeystoreFile(rabbitMQProperties.getKeystoreLocation());
        }
        if (!Strings.isNullOrEmpty(rabbitMQProperties.getKeystorePassword())) {
            builder.setKeystorePassword(rabbitMQProperties.getKeystorePassword());
        }

        return builder.build();
    }

    /**
     * Creates a custom RabbitMQ connection factory for SSL.
     * <p/>
     * <p>
     * Although Spring AMQP provides a simplified abstraction in {@link
     * org.springframework.amqp.rabbit.connection.ConnectionFactory}, in order to use SSL it's
     * necessary to instantiate the lower-level/driver {@link com.rabbitmq.client.ConnectionFactory}
     * and set it's socket factory to an SSL Socket Factory.
     * </p>
     * <p/>
     * <p>
     * In production, the {@code allowInsecureSSL} property should always be blank or set explicitly
     * to false, though there is a secondary check here to verify that we never set it to true when
     * the production profile is active.
     * </p>
     *
     * @return a new RabbitMQ connection factory
     * @throws KeyManagementException   for key errors
     * @throws NoSuchAlgorithmException for cryptographic algorithm errors
     */
    @Bean
    public com.rabbitmq.client.ConnectionFactory rabbitConnectionFactory(KeyManagerCabinet keyManagerCabinet) throws KeyManagementException, NoSuchAlgorithmException {
        com.rabbitmq.client.ConnectionFactory connectionFactory = new com.rabbitmq.client.ConnectionFactory();
        connectionFactory.setUsername(rabbitMQProperties.getUsername());
        connectionFactory.setPassword(rabbitMQProperties.getPassword());
        connectionFactory.setVirtualHost(rabbitMQProperties.getVirtualHost());
        connectionFactory.setRequestedHeartbeat(60);

        // Set up SSL and make sure we are only using insecure SSL when not in production
        if (rabbitMQProperties.isUseSSL()) {
            TrustManager[] trustManagers = keyManagerCabinet.getTrustManagers();
            SSLContext context = SSLContext.getInstance(SSLConnectionSocketFactory.TLS);
            context.init(keyManagerCabinet.getKeyManagers(), trustManagers, new SecureRandom());

            connectionFactory.useSslProtocol(context);
        }

        return connectionFactory;
    }

    @Bean
    public ConnectionFactory connectionFactory(com.rabbitmq.client.ConnectionFactory rabbitConnectionFactory) {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(rabbitConnectionFactory);
        cachingConnectionFactory.setAddresses(rabbitMQProperties.getAddresses());
        return cachingConnectionFactory;
    }
}
