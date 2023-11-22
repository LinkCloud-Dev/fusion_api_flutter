import 'package:flutter/services.dart';

import 'fusion_api_flutter_platform_interface.dart';

class FusionApiFlutter {
  static const MethodChannel _channel = MethodChannel("fusion_api_flutter");

  Future<String?> getPlatformVersion() {
    return FusionApiFlutterPlatform.instance.getPlatformVersion();
  }

  Future<void> init(String saleID, String poiID, String kek) async {
    return FusionApiFlutterPlatform.instance.init(saleID, poiID, kek);
  }

  Future<String> qrLogin(
      String saleID,
      String poiID,
      String providerIdentification,
      String applicationName,
      String softwareVersion,
      String certificationCode,
      bool useTestEnvironment) {
    return FusionApiFlutterPlatform.instance.qrLogin(
        saleID,
        poiID,
        providerIdentification,
        applicationName,
        softwareVersion,
        certificationCode,
        useTestEnvironment);
  }

  Future<bool> mannualLogin(
      String saleID,
      String poiID,
      String providerIdentification,
      String applicationName,
      String softwareVersion,
      String certificationCode,
      bool useTestEnvironment) {
    return FusionApiFlutterPlatform.instance.mannualLogin(
        saleID,
        poiID,
        providerIdentification,
        applicationName,
        softwareVersion,
        certificationCode,
        useTestEnvironment);
  }

  Future<bool> logout(String saleID, String poiID, bool useTestEnvironment) {
    return FusionApiFlutterPlatform.instance
        .logout(saleID, poiID, useTestEnvironment);
  }

  Future<void> doPayment(String saleID, String poiID,
      List<Map<String, dynamic>> items, bool useTestEnvironment) {
    return FusionApiFlutterPlatform.instance
        .doPayment(saleID, poiID, items, useTestEnvironment);
  }

  Future<void> doRefund(
      String saleID, String poiID, double amount, bool useTestEnvironment) {
    return FusionApiFlutterPlatform.instance
        .doRefund(saleID, poiID, amount, useTestEnvironment);
  }
}
