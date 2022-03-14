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

<https://github.com/xulihang/capacitor-qr-code-scanner>

## API

<docgen-index>

* [`initialize(...)`](#initialize)
* [`initRuntimeSettingsWithString(...)`](#initruntimesettingswithstring)
* [`toggleTorch(...)`](#toggletorch)
* [`startScan()`](#startscan)
* [`stopScan()`](#stopscan)
* [`resumeScan()`](#resumescan)
* [`pauseScan()`](#pausescan)
* [`getAllCameras()`](#getallcameras)
* [`selectCamera(...)`](#selectcamera)
* [`getResolution()`](#getresolution)
* [`setResolution(...)`](#setresolution)
* [`setScanRegion(...)`](#setscanregion)
* [`stopScan()`](#stopscan)
* [`destroy()`](#destroy)
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


### getAllCameras()

```typescript
getAllCameras() => Promise<{ cameras?: string[]; message?: string; }>
```

**Returns:** <code>Promise&lt;{ cameras?: string[]; message?: string; }&gt;</code>

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


### stopScan()

```typescript
stopScan() => Promise<void>
```

--------------------


### destroy()

```typescript
destroy() => Promise<void>
```

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
addListener(eventName: 'onPlayed ', listenerFunc: onPlayedListener) => Promise<PluginListenerHandle> & PluginListenerHandle
```

| Param              | Type                                                      |
| ------------------ | --------------------------------------------------------- |
| **`eventName`**    | <code>"onPlayed "</code>                                  |
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

| Prop                 | Type                |
| -------------------- | ------------------- |
| **`license`**        | <code>string</code> |
| **`organizationID`** | <code>string</code> |
| **`dceLicense`**     | <code>string</code> |


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
