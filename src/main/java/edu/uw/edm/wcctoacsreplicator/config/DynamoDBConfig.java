package edu.uw.edm.wcctoacsreplicator.config;

import com.google.common.base.Strings;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;

import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

import edu.uw.edm.wcctoacsreplicator.properties.ReplicatorProperties;
import edu.uw.edm.wcctoacsreplicator.wccmapping.WCCToACSMapping;

/**
 * @author Maxime Deravet Date: 10/4/18
 */
@Configuration
@EnableDynamoDBRepositories(
        dynamoDBMapperConfigRef = "dynamoDBMapperConfig",
        basePackages = "edu.uw.edm.wcctoacsreplicator.wccmapping")
public class DynamoDBConfig {

    @Value("${amazon.aws.accesskey?:}")
    private String amazonAWSAccessKey;

    @Value("${amazon.aws.secretkey?:}")
    private String amazonAWSSecretKey;


    @Value("${amazon.aws.region}")
    private String region;


    @Bean
    public AmazonDynamoDB amazonDynamoDB(ReplicatorProperties replicatorProperties) {

        AmazonDynamoDBClientBuilder builder = AmazonDynamoDBClientBuilder
                .standard();


        if (!Strings.isNullOrEmpty(this.amazonAWSAccessKey) && !Strings.isNullOrEmpty(this.amazonAWSSecretKey)) {
            builder.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(this.amazonAWSAccessKey, this.amazonAWSSecretKey)));
        }

        if (!Strings.isNullOrEmpty(replicatorProperties.getDynamoDBEndpointOverride())) {

            AwsClientBuilder.EndpointConfiguration endpointConfiguration = new AwsClientBuilder.EndpointConfiguration(replicatorProperties.getDynamoDBEndpointOverride(), "dev");

            builder.withEndpointConfiguration(endpointConfiguration);
        } else {
            builder.withRegion(this.region);
        }

        AmazonDynamoDB amazonDynamoDB = builder.build();


        if (isWorkstationEnvironment(replicatorProperties)) {

            Optional<String> table = amazonDynamoDB.listTables().getTableNames().stream().
                    filter(tableName -> tableName.equals(WCCToACSMapping.TABLE_NAME )).findFirst();

            if (!table.isPresent()) {
                amazonDynamoDB.createTable(new DynamoDBMapper(amazonDynamoDB)
                        .generateCreateTableRequest(WCCToACSMapping.class)
                        .withProvisionedThroughput(new ProvisionedThroughput(1000L, 1000L)));
            }


        }

        return amazonDynamoDB;
    }

    private boolean isWorkstationEnvironment(ReplicatorProperties replicatorProperties) {
        return !Strings.isNullOrEmpty(replicatorProperties.getDynamoDBEndpointOverride()) && replicatorProperties.isDynamoDBCreateTable();
    }

    @Bean
    public DynamoDBMapperConfig dynamoDBMapperConfig(DynamoDBMapperConfig.TableNameOverride tableNameOverrider, ReplicatorProperties replicatorProperties) {

        DynamoDBMapperConfig.Builder builder = new DynamoDBMapperConfig.Builder();

        if (!isWorkstationEnvironment(replicatorProperties)) {
            builder.setTableNameOverride(tableNameOverrider);
        }

        // Sadly this is a @deprecated method but new DynamoDBMapperConfig.Builder() is incomplete compared to DynamoDBMapperConfig.DEFAULT
        return new DynamoDBMapperConfig(DynamoDBMapperConfig.DEFAULT, builder.build());
    }

    @Bean
    public DynamoDBMapperConfig.TableNameOverride tableNameOverrider(ReplicatorProperties replicatorProperties) {
        String prefix = replicatorProperties.getDynamoTableNamePrefix(); // Use @Value to inject values via Spring or use any logic to define the table prefix
        return DynamoDBMapperConfig.TableNameOverride.withTableNamePrefix(prefix);
    }


}
