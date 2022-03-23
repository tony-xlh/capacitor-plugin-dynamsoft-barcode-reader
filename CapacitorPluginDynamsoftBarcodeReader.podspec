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
  s.source_files = 'ios/Plugin/**/*.{swift,h,m,c,cc,mm,cpp}'
  s.libraries = 'c++'
  s.ios.deployment_target  = '12.0'
  s.dependency 'Capacitor'
  s.static_framework = true
  s.dependency 'DynamsoftCameraEnhancer', '= 2.1.3'
  s.dependency 'DynamsoftBarcodeReader', '= 9.0.0'
  s.swift_version = '5.1'
end
