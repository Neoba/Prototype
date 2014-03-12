//
//  block.m
//  Vcd
//
//  Created by Akash K Sunny on 08/03/14.
//  Copyright 2014 Neoba Systems. All rights reserved.
//

#import "block.h"


@implementation block
-(void) initwith:(NSMutableString *) str:(int) off
{
	text=str;
	offset=off;
	nextblock=NULL;
	
}
-(NSMutableString *) gettext
{
	return text;
}

-(int) getoffset
{
	return offset;
}
-(void) setnextblock:(block *) b
{
	nextblock=b;
}
-(block *) getnextblock
{
	return nextblock;
}
-(id) copyWithZone: (NSZone *) zone 
{
block *newblock = [[block allocWithZone: zone] init];
[newblock initwith:text:offset]; 
	return newblock;
}
@end
