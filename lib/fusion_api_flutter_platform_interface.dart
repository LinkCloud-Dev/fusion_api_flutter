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

  Future<void> init(String saleID, String poiID, String kek) {
    return _instance.init(saleID, poiID, kek);
  }

  Future<bool> manualLogin(
      String saleID,
      String poiID,
      String providerIdentification,
      String applicationName,
      String softwareVersion,
      String certificationCode,
      bool useTestEnvironment) {
    return _instance.manualLogin(
        saleID,
        poiID,
        providerIdentification,
        applicationName,
        softwareVersion,
        certificationCode,
        useTestEnvironment);
  }

  Future<Map<dynamic, dynamic>> qrLogin(
      String saleID,
      String poiID,
      String providerIdentification,
      String applicationName,
      String softwareVersion,
      String certificationCode,
      bool useTestEnvironment) {
    return _instance.qrLogin(
        saleID,
        poiID,
        providerIdentification,
        applicationName,
        softwareVersion,
        certificationCode,
        useTestEnvironment);
  }

  Future<bool> logout(String saleID, String poiID, bool useTestEnvironment) {
    return _instance.logout(saleID, poiID, useTestEnvironment);
  }

  Future<Map<dynamic, dynamic>> doPayment(
      String saleID,
      String poiID,
      String transactionID,
      List<Map<String, dynamic>> items,
      double totalAmount,
      bool useTestEnvironment) {
    return _instance.doPayment(
        saleID, poiID, transactionID, items, totalAmount, useTestEnvironment);
  }

  Future<void> doRefund(
      String saleID, String poiID, double amount, bool useTestEnvironment) {
    return _instance.doRefund(saleID, poiID, amount, useTestEnvironment);
  }
}
