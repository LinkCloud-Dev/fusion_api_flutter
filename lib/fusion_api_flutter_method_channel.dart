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

  // TODO: Call platform specific functions
  @override
  Future<bool> mannualLogin(
      String saleID,
      String poiID,
      String providerIdentification,
      String applicationName,
      String softwareVersion,
      String certificationCode,
      bool useTestEnvironment) async {
    // TODO: implement login
    final response = await methodChannel.invokeMethod('mannualLogin', {
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
  Future<String> qrLogin(
      String saleID,
      String poiID,
      String providerIdentification,
      String applicationName,
      String softwareVersion,
      String certificationCode,
      bool useTestEnvironment) async {
    // TODO: implement login
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
    // TODO: implement login
    final response = await methodChannel.invokeMethod('logout', {
      "saleID": saleID,
      "poiID": poiID,
      "useTestEnvironment": useTestEnvironment,
    });
    return response;
  }

  // TODO: Fix the hardcoded doPayment
  @override
  Future<void> doPayment(String saleID, String poiID,
      List<Map<String, dynamic>> items, bool useTestEnvironment) async {
    methodChannel.invokeMethod('doPayment', {
      "saleID": saleID,
      "poiID": poiID,
      "items": items,
      "useTestEnvironment": useTestEnvironment,
    });
  }

  @override
  Future<void> doRefund(String saleID, String poiID, double amount,
      bool useTestEnvironment) async {
    methodChannel.invokeMethod('doRefund', {
      "saleID": saleID,
      "poiID": poiID,
      "amount": amount,
      "useTestEnvironment": useTestEnvironment,
    });
  }
}
