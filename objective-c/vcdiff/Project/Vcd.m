//main code encoding and decoding a string

#import <Foundation/Foundation.h>
#include "vcdiff.h"
#define BSIZE 2
int main(int argc, char *argv[])
{
	NSAutoreleasePool *pool=[[NSAutoreleasePool alloc] init];
	vcdiff* v=[[vcdiff alloc] init]; 	
	[v initwith:BSIZE];
	NSMutableString *src=[[NSMutableString alloc] initWithFormat:@"abcdefghijklmnop"];
	NSMutableString *tgt=[[NSMutableString alloc] initWithFormat:@"abcdwxyzefghefghefghzzzz"];
	NSMutableArray *delta=[v encode:src:tgt];  //delta is the vcdiff data
	NSLog(@"OUTPUT\nDELTA SIZE:%i\n%@",[delta count],[v decode:src:delta]);
	return 0;	
	[pool drain];
}
