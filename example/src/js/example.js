import { DBR } from 'capacitor-plugin-dynamsoft-barcode-reader';

window.testEcho = () => {
    const inputValue = document.getElementById("echoInput").value;
    DBR.echo({ value: inputValue })
}
