package com.example.fusion_api_flutter

import com.example.fusion_api_flutter.FusionApiFlutterPlugin.Companion.methodChannel

import au.com.dmg.fusion.response.*
import io.flutter.plugin.common.MethodChannel.Result
import kotlinx.coroutines.*

import android.os.Handler
import android.os.Looper
import android.content.Context

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.squareup.moshi.Json;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import java.lang.IllegalStateException

import au.com.dmg.fusion.MessageHeader;
import au.com.dmg.fusion.SaleToPOI;
import au.com.dmg.fusion.client.FusionClient;
import au.com.dmg.fusion.data.ErrorCondition;
import au.com.dmg.fusion.data.MessageCategory;
import au.com.dmg.fusion.data.PaymentInstrumentType;
import au.com.dmg.fusion.data.PaymentType;
import au.com.dmg.fusion.data.SaleCapability;
import au.com.dmg.fusion.data.TerminalEnvironment;
import au.com.dmg.fusion.data.UnitOfMeasure;
import au.com.dmg.fusion.exception.FusionException;
import au.com.dmg.fusion.request.SaleTerminalData;
import au.com.dmg.fusion.request.SaleToPOIRequest;
import au.com.dmg.fusion.request.aborttransactionrequest.AbortTransactionRequest;
import au.com.dmg.fusion.request.loginrequest.LoginRequest;
import au.com.dmg.fusion.request.logoutrequest.LogoutRequest;
import au.com.dmg.fusion.request.loginrequest.SaleSoftware;
import au.com.dmg.fusion.request.paymentrequest.AmountsReq;
import au.com.dmg.fusion.request.paymentrequest.PaymentData;
import au.com.dmg.fusion.request.paymentrequest.PaymentInstrumentData;
import au.com.dmg.fusion.request.paymentrequest.OriginalPOITransaction;
import au.com.dmg.fusion.request.paymentrequest.PaymentRequest;
import au.com.dmg.fusion.request.paymentrequest.PaymentTransaction;
import au.com.dmg.fusion.request.paymentrequest.SaleData;
import au.com.dmg.fusion.request.paymentrequest.SaleItem;
import au.com.dmg.fusion.request.paymentrequest.SaleTransactionID;
import au.com.dmg.fusion.request.paymentrequest.POITransactionID;
import au.com.dmg.fusion.request.transactionstatusrequest.MessageReference;
import au.com.dmg.fusion.request.transactionstatusrequest.TransactionStatusRequest;
import au.com.dmg.fusion.response.EventNotification;
import au.com.dmg.fusion.response.Response;
import au.com.dmg.fusion.response.SaleToPOIResponse;
import au.com.dmg.fusion.response.TransactionStatusResponse;
import au.com.dmg.fusion.response.paymentresponse.PaymentReceipt;
import au.com.dmg.fusion.response.paymentresponse.PaymentResponse;
import au.com.dmg.fusion.response.paymentresponse.PaymentResult;
import au.com.dmg.fusion.util.MessageHeaderUtil;

class FusionAPIManagerNew(private val fusionClient: FusionClient,private val context: Context) {


    private var executor = Executors.newSingleThreadExecutor()
    private var currentServiceID: String? = null
    private var currentTransaction: MessageCategory = MessageCategory.Login
    private var waitingForResponse = false

    private var secondsRemaining = 0
    private var prevSecond: Long = 0L

    private var loginTimeout = 15000L
    private var paymentTimeout = 15000L // in ms
    private var errorHandlingTimeout = 20000L

    private var saleID: String = ""
    private var poiID: String = ""
    private var kek: String = ""

    //DC5
    private val providerIdentification = "Company A"
    private val applicationName = "POS Retail"
    private val softwareVersion = "01.00.00"
    private val certificationCode = "98cf9dfc-0db7-4a92-8b8cb66d4d2d7169"

    private var abortReason: String = ""
    private var useTestEnvironment: Boolean = true

    @Volatile private var isAbortRequested = false


    fun initFusionClient(
        saleID: String,
        poiID: String,
        kek: String,
        useTestEnvironment: Boolean
    ) {
        this.saleID = saleID
        this.poiID = poiID
        this.kek = kek
        this.useTestEnvironment = useTestEnvironment

        fusionClient.setSettings(saleID, poiID, kek)
        log("FusionClient initialized with SaleID: $saleID, POIID: $poiID")
    }

//    fun initFromCache(result: Result){
//        val prefs = context.getSharedPreferences("fusion_pairing", Context.MODE_PRIVATE)
//        val saleID = prefs.getString("saleID", null)
//        val poiID = prefs.getString("poiID", null)
//        val kek = prefs.getString("kek", null)
//
//        if (saleID != null && poiID != null && kek != null) {
//            fusionClient.setSettings(saleID, poiID, kek)
//            log("Fusion plugin initialized from saved pairing info.")
//            result.success(true)
//        } else {
//            log("No valid pairing info found in cache.")
//            result.success(false)
//        }
//    }

    private fun listen() {
        try {
            prevSecond = computeSecondsRemaining(prevSecond)

            val saleToPOI = fusionClient.readMessage() ?: return
            log("Message Received: \n" + prettyPrintJson(saleToPOI))

            val fmh = FusionMessageHandler()
            var fmr: FusionMessageResponse? = null

            if (saleToPOI is SaleToPOIRequest) {
                fmr = fmh.handle(saleToPOI)

                // DISPLAY SaleToPOIRequest
                notifyDart(
                    type = "displayRequest",
                    status = "info",
                    message = fmr?.displayMessage ?: "Request received"
                )

                // Reset timeout if current transaction is payment
                if (currentTransaction == MessageCategory.Payment) {
                    secondsRemaining = (paymentTimeout / 1000).toInt()
                }
                waitingForResponse = true
            }

            if (saleToPOI is SaleToPOIResponse) {
                // TODO: check service ID
                fmr = fmh.handle(saleToPOI)

                //Ignore response if it's not the current transaction
                if (fmr.messageCategory != currentTransaction && fmr.messageCategory != MessageCategory.Event) {
                    log("Ignoring Response above.. waiting for $currentTransaction, received ${fmr.messageCategory}")
                    return
                }

                when (fmr.messageCategory) {
                    MessageCategory.Event -> { // This only logs an Event
                        val spr = fmr.saleToPOI as? SaleToPOIResponse
                        val eventNotification = spr?.getEventNotification()
                        log("Ignoring Event below...\n" +
                            prettyPrintJson(spr) + "\n" +
                            "Event Details: ${eventNotification?.eventDetails}")
                    }
                    MessageCategory.Login -> {
                        val newPoiID = (fmr.saleToPOI as? SaleToPOIResponse)?.messageHeader?.poiID
                        if (!newPoiID.isNullOrBlank()) { //for qrLogin
                            poiID = newPoiID!!
                            log("✅ Updated POIID to $poiID from messageHeader in login response")
                        }
                        displayLoginResponseMessage(fmr)
                    }
                    MessageCategory.Payment -> {
                        displayPaymentResponseMessage(fmr)
                    }
                    MessageCategory.TransactionStatus -> {
                        handleTransactionResponseMessage(fmr)
                    }
                    MessageCategory.Logout -> {
                        displayLogoutResponseMessage(fmr)
                    }
                    else -> {
                        log("⚠️ Unhandled message category: ${fmr.messageCategory}")
                        }
                }
            }
        } catch (e: FusionException) {
            endLog("Stopped listening to message. Reason:\n ${e.message}")

            if (currentTransaction != MessageCategory.TransactionStatus) {
                println("CURRENT SERVICE ID: $currentServiceID")

                executor.shutdownNow()
                executor = Executors.newSingleThreadExecutor()

                executor.execute {
                    println("go to try catch ...... Websocket connection interrupted")
                    checkTransactionStatus(
                        serviceID = currentServiceID ?: "",
                        abortReason = "Websocket connection interrupted"
                    )
                }
            }
        }
    }

    fun doLogin( qrPairing: Boolean = false) {
        executor.execute {
            try {
                currentServiceID = MessageHeaderUtil.generateServiceID()
                currentTransaction = MessageCategory.Login

                val loginRequest = buildLoginRequest(qrPairing = qrPairing)
                log("Sending login request to WebSocket:\n${prettyPrintJson(loginRequest)}")
                fusionClient.sendMessage(loginRequest, currentServiceID)

                prevSecond = System.currentTimeMillis()
                secondsRemaining = (loginTimeout / 1000).toInt()

                waitingForResponse = true

                while (waitingForResponse) {
                    listen()
                    if (secondsRemaining < 1) {
                        endLog("Login Request Timeout...", true)
                        notifyDart(
                            type = "login",
                            status = "timeout",
                            message = "Login response not received in time"
                        )
                        break
                    }
                }

            } catch (e: IllegalStateException) {
                endLog(e)
                notifyDart(
                    type = "login",
                    status = "fail",
                    message = e.message ?: "Unknown login error"
                )
            }
        }
    }

    fun doPayment(
        transactionID: String,
        items: List<Map<String, Any>>,
        totalAmount: Double,
    )  {
        executor.execute {
            try {
                currentServiceID = MessageHeaderUtil.generateServiceID()
                currentTransaction = MessageCategory.Payment

                val paymentRequest = buildPaymentRequest(
                    transactionID = transactionID,
                    items = items,
                    totalAmount = totalAmount
                )
                log("Sending payment request to WebSocket:\n" + prettyPrintJson(paymentRequest))
                fusionClient.sendMessage(paymentRequest, currentServiceID)

                prevSecond = System.currentTimeMillis()
                secondsRemaining = (paymentTimeout / 1000).toInt()

                waitingForResponse = true
                while (waitingForResponse) {
                    //handle do abort from dart, only send msg and expect payment response
                    if (isAbortRequested) {
                        isAbortRequested = false
                        val abortRequest =
                            buildAbortRequest(currentServiceID ?: "", "User Cancelled")
                        log("🔴 Sending AbortRequest...")
                        fusionClient.sendMessage(abortRequest)
                    }

                    listen()
                    if (secondsRemaining < 1) { //handle time out here
                        notifyDart(
                                type = "timeout",
                                status = "timeout",
                                message = "Payment timed out. Checking TX status"
                        )
                        abortReason = "Timeout"
                        endLog("Payment Request Timeout...", true)
                        checkTransactionStatus(//check tx and abort
                            serviceID = currentServiceID ?: "",
                            abortReason = abortReason
                        )
                        break
                    }
//                    if (!waitingForResponse) {
//                        result.success("Payment completed")
//                    }
                }

            } catch (e: IllegalStateException) {
                abortReason = "Other Exception"
                endLog("Exception: ${e.message}", true)
                checkTransactionStatus(
                    serviceID = currentServiceID ?: "",
                    abortReason = abortReason
                )
//                result.error("CONFIGURATION_EXCEPTION", e.message, null)

            } catch (e: FusionException) {
                endLog("FusionException: ${e.message}. Resending the Request...", true)
                // Continue the timer
                paymentTimeout = secondsRemaining * 1000L
                doPayment(transactionID, items, totalAmount)
            }
        }
    }

    fun doAbort(
        abortReason: String
    ) {
            // TODO: notify Dart layer - transaction is being aborted
            println("TODO: Show abortReason: $abortReason on UI")
            println("TODO: Show 'ABORTING TRANSACTION' on UI")

            val abortRequest = buildAbortRequest(currentServiceID ?: "", abortReason)
            log("Sending abort message to WebSocket server:\n${prettyPrintJson(abortRequest)}")

            fusionClient.sendMessage(abortRequest)
    }

    fun doAbortFromDart(abortReason: String, result: Result) {
        //just for isAbortRequested, no need new thread
        log("🛑 Received abort request from Dart. Terminating doPayment...")
        isAbortRequested = true // ✅ 通知 doPayment 退出监听
        result.success("Abort intent set")

    }


    fun doRefund(
        transactionID: String,
        items: List<Map<String, Any>>, // 结构同 payment 可复用
        refundAmount: Double,
        originalSaleID: String,
        originalPOIID: String,
        originalPOITransactionID: String,
        originalPOITransactionTime:String,
    ) {
        executor.execute {
            try {
                currentServiceID = MessageHeaderUtil.generateServiceID()
                currentTransaction = MessageCategory.Payment

                val refundRequest = buildRefundRequest(
                    transactionID = transactionID,
                    items = items,
                    refundAmount = refundAmount,
                    originalSaleID = originalSaleID,
                    originalPOIID = originalPOIID,
                    originalPOITransactionID = originalPOITransactionID,
                    originalPOITransactionTime =originalPOITransactionTime
                )

                log("Sending refund request to WebSocket:\n${prettyPrintJson(refundRequest)}")
                fusionClient.sendMessage(refundRequest, currentServiceID)

                prevSecond = System.currentTimeMillis()
                secondsRemaining = (paymentTimeout / 1000).toInt()

                waitingForResponse = true
                while (waitingForResponse) {
                    // handle refund abort
                    if (isAbortRequested) {
                        isAbortRequested = false
                        val abortRequest =
                                buildAbortRequest(currentServiceID ?: "", "User Cancelled")
                        log("🔴 Sending AbortRequest...")
                        fusionClient.sendMessage(abortRequest)
                    }

                    listen()
                    if (secondsRemaining < 1) {
                        notifyDart(
                                type = "timeout",
                                status = "timeout",
                                message = "Refund timed out. Checking TX status"
                        )
                        abortReason = "Timeout"
                        endLog("Refund Request Timeout...", true)
                        checkTransactionStatus(
                            serviceID = currentServiceID ?: "",
                            abortReason = abortReason
                        )
                        break
                    }
                }
            } catch (e: IllegalStateException) {
                abortReason = "Other Exception"
                endLog("Exception: ${e.message}", true)
                checkTransactionStatus(currentServiceID ?: "", abortReason)

            } catch (e: FusionException) {
                endLog("FusionException: ${e.message}. Resending the Request...", true)
                paymentTimeout = secondsRemaining * 1000L
                doRefund(
                    transactionID,
                    items,
                    refundAmount,
                    originalSaleID,
                    originalPOIID,
                    originalPOITransactionID,
                    originalPOITransactionTime,
                )
            }
        }
    }


    @Throws(IllegalStateException::class)
    private fun buildLoginRequest(qrPairing: Boolean = false): LoginRequest {
        val saleSoftware = SaleSoftware.Builder()
            .providerIdentification(providerIdentification)
            .applicationName(applicationName)
            .softwareVersion(softwareVersion)
            .certificationCode(certificationCode)
            .build()

        val saleTerminalData = SaleTerminalData.Builder()
            .terminalEnvironment(TerminalEnvironment.SemiAttended)
            .saleCapabilities(
                listOf(
                    SaleCapability.CashierStatus,
                    SaleCapability.CustomerAssistance,
                    SaleCapability.PrinterReceipt
                )
            )
            .build()

        return LoginRequest.Builder()
            .dateTime(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(Date()))
            .saleSoftware(saleSoftware)
            .saleTerminalData(saleTerminalData)
            .operatorLanguage("en")
            .pairing(qrPairing)
            .build()
    }

    fun doLogout(result: Result) {
        executor.execute {
            try {
                currentServiceID = MessageHeaderUtil.generateServiceID()
                currentTransaction = MessageCategory.Logout

                val logoutRequest = LogoutRequest()
                log("Sending logout request to WebSocket:\n${prettyPrintJson(logoutRequest)}")

                fusionClient.sendMessage(logoutRequest, currentServiceID)

                prevSecond = System.currentTimeMillis()
                secondsRemaining = (loginTimeout / 1000).toInt()
                waitingForResponse = true

                while (waitingForResponse) {
                    listen()
                    if (secondsRemaining < 1) {
                        result.error("LOGOUT_TIMEOUT", "Logout response timeout", null)
                        break
                    }
                }

                if (!waitingForResponse) {
                    result.success("Logout completed")
                }
            } catch (e: Exception) {
                endLog("Logout failed: ${e.message}")
                result.error("LOGOUT_FAILED", e.message, null)
            }
        }
    }


    private fun computeSecondsRemaining(start: Long): Long {
        val currentTime = System.currentTimeMillis()
        val sec = (currentTime - start) / 1000
        if (sec == 1L) {
            secondsRemaining--
            // TODO: send time remaining to Dart
            println("TODO... 每秒更新 UI: 剩余 $secondsRemaining 秒")
            return currentTime
        }
        return start
    }

    fun checkTransactionStatus(serviceID: String, abortReason: String) { //only for error handling
        try {
            currentTransaction = MessageCategory.TransactionStatus
            // if any abort reason，do abort first, only one case just check tx
            if (abortReason.isNotEmpty()) {
                println("......DODODODODO Abort")
                doAbort(abortReason)
            }

            println("......CHECKING TRANSACTION STATUS")

            val request = buildTransactionStatusRequest(serviceID)
            log("Sending transaction status request to check status of payment...\n" + prettyPrintJson(request))
            fusionClient.sendMessage(request)

            // ✅ set timeout 90s
            prevSecond = System.currentTimeMillis()
            secondsRemaining = (errorHandlingTimeout / 1000).toInt()
            waitingForResponse = true

            while (waitingForResponse) {
                listen()
                if (secondsRemaining < 1) {
                    println("......Time Out")
                    println(".....Please check Satellite Transaction History")
                    notifyDart(
                        type = "transactionStatus",
                        status = "timeout",
                        message = "Transaction status request timed out. Please check Satellite Transaction History",
                        data = mapOf("serviceID" to serviceID)
                    )
                    endLog("Transaction Status Request Timeout...", stopWaiting = true)
                    break
                }
            }

        } catch (e: IllegalStateException) {
//            endTransactionUi()
            endLog(e)
        }
    }


    @Throws(IllegalStateException::class)
    fun buildPaymentRequest(
        transactionID: String,
        items: List<Map<String, Any>>,
        totalAmount: Double
    ): PaymentRequest {
        val saleTransactionID = SaleTransactionID.Builder()
            .transactionID(transactionID)
            .timestamp(Instant.now())
            .build()

        val saleData = SaleData.Builder()
            .operatorLanguage("en")
            .saleTransactionID(saleTransactionID)
            .build()

        val amountsReq = AmountsReq.Builder()
            .currency("AUD")
            .requestedAmount(BigDecimal.valueOf(totalAmount))
            .build()

        val saleItems = items.mapIndexed { index, item ->
            SaleItem.Builder()
                .itemID(index)
                .productCode(item["productCode"] as String)
                .unitOfMeasure(UnitOfMeasure.Other)
                .quantity(BigDecimal((item["quantity"] as Number).toDouble()))
                .unitPrice(BigDecimal((item["unitPrice"] as Number).toDouble()))
                .itemAmount(BigDecimal((item["itemAmount"] as Number).toDouble()))
                .productLabel(item["productLabel"] as String)
                .build()
        }

        val paymentTransaction = PaymentTransaction.Builder()
            .amountsReq(amountsReq)
            .apply {
                saleItems.forEach { addSaleItem(it) }
            }
            .build()

        val paymentInstrumentData = PaymentInstrumentData.Builder()
            .paymentInstrumentType(PaymentInstrumentType.Card)
            .build()

        val paymentData = PaymentData.Builder()
            .paymentType(PaymentType.Normal)
            .paymentInstrumentData(paymentInstrumentData)
            .build()

        return PaymentRequest.Builder()
            .paymentTransaction(paymentTransaction)
            .paymentData(paymentData)
            .saleData(saleData)
            .build()
    }

    private fun buildAbortRequest(referenceServiceID: String, abortReason: String): AbortTransactionRequest {
        val messageReference = MessageReference.Builder()
            .messageCategory(MessageCategory.Abort)
            .serviceID(referenceServiceID)
            .build()

        return AbortTransactionRequest(messageReference, abortReason)
    }

    @Throws(IllegalStateException::class)
    private fun buildRefundRequest(
        transactionID: String,
        items: List<Map<String, Any>>,
        refundAmount: Double,
        originalSaleID: String,
        originalPOIID: String,
        originalPOITransactionID: String,
        originalPOITransactionTime: String
    ): PaymentRequest {
        val saleTransactionID = SaleTransactionID.Builder()
            .transactionID(transactionID)
            .timestamp(Instant.now())
            .build()

        val saleData = SaleData.Builder()
            .saleTransactionID(saleTransactionID)
            .operatorLanguage("en")
            .build()

        val amountsReq = AmountsReq.Builder()
            .currency("AUD")
            .requestedAmount(BigDecimal.valueOf(refundAmount))
            .build()

        val paymentInstrumentData = PaymentInstrumentData.Builder()
            .paymentInstrumentType(PaymentInstrumentType.Card)
            .build()

        val originalTransaction = OriginalPOITransaction.Builder()
            .saleID(originalSaleID)
            .POIID(originalPOIID)
            .POITransactionID(
                POITransactionID(
                    originalPOITransactionID,
                    Instant.parse(originalPOITransactionTime)
                )
            )
            .build()

        val saleItems = items.mapIndexed { index, item ->
            SaleItem.Builder()
                .itemID(index)
                .productCode(item["productCode"] as String)
                .unitOfMeasure(UnitOfMeasure.Other)
                .quantity(BigDecimal((item["quantity"] as Number).toDouble()))
                .unitPrice(BigDecimal((item["unitPrice"] as Number).toDouble()))
                .itemAmount(BigDecimal((item["itemAmount"] as Number).toDouble()))
                .productLabel(item["productLabel"] as String)
                .build()
        }

        val paymentData = PaymentData.Builder()
            .paymentType(PaymentType.Refund)
            .paymentInstrumentData(paymentInstrumentData)
            .build()

        // 6. 构造 PaymentTransaction（不一定包含 SaleItem）
        val paymentTransaction = PaymentTransaction.Builder()
            .amountsReq(amountsReq)
            .originalPOITransaction(originalTransaction)
            .apply {
                saleItems.forEach { addSaleItem(it) }
            }
            .build()

        return PaymentRequest.Builder()
            .paymentTransaction(paymentTransaction)
            .paymentData(paymentData)
            .saleData(saleData)
            .build()
    }


    private fun buildTransactionStatusRequest(serviceID: String): TransactionStatusRequest {
        val messageReference = MessageReference.Builder()
            .messageCategory(MessageCategory.Payment)
            .POIID(poiID)
            .saleID(saleID)
            .serviceID(serviceID)
            .build()

        return TransactionStatusRequest(messageReference)
    }

//    private fun savePairingInfo(context: Context, saleID: String, poiID: String, kek: String) {
//        val prefs = context.getSharedPreferences("fusion_pairing", Context.MODE_PRIVATE)
//
//        prefs.edit().apply {
//            putString("saleID", saleID)
//            putString("poiID", poiID)
//            putString("kek", kek)
//            apply()
//        }
//        println("✅ Saved Pairing Info → saleID: $saleID, poiID: $poiID, kek: $kek")
//
//    }

    // Placeholder methods for displaying responses
    private fun displayLoginResponseMessage(fmr: FusionMessageResponse) {
        println("...Login response: ${fmr.displayMessage}")

        val saleID = (fmr.saleToPOI as? SaleToPOIResponse)?.messageHeader?.saleID
        val poiID = (fmr.saleToPOI as? SaleToPOIResponse)?.messageHeader?.poiID

        val data = mapOf(
            "saleID" to saleID,
            "poiID" to poiID,
            "kek" to kek
        )
        // ✅ 添加本地保存逻辑
//        if (fmr.isSuccessful == true && saleID != null && poiID != null) {
//            savePairingInfo(context, saleID, poiID, kek)
//        }
        notifyDart(
            type = "login",
            status = if (fmr.isSuccessful == true) "success" else "fail",
            message = fmr.displayMessage ?: "No message",
            data = data
        )

        waitingForResponse = false

    }

    private fun displayPaymentResponseMessage(fmr: FusionMessageResponse) {
        val saleToPOI = fmr.saleToPOI as? SaleToPOIResponse ?: return
        val header = saleToPOI.messageHeader ?: return
        val paymentResponse = saleToPOI.paymentResponse ?: return
        val poiData = paymentResponse.poiData ?: return
        val poiTx   = poiData.poiTransactionID ?: return

        val data = mapOf(
            "MessageHeader" to mapOf(
                "SaleID" to header.saleID,
                "POIID"  to header.poiID
            ),
            "PaymentResponse" to mapOf(
                "POIData" to mapOf(
                    "POITransactionID" to mapOf(
                        "TransactionID" to poiTx.transactionID,
                        "TimeStamp"     to poiTx.timestamp.toString()
                    )
                )
            )
        )
        /**
         *
         * {
         *   "MessageHeader": {
         *     "SaleID": "xxx",
         *     "POIID":  "yyy"
         *   },
         *   "PaymentResponse": {
         *     "POIData": {
         *       "POITransactionID": {
         *         "TransactionID": "ttt",
         *         "TimeStamp":     "sss"
         *       }
         *     }
         *   }
         * }
         */
        notifyDart(
            type = "payment",
            status = if (fmr.isSuccessful == true) "success" else "fail",
            message = fmr.displayMessage ?: "No display message",
            data = data
        )

        waitingForResponse = false
    }

    private fun displayPaymentResponseMessage(
        pr: PaymentResponse,
        mh: MessageHeader
    ) {
        val result = pr.response?.result
        val paymentResult = pr.paymentResult ?: return

        val receiptHtml = pr.paymentReceipt?.getOrNull(0)?.receiptContentAsHtml
        println("🧾 Receipt (HTML):\n$receiptHtml")

        notifyDart(
                type = "payment",
                status = result?.name?.lowercase() ?: "unknown",
                message = "Payment result received",
                data = mapOf(
                        "authorizedAmount" to (paymentResult.amountsResp?.authorizedAmount ?: 0),
                        "tipAmount" to (paymentResult.amountsResp?.tipAmount ?: 0),
                        "surchargeAmount" to (paymentResult.amountsResp?.surchargeAmount ?: 0),
                        "maskedPAN" to (paymentResult.paymentInstrumentData?.cardData?.maskedPAN ?: ""),
                        "paymentBrand" to (paymentResult.paymentInstrumentData?.cardData?.paymentBrand ?: ""),
                        "entryMode" to (paymentResult.paymentInstrumentData?.cardData?.entryMode ?: ""),
                        "serviceID" to (mh.serviceID ?: ""),
                        "receiptHtml" to (receiptHtml ?: "")
                )
        )

        waitingForResponse = false
    }

    private fun displayTransactionResponseMessage(
        errorCondition: ErrorCondition?,
        additionalResponse: String?
    ) {
        val message = "${errorCondition?.name} - $additionalResponse"
        println("❌ Transaction Response Error: $message")

        waitingForResponse = false
    }

    private fun displayLogoutResponseMessage(fmr: FusionMessageResponse) {
        log("Logout response received.")
        waitingForResponse = false
    }

    private fun handleTransactionResponseMessage(fmr: FusionMessageResponse) {
        if (fmr.isSuccessful == true) {
            val tsr = (fmr.saleToPOI as? SaleToPOIResponse)?.transactionStatusResponse
            val response = tsr?.response
            log("Transaction Status Result: ${response?.result}")

            val paymentResponse = tsr?.repeatedMessageResponse?.repeatedResponseMessageBody?.paymentResponse
            val messageHeader = tsr?.repeatedMessageResponse?.messageHeader

            if (paymentResponse != null && messageHeader != null) {
                displayPaymentResponseMessage(paymentResponse, messageHeader)
            }

        } else if (fmr.errorCondition == ErrorCondition.InProgress) {
            log("Transaction still in progress...")

            if (secondsRemaining > 5) {
                errorHandlingTimeout = (secondsRemaining - 5) * 1000L
                log("Retrying transaction status in 5 seconds... \n Remaining seconds: $secondsRemaining")

                try {
                    Thread.sleep(5_000)
                    println("/////////sleep 5s!!!!!!!!")
                    val id = currentServiceID
                    if (id != null) {
                        val request = buildTransactionStatusRequest(id)
                        log("Sending transaction status request to check status of payment...\n" + prettyPrintJson(request))
                        fusionClient.sendMessage(request)
                    } else {
                        log("❌ currentServiceID is null, cannot send TX status request")
                    }

                } catch (e: InterruptedException) {
                    endLog(e)
                }
            }

        } else {
            val tsr = (fmr.saleToPOI as? SaleToPOIResponse)?.transactionStatusResponse
            val response = tsr?.response

            val err = response?.errorCondition
            val msg = response?.additionalResponse

            endLog("Error Condition: $err, Additional Response: $msg", stopWaiting = true)
            displayTransactionResponseMessage(err, msg)
        }
    }

    private fun startTransactionUi() {
        println("。。。UI START: ${currentTransaction?.name} in progress")

        if (currentTransaction == MessageCategory.Payment) {
            println("TODO：Show cancel button (future UI)")

            // TODO: 将来这里可以发送事件通知 Dart 层显示取消按钮
            // Example:
            // methodChannel.invokeMethod("showCancelButton", null)
        }

//         ✅ 用主线程执行 MethodChannel 的调用
        Handler(Looper.getMainLooper()).post {
            methodChannel.invokeMethod("transactionInProgress", mapOf(
                "type" to currentTransaction?.name
            ))
        }

//        println("Show progress circle")
//        println("Header: ${currentTransaction?.name?.uppercase()} IN PROGRESS")
//        println("Detail: ...")
//        println("Disable login/purchase/refund buttons")
    }

    private fun endTransactionUi() {
//        println("🔴 [UI END] Transaction finished")
        Handler(Looper.getMainLooper()).post {
            methodChannel.invokeMethod("transactionEnded", null)}
//        println("🟢 Hide progress circle and cancel button")
//        println("✅ Re-enable all buttons (login, purchase, refund)")
    }


    private fun endLog(message: String?, stopWaiting: Boolean = true) {
        log(message)
        // TODO: 显示到 UI（比如通过 MethodChannel 发回 Dart）
        println("TODO........Display Message:$message")
        if (stopWaiting) {
            waitingForResponse = false
        }
    }

    private fun endLog(e: Exception) {
        endLog(e.message, true)
    }

    private fun log(message: String?) {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        println("${sdf.format(Date())} $message")
    }

    private fun log(ex: Exception) {
        log(ex.message)
    }

//    private fun clearLog() {
//        // TODO: 通知 Dart 层清除 UI 日志区域（如果需要）
//        println("TODO: clearLog called")
//    }

    private fun prettyPrintJson(obj: Any?): String {
        val gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
        return gson.toJson(obj)
    }

    private fun notifyDart(
        type: String,
        status: String,
        message: String,
        data: Map<String, Any?>? = null
    ) {
        val payload = mutableMapOf<String, Any?>(
            "type" to type,
            "status" to status,
            "message" to message
        )
        if (data != null) {
            payload["data"] = data.safeToDart()
        }

        Handler(Looper.getMainLooper()).post {
            methodChannel.invokeMethod("onPluginEvent", payload)
        }
    }
    private fun Any?.safeToDart(): Any? {
        return when (this) {
            is BigDecimal -> this.toDouble()
            is Enum<*> -> this.name // 👍 support enum
            is Map<*, *> -> this.mapValues { it.value.safeToDart() }
            is List<*> -> this.map { it.safeToDart() }
            else -> this
        }
    }


}
