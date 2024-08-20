//
//  Interoperator.m
//  Plugin
//
//  Created by xulihang on 2023/12/8.
//  Copyright © 2023 Max Lynch. All rights reserved.
//

#import "Interoperator.h"

@implementation Interoperator

- (UIImage*)getUIImage: (NSString *)className methodName: (NSString *)methodName;{
    const char * classNameChar = [className cStringUsingEncoding:NSUTF8StringEncoding];
    const char * methodNameChar = [methodName cStringUsingEncoding:NSUTF8StringEncoding];
    UIImage *image = ((UIImage* (*)(id, SEL))objc_msgSend)(objc_getClass(classNameChar), sel_registerName(methodNameChar));
    return image;
}

@end
