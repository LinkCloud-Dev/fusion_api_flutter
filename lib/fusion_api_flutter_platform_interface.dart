import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'fusion_api_flutter_method_channel.dart';

abstract class FusionApiFlutterPlatform extends PlatformInterface {
  /// Constructs a FusionApiFlutterPlatform.
  FusionApiFlutterPlatform() : super(token: _token);

  static final Object _token = Object();

  static FusionApiFlutterPlatform _instance = MethodChannelFusionApiFlutter();

  /// The default instance of [FusionApiFlutterPlatform] to use.
  ///
  /// Defaults to [MethodChannelFusionApiFlutter].
  static FusionApiFlutterPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FusionApiFlutterPlatform] when
  /// they register themselves.
  static set instance(FusionApiFlutterPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    // throw UnimplementedError('platformVersion() has not been implemented.');
    return _instance.getPlatformVersion();
  }

  Future<String> init({
    required String saleID,
    required String poiID,
    required String kek,
    required bool useTestEnvironment,
  }) {
    return _instance.init(
      saleID: saleID,
      poiID: poiID,
      kek: kek,
      useTestEnvironment: useTestEnvironment,
    );
  }

  // Future<bool> initFromCache() {
  //   return _instance.initFromCache();
  // }

  Future<String> login({bool qrPairing = false}) {
    return _instance.login(qrPairing: qrPairing);
  }

  // Future<bool> logout(String saleID, String poiID, bool useTestEnvironment) {
  //   return _instance.logout(saleID, poiID, useTestEnvironment);
  // }
  Future<String> logout() {
    return _instance.logout();
  }

  Future<void> doPayment(
      String transactionID,
      List<Map<String, dynamic>> items,
      double totalAmount,
      ) {
    return _instance.doPayment(transactionID, items, totalAmount);
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
    return _instance.doRefund(
      transactionID: transactionID,
      items: items,
      refundAmount: refundAmount,
      originalSaleID: originalSaleID,
      originalPOIID: originalPOIID,
      originalPOITransactionID: originalPOITransactionID,
      originalPOITransactionTime: originalPOITransactionTime,
    );
  }

  Future<void> doUnmatchedRefund({
    required String transactionID,
    required List<Map<String, dynamic>> items,
    required double refundAmount,
  }) {
    return _instance.doUnmatchedRefund(
      transactionID: transactionID,
      items: items,
      refundAmount: refundAmount,
    );
  }
  
  Future<String> doAbort() {
    return _instance.doAbort();
  }
}
