//
//  Sendpost.m
//  httppost
//
//  Created by Neoba Systems on 27/09/14.
//  Copyright (c) 2014 Neoba Systems. All rights reserved.
//

#import "Sendpost.h"

@implementation Sendpost
+(NSData *)sendreq:(NSMutableData *)body
{
   
    NSURLResponse *res=nil;
    NSError *err=nil;
    NSData *mresp=nil;
    NSMutableURLRequest *req=[NSMutableURLRequest requestWithURL:[NSURL URLWithString:@"http://localhost:2811"]];
    [req setHTTPMethod:@"POST"];
    [req setValue:@"OSX Mavericks" forHTTPHeaderField:@"User-Agent"];
    [req setValue:@"en-US,en;q=0.5" forHTTPHeaderField:@"Accept-Language"];
    [req setValue:@"application/octet-stream" forHTTPHeaderField:@"Content-Type"];
    [req setHTTPBody:body];
    mresp =[NSURLConnection sendSynchronousRequest:req returningResponse:&res error:&err];
    if(err)
    {
        NSLog(@"%@",err);
        return nil;
    }
    return mresp;

}
@end
