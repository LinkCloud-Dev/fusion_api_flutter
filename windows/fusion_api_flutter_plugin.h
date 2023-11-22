#ifndef FLUTTER_PLUGIN_FUSION_API_FLUTTER_PLUGIN_H_
#define FLUTTER_PLUGIN_FUSION_API_FLUTTER_PLUGIN_H_

#include <flutter/method_channel.h>
#include <flutter/plugin_registrar_windows.h>

#include <memory>

namespace fusion_api_flutter {

class FusionApiFlutterPlugin : public flutter::Plugin {
 public:
  static void RegisterWithRegistrar(flutter::PluginRegistrarWindows *registrar);

  FusionApiFlutterPlugin();

  virtual ~FusionApiFlutterPlugin();

  // Disallow copy and assign.
  FusionApiFlutterPlugin(const FusionApiFlutterPlugin&) = delete;
  FusionApiFlutterPlugin& operator=(const FusionApiFlutterPlugin&) = delete;

  // Called when a method is called on this plugin's channel from Dart.
  void HandleMethodCall(
      const flutter::MethodCall<flutter::EncodableValue> &method_call,
      std::unique_ptr<flutter::MethodResult<flutter::EncodableValue>> result);
};

}  // namespace fusion_api_flutter

#endif  // FLUTTER_PLUGIN_FUSION_API_FLUTTER_PLUGIN_H_
