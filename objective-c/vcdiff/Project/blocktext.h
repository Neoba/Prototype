

#include "block.h"
//prototype of blocktext class

@interface blocktext : NSObject //bloctext consists of the source text (originaltext), blocksize(size of text in each block), and an array of blocks
{
	NSMutableString *originaltext;
	int blocksize;
	NSMutableArray *blocks;

}
-(void)initwith:(NSMutableString *)orgtxt: (int) bsize; //initialises the blocks
//usage [bt initwith:orgtxt:bsize] bt object of blocktext class

-(NSMutableArray *)getblocks;
//blks=[bt getblocks]

-(NSMutableString *)getoriginaltext;
//text=[bt getoriginaltext]

-(int)getblocksize;
//bsize=[bt getblocksize]

@end
