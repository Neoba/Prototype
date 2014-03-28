

#import "block.h"

//definition of block class
@implementation block
-(void) initwith:(NSMutableString *) str:(int) off //allocates and initialises the members with the given values
{
	text=[NSMutableString alloc];
	text=str;
	offset=off;
	
}
-(NSMutableString *) gettext    //Returns text
{
	return text;
}

-(int) getoffset
{
	return offset;
}

@end
