//
//  document.h
//  httppost
//
//  Created by Neoba Systems on 02/11/14.
//  Copyright (c) 2014 Neoba Systems. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface document : NSObject
@property NSUUID *did;
@property int age;
@property NSString *text;
@property Byte permission;
@property Boolean owns;
-(id)init;
-(void)setdocument:(NSUUID *)did age:(int)age text:(NSString *)text permission:(Byte)p owns:(Boolean)o;
@end
