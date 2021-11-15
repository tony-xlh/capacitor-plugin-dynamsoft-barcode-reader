# capacitor-plugin-dynamsoft-barcode-reader

A capacitor plugin for Dynamsoft Barcode Reader

## Install

```bash
npm install capacitor-plugin-dynamsoft-barcode-reader
npx cap sync
```

## API

<docgen-index>

* [`scan(...)`](#scan)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### scan(...)

```typescript
scan(options: { license: string; organizationID: string; dceLicense: string; }) => Promise<{ barcodeText: string; barcodeFormat: string; barcodeBytesBase64: string; }>
```

| Param         | Type                                                                          |
| ------------- | ----------------------------------------------------------------------------- |
| **`options`** | <code>{ license: string; organizationID: string; dceLicense: string; }</code> |

**Returns:** <code>Promise&lt;{ barcodeText: string; barcodeFormat: string; barcodeBytesBase64: string; }&gt;</code>

--------------------

</docgen-api>
