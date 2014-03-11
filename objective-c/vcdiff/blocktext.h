//
//  blocktext.h
//  Vcd
//
//  Created by Akash K Sunny on 08/03/14.
//  Copyright 2014 Neoba Systems. All rights reserved.
//

#include "block.h"

@interface blocktext : NSObject {
	NSString *originaltext;
	int blocksize;
	NSMutableArray *blocks;

}
-(void)initwith:(NSString *)orgtxt: (int) bsize;
-(NSMutableArray *)getblocks;
-(NSString *)getoriginaltext;
-(int)getblocksize;
@end
