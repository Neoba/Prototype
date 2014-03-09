//
//  blocktext.m
//  Vcd
//
//  Created by Akash K Sunny on 08/03/14.
//  Copyright 2014 Neoba Systems. All rights reserved.
//

#import "blocktext.h"


@implementation blocktext
-(void)initwith:(NSString *)orgtxt:(int) bsize
{
	NSString *s1=[[NSString alloc]init];
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
		/*for(j=0;j<[blocks count];++j)
		{
			NSLog(@"%@%d\n",[[blocks objectAtIndex:j] gettext],j);
		}	*/
	}
			
}
-(NSMutableArray *)getblocks
{
	
	return blocks;
}
-(NSString *)getoriginaltext
{
	return originaltext;
}
-(int)getblocksize
{
	return blocksize;
}
@end
