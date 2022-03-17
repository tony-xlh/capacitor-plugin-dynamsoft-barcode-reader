#import <Foundation/Foundation.h>
#import <Capacitor/Capacitor.h>

// Define the plugin using the CAP_PLUGIN Macro, and
// each method the plugin supports using the CAP_PLUGIN_METHOD macro.
CAP_PLUGIN(DBRPlugin, "DBR",
           CAP_PLUGIN_METHOD(destroy, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(initialize, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(initRuntimeSettingsWithString, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(startScan, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(toggleTorch, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(stopScan, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(pauseScan, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(resumeScan, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(getAllCameras, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(getSelectedCamera, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(selectCamera, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(getResolution, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(setResolution, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(setScanRegion, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(setZoom, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(setFocus, CAPPluginReturnPromise);
)
