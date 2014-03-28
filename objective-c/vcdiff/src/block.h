
//prototype of block class
//used to store a string and offset
@interface block :NSObject  //contains a string of fixed blocksize and starting offset of that in the source text
{
	int offset;
	NSMutableString *text;
	
}
-(void) initwith:(NSMutableString *) str:(int) off; //allocates and initializes variables
//usage [b initwith] where is an object of block class

-(NSMutableString *) gettext;
//usage text=[b gettext]

-(int) getoffset;
//o=[b getoffset]

@end