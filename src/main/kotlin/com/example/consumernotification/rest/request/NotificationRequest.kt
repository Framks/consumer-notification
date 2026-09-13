package com.example.consumernotification.rest.request

data class NotificationRequest (
    var message: String,
    var type: String,
) {
    constructor() : this("", "")
}