#import <Foundation/Foundation.h>
#import <Capacitor/Capacitor.h>

// Define the plugin using the CAP_PLUGIN Macro, and
// each method the plugin supports using the CAP_PLUGIN_METHOD macro.
CAP_PLUGIN(DBRPlugin, "DBR",
           CAP_PLUGIN_METHOD(initLicense, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(initialize, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(initRuntimeSettingsFromString, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(decode, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(decodeBitmap, CAPPluginReturnPromise);
)
