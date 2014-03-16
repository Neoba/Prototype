#import <Foundation/Foundation.h>
#include "dictionary.h"
#define SIZE 3
int main(int argc, char *argv[])
{
	NSAutoreleasePool *pool=[[NSAutoreleasePool alloc] init];
	int i,has;
	
	block *blk;
	blocktext *b= [[blocktext alloc] init];
	dictionary *d=[[dictionary alloc] init];
	rollinghash *h=[[rollinghash alloc] init];
	[h initwith];
	[d initwith];
	NSMutableString *dict=[[NSMutableString alloc] init];
	[dict setString:@"Akassh k Sunny"];
	NSMutableString *target=[[NSMutableString alloc] init],*tb=[[NSMutableString alloc] init];
	[target setString:@"Akasch k Sunny"];	
	[b initwith: dict:SIZE];
	[d populatedictionary:b :h];
	for(i=0;i<=[target length]-SIZE;++i)
	{
		[tb setString:[target substringWithRange:NSMakeRange(i, SIZE)]];
			has=[h hash:tb];
		blk=[[block alloc] init];
		if(blk=[d getmatch:has :SIZE :[target substringFromIndex:i]])
			NSLog(@"%d %@ %d",i,[blk gettext],[blk getoffset]);
	
	}
	
	
	return 0;	
	[pool drain];
}
