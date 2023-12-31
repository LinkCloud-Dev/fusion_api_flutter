import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'fusion_api_flutter_platform_interface.dart';

/// An implementation of [FusionApiFlutterPlatform] that uses method channels.
class MethodChannelFusionApiFlutter extends FusionApiFlutterPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('fusion_api_flutter');

  @override
  Future<String?> getPlatformVersion() async {
    final version =
        await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<void> init(String saleID, String poiID, String kek) async {
    methodChannel
        .invokeMethod('init', {"saleID": saleID, "poiID": poiID, "kek": kek});
  }

  @override
  Future<bool> manualLogin(
      String saleID,
      String poiID,
      String providerIdentification,
      String applicationName,
      String softwareVersion,
      String certificationCode,
      bool useTestEnvironment) async {
    final response = await methodChannel.invokeMethod('manualLogin', {
      "saleID": saleID,
      "poiID": poiID,
      "providerIdentification": providerIdentification,
      "applicationName": applicationName,
      "softwareVersion": softwareVersion,
      "certificationCode": certificationCode,
      "useTestEnvironment": useTestEnvironment,
    });
    return response;
  }

  @override
  Future<Map<dynamic, dynamic>> qrLogin(
      String saleID,
      String poiID,
      String providerIdentification,
      String applicationName,
      String softwareVersion,
      String certificationCode,
      bool useTestEnvironment) async {
    final response = await methodChannel.invokeMethod('qrLogin', {
      "saleID": saleID,
      "poiID": poiID,
      "providerIdentification": providerIdentification,
      "applicationName": applicationName,
      "softwareVersion": softwareVersion,
      "certificationCode": certificationCode,
      "useTestEnvironment": useTestEnvironment,
    });
    return response;
  }

  @override
  Future<bool> logout(
      String saleID, String poiID, bool useTestEnvironment) async {
    final response = await methodChannel.invokeMethod('logout', {
      "saleID": saleID,
      "poiID": poiID,
      "useTestEnvironment": useTestEnvironment,
    });
    return response;
  }

  @override
  Future<Map<dynamic, dynamic>> doPayment(
      String saleID,
      String poiID,
      String transactionID,
      List<Map<String, dynamic>> items,
      double totalAmount,
      bool useTestEnvironment) async {
    final response = await methodChannel.invokeMethod('doPayment', {
      "saleID": saleID,
      "poiID": poiID,
      "transactionID": transactionID,
      "items": items,
      "totalAmount": totalAmount,
      "useTestEnvironment": useTestEnvironment,
    });
    return response;
  }

  @override
  Future<Map<dynamic, dynamic>> doRefund(String saleID, String poiID,
      double amount, String? transactionID, bool useTestEnvironment) async {
    final response = await methodChannel.invokeMethod('doRefund', {
      "saleID": saleID,
      "poiID": poiID,
      "amount": amount,
      "transactionID": transactionID,
      "useTestEnvironment": useTestEnvironment,
    });
    return response;
  }
}
