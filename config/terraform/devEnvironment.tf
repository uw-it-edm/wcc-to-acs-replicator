variable "acs_event_sns_topic_arn" {
  description = "The topic you want to create a queue for"
}

variable "aws_region" {
  description = "AWS Region"
}

variable "queue_prefix" {
  description = "prefix you want to use for the queue, should be your name"
}

provider "aws" {
  version = "~> 1.14"
  region  = "${var.aws_region}"
}

data "aws_iam_policy_document" "sqs-queue-policy" {
  statement {
    sid    = "sqs-queue-policy"
    effect = "Allow"

    principals {
      type        = "AWS"
      identifiers = ["*"]
    }

    actions = [
      "SQS:SendMessage",
    ]

    resources = [
      "*",
    ]

    condition {
      test     = "ArnEquals"
      variable = "aws:SourceArn"

      values = [
        "${var.acs_event_sns_topic_arn}",
      ]
    }
  }
}

resource "aws_sqs_queue" "replicatorQueue" {
  name                      = "${var.queue_prefix}-wcc-to-acs-replicator"
  message_retention_seconds = 3600
  receive_wait_time_seconds = 10

  tags {
    Environment = "temp"
  }

  policy = "${data.aws_iam_policy_document.sqs-queue-policy.json}"
}

resource "aws_sns_topic_subscription" "replicatorQueue_sqs_target" {
  topic_arn = "${var.acs_event_sns_topic_arn}"
  protocol  = "sqs"
  endpoint  = "${aws_sqs_queue.replicatorQueue.arn}"

  filter_policy = <<EOF
  {
    "event-type":["create"]
  }
  EOF
}
