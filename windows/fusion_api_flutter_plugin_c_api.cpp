#include "include/fusion_api_flutter/fusion_api_flutter_plugin_c_api.h"

#include <flutter/plugin_registrar_windows.h>

#include "fusion_api_flutter_plugin.h"

void FusionApiFlutterPluginCApiRegisterWithRegistrar(
    FlutterDesktopPluginRegistrarRef registrar) {
  fusion_api_flutter::FusionApiFlutterPlugin::RegisterWithRegistrar(
      flutter::PluginRegistrarManager::GetInstance()
          ->GetRegistrar<flutter::PluginRegistrarWindows>(registrar));
}
