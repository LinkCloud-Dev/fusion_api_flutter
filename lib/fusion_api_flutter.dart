import 'fusion_api_flutter_platform_interface.dart';

class FusionApiFlutter {
  Future<String?> getPlatformVersion() {
    return FusionApiFlutterPlatform.instance.getPlatformVersion();
  }

  Future<void> init(String saleID, String poiID, String kek) async {
    return FusionApiFlutterPlatform.instance.init(saleID, poiID, kek);
  }

  /// Returns a map with the new poiID and the login result
  ///
  /// Eg. {"newPoiID": "12345678", "loginResult": "Success"}
  /// loginResult can be "Success", "Failure"
  Future<Map<dynamic, dynamic>> qrLogin(
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

  /// Returns manual login result
  Future<bool> manualLogin(
      String saleID,
      String poiID,
      String providerIdentification,
      String applicationName,
      String softwareVersion,
      String certificationCode,
      bool useTestEnvironment) {
    return FusionApiFlutterPlatform.instance.manualLogin(
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

  /// Returns a map with the payment result
  ///
  /// Eg. {GotValidResponse=true, WaitingForAnotherResponse=false, Result=false}
  Future<Map<dynamic, dynamic>> doPayment(
      String saleID,
      String poiID,
      String transactionID,
      List<Map<String, dynamic>> items,
      double totalAmount,
      bool useTestEnvironment) {
    return FusionApiFlutterPlatform.instance.doPayment(
        saleID, poiID, transactionID, items, totalAmount, useTestEnvironment);
  }

  /// Returns a map with the refund result
  ///
  /// [transactionID] is optional. (Support both matched and unmatched refund)
  /// Return value Eg. {GotValidResponse=true, WaitingForAnotherResponse=false, Result=false}
  Future<Map<dynamic, dynamic>> doRefund(String saleID, String poiID,
      double amount, String? transactionID, bool useTestEnvironment) {
    return FusionApiFlutterPlatform.instance
        .doRefund(saleID, poiID, amount, transactionID, useTestEnvironment);
  }
}
