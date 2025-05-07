import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:meta/meta.dart';

import 'fusion_api_flutter_platform_interface.dart';

/// An implementation of [FusionApiFlutterPlatform] that uses method channels.
class MethodChannelFusionApiFlutter extends FusionApiFlutterPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('fusion_api_flutter');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>(
      'getPlatformVersion',
    );
    return version;
  }

  @override
  Future<String> init({
    required String saleID,
    required String poiID,
    required String kek,
    required bool useTestEnvironment,
  }) async {
    final result = await methodChannel.invokeMethod('init', {
      "saleID": saleID,
      "poiID": poiID,
      "kek": kek,
      "useTestEnvironment": useTestEnvironment,
    });

    return result;
  }

  // @override
  // Future<bool> initFromCache() async {
  //   final result = await methodChannel.invokeMethod('initFromCache');
  //   return result;
  // }

  @override
  Future<String> login({bool qrPairing = false}) async {
    final result = await methodChannel.invokeMethod<String>('login', {
      "qrPairing": qrPairing,
    });

    return result?? "started";
  }

  // @override
  // Future<bool> logout(
  //     String saleID, String poiID, bool useTestEnvironment) async {
  //   final response = await methodChannel.invokeMethod('logout', {
  //     "saleID": saleID,
  //     "poiID": poiID,
  //     "useTestEnvironment": useTestEnvironment,
  //   });
  //   return response;
  // }

  @override
  Future<String> logout() async {
    final result = await methodChannel.invokeMethod<String>('logout');
    return result!;
  }

  @override
  Future<void> doPayment(
      String transactionID,
      List<Map<String, dynamic>> items,
      double totalAmount,
      ) async {
     await methodChannel.invokeMethod('doPayment', {
      "transactionID": transactionID,
      "items": items,
      "totalAmount": totalAmount,
    });
  }

  @override
  Future<void> doRefund({
    required String transactionID,
    required List<Map<String, dynamic>> items,
    required double refundAmount,
    required String originalSaleID,
    required String originalPOIID,
    required String originalPOITransactionID,
    required String originalPOITransactionTime,
  }) async {
    await methodChannel.invokeMethod('doRefund', {
      'transactionID': transactionID,
      'items': items,
      'refundAmount': refundAmount,
      'originalSaleID': originalSaleID,
      'originalPOIID': originalPOIID,
      'originalPOITransactionID': originalPOITransactionID,
      'originalPOITransactionTime': originalPOITransactionTime,
    });
  }

  @override
  Future<void> doUnmatchedRefund({
    required String transactionID,
    required List<Map<String, dynamic>> items,
    required double refundAmount,
  }) async {
    await methodChannel.invokeMethod('doUnmatchedRefund', {
      'transactionID': transactionID,
      'items': items,
      'refundAmount': refundAmount,
    });
  }

  @override
  Future<String> doAbort() async {
    final result = await methodChannel.invokeMethod<String>('doAbort');
    return result!;
  }
}
