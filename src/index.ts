import { registerPlugin } from '@capacitor/core';

import type { DBRPlugin } from './definitions';

const DBR = registerPlugin<DBRPlugin>('DBR', {
  web: () => import('./web').then(m => new m.DBRWeb()),
});

export * from './definitions';
export { DBR };
