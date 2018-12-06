package edu.uw.edm.wcctoacsreplicator.wccmapping;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static edu.uw.edm.wcctoacsreplicator.wccmapping.WCCToACSMapping.TABLE_NAME;

/**
 * @author Maxime Deravet Date: 10/4/18
 */
@DynamoDBTable(tableName = TABLE_NAME)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WCCToACSMapping {

    public static final String TABLE_NAME = "wcc-to-acs-mapping";
    @DynamoDBHashKey
    private String wccId;

    @DynamoDBAttribute
    private String acsId;

    @DynamoDBAttribute
    private String profile;
}
