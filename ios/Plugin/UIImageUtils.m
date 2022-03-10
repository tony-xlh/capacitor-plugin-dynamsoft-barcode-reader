//
//  UIImageUtils.m
//  CapacitorPluginDynamsoftBarcodeReader
//
//  Created by xulihang on 2022/3/10.
//

#import <Foundation/Foundation.h>
#import "UIImageUtils.h"

CGFloat degreesToRadians(CGFloat degrees) {return degrees * M_PI / 180;};

@implementation UIImageUtils


- (UIImage *)imageRotatedByDegrees:(CGFloat)degrees image: (UIImage*) image {
    __block UIImage *rotated;
    dispatch_sync(dispatch_get_main_queue(), ^{
        UIView *rotatedViewBox = [[UIView alloc] initWithFrame:CGRectMake(0,0,image.size.width, image.size.height)];
        CGAffineTransform t = CGAffineTransformMakeRotation(degreesToRadians(degrees));
        rotatedViewBox.transform = t;
        CGSize rotatedSize = rotatedViewBox.frame.size;
        // Create the bitmap context
        UIGraphicsBeginImageContext(rotatedSize);
        CGContextRef bitmap = UIGraphicsGetCurrentContext();
        // Move the origin to the middle of the image so we will rotate and scale around the center.
        CGContextTranslateCTM(bitmap, rotatedSize.width/2, rotatedSize.height/2);
        // Rotate the image context
        CGContextRotateCTM(bitmap, degreesToRadians(degrees));
        // Now, draw the rotated/scaled image into the context
        CGContextScaleCTM(bitmap, 1.0, -1.0);
        CGContextDrawImage(bitmap, CGRectMake(-image.size.width / 2, -image.size.height / 2, image.size.width, image.size.height), [image CGImage]);
        UIImage *newImage = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();
        rotated = newImage;
    });
    return rotated;
}

@end
