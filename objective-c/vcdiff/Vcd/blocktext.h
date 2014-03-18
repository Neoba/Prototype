//
//  blocktext.h
//  Vcd
//
//  Created by Neoba Systems on 08/03/14.
//  Copyright 2014 __MyCompanyName__. All rights reserved.
//

#include "block.h"

@interface blocktext : NSObject {
	NSMutableString *originaltext;
	int blocksize;
	NSMutableArray *blocks;

}
-(void)initwith:(NSMutableString *)orgtxt: (int) bsize;
-(NSMutableArray *)getblocks;
-(NSMutableString *)getoriginaltext;
-(int)getblocksize;
@end
