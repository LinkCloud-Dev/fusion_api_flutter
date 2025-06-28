import 'dart:async';
import 'dart:math';
import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:fusion_api_flutter/fusion_api_flutter.dart';
import 'package:qr_flutter/qr_flutter.dart';
import 'package:uuid/uuid.dart';

void main() {
  runApp(const MaterialApp(
    home: MyApp(),
  ));
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  static const MethodChannel fusionChannel = MethodChannel('fusion_api_flutter');
  // static const EventChannel _fusionEventChannel = EventChannel('fusion_plugin/events');

  final _fusionApiFlutterPlugin = FusionApiFlutter();

  // Cashier Configurable
  String saleID = 'LinkPos';
  String poiID = 'LINKPOS1';
  String kek = '7F1E8AA14851D1C41E254C6EFCBE44296ED7F8FB496F312D';

  // For QR pairing
  String qrSaleID = "";
  String qrPoiID = "";
  String qrKek = "";

  final String certificationCode = 'fbee5ab6-2c16-4395-872e-aaa57b1b86b9';
  final String posName = 'LinkPOS';
  final int version = 1;

  String _statusMessage = '';

  bool isQRLogin = false;
  TextEditingController saleIDController = TextEditingController(text: 'LinkPos');
  TextEditingController poiIDController = TextEditingController(text: 'LINKPOS1');
  TextEditingController kekController = TextEditingController(text: '7F1E8AA14851D1C41E254C6EFCBE44296ED7F8FB496F312D');
  bool manualLoginLocked = false;

  final TextEditingController productCodeController = TextEditingController(text: "DMGTC38292");
  String currentMockProductCode = "DMGTC38292";

  @override
  void initState() {
    super.initState();
    initPlatformState();
    setupFusionPluginCallbacks();
    getQRCodeData(certificationCode, posName, version);
  }

  // Future<String> initFromCache() async {
  //   final success = await _fusionApiFlutterPlugin.initFromCache();
  //   print('click init from cache');
  //   if (success) {
  //     return 'some value saved in the cache, try payment directly!';
  //   } else {
  //     return 'You still need to login';
  //   }
  // }

  Future<void> initWithManualInput() async {
    saleID = saleIDController.text;
    poiID = poiIDController.text;
    kek = kekController.text;

    await _fusionApiFlutterPlugin.init(
      saleID: saleID,
      poiID: poiID,
      kek: kek,
      useTestEnvironment: true,
    );
  }

  Future<void> initWithQRConfig() async {
    await _fusionApiFlutterPlugin.init(
      saleID: qrSaleID,
      poiID: qrPoiID,
      kek: qrKek,
      useTestEnvironment: true,
    );
  }

  Future<void> unpairQR() async {
    qrPoiID = '';

    await _fusionApiFlutterPlugin.init(
      saleID: qrSaleID,
      poiID: qrPoiID,
      kek: qrKek,
      useTestEnvironment: true,
    );

    setState(() {
      _statusMessage = "QR POIID cleared. QR settings reset.";
    });

    print("✅ QR POIID cleared via init method.");
  }

  // Create QR Code Data
  void getQRCodeData(
      String certificationCode, String posName, int version) {
    var uuid = const Uuid();

    qrKek = ""; // 防止重复累加
    String chars = "0123456789ABCDEF";
    Random random = Random();
    for (int i = 0; i < 48; i++) {
      qrKek += chars[random.nextInt(chars.length)];
    }

    qrSaleID = uuid.v4();
    qrPoiID = uuid.v4();
  }


  void setupFusionPluginCallbacks() {
    fusionChannel.setMethodCallHandler((call) async {

      if (call.method == "onPluginEvent") {
        final event = Map<String, dynamic>.from(call.arguments);
        final type = event["type"];
        final status = event["status"];
        final message = event["message"];
        final data = Map<String, dynamic>.from(event["data"] ?? {});

        if (type == "login") {
          if (status == "success") {
            print("===========✅ Login success:===========");
            print("=========== Message: $message===========");
            print("=========== saleID: ${data['saleID']}===========");
            print("=========== poiID: ${data['poiID']}===========");
            print("=========== kek: ${data['']}===========");
          } else {
            print("===========❌ Login $status:===========");
            print("===========Message: $message===========");
          }
        }
        else if (type == "payment") {
          print("=========== Payment Received:===========");
          print("=========== Status: $status===========");
          print("=========== Message: $message===========");
          print("=========== Payment Data:===========");
          data.forEach((key, value) {
            print("   $key: $value");
          });
          print("===========END===========");

          if (status == "success" || status == "fail") {
            paymentMessage.value = "Payment $status: $message";
            Future.delayed(const Duration(seconds: 2), () {
              if (Navigator.canPop(context)) {
                Navigator.of(context, rootNavigator: true).pop();
                paymentMessage.value = "Waiting for terminal...";
              }
            });
          }
        }
        else if (type == "transactionStatus") {
          print("=========== Transaction Status Event:============");
          print("=========== Status: ${event['status']}===========");
          print("=========== Message: ${event['message']}===========");

          if (event[status] == "timeout") {
            paymentMessage.value = "Transaction timed out. Please try again.";
          } else if(event[status] == "fail"){
            paymentMessage.value ==event['message']?? "Processing...";
          }
        }
        else if (type == "displayRequest") {
          paymentMessage.value = event['message'] ?? "Processing...";
        }
        else if(type == "timeout"){
          paymentMessage.value =  event['message'];
        }
        else if(type=="connection"){
          paymentMessage.value =  event['message'];
        }
      }
    });
  }
  //
  // void setupCallback() {
  //   _fusionEventChannel.receiveBroadcastStream().listen((event) {
  //     final parsed = Map<String, dynamic>.from(event);
  //     final type = parsed["type"];
  //     final status = parsed["status"];
  //     final message = parsed["message"];
  //     final data = Map<String, dynamic>.from(parsed["data"] ?? {});
  //
  //     print("📥 Plugin Event Received:");
  //     print("📌 Type: $type");
  //     print("📌 Status: $status");
  //     print("📩 Message: $message");
  //
  //     if (type == "login") {
  //       if (status == "success") {
  //         print("✅ Login success: $message");
  //         print("   saleID: ${data['saleID']}");
  //         print("   poiID: ${data['poiID']}");
  //       } else {
  //         print("❌ Login $status: $message");
  //       }
  //     } else if (type == "payment") {
  //       paymentMessage.value = "Payment $status: $message";
  //       if (status == "success" || status == "fail") {
  //         Future.delayed(const Duration(seconds: 2), () {
  //           if (Navigator.canPop(context)) {
  //             Navigator.of(context, rootNavigator: true).pop();
  //             paymentMessage.value = "Waiting for terminal...";
  //           }
  //         });
  //       }
  //     } else if (type == "transactionStatus") {
  //       paymentMessage.value = "Transaction $status: $message";
  //     } else if (type == "displayRequest") {
  //       paymentMessage.value = message ?? "Processing...";
  //     } else if (type == "timeout") {
  //       paymentMessage.value = message;
  //     }
  //   }, onError: (error) {
  //     print("❗ Event stream error: $error");
  //   });
  // }

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

  Future<void> _mockQrPay() async {
    List<Map<String, dynamic>> items = [
      {
        "productCode": "1234567890123",
        "quantity": 1,
        "unitPrice": 0.1,
        "itemAmount": 1.0,
        "productLabel": "Test Product"
      },
      {
        "productCode": "123456789",
        "quantity": 2,
        "unitPrice": 0.1,
        "itemAmount": 3,
        "productLabel": "Crème Brûlée 🍮"
      },
      // {"productCode": "DMGTC38292"} //mocktest
    ];
    _fusionApiFlutterPlugin.doPayment("TesttingID", items, 0.4);
  }

  Future<void> _mockPay() async {
    List<Map<String, dynamic>> items = [
      {
        "productCode": currentMockProductCode,
        "quantity": 2,
        "unitPrice": 0.2,
        "itemAmount": 0.4,
        "productLabel": "Test Item"
      }
    ];
    _fusionApiFlutterPlugin.doPayment("MockTest", items, 0.4);
  }

  Future<void> _mockRefund() async {
    List<Map<String, dynamic>> items = [
      {"productCode": currentMockProductCode}
    ];
    _fusionApiFlutterPlugin.doUnmatchedRefund(transactionID:"MockTest",refundAmount: 0.4 ,items:items);
  }

  Future<void> _refund() async {
    String originalSaleID = "f2685003-3898-42af-9d0d-d502541f2074";
    String originalPOIID = "120110fb-ca30-46bd-bbfa-dc9e9fba042a";
    String originalPOITransactionID = "6804bb32fe32184587c63941";
    String originalPOITransactionTime = "2025-04-20T07:15:49+10:00";
    _fusionApiFlutterPlugin.doRefund(
        transactionID: "refund-test-id-001",
        refundAmount: 4,
        items: [],
        originalSaleID: originalSaleID,
        originalPOIID: originalPOIID,
        originalPOITransactionID: originalPOITransactionID,
        originalPOITransactionTime: originalPOITransactionTime);
  }

  Future<void> _unmatchedRefund() async {
    _fusionApiFlutterPlugin.doUnmatchedRefund(
        transactionID: "refund-test-id-001",
        refundAmount: 4,
        items: []);
  }

  Future<void> _handleUnpairQR() async {
    await unpairQR();
    print("QR settings cleared via init()");
  }

  final ValueNotifier<String> paymentMessage = ValueNotifier("Waiting for terminal...");
  final ValueNotifier<bool> isCancelEnabled = ValueNotifier(true);

  void showTransactionDialog(BuildContext context) {
    // 重置状态
    isCancelEnabled.value = true;
    paymentMessage.value = "Waiting for terminal...";

    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (_) => TransactionDialog(
        messageNotifier: paymentMessage,
        isCancelEnabled: isCancelEnabled,
        onCancel: () {
          isCancelEnabled.value = false;
          _fusionApiFlutterPlugin.doAbort();
          paymentMessage.value = "Waiting for response...";
        },
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: SingleChildScrollView(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Text('Running on: $_platformVersion\n'),
                // ElevatedButton(
                //   onPressed: () async {
                //     final msg = await initFromCache();
                //     setState(() {
                //       _statusMessage = msg;
                //     });
                //   },
                //   child: const Text("Try Restore Pairing"),
                // ),
                ToggleButtons(
                  isSelected: [!isQRLogin, isQRLogin],
                  onPressed: (index) {
                    setState(() {
                      isQRLogin = index == 1;
                    });
                  },
                  children: const [
                    Padding(
                      padding: EdgeInsets.symmetric(horizontal: 16),
                      child: Text('Manual Login'),
                    ),
                    Padding(
                      padding: EdgeInsets.symmetric(horizontal: 16),
                      child: Text('QR Login'),
                    ),
                  ],
                ),

                const SizedBox(height: 16),

                if (!isQRLogin)
                  Card(
                    margin: const EdgeInsets.symmetric(horizontal: 24),
                    elevation: 4,
                    shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(12)),
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        children: [
                          TextField(
                            controller: saleIDController,
                            decoration:
                            const InputDecoration(labelText: 'Sale ID'),
                            autocorrect: false,
                            enabled: !manualLoginLocked,
                          ),
                          TextField(
                            controller: poiIDController,
                            decoration:
                            const InputDecoration(labelText: 'POI ID'),
                            autocorrect: false,
                            enabled: !manualLoginLocked,
                          ),
                          TextField(
                            controller: kekController,
                            decoration:
                            const InputDecoration(labelText: 'KEK'),
                            obscureText: true,
                            enableSuggestions: false,
                            autocorrect: false,
                            enabled: !manualLoginLocked,
                          ),
                          const SizedBox(height: 8),
                          ElevatedButton(
                            onPressed: () async {
                              await initWithManualInput();
                              await _fusionApiFlutterPlugin
                                  .login(qrPairing: false);
                            },
                            child: const Text("Manual Login"),
                          ),
                          if (manualLoginLocked)
                            TextButton(
                              onPressed: () {
                                setState(() {
                                  manualLoginLocked = false;
                                });
                              },
                              child: const Text("Edit Login Info"),
                            ),
                        ],
                      ),
                    ),
                  )
                else
                  Column(
                    children: [
                      Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          const Text("QR Code"),
                          IconButton(
                            icon: const Icon(Icons.refresh),
                            onPressed: () {
                              setState(() {
                                getQRCodeData(
                                    certificationCode, posName, version);
                              });
                            },
                          ),
                        ],
                      ),
                      QrImageView(
                        data: {
                          "s": qrSaleID,
                          "p": qrPoiID,
                          "k": qrKek,
                          "c": certificationCode,
                          "n": posName,
                          "v": 1
                        }.toString(),
                        size: 300,
                      ),
                      const SizedBox(height: 8),
                      ElevatedButton(
                        onPressed: () async {
                          await initWithQRConfig();
                          await _fusionApiFlutterPlugin.login(qrPairing: true);
                        },
                        child: const Text("QR Login"),
                      ),
                    ],
                  ),

                const SizedBox(height: 16),

                Wrap(
                  spacing: 8.0,
                  runSpacing: 8.0,
                  children: [
                    Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 24.0, vertical: 8),
                      child: Row(
                        children: [
                          Expanded(
                            child: TextField(
                              controller: productCodeController,
                              decoration: const InputDecoration(
                                labelText: "Product Code",
                                border: OutlineInputBorder(),
                              ),
                            ),
                          ),
                          const SizedBox(width: 8),
                          ElevatedButton(
                            onPressed: () async {
                              final code = productCodeController.text.trim();
                              if (code.isEmpty) return;

                              setState(() {
                                currentMockProductCode = code;
                              });

                              showTransactionDialog(context);
                              await _mockPay();
                            },
                            child: const Text("MockPay"),
                          ),
                          const SizedBox(width: 8),
                          ElevatedButton(
                            onPressed: () async {
                              final code = productCodeController.text.trim();
                              if (code.isEmpty) return;

                              setState(() {
                                currentMockProductCode = code;
                              });

                              showTransactionDialog(context);
                              await _mockRefund();
                            },
                            child: const Text("MockRefund"),
                          ),
                        ],
                      ),
                    ),
                    ElevatedButton(
                      onPressed: () async {
                        showTransactionDialog(context);
                        await _mockQrPay();
                      },
                      child: const Text("MockQrPay"),
                    ),
                    ElevatedButton(
                      onPressed: () async {
                        showTransactionDialog(context);
                        await _refund();
                      },
                      child: const Text("Refund"),
                    ),
                    ElevatedButton(
                      onPressed: () async {
                        showTransactionDialog(context);
                        await _unmatchedRefund();
                      },
                      child: const Text("XMRefund"), // unmatched refund
                    ),
                    ElevatedButton(
                      onPressed: () async {
                        await _handleUnpairQR();
                      },
                      child: const Text("Unpair QR"),
                    ),
                  ],
                ),
                Text(_statusMessage),
              ],
            ),
          ),
        ),
      ),
    );
  }
}


class TransactionDialog extends StatelessWidget {
  final ValueNotifier<String> messageNotifier;
  final ValueNotifier<bool> isCancelEnabled;
  final VoidCallback onCancel;

  const TransactionDialog({
    super.key,
    required this.messageNotifier,
    required this.isCancelEnabled,
    required this.onCancel,
  });

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text("Transaction"),
      content: ValueListenableBuilder(
        valueListenable: messageNotifier,
        builder: (_, value, __) => Text(value),
      ),
      actions: [
        ValueListenableBuilder<bool>(
          valueListenable: isCancelEnabled,
          builder: (_, enabled, __) => TextButton(
            onPressed: enabled ? onCancel : null,
            child: const Text("Cancel"),
          ),
        ),
      ],
    );
  }
}