
#import "vcdiff.h"
//definition of vcdiff class

@implementation vcdiff
-(void) initwith:(int) s //allocating and initializing members
{
	hash=[[rollinghash alloc] init];
	[hash initwith];
	dicttext=[[dictionary alloc] init];
	[dicttext initwith];
	blocksize=s;
}
-(NSMutableArray *) encode:(NSMutableString *) dict:(NSMutableString *) target  //dict is source text and target is target text
{
	NSMutableArray * diffstring=[[NSMutableArray alloc] init];
	int targetlength;
	int targetindex;
	int currenthash;
	NSMutableString *addbuffer=[NSMutableString alloc];
	NSNumber *len,*of;
	addbuffer=[addbuffer initWithFormat:@"" ];//addbuffer stores unmatched data in target b/w 2 successive matches
	block *match=[[block alloc] init];
	blocktext *bt=[[blocktext alloc] init];
	if([dict isEqual:target])//is source and target are same, diff is null
	{
		return diffstring;
	}
	[bt initwith:dict :blocksize];//creating blocks from dict
	[dicttext populatedictionary:bt :hash]; //filling the hash-block pairs into the map
	targetlength=[target length];
	targetindex=0;
	currenthash=-1;
	while (targetindex<targetlength) //traversing through each char in target
	{
		
		if (targetlength-targetindex<blocksize) //if targetlength is not a multiple of blocksize, string from the end will not get a match
		{
			[addbuffer appendString:[target substringFromIndex:targetindex]];//adding the remaining chars in target to addbuffer
			[diffstring addObject:addbuffer];//adding the addbuffer and breaking
			break;
		}
		else
		{
			if (currenthash==-1)//either at the first iterartion or after a match of number of chars>blocksize
			{   //rolling hash cannot be used as more than one new char is added to the new string and hence using hash
				currenthash=[hash hash:[[NSMutableString alloc] initWithFormat:@"%@",[target substringWithRange:NSMakeRange(targetindex, blocksize)]]];
			}
			else //adding a new char to end and removing first char
			{    //using rolling hash
				currenthash=[hash nexthash:[target characterAtIndex:targetindex+blocksize-1]];
				if(currenthash<0)
				{currenthash=[hash hash:[[NSMutableString alloc] initWithFormat:@"%@",[target substringToIndex:targetindex+blocksize]]];}
			}
			match=[dicttext getmatch:currenthash :blocksize :[[NSMutableString alloc] initWithFormat:@"%@",[target substringFromIndex:targetindex]]];//getting a match for current substring if present
			if (match==NULL) //No match
			{
				[addbuffer appendString:[NSMutableString stringWithFormat:@"%c",[target characterAtIndex:targetindex]]];//adding the first char of target substring to addbuffer
				targetindex=targetindex+1;
				
			}	
			else //match
			{
				
				
				if([addbuffer length]>0)//addbuffer should be added before match
				{
					
					
					[diffstring addObject:addbuffer];
					
					addbuffer=[NSMutableString stringWithFormat:@""];
					
				
				}
				
				of=[NSNumber alloc];
				of=[NSNumber numberWithInt:[match getoffset]];   //starting offset of matched substring in source
				[diffstring addObject:of];                       //adding offset
				len=[NSNumber alloc];                            //length of matched substring
				len=[NSNumber numberWithInt:[[match gettext] length]];
				[diffstring addObject:len];                      //adding length
				targetindex+=[[match gettext] length];           //skipping already matched chars
				currenthash=-1;                                  //setting currenthash to use hash method
			}

		}
	}
	return diffstring;
	
}
-(NSMutableString *) decode:(NSMutableString *) dict:(NSMutableArray *) diff  //dict is source ad diff the diff data
{
	
	NSMutableArray *output=[[NSMutableArray alloc] init];//array of strings that can make up target
	NSMutableString *outs=[[NSMutableString alloc] initWithFormat:@"" ];
	long int i,r1,r2;
	if ([diff count]==0) //in case of null source and target the same
	{
		return dict;
	}
	
	for(i=0;i<[diff count];++i)//traversing through each obj in diff
	{
		
		if ([[diff objectAtIndex:i] isKindOfClass:[NSNumber class]]==YES) //if the obj is a number(offset in source)
		{
			
			r1=[[diff objectAtIndex:i] intValue];//converting number obj into int (offset)
		
			r2=[[diff objectAtIndex:i+1] intValue];//match length
				
			[output addObject:[dict substringWithRange:NSMakeRange(r1,r2)]];//otaining the substring from source and adding to output
						i+=1;
		}
			 else if([[diff objectAtIndex:i] isKindOfClass:[NSString class]])//unmatched strings in target data
			 {
				
				 
				 [output addObject:[diff objectAtIndex:i]];//adding the string
			 
			 }
	}
	
	for(i=0;i<[output count];++i)//converting the array of strings output into the target string outs
	{
		

		[outs appendString:[output objectAtIndex:i]];
		
			 
	}
	return outs;
			 
}

@end
