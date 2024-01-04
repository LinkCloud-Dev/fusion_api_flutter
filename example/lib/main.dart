import 'dart:async';
import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:fusion_api_flutter/fusion_api_flutter.dart';
import 'package:qr_flutter/qr_flutter.dart';
import 'package:uuid/uuid.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  final _fusionApiFlutterPlugin = FusionApiFlutter();

  // Cashier Configurable
  String saleID = 'INT POS';
  String poiID = 'LINKPOS0';
  String kek = '44DACB2A22A4A752ADC1BBFFE6CEFB589451E0FFD83F8B21';

  // For QR pairing
  String qrSaleID = "";
  String qrPoiID = "";
  String qrKek = "";

  // Static Sale System Settings
  // Need to be the same for all merchants
  final String providerIdentification = 'Company A';
  final String applicationName = 'POS Retail';
  final String softwareVersion = "01.00.00";
  final String certificationCode = '98cf9dfc-0db7-4a92-8b8cb66d4d2d7169';
  final String posName = 'LinkPOS';
  final int version = 1;

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Create QR Code Data
  Map<String, dynamic> getQRCodeData(
      String certificationCode, String posName, int version) {
    var uuid = const Uuid();

    String chars = "0123456789ABCDEF";
    Random random = Random();
    for (int i = 0; i < 48; i++) {
      qrKek += chars[random.nextInt(chars.length)];
    }

    qrSaleID = uuid.v4();
    qrPoiID = uuid.v4();

    Map<String, dynamic> qrCodeData = {
      "s": qrSaleID,
      "p": qrPoiID,
      "k": kek,
      "c": certificationCode,
      "n": posName,
      "v": 1
    };
    return qrCodeData;
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      platformVersion = await _fusionApiFlutterPlugin.getPlatformVersion() ??
          'Unknown platform version';
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  Future<void> _login() async {
    _fusionApiFlutterPlugin.init(saleID, poiID, kek);
    var result = await _fusionApiFlutterPlugin.manualLogin(
        saleID,
        poiID,
        providerIdentification,
        applicationName,
        softwareVersion,
        certificationCode,
        true);
    print("________________________");
    print(result);
    print("________________________");
  }

  Future<void> _qrLogin() async {
    await _fusionApiFlutterPlugin.init(qrSaleID, qrPoiID, kek);
    var result = await _fusionApiFlutterPlugin.qrLogin(
        qrSaleID,
        qrPoiID,
        providerIdentification,
        applicationName,
        softwareVersion,
        certificationCode,
        true);
    print(result);
    qrPoiID = result["newPoiID"];
    var secondResult = await _fusionApiFlutterPlugin.manualLogin(
        qrSaleID,
        qrPoiID,
        providerIdentification,
        applicationName,
        softwareVersion,
        certificationCode,
        true);
    print("_____________");
    print("Following real login request");
    print(secondResult);
  }

  Future<void> _qrLogout() async {
    _fusionApiFlutterPlugin.logout(qrSaleID, qrPoiID, true);
    setState(() {});
  }

  Future<void> _logout() async {
    var result = await _fusionApiFlutterPlugin.logout(saleID, poiID, true);
    print("??????????????????");
    print(result);
    print("??????????????????");
  }

  Future<void> _mockPay() async {
    // items.forEachIndexed() { index, item ->
    //             val saleItem = SaleItem.Builder() //
    //                 .itemID(index) //
    //                 .productCode(item["productCode"].toString()) //
    //                 .unitOfMeasure(UnitOfMeasure.Other) //
    //                 .quantity(BigDecimal(item["quantity"].toString())) //
    //                 .unitPrice(BigDecimal(item["unitPrice"].toString())) //
    //                 .itemAmount(BigDecimal(item["itemAmount"].toString())) //
    //                 .productLabel(item["productLabel"].toString()) //
    //                 .build()
    //             saleItems.add(saleItem)
    //         }
    List<Map<String, dynamic>> items = [
      {
        "productCode": "1234567890123",
        "quantity": 1,
        "unitPrice": 1.0,
        "itemAmount": 1.0,
        "productLabel": "Test Product"
      },
      {
        "productCode": "123456789",
        "quantity": 2,
        "unitPrice": 1.5,
        "itemAmount": 3,
        "productLabel": "Test Product2"
      }
    ];
    _fusionApiFlutterPlugin.doPayment(
        saleID, poiID, "teststringID", items, 4, true);
  }

  Future<void> _mockQrPay() async {
    List<Map<String, dynamic>> items = [
      {
        "productCode": "1234567890123",
        "quantity": 1,
        "unitPrice": 1.0,
        "itemAmount": 1.0,
        "productLabel": "Test Product"
      },
      {
        "productCode": "123456789",
        "quantity": 2,
        "unitPrice": 1.5,
        "itemAmount": 3,
        "productLabel": "Test Product2"
      }
    ];
    var result = await _fusionApiFlutterPlugin.doPayment(
        qrSaleID, qrPoiID, "TesttingID", items, 4, true);
    print("??????????????????");
    print(result);
  }

  Future<void> _refund() async {
    double amount = 0.0;
    _fusionApiFlutterPlugin.doRefund(qrSaleID, qrPoiID, amount, null, true);
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
            child: Column(
          children: [
            Text('Running on: $_platformVersion\n'),
            QrImageView(
              data:
                  getQRCodeData(certificationCode, posName, version).toString(),
              size: 300,
            ),
            ElevatedButton(
                onPressed: () => _qrLogin(), child: const Text("QR Login")),
            ElevatedButton(
                onPressed: () => _qrLogout(), child: const Text("QR Logout")),
            ElevatedButton(
                onPressed: () => _login(), child: const Text("Login")),
            ElevatedButton(
                onPressed: () => _logout(), child: const Text("Logout")),
            ElevatedButton(
                onPressed: () => _mockPay(), child: const Text("MockPay")),
            ElevatedButton(
                onPressed: () => _mockQrPay(), child: const Text("MockQrPay")),
            ElevatedButton(
                onPressed: () => _refund(), child: const Text("Refund"))
          ],
        )),
      ),
    );
  }
}
