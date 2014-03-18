//
//  vcdiff.m
//  Vcd
//
//  Created by Akash K Sunny on 15/03/14.
//  Copyright 2014 Neoba Systems. All rights reserved.
//

#import "vcdiff.h"


@implementation vcdiff
-(void) initwith:(int) s
{
	hash=[[rollinghash alloc] init];
	[hash initwith];
	dicttext=[[dictionary alloc] init];
	[dicttext initwith];
	blocksize=s;
}
-(NSMutableArray *) encode:(NSMutableString *) dict:(NSMutableString *) target
{
	NSMutableArray * diffstring=[[NSMutableArray alloc] init];
	int targetlength;
	int targetindex;
	int currenthash;
	NSMutableString *addbuffer=[NSMutableString alloc];
	NSNumber *len,*of;
	addbuffer=[addbuffer initWithFormat:@"" ];
	block *match=[[block alloc] init];
	blocktext *bt=[[blocktext alloc] init];
	if([dict isEqual:target])
	{
		return diffstring;
	}
	[bt initwith:dict :blocksize];
	[dicttext populatedictionary:bt :hash];
	targetlength=[target length];
	targetindex=0;
	currenthash=-1;
	while (targetindex<targetlength) 
	{
		
		if (targetlength-targetindex<blocksize) 
		{
			[addbuffer appendString:[target substringFromIndex:targetindex]];
			[diffstring addObject:addbuffer];
			break;
		}
		else
		{
			if (currenthash==-1)
			{
				currenthash=[hash hash:[[NSMutableString alloc] initWithFormat:@"%@",[target substringWithRange:NSMakeRange(targetindex, blocksize)]]];
			}
			else 
			{
				currenthash=[hash nexthash:[target characterAtIndex:targetindex+blocksize-1]];
				if(currenthash<0)
				{currenthash=[hash hash:[[NSMutableString alloc] initWithFormat:@"%@",[target substringToIndex:targetindex+blocksize]]];}
			}
			match=[dicttext getmatch:currenthash :blocksize :[[NSMutableString alloc] initWithFormat:@"%@",[target substringFromIndex:targetindex]]];
			if (match==NULL) 
			{
				[addbuffer appendString:[NSMutableString stringWithFormat:@"%c",[target characterAtIndex:targetindex]]];
				targetindex=targetindex+1;
				
			}	
			else 
			{
				
				
				if([addbuffer length]>0)
				{
					
					
					[diffstring addObject:addbuffer];
					
					addbuffer=[NSMutableString stringWithFormat:@""];
					
				
				}
				//temp=[NSMutableString stringWithFormat:@"%i",[match getoffset]];
				of=[NSNumber alloc];
				of=[NSNumber numberWithInt:[match getoffset]];
				[diffstring addObject:of];
				len=[NSNumber alloc];
				len=[NSNumber numberWithInt:[[match gettext] length]];
				//[[NSMutableString alloc] initWithFormat:@"%i",[[match gettext] length]];
				[diffstring addObject:len];
				targetindex+=[[match gettext] length];
				currenthash=-1;
			}

		}
	}
	return diffstring;
	
}
-(NSMutableString *) decode:(NSMutableString *) dict:(NSMutableArray *) diff
{
	
	NSMutableArray *output=[[NSMutableArray alloc] init];
	NSMutableString *outs=[[NSMutableString alloc] initWithFormat:@"" ];
	long int i,r1,r2;
	if ([diff count]==0) 
	{
		return dict;
	}
	
	for(i=0;i<[diff count];++i)
	{
		
		if ([[diff objectAtIndex:i] isKindOfClass:[NSNumber class]]==YES) 
		{
			
			r1=[[diff objectAtIndex:i] intValue];
		
			r2=[[diff objectAtIndex:i+1] intValue];
				
			[output addObject:[dict substringWithRange:NSMakeRange(r1,r2)]];
						i+=1;
		}
			 else if([[diff objectAtIndex:i] isKindOfClass:[NSString class]])
			 {
				
				 
				 [output addObject:[diff objectAtIndex:i]];
			 
			 }
	}
	
	for(i=0;i<[output count];++i)
	{
		

		[outs appendString:[output objectAtIndex:i]];
		
			 
	}
	return outs;
			 
}

@end
