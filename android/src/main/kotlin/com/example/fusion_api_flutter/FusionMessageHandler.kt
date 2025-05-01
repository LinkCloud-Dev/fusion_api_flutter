package com.example.fusion_api_flutter

import au.com.dmg.fusion.data.ErrorCondition
import au.com.dmg.fusion.data.MessageCategory
import au.com.dmg.fusion.data.MessageType
import au.com.dmg.fusion.data.PaymentType
import au.com.dmg.fusion.request.SaleToPOIRequest
import au.com.dmg.fusion.request.displayrequest.DisplayRequest
import au.com.dmg.fusion.response.EventNotification
import au.com.dmg.fusion.response.ResponseResult
import au.com.dmg.fusion.response.SaleToPOIResponse
import java.text.SimpleDateFormat
import java.util.Date

class FusionMessageHandler {
    private val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    private val TAG = "FusionMessageHandler"
    private val fusionMessageResponse = FusionMessageResponse()

    fun handle(request: SaleToPOIRequest): FusionMessageResponse {
        log("Start SaleToPOIRequest")
        log("Request(JSON): ${request.toJson()}")

        if (request.messageHeader == null) {
            fusionMessageResponse.setMessage(false, "Invalid Message")
            return fusionMessageResponse
        }

        val messageCategory = request.messageHeader.messageCategory
        if (messageCategory == MessageCategory.Display) {
            val displayRequest: DisplayRequest? = request.displayRequest
            if (displayRequest != null) {
                log("Display Output = ${displayRequest.displayText}")
                fusionMessageResponse.setMessage(
                    true,
                    MessageType.Request,
                    MessageCategory.Display,
                    null,
                    displayRequest.displayText
                )
                return fusionMessageResponse
            }
        }

        log("End SaleToPOIRequest")
        fusionMessageResponse.setMessage(false, "Unknown Error")
        return fusionMessageResponse
    }

    fun handle(response: SaleToPOIResponse): FusionMessageResponse {
        log("Start SaleToPOIResponse")
        log("Response(JSON): ${response.toJson()}")

        val messageCategory = response.messageHeader.messageCategory
        log("Message Category: $messageCategory")

        when (messageCategory) {
            MessageCategory.Event -> {
                val eventNotification = requireNotNull(response.eventNotification) { "Missing eventNotification" }
                log("Event Details: ${eventNotification.eventDetails}")
                fusionMessageResponse.setMessage(MessageType.Response, MessageCategory.Event, response)
            }
            MessageCategory.Login -> {
                val loginResponse = requireNotNull(response.loginResponse) { "Missing loginResponse" }
                val responseResult = loginResponse.response.result

                if (responseResult == ResponseResult.Success) {
                    fusionMessageResponse.setMessage(
                        true,
                        MessageType.Response,
                        MessageCategory.Login,
                        response,
                        "LOGIN SUCCESSFUL"
                    )
                } else {
                    val additionalResponse: String = loginResponse.response.additionalResponse
                    fusionMessageResponse.setMessage(
                        false,
                        MessageType.Response,
                        MessageCategory.Login,
                        response,
                        additionalResponse
                    )
                }
            }
            MessageCategory.Payment -> {
                val paymentResponse = requireNotNull(response.paymentResponse) { "Missing paymentResponse" }
                val paymentResult = requireNotNull(paymentResponse.paymentResult) { "Missing paymentResult" }
                val paymentType = requireNotNull(paymentResult.paymentType) { "Missing paymentType" }
                val responseResult = paymentResponse.response.result
                val type = if (paymentType == PaymentType.Normal) "Payment" else paymentType.name


                if (responseResult == ResponseResult.Success) {
                    fusionMessageResponse.setMessage(
                        true,
                        MessageType.Response,
                        MessageCategory.Payment,
                        response,
                        "$type SUCCESSFUL"
                    )
                } else {
                    val additionalResponse: String = paymentResponse.response.additionalResponse
                    fusionMessageResponse.setMessage(
                        false,
                        MessageType.Response,
                        MessageCategory.Payment,
                        response,
                        additionalResponse
                    )
                }
            }
            MessageCategory.TransactionStatus -> {
                val tsr = requireNotNull(response.transactionStatusResponse) { "Missing transactionStatusResponse" }
                val responseResult = tsr.response.result
                if (responseResult == ResponseResult.Success) {
                    fusionMessageResponse.setMessage(
                        true,
                        MessageType.Response,
                        MessageCategory.TransactionStatus,
                        response,
                        "TRANSACTION STATUS FOUND"
                    )
                } else {
                    val errorCondition: ErrorCondition = tsr.response.errorCondition
                    fusionMessageResponse.setMessage(
                            isSuccessful = false,
                            messageType = MessageType.Response,
                            messageCategory = MessageCategory.TransactionStatus,
                            saleToPOI = response,
                            err = errorCondition
                    )
                }
            }
            else -> {
                fusionMessageResponse.setMessage(
                    false,
                    MessageType.Response,
                    messageCategory,
                    response,
                    "UNHANDLED MESSAGE CATEGORY: $messageCategory"
                )   
            }
        }

        log("End SaleToPOIResponse")
        return fusionMessageResponse
    }

    private fun log(data: String) {
        println("${sdf.format(Date())}: $TAG: $data")
    }
}
