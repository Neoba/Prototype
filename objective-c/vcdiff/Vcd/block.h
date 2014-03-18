//
//  block.h
//  Vcd
//
//  Created by Neoba Systems on 08/03/14.
//  Copyright 2014 __MyCompanyName__. All rights reserved.
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