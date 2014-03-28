
#import "rollinghash.h"
//definition of rollinghash class

@implementation rollinghash
-(void) initwith
{
	base=257;
	mod=1000000007; //Any large value will do
	lastpower=0;
	laststring=[NSMutableString stringWithString:@""];
	lasthash=0;
}
-(int) moduloexponent:(int) power//returns (base^power)%mod
{
	int toreturn=1,i;
	for(i=0;i<power;i+=1)
		toreturn=(base*toreturn)%mod;
	return toreturn;
}
-(int) hash: (NSMutableString *) tohash
{
	int hash=0;
	int i,len;
	len=[tohash length];
	for(i=0;i<len;i+=1)
	{
		hash+=(int)[tohash characterAtIndex:i]*[self moduloexponent:len-i-1];//adding the each term to the polynomial fingerprint
		hash%=mod;
	}
	lastpower=[self moduloexponent:len-1];//used in nexthash
	laststring=tohash;//used in nexthash
	lasthash=hash;//used in nexthash
	return hash;
}
-(int) nexthash:(char)toadd//adds the finger print of toadd and removes fingerprint of first char in laststring and hence implements more efficient rollin window method
{
	int hash=lasthash;
	hash-=lastpower*((int) [laststring characterAtIndex:0]);
	hash=hash*base+toadd;
	hash%=mod;
	if(hash<0)//If the mod was taken in hash, and lasthash was less than the first term removed from polynomial sum, mod must be added to hash to make it the true positive value
		hash+=mod;
	[laststring deleteCharactersInRange:NSMakeRange(0, 1)];//Removes first char
	[laststring appendString:[NSMutableString stringWithFormat:@"%c", toadd]];//Adds new char to the end
	lasthash=hash;
	return hash;
}
@end
