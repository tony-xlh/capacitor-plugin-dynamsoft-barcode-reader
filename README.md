# capacitor-plugin-dynamsoft-barcode-reader

A capacitor plugin for Dynamsoft Barcode Reader

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

Dependent frameworks for iOS will be downloaded automatically via postinstall script. You can also download them and put them under the plugin's folder by yourself:

* [Dynamsoft Camera Enhancer](https://www.dynamsoft.com/camera-enhancer/docs/introduction/)
* [Dynamsoft Barcode Reader](https://www.dynamsoft.com/barcode-reader/overview/)

## API

<docgen-index>

* [`scan(...)`](#scan)
* [`toggleTorch(...)`](#toggletorch)
* [`stopScan()`](#stopscan)
* [`destroy()`](#destroy)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### scan(...)

```typescript
scan(options: ScanOptions) => Promise<{ results: ScanResult[]; }>
```

| Param         | Type                                                |
| ------------- | --------------------------------------------------- |
| **`options`** | <code><a href="#scanoptions">ScanOptions</a></code> |

**Returns:** <code>Promise&lt;{ results: ScanResult[]; }&gt;</code>

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


### destroy()

```typescript
destroy() => Promise<void>
```

--------------------


### Interfaces


#### ScanResult

| Prop                     | Type                |
| ------------------------ | ------------------- |
| **`barcodeText`**        | <code>string</code> |
| **`barcodeFormat`**      | <code>string</code> |
| **`barcodeBytesBase64`** | <code>string</code> |


#### ScanOptions

| Prop                 | Type                |
| -------------------- | ------------------- |
| **`license`**        | <code>string</code> |
| **`organizationID`** | <code>string</code> |
| **`dceLicense`**     | <code>string</code> |
| **`template`**       | <code>string</code> |

</docgen-api>
