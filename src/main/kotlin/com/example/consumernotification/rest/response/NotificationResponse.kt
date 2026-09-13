package com.example.consumernotification.rest.response

data class NotificationResponse(
    var message: String,
    var type: String,
    var createDate: String,
    var readDate: String
)