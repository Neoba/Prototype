//
//  rollinghash.m
//  Vcd
//
//  Created by Akash K Sunny on 09/03/14.
//  Copyright 2014 Neoba Systems. All rights reserved.
//

#import "rollinghash.h"


@implementation rollinghash
-(void) initwith
{
	base=257;
	mod=1000000007;
	lastpower=0;
	laststring=[NSMutableString stringWithString:@""];
	lasthash=0;
}
-(int) moduloexponent:(int) bas:(int) power:(int) modulo
{
	int toreturn=1,i;
	for(i=0;i<power;i+=1)
		toreturn=(bas*toreturn)%modulo;
	return toreturn;
}
-(int) hash: (NSMutableString *) tohash
{
	int hash=0;
	int i,len;
	len=[tohash length];
	for(i=0;i<len;i+=1)
	{
		hash+=((int)[tohash characterAtIndex:i]*[self moduloexponent:base:len-i-1:mod]);
		hash%=mod;
	}
	lastpower=[self moduloexponent:base:len-1:mod];
	laststring=tohash;
	lasthash=hash;
	return hash;
}
-(int) nexthash:(char)toadd
{
	int hash=lasthash;
	hash-=lastpower*((int) [laststring characterAtIndex:0]);
	hash=hash*base+toadd;
	hash%=mod;
	if(hash<0)
		hash+=mod;
	[laststring deleteCharactersInRange:NSMakeRange(0, 1)];
	[laststring appendString:[NSMutableString stringWithFormat:@"%c", toadd]];
	lasthash=hash;
	return hash;
}
@end
