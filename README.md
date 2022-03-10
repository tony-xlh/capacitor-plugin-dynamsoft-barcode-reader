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

* [`startScan(...)`](#startscan)
* [`toggleTorch(...)`](#toggletorch)
* [`stopScan()`](#stopscan)
* [`resumeScan()`](#resumescan)
* [`pauseScan()`](#pausescan)
* [`destroy()`](#destroy)
* [`addListener(...)`](#addlistener)
* [`removeAllListeners()`](#removealllisteners)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### startScan(...)

```typescript
startScan(options: ScanOptions) => Promise<void>
```

| Param         | Type                                                |
| ------------- | --------------------------------------------------- |
| **`options`** | <code><a href="#scanoptions">ScanOptions</a></code> |

--------------------


### toggleTorch(...)

```typescript
toggleTorch(options: { on: boolean; }) => Promise<void>
```

| Param         | Type                          |
| ------------- | ----------------------------- |
| **`options`** | <code>{ on: boolean; }</code> |

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


### removeAllListeners()

```typescript
removeAllListeners() => Promise<void>
```

--------------------


### Interfaces


#### ScanOptions

| Prop                 | Type                 |
| -------------------- | -------------------- |
| **`license`**        | <code>string</code>  |
| **`organizationID`** | <code>string</code>  |
| **`dceLicense`**     | <code>string</code>  |
| **`template`**       | <code>string</code>  |
| **`continuous`**     | <code>boolean</code> |


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
