import { DBR } from 'capacitor-plugin-dynamsoft-barcode-reader';
import { CameraPreview } from 'capacitor-plugin-camera';

let decoding = false;
let torchStatus = false;
let intervel;
let onPlayedListener;
let onOrientationChangedListener;
let closeButton = document.getElementById("closeButton");
let startScanButton = document.getElementById("startScanButton");
let toggleTorchButton = document.getElementById("toggleTorchButton");
startScanButton.addEventListener("click",startScan);
closeButton.addEventListener("click",stopScan);
toggleTorchButton.addEventListener("click",toggleTorch);
initialize();

async function initialize(){
  startScanButton.innerText = "Initializing...";
  if (!Capacitor.isNativePlatform()) {
    await CameraPreview.setElement(document.getElementsByClassName("camera")[0]);
  }
  await CameraPreview.initialize();
  await DBR.initLicense({license:"DLS2eyJoYW5kc2hha2VDb2RlIjoiMjAwMDAxLTE2NDk4Mjk3OTI2MzUiLCJvcmdhbml6YXRpb25JRCI6IjIwMDAwMSIsInNlc3Npb25QYXNzd29yZCI6IndTcGR6Vm05WDJrcEQ5YUoifQ=="});
  await DBR.initialize();
  if (onPlayedListener) {
    await onPlayedListener.remove();
  }
  if (onOrientationChangedListener) {
    await onOrientationChangedListener.remove();
  }
  onPlayedListener = await CameraPreview.addListener('onPlayed', async (res) => {
    console.log("onPlayed");
    console.log(res);
    startDecoding();
  });
  onOrientationChangedListener = await CameraPreview.addListener('onOrientationChanged',async () => {
    console.log("onOrientationChanged");
  });
  await CameraPreview.requestCameraPermission();
  startScanButton.innerText = "Start Live Scan";
  startScanButton.disabled = "";
  //console.log("set layout");
  //await CameraPreview.setLayout({left:"80%",width:"20%",top:"0%",height:"20%"});
}

async function startScan(){
  toggleControlsDisplay(true);
  await CameraPreview.startCamera();
}

function toggleControlsDisplay(show){
  if (show) {
    document.getElementsByClassName("home")[0].style.display = "none";
    document.getElementsByClassName("controls")[0].style.display = "";
  }else {
    document.getElementsByClassName("home")[0].style.display = "";
    document.getElementsByClassName("controls")[0].style.display = "none";
  }
}

async function stopScan(){
  stopDecoding();
  await CameraPreview.stopCamera();
  toggleControlsDisplay(false);
}

function startDecoding(){
  stopDecoding();
  intervel = setInterval(captureAndDecode,200);
}

function stopDecoding(){
  clearInterval(intervel);
}

async function captureAndDecode(){
  if (decoding === true) {
    return;
  }
  let results = [];
  let dataURL;
  decoding = true;
  try {
    if (Capacitor.isNativePlatform()) {
      await CameraPreview.saveFrame();
      results = (await DBR.decodeBitmap({})).results;
    }else{
      let frame = await CameraPreview.takeSnapshot({quality:50});
      dataURL = "data:image/jpeg;base64,"+frame.base64;
      results = await readDataURL(dataURL);
    }
    console.log(results);
    if (results.length>0) {
      stopScan();
      displayResults(results);
    }
  } catch (error) {
    console.log(error);
  }
  decoding = false;
}

function displayResults(results){
  let resultsContainer = document.getElementById("results");
  let ol = document.createElement("ol");
  for (let index = 0; index < results.length; index++) {
    const result = results[index];
    let li = document.createElement("li");
    li.innerText = result.barcodeFormat + ": " + result.barcodeText;
    ol.appendChild(li);
  }
  resultsContainer.innerHTML = ol.outerHTML;
}

async function readDataURL(dataURL){
  let response = await DBR.decode({source:dataURL});
  let results = response.results;
  return results;
}

async function toggleTorch(){
  try {
    let desiredStatus = !torchStatus;
    await CameraPreview.toggleTorch({on:desiredStatus});
    torchStatus = desiredStatus;   
  } catch (error) {
    alert(error);
  }
}