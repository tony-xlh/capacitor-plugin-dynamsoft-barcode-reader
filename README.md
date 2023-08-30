# capacitor-plugin-dynamsoft-barcode-reader

![version](https://img.shields.io/npm/v/capacitor-plugin-dynamsoft-barcode-reader.svg)

A capacitor plugin for [Dynamsoft Barcode Reader](https://www.dynamsoft.com/barcode-reader/overview/) and [Dynamsoft Camera Enhancer](https://www.dynamsoft.com/camera-enhancer/docs/introduction/).

## Supported Platforms

* Android
* iOS
* Web

## Install

```bash
npm install capacitor-plugin-dynamsoft-barcode-reader
npx cap sync
```

Or install from a local folder

```bash
npm install <path-to-the-project>
npx cap sync
```

## Demo

* <https://github.com/xulihang/Ionic-React-QR-Code-Scanner>
* <https://github.com/xulihang/capacitor-plugin-dynamsoft-barcode-reader/tree/main/example>
* <https://github.com/xulihang/capacitor-qr-code-scanner>


## API

<docgen-index>

* [`initialize(...)`](#initialize)
* [`initRuntimeSettingsWithString(...)`](#initruntimesettingswithstring)
* [`toggleTorch(...)`](#toggletorch)
* [`startScan()`](#startscan)
* [`stopScan()`](#stopscan)
* [`resumeScan()`](#resumescan)
* [`pauseScan()`](#pausescan)
* [`stopScan()`](#stopscan)
* [`readImage(...)`](#readimage)
* [`requestCameraPermission()`](#requestcamerapermission)
* [`getAllCameras()`](#getallcameras)
* [`getSelectedCamera()`](#getselectedcamera)
* [`selectCamera(...)`](#selectcamera)
* [`getResolution()`](#getresolution)
* [`setResolution(...)`](#setresolution)
* [`setScanRegion(...)`](#setscanregion)
* [`setZoom(...)`](#setzoom)
* [`setFocus(...)`](#setfocus)
* [`setLayout(...)`](#setlayout)
* [`destroy()`](#destroy)
* [`setEngineResourcePath(...)`](#setengineresourcepath)
* [`setDefaultUIElementURL(...)`](#setdefaultuielementurl)
* [`addListener(...)`](#addlistener)
* [`addListener(...)`](#addlistener)
* [`removeAllListeners()`](#removealllisteners)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### initialize(...)

```typescript
initialize(options?: Options | undefined) => Promise<{ success?: boolean; message?: string; }>
```

| Param         | Type                                        |
| ------------- | ------------------------------------------- |
| **`options`** | <code><a href="#options">Options</a></code> |

**Returns:** <code>Promise&lt;{ success?: boolean; message?: string; }&gt;</code>

--------------------


### initRuntimeSettingsWithString(...)

```typescript
initRuntimeSettingsWithString(options: { template: string; }) => Promise<void>
```

| Param         | Type                               |
| ------------- | ---------------------------------- |
| **`options`** | <code>{ template: string; }</code> |

--------------------


### toggleTorch(...)

```typescript
toggleTorch(options: { on: boolean; }) => Promise<void>
```

| Param         | Type                          |
| ------------- | ----------------------------- |
| **`options`** | <code>{ on: boolean; }</code> |

--------------------


### startScan()

```typescript
startScan() => Promise<void>
```

--------------------


### stopScan()

```typescript
stopScan() => Promise<void>
```

--------------------


### resumeScan()

```typescript
resumeScan() => Promise<void>
```

--------------------


### pauseScan()

```typescript
pauseScan() => Promise<void>
```

--------------------


### stopScan()

```typescript
stopScan() => Promise<void>
```

--------------------


### readImage(...)

```typescript
readImage(options: { base64: string; }) => Promise<TextResult[]>
```

| Param         | Type                             |
| ------------- | -------------------------------- |
| **`options`** | <code>{ base64: string; }</code> |

**Returns:** <code>Promise&lt;TextResult[]&gt;</code>

--------------------


### requestCameraPermission()

```typescript
requestCameraPermission() => Promise<void>
```

--------------------


### getAllCameras()

```typescript
getAllCameras() => Promise<{ cameras?: string[]; message?: string; }>
```

**Returns:** <code>Promise&lt;{ cameras?: string[]; message?: string; }&gt;</code>

--------------------


### getSelectedCamera()

```typescript
getSelectedCamera() => Promise<{ selectedCamera?: string; message?: string; }>
```

**Returns:** <code>Promise&lt;{ selectedCamera?: string; message?: string; }&gt;</code>

--------------------


### selectCamera(...)

```typescript
selectCamera(options: { cameraID: string; }) => Promise<{ success?: boolean; message?: string; }>
```

| Param         | Type                               |
| ------------- | ---------------------------------- |
| **`options`** | <code>{ cameraID: string; }</code> |

**Returns:** <code>Promise&lt;{ success?: boolean; message?: string; }&gt;</code>

--------------------


### getResolution()

```typescript
getResolution() => Promise<{ resolution?: string; message?: string; }>
```

**Returns:** <code>Promise&lt;{ resolution?: string; message?: string; }&gt;</code>

--------------------


### setResolution(...)

```typescript
setResolution(options: { resolution: number; }) => Promise<{ success?: boolean; message?: string; }>
```

| Param         | Type                                 |
| ------------- | ------------------------------------ |
| **`options`** | <code>{ resolution: number; }</code> |

**Returns:** <code>Promise&lt;{ success?: boolean; message?: string; }&gt;</code>

--------------------


### setScanRegion(...)

```typescript
setScanRegion(options: ScanRegion) => Promise<{ success?: boolean; message?: string; }>
```

| Param         | Type                                              |
| ------------- | ------------------------------------------------- |
| **`options`** | <code><a href="#scanregion">ScanRegion</a></code> |

**Returns:** <code>Promise&lt;{ success?: boolean; message?: string; }&gt;</code>

--------------------


### setZoom(...)

```typescript
setZoom(options: { factor: number; }) => Promise<{ success?: boolean; message?: string; }>
```

| Param         | Type                             |
| ------------- | -------------------------------- |
| **`options`** | <code>{ factor: number; }</code> |

**Returns:** <code>Promise&lt;{ success?: boolean; message?: string; }&gt;</code>

--------------------


### setFocus(...)

```typescript
setFocus(options: { x: number; y: number; }) => Promise<{ success?: boolean; message?: string; }>
```

| Param         | Type                                   |
| ------------- | -------------------------------------- |
| **`options`** | <code>{ x: number; y: number; }</code> |

**Returns:** <code>Promise&lt;{ success?: boolean; message?: string; }&gt;</code>

--------------------


### setLayout(...)

```typescript
setLayout(options: { top: string; left: string; width: string; height: string; }) => Promise<{ success?: boolean; message?: string; }>
```

| Param         | Type                                                                       |
| ------------- | -------------------------------------------------------------------------- |
| **`options`** | <code>{ top: string; left: string; width: string; height: string; }</code> |

**Returns:** <code>Promise&lt;{ success?: boolean; message?: string; }&gt;</code>

--------------------


### destroy()

```typescript
destroy() => Promise<void>
```

--------------------


### setEngineResourcePath(...)

```typescript
setEngineResourcePath(path: string) => Promise<void>
```

| Param      | Type                |
| ---------- | ------------------- |
| **`path`** | <code>string</code> |

--------------------


### setDefaultUIElementURL(...)

```typescript
setDefaultUIElementURL(url: string) => Promise<void>
```

| Param     | Type                |
| --------- | ------------------- |
| **`url`** | <code>string</code> |

--------------------


### addListener(...)

```typescript
addListener(eventName: 'onFrameRead', listenerFunc: onFrameReadListener) => Promise<PluginListenerHandle> & PluginListenerHandle
```

| Param              | Type                                         |
| ------------------ | -------------------------------------------- |
| **`eventName`**    | <code>"onFrameRead"</code>                   |
| **`listenerFunc`** | <code>(result: ScanResult) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt; & <a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

--------------------


### addListener(...)

```typescript
addListener(eventName: 'onPlayed', listenerFunc: onPlayedListener) => Promise<PluginListenerHandle> & PluginListenerHandle
```

| Param              | Type                                                      |
| ------------------ | --------------------------------------------------------- |
| **`eventName`**    | <code>"onPlayed"</code>                                   |
| **`listenerFunc`** | <code>(result: { resolution: string; }) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt; & <a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

--------------------


### removeAllListeners()

```typescript
removeAllListeners() => Promise<void>
```

--------------------


### Interfaces


#### Options

| Prop             | Type                |
| ---------------- | ------------------- |
| **`license`**    | <code>string</code> |
| **`dceLicense`** | <code>string</code> |


#### TextResult

| Prop                     | Type                |
| ------------------------ | ------------------- |
| **`barcodeText`**        | <code>string</code> |
| **`barcodeFormat`**      | <code>string</code> |
| **`barcodeBytesBase64`** | <code>string</code> |
| **`x1`**                 | <code>number</code> |
| **`y1`**                 | <code>number</code> |
| **`x2`**                 | <code>number</code> |
| **`y2`**                 | <code>number</code> |
| **`x3`**                 | <code>number</code> |
| **`y3`**                 | <code>number</code> |
| **`x4`**                 | <code>number</code> |
| **`y4`**                 | <code>number</code> |


#### ScanRegion

measuredByPercentage: 0 in pixel, 1 in percent

| Prop                       | Type                |
| -------------------------- | ------------------- |
| **`left`**                 | <code>number</code> |
| **`top`**                  | <code>number</code> |
| **`right`**                | <code>number</code> |
| **`bottom`**               | <code>number</code> |
| **`measuredByPercentage`** | <code>number</code> |


#### PluginListenerHandle

| Prop         | Type                                      |
| ------------ | ----------------------------------------- |
| **`remove`** | <code>() =&gt; Promise&lt;void&gt;</code> |

</docgen-api>

## Supported Barcode Symbologies

* Code 11
* Code 39
* Code 93
* Code 128
* Codabar
* EAN-8
* EAN-13
* UPC-A
* UPC-E
* Interleaved 2 of 5 (ITF)
* Industrial 2 of 5 (Code 2 of 5 Industry, Standard 2 of 5, Code 2 of 5)
* ITF-14 
* QRCode
* DataMatrix
* PDF417
* GS1 DataBar
* Maxicode
* Micro PDF417
* Micro QR
* PatchCode
* GS1 Composite
* Postal Code
* Dot Code
* Pharmacode

## Version

For versions >= 1.3.1, Dynamsoft Barcode Reader 9 is used.

For version 1.3.0, Dynamsoft Barcode Reader Mobile 9 and Dynamsoft Barcode Reader JavaScript 8.8.7 are used.

For versions < 1.3.0, Dynamsoft Barcode Reader 8 is used.
