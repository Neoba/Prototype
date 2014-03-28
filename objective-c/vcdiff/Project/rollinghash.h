
//prototype of rollinghash class, having functions for hashing 

//The encoding strategy is largely based on Bentley-McIlroy 99: "Data Compression Using Long Common Strings".
//http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.11.8470&rep=rep1&type=pdf
//hash method is used to hash a string of blocksize, it is mainly used more with source text after splitting it into chunks of fixed size called blocks
//nexthash implements a moving window method to hash a substring in target text

@interface rollinghash : NSObject 
{
	int base;
	int mod;
	int lastpower;//used by func nexthash
	NSMutableString *laststring;//used by func nexthash
	int lasthash; //used by func nexthash

}
-(void) initwith; //initialises the variables
// usage [r initwith]

-(int) moduloexponent:(int) power;//returns (base^power)%mod
// p=[r moduloexponent:pow]

-(int) hash: (NSMutableString *) tohash;//Hashes and returns given string 
//h=[r hash:string]

-(int) nexthash:(char) toadd; //Finds the hash after adding a new char and removing the first char from laststring, used with target text as a windowing algorithm
//h=[r nexthash:ch]
@end
