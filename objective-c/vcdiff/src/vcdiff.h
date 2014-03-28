
#import "dictionary.h"
//prototype of vcdiff class

@interface vcdiff : NSObject {
	rollinghash *hash;
	dictionary *dicttext;
	int blocksize;

}
-(void) initwith:(int) s;
//usage [c initwith], c is object of vcdiff 

-(NSMutableArray *) encode:(NSMutableString *) dict:(NSMutableString *) target; //takes source and target string and returns diff
//usage diff=[c encode:dict:target]   dict and target are strings

-(NSMutableString *) decode:(NSMutableString *) dict:(NSMutableArray *) diff;   //takes diff and source and returns target text
//usage target=[c decode:dict:diff]
@end
