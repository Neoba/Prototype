//
//  main.m
//  httppost
//
//  Created by Neoba Systems on 23/09/14.
//  Copyright (c) 2014 Neoba Systems. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "Sendpost.h"
#import "PacketForger.h"
#import "document.h"
int main(int argc, const char * argv[])
{
    
    @autoreleasepool {
        int ch=0;
      
        
        NSUUID *cookiePacked=nil;
        document *docid=nil;
        NSMutableArray *docs=nil;
        
        NSMutableDictionary *dfollowers=nil;
        NSMutableDictionary *dfollowings=nil;
        
                do{
                    
                    NSLog(@"Enter your choice\n1.Ping\n2.Create user\n3.Login\n4.Logout\n5.Follow\n6.Unfollow\n7.Create Doc\n8.Delete doc\n9.Digest\n10.poke\n11.Grant Permission\n12.Edit doc\n13.Ios Get digest\n\n14.Display0.Exit");
                    scanf("%d",&ch);
                    if (ch==1)
                        [PacketForger pinger];
        
                    else if (ch==2)
                    {
                        char name[10];
                        char pass[21];
                        NSLog(@"Username:");
                        scanf("%s",name);
                        NSLog(@"Pwd");
                        scanf("%s",pass);
                        NSString *un=[[NSString alloc] initWithCString:name encoding:NSUTF8StringEncoding];
                        NSString *pw=[[NSString alloc] initWithCString:pass encoding:NSUTF8StringEncoding];
                        UInt16 status=[PacketForger createUser:un password:pw];
                        if (status==0xffff)
                            NSLog(@"Done..");
            
                        else
                            NSLog(@"Error %d",status);
            
                    }
                    else  if(ch==3)
                    {
                        char name[10];
                        char pass[21];
                        NSLog(@"Username:");
                        scanf("%s",name);
                        NSLog(@"Pwd");
                        scanf("%s",pass);
                        NSString *un=[[NSString alloc] initWithCString:name encoding:NSUTF8StringEncoding];
                        NSString *pw=[[NSString alloc] initWithCString:pass encoding:NSUTF8StringEncoding];
                        UInt16 status=[PacketForger login:un password:pw putcookie:&cookiePacked];
                        
                        if (status==0xffff)
                        {
                            NSLog(@"Logged in..");
                            docs=[[NSMutableArray alloc] init];
                            dfollowers=[[NSMutableDictionary alloc] init];
                            dfollowings=[[NSMutableDictionary alloc] init];
                            [PacketForger iosgetDigestwith:cookiePacked followers:dfollowers followings:dfollowings docs:docs];
                                                   }
                        else
                            NSLog(@"Error %d",status);
                        
                      
            
                    }
                    else  if (ch==4)
                    {
                  
                        UInt16 rstatus=[PacketForger logoutCookie:&cookiePacked];
                        if(rstatus==0xffff)
                        {
                            NSLog(@"Logout Successfull");
                            docs=nil;
                            dfollowers=nil;
                            dfollowings=nil;

                        }
                    else
                        NSLog(@"Logout Failed, error %d",rstatus);

                    }
                    else if (ch==5)
                    {
                 
                        char name[10];
                        NSLog(@"User");
                        scanf("%s",name);
                        NSString *un=[[NSString alloc] initWithCString:name encoding:NSUTF8StringEncoding];
                        
                        UInt16 rstatus=[PacketForger followUser:un cookie:cookiePacked];
                        if(rstatus==0xffff)
                        {
                            NSLog(@"User followed");
                        }
                        else
                            NSLog(@"Error %d",rstatus);
                    }
                    else if (ch==6)
                    {
                
                        char name[10];
                        NSLog(@"User");
                        scanf("%s",name);
                        NSString *un=[[NSString alloc] initWithCString:name encoding:NSUTF8StringEncoding];
                        
                        UInt16 rstatus=[PacketForger unfollowUser:un cookie:cookiePacked];
                        if(rstatus==0xffff)
                        {
                            NSLog(@"User Unfollowed");
                        }
                        else
                            NSLog(@"Error");

                    }
                    else if (ch==7)
                    {
                        
                        docid=nil;
                        NSUUID *udocid=nil;
                        UInt16 rstatus=[PacketForger createDocUsercookie:cookiePacked putDocid:&udocid];
                        if(rstatus==0xffff)
                        {
                            docid=[[document alloc] init];
                            docid.did=udocid;
                            NSLog(@"Doc created %d old",docid.age);
                            [docs addObject:docid];
                        }
                        else
                            NSLog(@"Error %d",rstatus);
                    }
                    else if (ch==8)
                    {
                        int i=0,docidnum;
                        NSLog(@"Doc ids, enter number to delete\n");
                        for (document *d in docs) {
                            NSString *idflt;
                            
                            idflt=[d.did UUIDString];
                            NSLog(@"%d.%@\n",i,idflt);
                            ++i;
                        }
                        if([docs count]==0)
                            continue;
                        scanf("%d",&docidnum);
                        docid=[docs objectAtIndex:docidnum];
                        UInt16 rstatus=[PacketForger deleteDocUsercookie:cookiePacked withDocid:docid.did];
                        if(rstatus==0xffff)
                        {
                            NSLog(@"Doc deleted");
                            [docs removeObject:docid];
                            //docid=nil;
                        }
                        else
                            NSLog(@"Error %d",rstatus);

                    }
                    else if (ch==9)
                    {
                        
                        
                        

                        [PacketForger getDigestwith:cookiePacked followers:dfollowers followings:dfollowings docs:docs ];
                        
                        
                      
                        NSLog(@"Doc ids\n");
                        for (document *d in docs) {
                            NSString *idflt;
                            
                            idflt=[d.did UUIDString];
                            NSLog(@"%@\n",idflt);
                        }
                        
                        NSLog(@"Followers\n");
                        for (NSString *d in dfollowers) {
                            
                            NSLog(@"%@ with id %ld\n",d,[[dfollowers objectForKey:d] longValue]);
                        }
                        
                        NSLog(@"Followings\n");
                        for (NSString *y in dfollowings) {
                            
                             NSLog(@"%@ with id %ld\n",y,[[dfollowings objectForKey:y] longValue]);
                        }
                    
                    }
                    else if (ch==10)
                    {
                        
                        char name[10];
                        NSLog(@"User");
                        scanf("%s",name);
                        NSString *un=[[NSString alloc] initWithCString:name encoding:NSUTF8StringEncoding];
                        
                        UInt16 rstatus=[PacketForger followUser:un cookie:cookiePacked];
                        if(rstatus==0xffff)
                        {
                            NSLog(@"Pinged");
                        }
                        else
                            NSLog(@"Error %d",rstatus);
                    }
                    else if(ch==11)
                    {
                        int i=0,dnum,p;
                        document *gdocid;
                        NSMutableDictionary *userp=[[NSMutableDictionary alloc] init];
                         NSLog(@"Enter docid num");
                        for (document *d in docs) {
                            NSString *idflt;
                            
                            idflt=[d.did UUIDString];
                            NSLog(@"%d.%@\n",i,idflt);
                            ++i;
                        }
                        scanf("%d",&dnum);
                        gdocid=[docs objectAtIndex:dnum];
                        while (true) {
                            NSLog(@"Enter permission(0.n 1.r 2.w) followed by username(11.when finished)");
                            scanf("%d",&p);
                            if (p==11) {
                                break;
                            }
                            char name[10];
                           
                            scanf("%s",name);
                            NSString *un=[[NSString alloc] initWithCString:name encoding:NSUTF8StringEncoding];
                            NSNumber *gp=[NSNumber numberWithInt:p];
                            NSNumber *user=[dfollowers objectForKey:un];
                            if(user==nil){
                                NSLog(@"User not following,error");
                                break;
                            }
                            [userp setObject:gp forKey:user];
                            user=nil;
                            
                        }
                        UInt16 rstatus;
                        rstatus=[PacketForger grant:cookiePacked doc:gdocid.did userpermissions:userp];
                        if (rstatus==0xffff) {
                            NSLog(@"Success");
                            
                        }
                        else
                            NSLog(@"Error %d",rstatus);
                    }
                    else if (ch==12)
                    {
                        char ctext[100];
                        int i=0,dnum,age;
                        document *gdocid;
                        
                        for (document *d in docs) {
                            NSString *idflt;
                            
                            idflt=[d.did UUIDString];
                            NSLog(@"%d.%@\n",i,idflt);
                            ++i;
                        }
                        if ([docs count]==0) {
                            NSLog(@"No docs");
                            continue;
                        }
                        scanf("%d",&dnum);
                        gdocid=[docs objectAtIndex:dnum];
                        NSLog(@"Text");
                        scanf("%s",ctext);
                        NSString *text=[[NSString alloc] initWithCString:ctext encoding:NSUTF8StringEncoding];
                        NSLog(@"Age:%d",gdocid.age);
                        
                        UInt16 rstatus;
                        rstatus=[PacketForger editDocUsercookie:cookiePacked docid:gdocid.did text:text age:gdocid.age];
                        if (rstatus==0xffff) {
                            NSLog(@"Success");
                            gdocid.age+=1;
                        }
                        else
                            NSLog(@"Error %d",rstatus);
                    }
                    else if (ch==13)
                    {
                        docs=[[NSMutableArray alloc] init];
                        dfollowers=[[NSMutableDictionary alloc] init];
                        dfollowings=[[NSMutableDictionary alloc] init];
                        [PacketForger iosgetDigestwith:cookiePacked followers:dfollowers followings:dfollowings docs:docs];
                    }
                    else if (ch==14)
                    {
                        for (document *d in docs) {
                            NSLog([d.did UUIDString]);
                            NSLog(@"Age %d",d.age);
                            NSLog(@"%@",d.text);
                        }
                        NSLog(@"Followers\n");
                        for (NSString *d in dfollowers) {
                            
                            NSLog(@"%@ with id %ld\n",d,[[dfollowers objectForKey:d] longValue]);
                        }
                        
                        NSLog(@"Followings\n");
                        for (NSString *y in dfollowings) {
                            
                            NSLog(@"%@ with id %ld\n",y,[[dfollowings objectForKey:y] longValue]);
                        }

                    }
            
                }while (ch);
            return 0;
    
        }
    
}

