# capacitor-plugin-dynamsoft-barcode-reader

A capacitor plugin for Dynamsoft Barcode Reader

## Install

```bash
npm install capacitor-plugin-dynamsoft-barcode-reader
npx cap sync
```

Download the frameworks for iOS and put them under the plugin's folder:

* [Dynamsoft Camera Enhancer](https://www.dynamsoft.com/camera-enhancer/docs/introduction/)
* [Dynamsoft Barcode Reader](https://www.dynamsoft.com/barcode-reader/overview/)

## API

<docgen-index>

* [`scan(...)`](#scan)
* [`toggleTorch(...)`](#toggletorch)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### scan(...)

```typescript
scan(_options: { license: string; organizationID: string; dceLicense: string; }) => Promise<{ barcodeText: string; barcodeFormat: string; barcodeBytesBase64: string; }>
```

| Param          | Type                                                                          |
| -------------- | ----------------------------------------------------------------------------- |
| **`_options`** | <code>{ license: string; organizationID: string; dceLicense: string; }</code> |

**Returns:** <code>Promise&lt;{ barcodeText: string; barcodeFormat: string; barcodeBytesBase64: string; }&gt;</code>

--------------------


### toggleTorch(...)

```typescript
toggleTorch(_options: { on: boolean; }) => Promise<void>
```

| Param          | Type                          |
| -------------- | ----------------------------- |
| **`_options`** | <code>{ on: boolean; }</code> |

--------------------

</docgen-api>
