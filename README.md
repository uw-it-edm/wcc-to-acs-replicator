# wcc-to-acs-replicator

### Travis 

develop: [![Build Status](https://travis-ci.org/uw-it-edm/wcc-to-acs-replicator.svg?branch=develop)](https://travis-ci.org/uw-it-edm/wcc-to-acs-replicator)
master: [![Build Status](https://travis-ci.org/uw-it-edm/wcc-to-acs-replicator.svg?branch=master)](https://travis-ci.org/uw-it-edm/wcc-to-acs-replicator)


## Running locally.

### Dynamodb 

start local docker dynamodb server by running


```
docker run -p 8123:8000 amazon/dynamodb-local
```

Then you'll want to set these 2 properties in your config file : 

```
uw.replicator.dynamoDBEndpointOverride=http://localhost:8000
uw.replicator.dynamoDBCreateTable=true
```

### SQS queue

You can create a temporary queue listening to acs events like this : 

```
cd config/terraform
terraform init 
terraform apply # respond to the different questions 

```

When you're done, you can delete the queue and the subscription by running 

```
terraform destroy 

```