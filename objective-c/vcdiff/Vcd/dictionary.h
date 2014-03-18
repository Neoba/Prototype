//
//  dictionary.h
//  Vcd
//
//  Created by Akash K Sunny on 09/03/14.
//  Copyright 2014 Neoba Systems. All rights reserved.
//
#import "blocktext.h"
#import "rollinghash.h"
@interface dictionary : NSObject {
	NSMutableDictionary *dictionar;
	blocktext *dictionarytext;
}
-(void) initwith;
-(void) put:(int) ke:(block *) blk;
-(void) populatedictionary:(blocktext *) dicttext:(rollinghash*) hasher;
-(block *) getmatch:(int) has:(int) blocksize:(NSMutableString *)target;
@end
