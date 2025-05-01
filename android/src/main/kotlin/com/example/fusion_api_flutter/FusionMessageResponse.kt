package com.example.fusion_api_flutter

import au.com.dmg.fusion.SaleToPOI
import au.com.dmg.fusion.data.ErrorCondition
import au.com.dmg.fusion.data.MessageCategory
import au.com.dmg.fusion.data.MessageType

class FusionMessageResponse {
    var isSuccessful: Boolean? = null
    var messageType: MessageType? = null
    var messageCategory: MessageCategory? = null
    var saleToPOI: SaleToPOI? = null
    var displayMessage: String? = null
    var errorCondition: ErrorCondition? = null

    fun setMessage(
        isSuccessful: Boolean,
        messageType: MessageType?,
        messageCategory: MessageCategory?,
        saleToPOI: SaleToPOI?,
        displayMsg: String?
    ) {
        this.isSuccessful = isSuccessful
        this.messageType = messageType
        this.messageCategory = messageCategory
        this.saleToPOI = saleToPOI
        this.displayMessage = displayMsg
        this.errorCondition = null
    }

    fun setMessage(
        messageType: MessageType?,
        messageCategory: MessageCategory?,
        saleToPOI: SaleToPOI?
    ) {
        this.isSuccessful = true
        this.messageType = messageType
        this.messageCategory = messageCategory
        this.saleToPOI = saleToPOI
        this.displayMessage = ""
        this.errorCondition = null
    }

    fun setMessage(
        isSuccessful: Boolean,
        displayMsg: String?
    ) {
        this.isSuccessful = isSuccessful
        this.messageType = null
        this.messageCategory = null
        this.saleToPOI = null
        this.displayMessage = displayMsg
        this.errorCondition = null
    }

    fun setMessage(
        isSuccessful: Boolean,
        messageType: MessageType?,
        messageCategory: MessageCategory?,
        saleToPOI: SaleToPOI?,
        err: ErrorCondition?
    ) {
        this.isSuccessful = isSuccessful
        this.messageType = messageType
        this.messageCategory = messageCategory
        this.saleToPOI = saleToPOI
        this.displayMessage = ""
        this.errorCondition = err
    }
}
