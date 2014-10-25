//
//  PacketForger.h
//  httppost
//
//  Created by Neoba Systems on 23/10/14.
//  Copyright (c) 2014 Neoba Systems. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface PacketForger : NSObject

+(void)pinger;
+(UInt16)createUser:(NSString *)name password:(NSString *)passw;
+(UInt16)login:(NSString *)name password:(NSString *)passw putcookie:(NSUUID **)cookie;
+(UInt16)createDocUsercookie:(NSUUID *)cookie putDocid:(NSUUID **)docid;
+(UInt16)deleteDocUsercookie:(NSUUID *)cookie withDocid:(NSUUID *)docid;
+(UInt16)followUser:(NSString *)name cookie:(NSUUID *)cookie;
+(UInt16)unfollowUser:(NSString *)name cookie:(NSUUID *)cookie;
+(UInt16)logoutCookie:(NSUUID **)cookie;
+(void)getDigestwith:(NSUUID *)cookie followers:(NSMutableDictionary *)dfollowers followings:(NSMutableDictionary *)dfollowings docs:(NSMutableArray *)docs;
+(UInt16)pokeUser:(NSString *)name cookie:(NSUUID *)cookie;
+(UInt16)grant:(NSUUID *)cookie doc:(NSUUID *)doc userpermissions:(NSMutableDictionary *)userp;
@end
