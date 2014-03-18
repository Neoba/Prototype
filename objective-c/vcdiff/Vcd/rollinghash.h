//
//  rollinghash.h
//  Vcd
//
//  Created by Neoba Systems on 09/03/14.
//  Copyright 2014 __MyCompanyName__. All rights reserved.
//


@interface rollinghash : NSObject {
	int base;
	int mod;
	int lastpower;
	NSMutableString *laststring;
	int lasthash;

}
-(void) initwith;
-(int) moduloexponent:(int) base:(int) power:(int) modulo;
-(int) hash: (NSMutableString *) tohash;
-(int) nexthash:(char) toadd;
@end
