//
//  dictionary.m
//  Vcd
//
//  Created by Akash K Sunny on 09/03/14.
//  Copyright 2014 Neoba Systems. All rights reserved.
//

#import "dictionary.h"


@implementation dictionary
-(void) initwith
{
	dictionar=[[NSMutableDictionary alloc] init];
	dictionarytext=[[blocktext alloc] init];
	dictionarytext=NULL;
	
}

-(void) put:(int)ke :(block *)blk
{
	NSMutableString *key=[NSMutableString stringWithFormat:@"%d",ke];
NSMutableArray *q=[[NSMutableArray alloc] init];
	if([dictionar objectForKey:key]==nil)
	{
		[dictionar setObject:[[NSMutableArray alloc] init] forKey:key];
	}
	q=[dictionar objectForKey:key];
	[q addObject:blk];
	[dictionar setObject:q forKey:key];

}
-(void) populatedictionary:(blocktext *) dicttext:(rollinghash *) hasher
{
	
	dictionarytext=dicttext;
	NSMutableArray *blocks=[[NSMutableArray alloc] init];
	blocks=[dicttext getblocks];
	int len=[blocks count],i;
	for(i=0;i<len;++i)
		[self put:[hasher hash:[[blocks objectAtIndex:i] gettext]] :[blocks objectAtIndex:i]];
		
}
-(block *) getmatch:(int) has:(int) blocksize:(NSMutableString *)target
{
	NSMutableArray *blocks= [[NSMutableArray alloc] init];
	int i,len,currentpointer;
	NSMutableString *dicttext;
	NSMutableString *targettext;
	NSMutableString *hash=[NSMutableString stringWithFormat:@"%d",has],*t;
	if([dictionar objectForKey:hash]!=nil)
	{
		blocks=[dictionar objectForKey:hash];
		for(i=0,len=[blocks count];i<len;++i)
		{
			if([[[blocks objectAtIndex:i] gettext] isEqual:[target substringWithRange:NSMakeRange(0, blocksize)]])
				{
				if(dictionarytext!=NULL&&[[blocks objectAtIndex:i] getnextblock]==NULL)
					
				{
					
					t=[[NSMutableString alloc] init];
					[t setString :[[dictionarytext getoriginaltext] substringFromIndex:[[blocks objectAtIndex:i] getoffset]+blocksize]];
					dicttext=t;
							  t=[[NSMutableString alloc] init];
					[t setString:[target substringFromIndex:blocksize]];
							  targettext=t;
					if([dicttext length]==0 ||[targettext length]==0)
					{printf("1\n"); return [blocks objectAtIndex:i];}
				}
				currentpointer=0;
				while(currentpointer<[dicttext length] && currentpointer<[targettext length]&&[dicttext characterAtIndex:currentpointer]==[targettext characterAtIndex:currentpointer])
				{
					
					currentpointer+=1;
				}
				block *b=[[block alloc] init];
					NSMutableString *s=[[NSMutableString alloc] init];
					[s setString:[[blocks objectAtIndex:i] gettext]];
					[s appendString:[dicttext substringWithRange:NSMakeRange(0, currentpointer)]];
					
				[b initwith:s:[[blocks objectAtIndex:i] getoffset]];
					return b;
				}
				else
				{return [blocks objectAtIndex:i];}
		
		}
				return NULL;
	
	}
				return NULL;

}
@end
