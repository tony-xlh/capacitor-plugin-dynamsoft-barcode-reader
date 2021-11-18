const download = require('download');

async function main(){
    try{
        var options = {"extract":true};
        console.log("Downloading DBR");
        await download('https://download.dynamsoft.com/cocoapods/dynamsoft-barcodereader-ios-8.8.0.zip', './',options);
        console.log("Downloading DCE");
        await download('https://github.com/xulihang/dbr-podspec/releases/download/dce/dynamsoft-cameraenhancer-ios-2.0.0.zip', './',options);
    }catch (e){
        console.log(e)
    }
    
}

main();


