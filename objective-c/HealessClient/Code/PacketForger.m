//
//  PacketForger.m
//  httppost
//
//  Created by Neoba Systems on 23/10/14.
//  Copyright (c) 2014 Neoba Systems. All rights reserved.
//
#import <CommonCrypto/CommonDigest.h>
#import "PacketForger.h"
#import "Sendpost.h"
@implementation PacketForger
const Byte ver=2;
const Byte console[7]={'C','O','N','S','O','L','E'};
+(void)pinger
{
    
    const char pingd[]={0x02,0x01,'P','I','N','G'};
    Byte pong[10];
    NSMutableData *body=[NSMutableData dataWithBytes:pingd length:sizeof(pingd)];
    [[Sendpost sendreq:body] getBytes:pong];
    pong[6]='\0';
    NSLog(@"%s",pong);
}
+(UInt16)createUser:(NSString *)name password:(NSString *)passw
{
    const Byte code=5;
    UInt16 rstatus;
    NSMutableData *body=[[NSMutableData alloc] init];
    char *cname,cpass[CC_SHA1_DIGEST_LENGTH];
    NSData *passdata=[passw dataUsingEncoding:NSUTF8StringEncoding];
    CC_SHA1([passdata bytes],[passdata length], cpass);
    int namel=(int)[name length];
   
    cname=malloc(namel);
    
    
    [name getCString:cname maxLength:20 encoding:NSUTF8StringEncoding];
    namel=OSSwapBigToHostInt32(namel);
    
    //Forging packet
    [body appendBytes:&ver length:1];
    [body appendBytes:&code length:1];
    [body appendBytes:&namel length:sizeof(int)];
    [body appendBytes:cname length:[name length]];
    [body appendBytes:cpass length:20];
    
    //Send Packet
    
    NSRange range=NSMakeRange(4, 2);
    [[Sendpost sendreq:body] getBytes:&rstatus range:range];
    free(cname);
    return rstatus;
}
+(UInt16)login:(NSString *)name password:(NSString *)passw putcookie:(NSUUID **)cookie
{
    const Byte code=6;
    const Byte consolecode=0x0C;
    const int consolelength=7;
    UInt16 rstatus;
    NSMutableData *body=[[NSMutableData alloc] init],*result=nil;
    char *cname,cpass[CC_SHA1_DIGEST_LENGTH];
    NSData *passdata=[passw dataUsingEncoding:NSUTF8StringEncoding];
    CC_SHA1([passdata bytes],[passdata length], cpass);
    int namel=(int)[name length];
   
    cname=malloc(namel);
  
   
    [name getCString:cname maxLength:20 encoding:NSUTF8StringEncoding];
    namel=OSSwapBigToHostInt32(namel);
    
    [body appendBytes:&ver length:1];
    [body appendBytes:&code length:1];
    [body appendBytes:&namel length:sizeof(int)];
    [body appendBytes:cname length:[name length]];
    [body appendBytes:cpass length:20];
    [body appendBytes:&consolecode length:1];
    [body appendBytes:&consolelength length:4];
    [body appendBytes:console length:7];
    
    
    *cookie=nil;
    uuid_t cookieflat;
    
    NSRange range=NSMakeRange(4, 2);
    result=[Sendpost sendreq:body];
    [result getBytes:&rstatus range:range];
    NSLog(@"%x",rstatus);
    if(rstatus==0xFFFF)
    {
        range=NSMakeRange(6, 16);
        [result getBytes:cookieflat range:range];
        *cookie=[[NSUUID alloc] initWithUUIDBytes:cookieflat];
        
    }
    free(cname);
   
    
    return rstatus;
}
+(UInt16)followUser:(NSString *)name cookie:(NSUUID *)cookie
{
    const Byte code=0x07;
    int namel=(int)[name length];
    char *cname;
    uuid_t cookieflat;
    UInt16 rstatus;
    [cookie getUUIDBytes:cookieflat];
    cname=malloc(namel);
    [name getCString:cname maxLength:20 encoding:NSUTF8StringEncoding];
    namel=OSSwapBigToHostInt32(namel);
    NSMutableData *body=[[NSMutableData alloc] init],*result=nil;

   
    [body appendBytes:&ver length:1];
    [body appendBytes:&code length:1];
    [body appendBytes:&namel length:sizeof(int)];
    [body appendBytes:cookieflat length:16];
    [body appendBytes:cname length:strlen(cname)];
    
    result=[Sendpost sendreq:body];
    NSRange range=NSMakeRange(4, 2);
    [result getBytes:&rstatus range:range];
    return rstatus;
}
+(UInt16)unfollowUser:(NSString *)name cookie:(NSUUID *)cookie
{
    const Byte code=0x0C;
    int namel=(int)[name length];
    char *cname;
    uuid_t cookieflat;
    UInt16 rstatus;
    [cookie getUUIDBytes:cookieflat];
    cname=malloc(namel);
    [name getCString:cname maxLength:20 encoding:NSUTF8StringEncoding];
    namel=OSSwapBigToHostInt32(namel);
    NSMutableData *body=[[NSMutableData alloc] init],*result=nil;
    
    
    [body appendBytes:&ver length:1];
    [body appendBytes:&code length:1];
    [body appendBytes:&namel length:sizeof(int)];
    [body appendBytes:cookieflat length:16];
    [body appendBytes:cname length:strlen(cname)];
    
    result=[Sendpost sendreq:body];
    NSRange range=NSMakeRange(4, 2);
    [result getBytes:&rstatus range:range];
    return rstatus;
}
+(UInt16)logoutCookie:(NSUUID **)cookie
{
    const Byte code=9;
    int status=0xffff;
    UInt16 rstatus;
    uuid_t cookieflat;
    [*cookie getUUIDBytes:cookieflat];
    status=OSSwapBigToHostInt32(status);
    NSMutableData *body=[[NSMutableData alloc] init],*result=nil;
    
    [body appendBytes:&ver length:1];
    [body appendBytes:&code length:1];
    [body appendBytes:&status length:4];
    [body appendBytes:cookieflat length:16];
    result=[Sendpost sendreq:body];
    NSRange range=NSMakeRange(4, 2);
    [result getBytes:&rstatus range:range];
    if(rstatus==0xffff)
        *cookie=nil;
    return rstatus;
}
+(UInt16)createDocUsercookie:(NSUUID *)cookie putDocid:(NSUUID **)docid
{
    const Byte code=0x02;
    int status=0xffff;
    UInt16 rstatus;
    *docid=[[NSUUID alloc] init];
    uuid_t cookieflat,docidflat;
    status=OSSwapBigToHostInt32(status);
    [cookie getUUIDBytes:cookieflat];
    [*docid getUUIDBytes:docidflat];
    NSMutableData *body=[[NSMutableData alloc] init],*result=nil;
    
    [body appendBytes:&ver length:1];
    [body appendBytes:&code length:1];
    [body appendBytes:&status length:4];
    [body appendBytes:cookieflat length:16];
    [body appendBytes:docidflat length:16];
    
    result=[Sendpost sendreq:body];
    NSRange range=NSMakeRange(4, 2);
    [result getBytes:&rstatus range:range];
    if(rstatus!=0xffff)
        *docid=nil;
    return rstatus;

}
+(UInt16)deleteDocUsercookie:(NSUUID *)cookie withDocid:(NSUUID *)docid
{
    const Byte code=0x0B;
    int status=0x0000DE1E;
    UInt16 rstatus;
    uuid_t cookieflat,docidflat;
    [cookie getUUIDBytes:cookieflat];
    [docid getUUIDBytes:docidflat];
    status=OSSwapBigToHostInt32(status);
    NSMutableData *body=[[NSMutableData alloc] init],*result=nil;
    
    [body appendBytes:&ver length:1];
    [body appendBytes:&code length:1];
    [body appendBytes:&status length:4];
    [body appendBytes:cookieflat length:16];
    [body appendBytes:docidflat length:16];
    
    result=[Sendpost sendreq:body];
    NSRange range=NSMakeRange(4, 2);
    [result getBytes:&rstatus range:range];
    
    return rstatus;

}
+(void)getDigestwith:(NSUUID *)cookie followers:(NSMutableDictionary *)dfollowers followings:(NSMutableDictionary *) dfollowings docs:(NSMutableArray *) docs
{
    const Byte code=0x04;
    int status=0xFFFF;
    status=OSSwapBigToHostInt32(status);
    uuid_t cookieflat;
    
    [cookie getUUIDBytes:cookieflat];
     NSMutableData *body=[[NSMutableData alloc] init],*dgst=nil;
    
    [body appendBytes:&ver length:1];
    [body appendBytes:&code length:1];
    [body appendBytes:&status length:4];
    [body appendBytes:cookieflat length:16];
    
    dgst=[Sendpost sendreq:body];
    
    
    
    if ([dgst length]>6) {    //No success code for gd, so relying on length
        NSLog(@"Got digest");
        int doccount,sdict,sdiff,stitle,followerc;
        NSRange range;
        uuid_t docidflat;
        range=NSMakeRange(2, 4);
        [dgst getBytes:&doccount range:range];
        doccount=OSSwapBigToHostInt32(doccount);
        int base=6;
        
        for (int i=0; i<doccount; ++i) {
            
            range=NSMakeRange(base, 16);
            [dgst getBytes:docidflat range:range];
            
            range=NSMakeRange(base+16, 4);
            [dgst getBytes:&sdiff range:range];
            sdiff=OSSwapBigToHostInt32(sdiff);
            range=NSMakeRange(base+20+sdiff, 4);
            [dgst getBytes:&sdict range:range];
            sdict=OSSwapBigToHostInt32(sdict);
            range=NSMakeRange(base+20+sdiff+sdict, 4);
            [dgst getBytes:&stitle range:range];
            stitle=OSSwapBigToHostInt32(stitle);
            
            base = base + 32 + sdiff + sdict + stitle + 1 + 1;
            NSUUID *tempdid=[[NSUUID alloc] initWithUUIDBytes:docidflat];
            if (![docs containsObject:tempdid]) {
                [docs addObject:tempdid];
            }
            
        }
        
        
        range=NSMakeRange(base, 4);
        [dgst getBytes:&followerc range:range];
        followerc=OSSwapBigToHostInt32(followerc);
        base+=4;
        
        
        int strc;
        for (int i = 0; i < followerc; i++) {
            UInt64 fid;
            range=NSMakeRange(base, 4);
            [dgst getBytes:&strc range:range];
            base += 4;
            strc=OSSwapBigToHostInt32(strc);
            
            char *ff = malloc(strc);
            
            range=NSMakeRange(base, strc);
            [dgst getBytes:ff range:range];
            base += strc;
            NSString *fname=[[NSString alloc] initWithBytes:ff length:strc encoding:NSUTF8StringEncoding];
            
            
            range=NSMakeRange(base, 8);
            [dgst getBytes:&fid range:range];
            fid=OSSwapBigToHostInt64(fid);
          
            base += 8;
            free(ff);
            NSNumber *uid=[NSNumber numberWithLong: fid];
            if (![dfollowers doesContain:uid]) {
                 [dfollowers setValue:uid forKey:fname];
            }
           
        }
        
        
        range=NSMakeRange(base, 4);
        [dgst getBytes:&followerc range:range];
        followerc=OSSwapBigToHostInt32(followerc);
        
        base+=4;
        for (int i = 0; i < followerc; i++) {
            UInt64 fid;
            range=NSMakeRange(base, 4);
            [dgst getBytes:&strc range:range];
            base += 4;
            strc=OSSwapBigToHostInt32(strc);
            char *ff = malloc(strc);
            range=NSMakeRange(base, strc);
            [dgst getBytes:ff range:range];
            base += strc;
            
            NSString *fname=[[NSString alloc] initWithBytes:ff length:strc encoding:NSUTF8StringEncoding];
            
            range=NSMakeRange(base, 8);
            [dgst getBytes:&fid range:range];
            fid=OSSwapBigToHostInt64(fid);
           
            base += 8;
            free(ff);
             NSNumber *uid=[NSNumber numberWithLong: fid];
            if (![dfollowings doesContain:uid]) {
                [dfollowings setValue:uid forKey:fname];
            }
        }
    }
}
+(UInt16)pokeUser:(NSString *)name cookie:(NSUUID *)cookie
{
const Byte code=0x0A;
    int namel=(int)[name length];
    char *cname;
    uuid_t cookieflat;
    UInt16 rstatus;
    [cookie getUUIDBytes:cookieflat];
    cname=malloc(namel);
    [name getCString:cname maxLength:20 encoding:NSUTF8StringEncoding];
    namel=OSSwapBigToHostInt32(namel);
    NSMutableData *body=[[NSMutableData alloc] init],*result=nil;
    
    
    [body appendBytes:&ver length:1];
    [body appendBytes:&code length:1];
    [body appendBytes:&namel length:sizeof(int)];
    [body appendBytes:cookieflat length:16];
    [body appendBytes:cname length:strlen(cname)];
    
    result=[Sendpost sendreq:body];
    NSRange range=NSMakeRange(4, 2);
    [result getBytes:&rstatus range:range];
    return rstatus;

}
+(UInt16)grant:(NSUUID *)cookie doc:(NSUUID *)doc userpermissions:(NSMutableDictionary *)userp
{
    const Byte code=0x08;
    int length=[[userp allKeys] count];
    uuid_t cookieflat,docid;
    [cookie getUUIDBytes:cookieflat];
    [doc getUUIDBytes:docid];
    length=OSSwapBigToHostInt32(length);
    NSMutableData *body=[[NSMutableData alloc] init],*result=nil;
    
    [body appendBytes:&ver length:1];
    [body appendBytes:&code length:1];
    [body appendBytes:&length length:sizeof(int)];
    [body appendBytes:cookieflat length:16];
    [body appendBytes:docid length:16];
    
    length=OSSwapBigToHostInt32(length);
    
    for (NSNumber *uid in userp) {
        Byte perm=(Byte)[[userp objectForKey:uid] intValue];
        [body appendBytes:&perm length:1];
        long uidl=[uid longValue];
        uidl=OSSwapBigToHostInt64(uidl);
        [body appendBytes:&uidl length:64];
    }
    UInt16 rstatus;
    result=[Sendpost sendreq:body];
    NSRange range=NSMakeRange(4, 2);
    [result getBytes:&rstatus range:range];
    
    return rstatus;
}
@end
