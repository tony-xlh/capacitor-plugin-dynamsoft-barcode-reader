import { WebPlugin } from '@capacitor/core';

import type { DBRPlugin } from './definitions';

export class DBRWeb extends WebPlugin implements DBRPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
