resource "aws_sqs_queue" "streams_fs2_learning_queue" {
  count                     = 1
  name                      = "streams_fs2_learning_queue"
  delay_seconds             = 0
  max_message_size          = 262144
  message_retention_seconds = 345600
  receive_wait_time_seconds = 20
  visibility_timeout_seconds = 30
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.streams_fs2_learning_deadletter_queue.arn
    maxReceiveCount     = 3
  })
}

resource "aws_sqs_queue" "streams_fs2_learning_deadletter_queue" {
  name = "streams_fs2_learning_deadletter_queue"
}