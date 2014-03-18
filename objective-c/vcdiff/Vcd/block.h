//
//  block.h
//  Vcd
//
//  Created by Akash K Sunny on 08/03/14.
//  Copyright 2014 Neoba Systems. All rights reserved.
//

@interface block :NSObject <NSCopying>
{
	int offset;
	NSMutableString *text;
	block *nextblock;
	
}
-(void) initwith:(NSMutableString *) str:(int) off; 
-(NSMutableString *) gettext;
-(int) getoffset;
-(void) setnextblock:(block *) b;
-(block *) getnextblock;
-(id) copyWithZone: (NSZone *) zone;
@end
