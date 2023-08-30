import { Camera } from '@capacitor/camera';
import { DBR  } from "capacitor-plugin-dynamsoft-barcode-reader";

global.DBR = DBR;
global.Camera = Camera;

console.log('webpack starterkit');
