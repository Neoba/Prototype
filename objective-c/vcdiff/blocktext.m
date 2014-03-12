//
//  blocktext.m
//  Vcd
//
//  Created by Akash K Sunny on 08/03/14.
//  Copyright 2014 Neoba Systems. All rights reserved.
//

#import "blocktext.h"


@implementation blocktext
-(void)initwith:(NSMutableString *)orgtxt:(int) bsize
{
	NSMutableString *s1=[[NSMutableString alloc]init];
	blocks=[[NSMutableArray alloc] init];
	block *b=[[block alloc] init];
	originaltext=orgtxt;
	blocksize=bsize;
	int i,len,endindex;
	len=[originaltext length];
	for(i=0;i<len;i+=blocksize)
	{
		
		endindex=i+blocksize>=len?len:i+blocksize;
		s1=[originaltext substringWithRange:NSMakeRange(i,endindex-i)];
		
		[b initwith:s1:i];
		[blocks addObject:[b copy]];
			}
			
}
-(NSMutableArray *)getblocks
{
	
	return blocks;
}
-(NSMutableString *)getoriginaltext
{
	return originaltext;
}
-(int)getblocksize
{
	return blocksize;
}
@end
