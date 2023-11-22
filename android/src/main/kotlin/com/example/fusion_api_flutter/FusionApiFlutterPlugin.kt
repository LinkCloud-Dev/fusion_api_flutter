package com.example.fusion_api_flutter

import android.os.Build
import androidx.annotation.RequiresApi
import au.com.dmg.fusion.MessageHeader
import au.com.dmg.fusion.SaleToPOI
import au.com.dmg.fusion.client.FusionClient
import au.com.dmg.fusion.data.ErrorCondition
import au.com.dmg.fusion.data.MessageCategory
import au.com.dmg.fusion.data.MessageClass
import au.com.dmg.fusion.data.MessageType
import au.com.dmg.fusion.data.PaymentInstrumentType
import au.com.dmg.fusion.data.PaymentType
import au.com.dmg.fusion.data.SaleCapability
import au.com.dmg.fusion.data.TerminalEnvironment
import au.com.dmg.fusion.data.UnitOfMeasure
import au.com.dmg.fusion.exception.FusionException
import au.com.dmg.fusion.request.SaleTerminalData
import au.com.dmg.fusion.request.SaleToPOIRequest
import au.com.dmg.fusion.request.aborttransactionrequest.AbortTransactionRequest
import au.com.dmg.fusion.request.loginrequest.LoginRequest
import au.com.dmg.fusion.request.loginrequest.SaleSoftware
import au.com.dmg.fusion.request.logoutrequest.LogoutRequest
import au.com.dmg.fusion.request.paymentrequest.AmountsReq
import au.com.dmg.fusion.request.paymentrequest.PaymentData
import au.com.dmg.fusion.request.paymentrequest.PaymentInstrumentData
import au.com.dmg.fusion.request.paymentrequest.PaymentRequest
import au.com.dmg.fusion.request.paymentrequest.PaymentTransaction
import au.com.dmg.fusion.request.paymentrequest.SaleData
import au.com.dmg.fusion.request.paymentrequest.SaleItem
import au.com.dmg.fusion.request.paymentrequest.SaleTransactionID
import au.com.dmg.fusion.request.transactionstatusrequest.MessageReference
import au.com.dmg.fusion.request.transactionstatusrequest.TransactionStatusRequest
import au.com.dmg.fusion.response.Response
import au.com.dmg.fusion.response.ResponseResult
import au.com.dmg.fusion.response.SaleToPOIResponse
import au.com.dmg.fusion.util.MessageHeaderUtil
import au.com.dmg.fusion.util.SecurityTrailerUtil.generateSecurityTrailer
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.io.IOException
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.concurrent.*


/** FusionApiFlutterPlugin */
class FusionApiFlutterPlugin : FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel

    private var fusionClient: FusionClient = FusionClient(true)

    override fun onAttachedToEngine(
        flutterPluginBinding: FlutterPlugin.FlutterPluginBinding
    ) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "fusion_api_flutter")
        channel.setMethodCallHandler(this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "getPlatformVersion" -> {
                result.success("Android ${Build.VERSION.RELEASE}")
            }

            "init" -> {
                // TODO: Implement initialization here
                init(call.argument("saleID")!!, call.argument("poiID")!!, call.argument("kek")!!,
                    result)
            }

            "qrLogin" -> {
                qrLogin(
                    call.argument("saleID")!!,
                    call.argument("poiID")!!,
                    call.argument("providerIdentification")!!,
                    call.argument("applicationName")!!,
                    call.argument("softwareVersion")!!,
                    call.argument("certificationCode")!!,
                    call.argument<Boolean>("useTestEnvironment")!!,
                    result
                )
            }

            "mannualLogin" -> {
                mannualLogin(
                    call.argument("saleID")!!,
                    call.argument("poiID")!!,
                    call.argument("providerIdentification")!!,
                    call.argument("applicationName")!!,
                    call.argument("softwareVersion")!!,
                    call.argument("certificationCode")!!,
                    call.argument<Boolean>("useTestEnvironment")!!,
                    result
                )
            }

            "logout" -> {
                logout(
                    call.argument("saleID")!!,
                    call.argument("poiID")!!,
                    call.argument("useTestEnvironment")!!,
                    result
                )
            }

            "doPayment" -> {
                doPayment(call.argument("saleID")!!, call.argument("poiID")!!, call.argument
                    ("items")!!, call.argument("useTestEnvironment")!!)
            }

            "doRefund" -> {
                doRefund(call.argument("saleID")!!, call.argument("poiID")!!, call.argument
                    ("amount")!!, call
                    .argument("useTestEnvironment")!!)
            }

            "initiateTransaction" -> {
                initiateTransaction(
                    call.argument("saleID")!!,
                    call.argument("poiID")!!,
                    call.argument("providerIdentification")!!,
                    call.argument("applicationName")!!,
                    call.argument("softwareVersion")!!,
                    call.argument("certificationCode")!!,
                    call.argument("qrPairing")!!,
                    call.argument<Boolean>("useTestEnvironment")!!,
                    result
                )
            }

            else -> {
                result.notImplemented()
            }
        }
    }


    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    private fun init(saleID: String, poiID: String, kek: String, result: Result) {
        fusionClient.setSettings(saleID, poiID, kek)
        result.success(null)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initiateTransaction(
        saleID: String,
        poiID: String,
        providerIdentification: String,
        applicationName: String,
        softwareVersion: String,
        certificationCode: String,
        qrPairing: Boolean,
        useTestEnvironment: Boolean,
        result: Result
    ) {
        try {
//            fusionClient.connect()
//
//            if (login(
//                    saleID,
//                    poiID,
//                    providerIdentification,
//                    applicationName,
//                    softwareVersion,
//                    certificationCode,
//                    qrPairing,
//                    useTestEnvironment,
//                result
//                )
//            ) {
//                doPayment(saleID, poiID, useTestEnvironment)
//            }
//            fusionClient.disconnect()
        } catch (e: IOException) {
            log(e)
        } catch (e: FusionException) {
            log(e)
        }
    }

    private fun mannualLogin(
        saleID: String,
        poiID: String,
        providerIdentification: String,
        applicationName: String,
        softwareVersion: String,
        certificationCode: String,
        useTestEnvironment: Boolean,
        result: Result
    ){
        val executor = Executors.newSingleThreadExecutor()
        val login = executor.submit<Boolean> {
            var loginRequest: SaleToPOIRequest?
            var gotValidResponse = false
            // Payment request
            try {
                loginRequest = buildLoginRequest(
                    saleID,
                    poiID,
                    providerIdentification,
                    applicationName,
                    softwareVersion,
                    certificationCode,
                    false,
                    useTestEnvironment,
                )

                log("Sending message to websocket server:\n$loginRequest")
                fusionClient.sendMessage(loginRequest)

                // Wait for response & handle
                var waitingForResponse = true // TODO: timeout handling
                while (waitingForResponse) {
                    val saleToPOI = fusionClient.readMessage() ?: continue

                    if (saleToPOI is SaleToPOIResponse) {
                        waitingForResponse = handleLoginResponseMessage(saleToPOI)
                        gotValidResponse = true
                    }
                }
            } catch (e: FusionException) {
                log(e)
            } catch (e: Exception) {
                log(e)
            }

            gotValidResponse
        }

        var gotValidResponse = false
        try {
            gotValidResponse = login.get(60, TimeUnit.SECONDS) // set timeout
        } catch (e: TimeoutException) {
            System.err.println("Payment Request Timeout...")
        } catch (e: ExecutionException) {
            log("Exception: $e")
        } catch (e: InterruptedException) {
            log("Exception: $e")
        }
        result.success(gotValidResponse)
    }


    private fun qrLogin(
        saleID: String,
        poiID: String,
        providerIdentification: String,
        applicationName: String,
        softwareVersion: String,
        certificationCode: String,
        useTestEnvironment: Boolean,
        result: Result
    ){
        var newPoiID = ""
        val executor = Executors.newSingleThreadExecutor()
        val login = executor.submit<String> {
            var loginRequest: SaleToPOIRequest?
            var gotValidResponse = false
            // Payment request
            try {
                loginRequest = buildLoginRequest(
                    saleID,
                    poiID,
                    providerIdentification,
                    applicationName,
                    softwareVersion,
                    certificationCode,
                    true,
                    useTestEnvironment,
                )

                log("Sending message to websocket server:\n$loginRequest")
                fusionClient.sendMessage(loginRequest)

                // Wait for response & handle
                var waitingForResponse = true // TODO: timeout handling
                while (waitingForResponse) {
                    val saleToPOI = fusionClient.readMessage() ?: continue

                    if (saleToPOI is SaleToPOIResponse) {
                        waitingForResponse = handleLoginResponseMessage(saleToPOI)
                        gotValidResponse = true
                        newPoiID = getPOIID(saleToPOI)
                    }
                }
            } catch (e: FusionException) {
                log(e)
            } catch (e: Exception) {
                log(e)
            }

            newPoiID
        }

        try {
            newPoiID = login.get(60, TimeUnit.SECONDS) // set timeout
        } catch (e: TimeoutException) {
            System.err.println("Payment Request Timeout...")
        } catch (e: ExecutionException) {
            log("Exception: $e")
        } catch (e: InterruptedException) {
            log("Exception: $e")
        }
        result.success(newPoiID)
    }

    private fun logout(
        saleID: String,
        poiID: String,
        useTestEnvironment: Boolean,
        result: Result
    ) {
        val executor = Executors.newSingleThreadExecutor()
        val logout = executor.submit<Boolean> {
            var logoutRequest: SaleToPOIRequest?
            var gotValidResponse = false
            // Payment request
            try {
                logoutRequest = buildLogoutRequest(
                    saleID,
                    poiID,
                    useTestEnvironment
                )

                log("Sending message to websocket server:\n$logoutRequest")
                fusionClient.sendMessage(logoutRequest)

                // Wait for response & handle
                var waitingForResponse = true // TODO: timeout handling
                while (waitingForResponse) {
                    val saleToPOI = fusionClient.readMessage() ?: continue

                    if (saleToPOI is SaleToPOIResponse) {
                        waitingForResponse = handleLoginResponseMessage(saleToPOI)
                        gotValidResponse = true
                    }
                }
            } catch (e: FusionException) {
                log(e)
            } catch (e: Exception) {
                log(e)
            }

            gotValidResponse
        }

        var gotValidResponse = false
        try {
            gotValidResponse = logout.get(60, TimeUnit.SECONDS) // set timeout
        } catch (e: TimeoutException) {
            System.err.println("Payment Request Timeout...")
        } catch (e: ExecutionException) {
            log("Exception: $e")
        } catch (e: InterruptedException) {
            log("Exception: $e")
        }
        result.success(gotValidResponse)
    }

    @Throws(Exception::class)
    private fun buildLoginRequest(
        saleID: String,
        poiID: String,
        providerIdentification: String,
        applicationName: String,
        softwareVersion: String,
        certificationCode: String,
        qrPairing: Boolean,
        useTestEnvironment: Boolean,
    ): SaleToPOIRequest {
        // Login Request
        val saleSoftware = SaleSoftware.Builder()
            .providerIdentification(providerIdentification)
            .applicationName(applicationName)
            .softwareVersion(softwareVersion)
            .certificationCode(certificationCode)
            .build()

        val saleCapabilities = listOf(
            SaleCapability.CashierStatus,
            SaleCapability.CustomerAssistance,
            SaleCapability.PrinterReceipt
        )

        val saleTerminalData = SaleTerminalData.Builder()
            .terminalEnvironment(TerminalEnvironment.SemiAttended)
            .saleCapabilities(saleCapabilities)
            .build()

        val currentDateTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(Date()).toString()

        val loginRequest = LoginRequest.Builder()
            .dateTime(currentDateTime)
            .saleSoftware(saleSoftware)
            .saleTerminalData(saleTerminalData)
            .operatorLanguage("en")
            .pairing(qrPairing)
            .build()

        // Message Header
        val messageHeader = MessageHeader.Builder()
            .messageClass(MessageClass.Service)
            .messageCategory(MessageCategory.Login)
            .messageType(MessageType.Request)
            .serviceID(MessageHeaderUtil.generateServiceID(10))
            .saleID(saleID)
            .POIID(poiID)
            .build()

        val securityTrailer = generateSecurityTrailer(
            messageHeader,
            loginRequest,
            useTestEnvironment
        )

        return SaleToPOIRequest.Builder()
            .messageHeader(messageHeader)
            .request(loginRequest)
            .securityTrailer(securityTrailer)
            .build()
    }

    @Throws(Exception::class)
    private fun buildLogoutRequest(
        saleID: String,
        poiID: String,
        useTestEnvironment: Boolean
    ): SaleToPOIRequest {

        val logoutRequest = LogoutRequest()

        // Message Header
        val messageHeader = MessageHeader.Builder()
            .messageClass(MessageClass.Service)
            .messageCategory(MessageCategory.Logout)
            .messageType(MessageType.Request)
            .serviceID(MessageHeaderUtil.generateServiceID(10))
            .saleID(saleID)
            .POIID(poiID)
            .build()

        val securityTrailer = generateSecurityTrailer(
            messageHeader,
            logoutRequest,
            useTestEnvironment
        )

        return SaleToPOIRequest.Builder()
            .messageHeader(messageHeader)
            .request(logoutRequest)
            .securityTrailer(securityTrailer)
            .build()
    }

    private fun handleLoginResponseMessage(msg: SaleToPOI): Boolean {
        var waitingForResponse = true
        val messageCategory: MessageCategory
        if (msg is SaleToPOIResponse) {
            val response: SaleToPOIResponse = msg
            log(String.format("Response(JSON): %s", response.toJson()))
            response.messageHeader
            messageCategory = response.messageHeader.messageCategory

            var responseBody: Response?
            log("Message Category: $messageCategory")
            when (messageCategory) {
                MessageCategory.Event -> {
                    val eventNotification = response.eventNotification
                    log("Event Details: " + eventNotification!!.eventDetails)
                }

                MessageCategory.Login -> if (response.loginResponse != null) {
                    response.loginResponse!!.response
                    responseBody = response.loginResponse!!.response
                    if (responseBody.result != null) {
                        log(java.lang.String.format("Login Result: %s ", responseBody.result))
                        if (responseBody.result !== ResponseResult.Success) {
                            log(
                                java.lang.String.format(
                                    "Error Condition: %s, Additional Response: %s",
                                    responseBody.errorCondition,
                                    responseBody.additionalResponse
                                )
                            )
                        }
                    }
                    waitingForResponse = false
                }

                else -> log("$messageCategory received during Payment response message " +
                        "handling.")
            }
        } else log("Unexpected response message received.")
        return waitingForResponse
    }

    private fun getPOIID(msg: SaleToPOI): String {
        return if (msg is SaleToPOIResponse) {
            val response: SaleToPOIResponse = msg
            response.messageHeader.poiID
        } else ""
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun doPayment(saleID: String, poiID: String, items: List<MutableMap<String, Any>>,
                          useTestEnvironment:
    Boolean) {
        val serviceID = MessageHeaderUtil.generateServiceID(10)
        val executor = Executors.newSingleThreadExecutor()
        var abortReason = ""
        val payment = executor.submit<Boolean> {
            var paymentRequest: SaleToPOIRequest?
            var gotValidResponse = false
            // Payment request
            try {
                paymentRequest = buildPaymentRequest(saleID, poiID, serviceID, items,
                    useTestEnvironment)
                log("Sending message to websocket server: \n$paymentRequest")
                fusionClient.sendMessage(paymentRequest)

                // Wait for response & handle
                var waitingForResponse = true // TODO: timeout handling
                while (waitingForResponse) {
                    val saleToPOI = fusionClient.readMessage() ?: continue
                    if (saleToPOI is SaleToPOIRequest) {
                        handleRequestMessage(saleToPOI)
                        continue
                    }
                    if (saleToPOI is SaleToPOIResponse) {
                        val responseResult: Map<String, Boolean>? =
                            handlePaymentResponseMessage(saleToPOI)
                        waitingForResponse =
                            responseResult?.get("WaitingForAnotherResponse") ?: true
                        if (!waitingForResponse) {
                            gotValidResponse = responseResult?.get("GotValidResponse") ?: false
                        }
                    }
                }
            } catch (e: Exception) {
                log(e)
            } catch (e: FusionException) {
                log(e)
            }
            gotValidResponse
        }
        var gotValidResponse = false
        try {
            gotValidResponse = payment[60, TimeUnit.SECONDS] // set timeout
        } catch (e: TimeoutException) {
            System.err.println("Payment Request Timeout...")
            abortReason = "Timeout"
        } catch (e: ExecutionException) {
            log(String.format("Exception: %s", e.toString()))
            abortReason = "Other Exception"
        } catch (e: InterruptedException) {
            log(String.format("Exception: %s", e.toString()))
            abortReason = "Other Exception"
        } finally {
            executor.shutdownNow()
            if (!gotValidResponse) checkTransactionStatus(saleID, poiID, serviceID, abortReason,
                useTestEnvironment)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(Exception::class)
    private fun buildPaymentRequest(saleID: String, poiID: String, serviceID: String, items:
    List<MutableMap<String, Any>>, useTestEnvironment: Boolean):
            SaleToPOIRequest? {
        // Payment Request
        val saleTransactionID = SaleTransactionID.Builder() //
            .transactionID(
                "transactionID" + SimpleDateFormat("HH:mm:ssXXX").format(Date()).toString()
            )
            .timestamp(Instant.now()).build()
        val saleData = SaleData.Builder() //
            // .operatorID("")//
            .operatorLanguage("en") //
            .saleTransactionID(saleTransactionID) //
            .build()
        // TODO: Test the saleItems
        var totalAmount = 0.0
        val saleItems = mutableListOf<SaleItem>()
        items.forEachIndexed() { index, item ->
            val saleItem = SaleItem.Builder() //
                .itemID(index) //
                .productCode(item["productCode"].toString()) //
                .unitOfMeasure(UnitOfMeasure.Other) //
                .quantity(BigDecimal(item["quantity"].toString())) //
                .unitPrice(BigDecimal(item["unitPrice"].toString())) //
                .itemAmount(BigDecimal(item["itemAmount"].toString())) //
                .productLabel(item["productLabel"].toString()) //
                .build()
            saleItems.add(saleItem)
            totalAmount += item["itemAmount"].toString().toDouble()
        }
        val amountsReq = AmountsReq.Builder() //
            .currency("AUD") //
            .requestedAmount(BigDecimal(totalAmount)) //
            .build()
        val paymentInstrumentData: PaymentInstrumentData = PaymentInstrumentData.Builder() //
            .paymentInstrumentType(PaymentInstrumentType.Card) //
            .build()
        val paymentData = PaymentData.Builder() //
            .paymentType(PaymentType.Normal) //
            .paymentInstrumentData(paymentInstrumentData) //
            .build()
        val paymentTransaction = PaymentTransaction.Builder() //
            .amountsReq(amountsReq) //
            .addSaleItems(saleItems) //
            .build()
        val paymentRequest = PaymentRequest.Builder() //
            .paymentTransaction(paymentTransaction) //
            .paymentData(paymentData) //
            .saleData(saleData).build()

        // Message Header
        val messageHeader =
            MessageHeader.Builder() //
                .messageClass(MessageClass.Service) //
                .messageCategory(MessageCategory.Payment) //
                .messageType(MessageType.Request) //
                .serviceID(serviceID) //
                .saleID(saleID) //
                .POIID(poiID) //
                .build()
        val securityTrailer =
            generateSecurityTrailer(messageHeader, paymentRequest, useTestEnvironment)
        return SaleToPOIRequest.Builder() //
            .messageHeader(messageHeader) //
            .request(paymentRequest) //
            .securityTrailer(securityTrailer) //
            .build()
    }

    private fun handleRequestMessage(msg: SaleToPOI) {
        var messageCategory = MessageCategory.Other
        if (msg is SaleToPOIRequest) {
            log(String.format("Request(JSON): %s", msg.toJson()))
            if (msg.messageHeader != null) messageCategory =
                msg.messageHeader.messageCategory
            if (messageCategory == MessageCategory.Display) {
                val displayRequest = msg.displayRequest
                if (displayRequest != null) {
                    log("Display Output = " + displayRequest.displayText)
                }
            } else log("$messageCategory received during response message handling.")
        } else log("Unexpected request message received.")
    }

    private fun handlePaymentResponseMessage(msg: SaleToPOI): Map<String, Boolean>? {
        val responseResult: MutableMap<String, Boolean> = HashMap()
        val messageCategory: MessageCategory
        if (msg is SaleToPOIResponse) {
            log(String.format("Response(JSON): %s", msg.toJson()))
            msg.messageHeader
            messageCategory = msg.messageHeader.messageCategory
            var responseBody: Response?
            log("Message Category: $messageCategory")
            when (messageCategory) {
                MessageCategory.Event -> {
                    val eventNotification = msg.eventNotification
                    log("Event Details: " + eventNotification!!.eventDetails)
                }

                MessageCategory.Payment -> {
                    responseBody = msg.paymentResponse!!.response
                    if (responseBody.result != null) {
                        log(String.format("Payment Result: %s", responseBody.result))
                        if (responseBody.result != ResponseResult.Success) {
                            log(
                                String.format(
                                    "Error Condition: %s, Additional Response: %s",
                                    responseBody.errorCondition,
                                    responseBody.additionalResponse
                                )
                            )
                        }
                        responseResult["GotValidResponse"] = true
                    }
                    responseResult["WaitingForAnotherResponse"] = false
                }

                else -> log(
                    "$messageCategory received during Payment response message " +
                            "handling."
                )
            }
        } else log("Unexpected response message received.")
        return responseResult
    }

    private fun checkTransactionStatus(saleID: String, poiID: String, serviceID: String,
                                       abortReason: String, useTestEnvironment: Boolean) {
        log("Sending transaction status request to check status of payment...")
        val executor = Executors.newSingleThreadExecutor()
        val transaction = executor.submit<Boolean> {
            var transactionStatusRequest: SaleToPOIRequest?
            var gotValidResponse = false
            try {
                if (abortReason !== "") {
                    val abortTransactionPOIRequest: SaleToPOIRequest =
                        buildAbortRequest(saleID, poiID, serviceID, abortReason, useTestEnvironment)
                    log("Sending abort message to websocket server: " +
                            "\n$abortTransactionPOIRequest")
                    fusionClient.sendMessage(abortTransactionPOIRequest)
                }
                var buildAndSendRequestMessage = true
                var waitingForResponse = true
                while (waitingForResponse) {
                    if (buildAndSendRequestMessage) {
                        transactionStatusRequest = buildTransactionStatusRequest(saleID, poiID,
                            serviceID, useTestEnvironment)
                        log("Sending message to websocket server: \n$transactionStatusRequest")
                        fusionClient.sendMessage(transactionStatusRequest)
                    }
                    buildAndSendRequestMessage = false
                    val saleToPOI = fusionClient.readMessage() ?: continue
                    val responseResult: Map<String, Boolean> =
                        handleTransactionResponseMessage(saleToPOI)
                    waitingForResponse = responseResult["WaitingForAnotherResponse"] ?: true
                    if (waitingForResponse) {
                        buildAndSendRequestMessage =
                            responseResult["BuildAndSendRequestMessage"] ?: false
                    } else {
                        gotValidResponse = responseResult["GotValidResponse"] ?: false
                    }
                }
            } catch (e: Exception) {
                log(java.lang.String.format("ConfigurationException: %s", e.toString()))
            } catch (e: FusionException) {
                log(String.format("NotConnectedException: %s", e.toString()))
            }
            gotValidResponse
        }
        try {
            transaction[90, TimeUnit.SECONDS] // set timeout
        } catch (e: TimeoutException) {
            System.err.println("Transaction Status Timeout...")
        } catch (e: ExecutionException) {
            log(String.format("Exception: %s", e.toString()))
        } catch (e: InterruptedException) {
            log(String.format("Exception: %s", e.toString()))
        } finally {
            executor.shutdownNow()
        }
    }

    @Throws(Exception::class)
    private fun buildTransactionStatusRequest(saleID: String, poiID: String, serviceID: String,
                                              useTestEnvironment: Boolean):
            SaleToPOIRequest? {
        // Transaction Status Request
        val messageReference = MessageReference.Builder() //
            .messageCategory(MessageCategory.Payment) //
            .POIID(poiID) //
            .saleID(saleID) //
            .serviceID(serviceID) //
            .build()
        val transactionStatusRequest =
            TransactionStatusRequest(messageReference)

        // Message Header
        val messageHeader =
            MessageHeader.Builder() //
                .messageClass(MessageClass.Service) //
                .messageCategory(MessageCategory.TransactionStatus) //
                .messageType(MessageType.Request) //
                .serviceID(MessageHeaderUtil.generateServiceID(10)) //
                .saleID(saleID) //
                .POIID(poiID) //
                .build()
        val securityTrailer =
            generateSecurityTrailer(messageHeader, transactionStatusRequest, useTestEnvironment)
        return SaleToPOIRequest.Builder() //
            .messageHeader(messageHeader) //
            .request(transactionStatusRequest) //
            .securityTrailer(securityTrailer) //
            .build()
    }

    @Throws(Exception::class)
    private fun buildAbortRequest(
        saleID: String,
        poiID: String,
        paymentServiceID: String,
        abortReason: String,
        useTestEnvironment: Boolean
    ): SaleToPOIRequest {

        // Message Header
        val messageHeader =
            MessageHeader.Builder() //
                .messageClass(MessageClass.Service) //
                .messageCategory(MessageCategory.Abort) //
                .messageType(MessageType.Request) //
                .serviceID(MessageHeaderUtil.generateServiceID(10)) //
                .saleID(saleID) //
                .POIID(poiID) //
                .build()
        val messageReference =
            MessageReference.Builder().messageCategory(MessageCategory.Abort)
                .serviceID(paymentServiceID).build()
        val abortTransactionRequest =
            AbortTransactionRequest(messageReference, abortReason)
        val securityTrailer =
            generateSecurityTrailer(messageHeader, abortTransactionRequest, useTestEnvironment)
        return SaleToPOIRequest.Builder() //
            .messageHeader(messageHeader) //
            .request(abortTransactionRequest) //
            .securityTrailer(securityTrailer) //
            .build()
    }

    private fun handleTransactionResponseMessage(msg: SaleToPOI): Map<String, Boolean> {
        val responseResult: MutableMap<String, Boolean> = HashMap()
        var messageCategory = MessageCategory.Other
        if (msg is SaleToPOIResponse) {
            log(String.format("Response(JSON): %s", msg.toJson()))
            msg.messageHeader
            messageCategory = msg.messageHeader.messageCategory
            var responseBody: Response?
            log("Message Category: $messageCategory")
            when (messageCategory) {
                MessageCategory.Event -> {
                    val eventNotification = msg.eventNotification
                    log("Event Details: " + eventNotification!!.eventDetails)
                }

                MessageCategory.TransactionStatus -> {
                    if (msg.transactionStatusResponse != null
                        && msg.transactionStatusResponse!!.response != null
                    ) {
                        responseBody = msg.transactionStatusResponse!!.response
                        if (responseBody.result != null) {
                            log(
                                String.format(
                                    "Transaction Status Result: %s ",
                                    responseBody.result
                                )
                            )
                            if (responseBody.result == ResponseResult.Success) {
                                var paymentResponseBody: Response? = null
                                if (msg.transactionStatusResponse!!.repeatedMessageResponse != null && msg.transactionStatusResponse!!.repeatedMessageResponse
                                        .repeatedResponseMessageBody != null && msg.transactionStatusResponse!!.repeatedMessageResponse
                                        .repeatedResponseMessageBody.paymentResponse != null
                                ) {
                                    paymentResponseBody = msg.transactionStatusResponse!!
                                        .repeatedMessageResponse.repeatedResponseMessageBody
                                        .paymentResponse.response
                                }
                                if (paymentResponseBody != null) {
                                    log(
                                        String.format(
                                            "Actual Payment Result: %s",
                                            paymentResponseBody.result
                                        )
                                    )
                                    if (paymentResponseBody.errorCondition != null
                                        || paymentResponseBody.additionalResponse != null
                                    ) {
                                        log(
                                            String.format(
                                                "Error Condition: %s, Additional Response: %s",
                                                paymentResponseBody.errorCondition,
                                                paymentResponseBody.additionalResponse
                                            )
                                        )
                                    }
                                }
                                responseResult["GotValidResponse"] = true
                                responseResult["WaitingForAnotherResponse"] = false
                            } else if (responseBody.errorCondition == ErrorCondition.InProgress) {
                                log("Payment in progress...")
                                log(
                                    String.format(
                                        "Error Condition: %s, Additional Response: %s",
                                        responseBody.errorCondition, responseBody.additionalResponse
                                    )
                                )
                                responseResult["BuildAndSendRequestMessage"] = true
                            } else {
                                log(
                                    String.format(
                                        "Error Condition: %s, Additional Response: %s",
                                        responseBody.errorCondition, responseBody.additionalResponse
                                    )
                                )
                                responseResult["GotValidResponse"] = true
                                responseResult["WaitingForAnotherResponse"] = false
                            }
                        }
                    }
                    log("$messageCategory received during Transaction Status response message handling.")
                }

                else -> log("$messageCategory received during Transaction Status response message handling.")
            }
        } else log("Unexpected response message received.")
        return responseResult
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun doRefund(saleID: String, poiID: String, amount: Double, useTestEnvironment:
    Boolean) {
        val serviceID = MessageHeaderUtil.generateServiceID(10)
        val executor = Executors.newSingleThreadExecutor()
        var abortReason = ""
        val refund = executor.submit<Boolean> {
            var refundRequest: SaleToPOIRequest?
            var gotValidResponse = false
            // Payment request
            try {
                refundRequest = buildRefundRequest(saleID, poiID, serviceID,
                    amount, useTestEnvironment)
                log("Sending message to websocket server: \n$refundRequest")
                fusionClient.sendMessage(refundRequest)

                // Wait for response & handle
                var waitingForResponse = true // TODO: timeout handling
                while (waitingForResponse) {
                    val saleToPOI = fusionClient.readMessage() ?: continue
                    if (saleToPOI is SaleToPOIRequest) {
                        handleRequestMessage(saleToPOI)
                        continue
                    }
                    if (saleToPOI is SaleToPOIResponse) {
                        val responseResult: Map<String, Boolean>? =
                            handlePaymentResponseMessage(saleToPOI)
                        waitingForResponse =
                            responseResult?.get("WaitingForAnotherResponse") ?: true
                        if (!waitingForResponse) {
                            gotValidResponse = responseResult?.get("GotValidResponse") ?: false
                        }
                    }
                }
            } catch (e: Exception) {
                log(e)
            } catch (e: FusionException) {
                log(e)
            }
            gotValidResponse
        }
        var gotValidResponse = false
        try {
            gotValidResponse = refund[60, TimeUnit.SECONDS] // set timeout
        } catch (e: TimeoutException) {
            System.err.println("Payment Request Timeout...")
            abortReason = "Timeout"
        } catch (e: ExecutionException) {
            log(String.format("Exception: %s", e.toString()))
            abortReason = "Other Exception"
        } catch (e: InterruptedException) {
            log(String.format("Exception: %s", e.toString()))
            abortReason = "Other Exception"
        } finally {
            executor.shutdownNow()
            if (!gotValidResponse) checkTransactionStatus(saleID, poiID, serviceID, abortReason,
                useTestEnvironment)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun buildRefundRequest(
        saleID: String,
        poiID: String,
        serviceID: String,
        amount: Double,
        useTestEnvironment: Boolean
    ): SaleToPOIRequest? {
        // Refund Request
        // TODO: Change to use the original transactionID
        val saleTransactionID = SaleTransactionID.Builder() //
            .transactionID(
                "transactionID" + SimpleDateFormat("HH:mm:ssXXX").format(Date()).toString()
            )
            .timestamp(Instant.now()).build()
        val saleData = SaleData.Builder() //
            // .operatorID("")//
            .operatorLanguage("en") //
            .saleTransactionID(saleTransactionID) //
            .build()
        val amountsReq = AmountsReq.Builder() //
            .currency("AUD") //
            .requestedAmount(BigDecimal(amount)) //
            .build()
        val paymentInstrumentData: PaymentInstrumentData = PaymentInstrumentData.Builder() //
            .paymentInstrumentType(PaymentInstrumentType.Card) //
            .build()
        val paymentData = PaymentData.Builder() //
            .paymentType(PaymentType.Refund) //
            .paymentInstrumentData(paymentInstrumentData) //
            .build()
        val paymentTransaction = PaymentTransaction.Builder() //
            .amountsReq(amountsReq) //
            .build()
        val refundRequest = PaymentRequest.Builder() //
            .paymentTransaction(paymentTransaction) //
            .paymentData(paymentData) //
            .saleData(saleData).build()

        // Message Header
        val messageHeader =
            MessageHeader.Builder() //
                .messageClass(MessageClass.Service) //
                .messageCategory(MessageCategory.Payment) //
                .messageType(MessageType.Request) //
                .serviceID(serviceID) //
                .saleID(saleID) //
                .POIID(poiID) //
                .build()
        val securityTrailer =
            generateSecurityTrailer(messageHeader, refundRequest, useTestEnvironment)
        return SaleToPOIRequest.Builder() //
            .messageHeader(messageHeader) //
            .request(refundRequest) //
            .securityTrailer(securityTrailer) //
            .build()
    }

    private fun log(ex: java.lang.Exception) {
        log(ex.message)
    }

    private fun log(logData: String?) {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        println(sdf.format(Date(System.currentTimeMillis())) + " " + logData) // 2021.03.24.16.34.26
    }

}
