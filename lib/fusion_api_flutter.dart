import 'fusion_api_flutter_platform_interface.dart';

class FusionApiFlutter {
  Future<String?> getPlatformVersion() {
    return FusionApiFlutterPlatform.instance.getPlatformVersion();
  }

  Future<String> init({
    required String saleID,
    required String poiID,
    required String kek,
    required bool useTestEnvironment,
  }) async {
    return FusionApiFlutterPlatform.instance.init(
      saleID: saleID,
      poiID: poiID,
      kek: kek,
      useTestEnvironment: useTestEnvironment,
    );
  }

  // Future<bool> initFromCache() {
  //   return FusionApiFlutterPlatform.instance.initFromCache();
  // }

  Future<String> login({bool qrPairing = false}) {
    return FusionApiFlutterPlatform.instance.login(qrPairing: qrPairing);
  }

  // Future<bool> logout(String saleID, String poiID, bool useTestEnvironment) {
  //   return FusionApiFlutterPlatform.instance
  //       .logout(saleID, poiID, useTestEnvironment);
  // }
  Future<String> logout() {
    return FusionApiFlutterPlatform.instance.logout();
  }


  Future<void> doPayment(
      String transactionID,
      List<Map<String, dynamic>> items,
      double totalAmount,
      ) {
    return FusionApiFlutterPlatform.instance.doPayment(
      transactionID,
      items,
      totalAmount,
    );
  }

  Future<void> doRefund({
    required String transactionID,
    required List<Map<String, dynamic>> items,
    required double refundAmount,
    required String originalSaleID,
    required String originalPOIID,
    required String originalPOITransactionID,
    required String originalPOITransactionTime,
  }) {
    return FusionApiFlutterPlatform.instance.doRefund(
      transactionID: transactionID,
      items: items,
      refundAmount: refundAmount,
      originalSaleID: originalSaleID,
      originalPOIID: originalPOIID,
      originalPOITransactionID: originalPOITransactionID,
      originalPOITransactionTime: originalPOITransactionTime,
    );
  }

  Future<String> doAbort() {
    return FusionApiFlutterPlatform.instance.doAbort();
  }
}
