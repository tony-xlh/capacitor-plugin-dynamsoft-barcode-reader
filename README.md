# capacitor-plugin-dynamsoft-barcode-reader

A capacitor plugin for Dynamsoft Barcode Reader

## Install

```bash
npm install capacitor-plugin-dynamsoft-barcode-reader
npx cap sync
```

## API

<docgen-index>

* [`initLicense(...)`](#initlicense)
* [`initialize()`](#initialize)
* [`initRuntimeSettingsFromString(...)`](#initruntimesettingsfromstring)
* [`decode(...)`](#decode)
* [`decodeBitmap(...)`](#decodebitmap)
* [`setEngineResourcePaths(...)`](#setengineresourcepaths)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### initLicense(...)

```typescript
initLicense(options: { license: string; }) => Promise<{ success?: boolean; message?: string; }>
```

| Param         | Type                              |
| ------------- | --------------------------------- |
| **`options`** | <code>{ license: string; }</code> |

**Returns:** <code>Promise&lt;{ success?: boolean; message?: string; }&gt;</code>

--------------------


### initialize()

```typescript
initialize() => Promise<{ success?: boolean; message?: string; }>
```

**Returns:** <code>Promise&lt;{ success?: boolean; message?: string; }&gt;</code>

--------------------


### initRuntimeSettingsFromString(...)

```typescript
initRuntimeSettingsFromString(options: { template: string; }) => Promise<void>
```

| Param         | Type                               |
| ------------- | ---------------------------------- |
| **`options`** | <code>{ template: string; }</code> |

--------------------


### decode(...)

```typescript
decode(options: { source: string | HTMLImageElement | HTMLCanvasElement | HTMLVideoElement; template?: string; }) => Promise<{ results: TextResult[]; }>
```

source: Android and iOS only support base64 string.
path: for Android and iOS.
template: pass a template name to specify the template

| Param         | Type                                             |
| ------------- | ------------------------------------------------ |
| **`options`** | <code>{ source: any; template?: string; }</code> |

**Returns:** <code>Promise&lt;{ results: TextResult[]; }&gt;</code>

--------------------


### decodeBitmap(...)

```typescript
decodeBitmap(options: { className?: string; methodName?: string; template?: string; }) => Promise<{ results: TextResult[]; }>
```

Android and iOS only method which directly read camera frames from capacitor-plugin-camera

| Param         | Type                                                                         |
| ------------- | ---------------------------------------------------------------------------- |
| **`options`** | <code>{ className?: string; methodName?: string; template?: string; }</code> |

**Returns:** <code>Promise&lt;{ results: TextResult[]; }&gt;</code>

--------------------


### setEngineResourcePaths(...)

```typescript
setEngineResourcePaths(options: { paths: any; }) => Promise<void>
```

Web only method to set the engine resource path

| Param         | Type                         |
| ------------- | ---------------------------- |
| **`options`** | <code>{ paths: any; }</code> |

--------------------


### Interfaces


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

</docgen-api>
