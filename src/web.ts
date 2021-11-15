import { WebPlugin } from '@capacitor/core';

import type { DBRPlugin } from './definitions';

export class DBRWeb extends WebPlugin implements DBRPlugin {
  async scan(options:{ license: string,
    organizationID: string,
    dceLicense:string}): Promise<{ barcodeText: string,
                          barcodeFormat:string,
                          barcodeBytesBase64: string}> {
    throw this.unimplemented('Not implemented on web.');
  }
}
