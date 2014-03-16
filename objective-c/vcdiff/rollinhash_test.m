#import <Foundation/Foundation.h>
#include "blocktext.h"
#include "rollinghash.h"

int main(int argc, char *argv[])
{
	int h=1,i;
	NSAutoreleasePool *pool=[[NSAutoreleasePool alloc] init];
	rollinghash *rh=[[rollinghash alloc] init];
	[rh initwith];
	NSMutableString *str=[[NSMutableString alloc] init];
	[str setString:@"a"];
	h=[rh hash:str];
	i=[rh nexthash:'b'];
	NSLog(@"%i %i",h,i);
	[pool drain];
	return 0;	
}
