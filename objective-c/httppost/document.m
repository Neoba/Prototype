//
//  document.m
//  httppost
//
//  Created by Neoba Systems on 02/11/14.
//  Copyright (c) 2014 Neoba Systems. All rights reserved.
//

#import "document.h"

@implementation document
@synthesize did,age,text,owns,permission;
-(void)setdocument:(NSUUID *)did1 age:(int)age1 text:(NSString *)text1 permission:(Byte)p owns:(Boolean)o
{
    did=did1;
    age=age1;
    text=text1;
    permission=p;
    owns=o;
}
-(id)init
{
    did=nil;
    age=-1;
    text=nil;
    permission=0x1;
    owns=1;
    return self;
}
@end
