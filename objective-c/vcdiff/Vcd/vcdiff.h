//
//  vcdiff.h
//  Vcd
//
//  Created by Akash K Sunny on 15/03/14.
//  Copyright 2014 Neoba Systems. All rights reserved.
//

#import "dictionary.h"


@interface vcdiff : NSObject {
	rollinghash *hash;
	dictionary *dicttext;
	int blocksize;

}
-(void) initwith:(int) s;
-(NSMutableArray *) encode:(NSMutableString *) dict:(NSMutableString *) target;
-(NSMutableString *) decode:(NSMutableString *) dict:(NSMutableArray *) diff; 
@end
