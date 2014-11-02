//
//  Sendpost.h
//  httppost
//
//  Created by Neoba Systems on 27/09/14.
//  Copyright (c) 2014 Neoba Systems. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface Sendpost : NSObject
+(NSMutableData *)sendreq:(NSMutableData *)body;
@end
