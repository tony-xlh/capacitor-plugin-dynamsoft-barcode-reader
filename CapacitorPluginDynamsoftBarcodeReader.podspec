require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name = 'CapacitorPluginDynamsoftBarcodeReader'
  s.version = package['version']
  s.summary = package['description']
  s.license = package['license']
  s.homepage = package['repository']['url']
  s.author = package['author']
  s.source = { :git => package['repository']['url'], :tag => s.version.to_s }
  s.source_files = 'ios/Sources/**/*.{swift,h,m,c,cc,mm,cpp}'
  s.ios.deployment_target  = '13.0'
  s.dependency 'Capacitor'
  s.dependency 'DynamsoftBarcodeReader', '= 10.0.20'
  s.dependency 'DynamsoftCaptureVisionRouter', '= 2.0.20'
  s.dependency 'DynamsoftCore', '= 3.0.20'
  s.dependency 'DynamsoftImageProcessing', '= 2.0.20'
  s.dependency 'DynamsoftLicense', '= 3.0.20'
  s.swift_version = '5.1'
end
