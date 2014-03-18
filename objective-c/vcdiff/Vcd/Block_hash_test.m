#import <Foundation/Foundation.h>
#include "blocktext.h"


int main(int argc, char *argv[])
{
	NSAutoreleasePool *pool=[[NSAutoreleasePool alloc] init];
	block *b;
	blocktext *bt;
	NSMutableArray *bs;
	int j;
	bt=[[blocktext alloc] init];
	b=[[block alloc] init];
	[bt initwith:@"Akash k sunn":5];
	bs=[bt getblocks];
	NSLog(@"%@",[bt getoriginaltext]);
	 for(j=0;j<[bs count];++j)
	 {
		 NSLog(@"%@ %d\n",[[bs objectAtIndex:j] gettext],[[bs objectAtIndex:j] getoffset]);
	 }	
	[pool drain];
	return 0;	
}
