
#import "dictionary.h"

//definition of dictionary class
@implementation dictionary
-(void) initwith
{
	dictionar=[[NSMutableDictionary alloc] init];
	dictionarytext=NULL;
	
}

-(void) put:(int)ke :(block *)blk //for constructing the hash map(dictionary)
{
	NSMutableString *key=[NSMutableString stringWithFormat:@"%d",ke];//Converting int variable into an object for adding into map
	NSMutableArray *q=[[NSMutableArray alloc] init];
	if([dictionar objectForKey:key]==nil)//If the map do not contain the key(hash of block) 
	{
		[dictionar setObject:[[NSMutableArray alloc] init] forKey:key];//allocate space for adding blocks
	}
	q=[dictionar objectForKey:key]; //otaining the array of blocks of the given hash(key)
	[q addObject:blk];              //adding new block of of hash key to the end of array
	[dictionar setObject:q forKey:key]; //puting back the updated array into dictionary

}
-(void) populatedictionary:(blocktext *) dicttext:(rollinghash *) hasher //used with source text (dictionary text), The hash map (dictionary) is constructed using put()
{
	dictionarytext=[[blocktext alloc] init];
	dictionarytext=dicttext;       //source text
	NSMutableArray *blocks=[[NSMutableArray alloc] init];
	blocks=[dicttext getblocks];   //retreiving blocks from blocktext
	int len=[blocks count],i;
	for(i=0;i<len;++i)             //hashing and adding each block to the hashmap (dictionary)
		[self put:[hasher hash:[[blocks objectAtIndex:i] gettext]] :[blocks objectAtIndex:i]];
		
}
-(block *) getmatch:(int) has:(int) blocksize:(NSMutableString *)target //has is the hash of a part of target text of blocksize size obtained using the rollinghash function(nexthash())  
{                                                                       //target is substring of originat target text passed for finding matching characters beyond blocksize
	NSMutableArray *blocks= [[NSMutableArray alloc] init];
	int i,len,currentpointer;
	NSMutableString *dicttext;
	NSMutableString *targettext;
	NSMutableString *hash=[NSMutableString stringWithFormat:@"%d",has],*t; //hashmap contains string objects as key, so converting has into string hash
	if([dictionar objectForKey:hash]!=nil)    //if key found in dictionary
	{
		blocks=[dictionar objectForKey:hash]; //obtaining the list blocks associated with same key hash
		for(i=0,len=[blocks count];i<len;++i)//traversing through each block
		{
			if([[[blocks objectAtIndex:i] gettext] isEqual:[target substringWithRange:NSMakeRange(0, blocksize)]])//making sure it is a valid match
				{
				if(dictionarytext!=NULL)
					
				{
					
					t=[[NSMutableString alloc] init];
					[t setString :[[dictionarytext getoriginaltext] substringFromIndex:[[blocks objectAtIndex:i] getoffset]+blocksize]];
					dicttext=t;//dicttext stores the source text beyond the found match of block
							  t=[[NSMutableString alloc] init];
					[t setString:[target substringFromIndex:blocksize]];
							  targettext=t;//targettext stores the target text beyond the found match of text
					if([dicttext length]==0 ||[targettext length]==0)//No text beyond the match
					return [blocks objectAtIndex:i];
				
				currentpointer=0;//for number of matching char beyond blocksize
				while(currentpointer<[dicttext length] && currentpointer<[targettext length]&&[dicttext characterAtIndex:currentpointer]==[targettext characterAtIndex:currentpointer])
				{
					
					currentpointer+=1;//incriments 1 for each matching char
				}
				block *b=[[block alloc] init];
					NSMutableString *s=[[NSMutableString alloc] init];
					[s setString:[[blocks objectAtIndex:i] gettext]];
					[s appendString:[dicttext substringWithRange:NSMakeRange(0, currentpointer)]];//adds the charecters beyond blocksize match into the string
					
				[b initwith:s:[[blocks objectAtIndex:i] getoffset]];//updating the complete matching string into block 
					return b;
				}
				else
				{return [blocks objectAtIndex:i];}
		
		}
		}
				return NULL;  //No match
	
	}
				return NULL;  //No match

}

@end
