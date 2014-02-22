Dsync
=====

Neoba Dsync is a vcdiff based data synchronization platform

Features
--------

- client-server messages in wire are in *vcdiff* (Although slightly non-conformant to RFC 3284)
- MD5 hash based versioning
- This is a Proof of Concept program

To Do
-----

- All messages in wire should be in vcdiff format
- vcdiff format should conform to RFC 3284
- diff currently being sent as a serialized `List<Object>`
- diff should be compressed
- Android/iOS clients
- Users,Permissions & Everything in the specifications document

Building
--------
All three components of this prototype are built using NetBeans IDE. Use Export.

Running
-------
Use either the pre built version or build your own jar.
- Client
```
cd Prototype\com.neoba.dsync.client\dist
java -jar com.neoba.dsync.client.jar
```
- Server
```
cd Prototype\com.neoba.dsync.client\dist
java -jar com.neoba.dsync.client.jar
```

Screenshots
-----------
![Starting](https://raw2.github.com/Neoba/Prototype/master/Capture1.PNG)
![Updating](https://raw2.github.com/Neoba/Prototype/master/Capture2.PNG)
![Syncing](https://raw2.github.com/Neoba/Prototype/master/Capture3.PNG)

Version
----

0.1
