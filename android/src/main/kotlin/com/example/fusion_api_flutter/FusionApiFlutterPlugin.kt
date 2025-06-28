package com.example.fusion_api_flutter

import com.example.fusion_api_flutter.FusionAPIManager
import com.example.fusion_api_flutter.FusionAPIManagerNew
import au.com.dmg.fusion.client.FusionClient
import android.content.Context

import android.os.Build
import androidx.annotation.RequiresApi
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.EventChannel

import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord

class CustomLogHandler(private val logCallback: (String) -> Unit) : Handler() {

    override fun publish(record: LogRecord) {
        val logMessage = "${record.level}: ${record.message}"
        logCallback(logMessage)
    }

    override fun flush() {
        // No-op
    }

    override fun close() {
        // No-op
    }
}
/** FusionApiFlutterPlugin */
class FusionApiFlutterPlugin : FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var context: Context
    private lateinit var channel: MethodChannel
    private lateinit var fusionManager: FusionAPIManagerNew
    private var fusionClient: FusionClient = FusionClient(true)

    private lateinit var eventChannel: EventChannel
    private var eventSink: EventChannel.EventSink? = null

    companion object {
        lateinit var methodChannel: MethodChannel

        var eventSink: EventChannel.EventSink? = null
        fun sendEvent(payload: Map<String, Any?>) {
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                eventSink?.success(payload)
            }
        }
    }

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "fusion_api_flutter")
        methodChannel = channel
        channel.setMethodCallHandler(this)
        fusionManager = FusionAPIManagerNew(fusionClient, context)

        eventChannel = EventChannel(flutterPluginBinding.binaryMessenger, "fusion_plugin/events")
        eventChannel.setStreamHandler(object : EventChannel.StreamHandler {
            override fun onListen(arguments: Any?, sink: EventChannel.EventSink?) {
                eventSink = sink
            }

            override fun onCancel(arguments: Any?) {
                eventSink = null
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "getPlatformVersion" -> {
                result.success("Android ${Build.VERSION.RELEASE}")
            }
            "init" -> {
                fusionManager.initFusionClient(
                        call.argument("saleID")!!,
                        call.argument("poiID")!!,
                        call.argument("kek")!!,
                        call.argument<Boolean>("useTestEnvironment")!!
                )
                result.success("FusionClient Initialized")
            }
//            "initFromCache" -> {
//                fusionManager.initFromCache(
//                        result
//                )
//            }
            "login" ->{
                fusionManager.doLogin(
                        call.argument<Boolean>("qrPairing") ?: false
                )
                result.success("started")
            }
            // "logout" -> {
            //     fusionManager.logout(
            //             call.argument("saleID")!!,
            //             call.argument("poiID")!!,
            //             call.argument("useTestEnvironment")!!,
            //             result
            //     )
            // }
            "logout" -> {
                fusionManager.doLogout(
                        result
                )
            }
            "doPayment" -> {
                fusionManager.doPayment(
                        call.argument("transactionID")!!,
                        call.argument("items")!!,
                        call.argument("totalAmount")!!,
                )
            }
            "doRefund" -> {
                fusionManager.doRefund(
                        call.argument<String>("transactionID")!!,
                        call.argument<List<Map<String, Any>>>("items")!!,
                        call.argument<Double>("refundAmount")!!,
                        call.argument<String>("originalSaleID")!!,
                        call.argument<String>("originalPOIID")!!,
                        call.argument<String>("originalPOITransactionID")!!,
                        call.argument<String>("originalPOITransactionTime")!!,
                )
            }
            "doUnmatchedRefund" -> {
                fusionManager.doUnmatchedRefund(
                    call.argument<String>("transactionID")!!,
                    call.argument<List<Map<String, Any>>>("items")!!,
                    call.argument<Double>("refundAmount")!!,
                )
            }
            "doAbort" -> {
                fusionManager.doAbortFromDart(
                        "User Cancel",
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
        eventSink = null

    }

}
