

#import "blocktext.h"

//definition of blocktext class

@implementation blocktext

//Function to split and add the source text into array of blocks according to the given blocksize
-(void)initwith:(NSMutableString *)orgtxt:(int) bsize
{
	NSMutableString *s1=[[NSMutableString alloc]init];
	blocks=[[NSMutableArray alloc] init];
	block *b;
	originaltext=orgtxt;
	blocksize=bsize;
	int i,len,endindex;//endindex used to obtain substring from originaltext
	len=[originaltext length];
	for(i=0;i<len;i+=blocksize)
	{
		
		endindex=i+blocksize>=len?len:i+blocksize;//endindex must be len for the last block
		s1=[NSMutableString stringWithFormat:@"%@",[originaltext substringWithRange:NSMakeRange(i,endindex-i)]];//Splits the string from i to endindex
		b=[[block alloc] init]; //Allocates memory for each blocks
		[b initwith:s1:i];
		[blocks addObject:b];//Adds to the array
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
