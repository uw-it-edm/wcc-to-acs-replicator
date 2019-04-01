# DEPRECATED - This project is deprecated and no longer maintained

# wcc-to-acs-replicator

### Travis 

develop: [![Build Status](https://travis-ci.com/uw-it-edm/wcc-to-acs-replicator.svg?branch=develop)](https://travis-ci.com/uw-it-edm/wcc-to-acs-replicator)
master: [![Build Status](https://travis-ci.com/uw-it-edm/wcc-to-acs-replicator.svg?branch=master)](https://travis-ci.com/uw-it-edm/wcc-to-acs-replicator)

### Codacy

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/b26e733e777f400595e8b268e218a772)](https://www.codacy.com/app/uw-it-edm/wcc-to-acs-replicator?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=uw-it-edm/wcc-to-acs-replicator&amp;utm_campaign=Badge_Grade)

## Running locally.

### Dynamodb 

start local docker dynamodb server by running


```
docker run -p 8123:8000 amazon/dynamodb-local
```

Then you'll want to set these 2 properties in your config file : 

```
uw.replicator.dynamoDBEndpointOverride=http://localhost:8123
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
