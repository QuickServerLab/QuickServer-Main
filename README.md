Readme for QuickServer v 2.0.0
------------------------------

Read the "install.txt" file for instructions to setup your environment
variables to be followed after extracting or installing QuickServer.


Introduction
---------------------
QuickServer is an open source Java library/framework for quick creation 
of robust multi-client TCP server applications. With QuickServer you can 
concentrate on just the application logic/protocol on hand.  

QuickServer provides an abstraction over the ServerSocket, Socket and 
other network and input output classes and it eases the creation of 
powerful network servers.

Example programs demonstrating the use of the library can be found 
with the QuickServer distribution  [examples folder]. 
Latest examples, documentation is available through the website. 


Brief Architecture
---------------------
QuickServer divides the application logic of its developer over eight 
main classes,
 * ClientEventHandler [Optional Class]
    Handles client events.
 * ClientCommandHandler [#]
    Handles client interaction - Character/String commands.
 * ClientObjectHandler [#]
    Handles client interaction - Java Object commands.
 * ClientBinaryHandler [#]
    Handles client interaction - Binary data (byte array).
 * ClientWriteHandler [Optional Class]
    Handles client write operation - Non-Blocking Mode.
 * ClientAuthenticationHandler [Optional Class]
    Used to authenticate a client.
 * ClientData [Optional Class]
    Client data carrier (support class)
 * ClientExtendedEventHandler [Optional Class]
    Handles extended client events.

[#] = Any one of these have to be set based on default DataMode for input. 
      The default DataMode for input is String so if not changes you will
      have to set ClientCommandHandler.

See the architecture section of Wiki for basic architecture of QuickServer.


Major Features
---------------------
 * Create multi-client TCP server applications.
 * Support for secure server creations: SSL, TLS
 * Support for thread per client (multi-threaded) - Blocking Mode.
 * Support for non-blocking input output - Non-Blocking Mode.
 * Clear separation of server, protocol and authentication logic.
 * Remote administration support: QsAdminServer 
   (With support for plugable application commands)
 * Support for command shell for local administration of server.
 * GUI based remote administration: QsAdminGUI 
   (With support for gui plug-ins)
 * Restart or Suspend the server without killing connected clients.
 * Inbuilt pools for reusing of threads and most used Objects.
 * Full logging support [Java built in Logging].
 * Support for sending and receiving Strings, Bytes, Binary and 
   serialized java objects.
 * Support for identifying and searching clients. 
 * Support for xml configuration with ability to store application 
   specific data in the same xml.
 * Support for xml based JDBC mapping.
 * Support for Service Configurator pattern.
 * Support for restricting access to server by ip address.
 * Support for loading/reloading application jar from xml.
 * Ability to add process hooks into QuickServer.
 * Ability to specify maximum number of clients allowed.
 * Support for negotiating secure connection over normal tcp connection.
 * Nice and easy examples come with the distribution - FTPServer, 
   CmdServer, EchoWebServer, ChatServer, SecureEchoWebServer, XmlAdder, 
   PipeServer, Filesrv.


What's New in 2.0.0
---------------------
 * Added QuickServer client package - supports abstract clients, loaddistribution, monitoring and thread pool framework
 * Added socket pool in QuickServer client package.
 * Added support to set advanced-settings (like tcp-no-delay, performance-preferences).
 * Added AsyncHandler for JDK logging.
 * Improved readInputStream() method in BasicClientHandler.
 * Added support for raw Communication Logging (in case of binary mode).
 * Added BroadcastServer example.
 * Many more bug fixes and enhancements. 


System Requirements
---------------------
Read the "requirements.txt" file for details on system requirements.


QuickServer Credits 
---------------------
Thanks to everyone who helped me in this project. Thanks to all users 
who posted/sent their valuable comments and suggestion. 
I would also like to thank all the people who have posted/sent bug 
reports. Please do keep reporting any bugs that you find in QuickServer, 
this way you will be helping in improving it. Do visit our web site for 
full credits listing.   


Get Support 
---------------------
Do post your questions, suggestions, bug-reports, enhancement-requests 
etc. at Developers Forum. Please do not contact development team 
directly unless you really would like to send a private message. 

Note: We provide a higher level of support to individuals and companies 
who have contributed to QuickServer project in some way.
There is a number of ways to contribute visit www.quickserver.org for details.


Request to Developers
---------------------
If you would like to contribute to the development of QuickServer 
please do get in touch with us. We are always on the lookout for
people who can contribute to make this library even better.

If you use QuickServer in your development and if you would like to 
share your experience with the QuickServer community, please feel 
free to post it in the QuickServer Forums. Thanks.


Website
---------------------
http://www.quickserver.org
https://github.com/QuickServerLab/QuickServer-Main
http://quickserver.sourceforge.net
http://code.google.com/p/quickserver/


License, Copyright
---------------------
QuickServer
Java library/framework for creating robust multi-client TCP servers.
Copyright (C) 2003-2013 QuickServer.org
Website	   : http://www.quickserver.org

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

Note: The GNU LGPL v2.1 is included in the file "license.txt" for 
your convenience.


Other Libraries/Tools Used
--------------------------
QuickServer uses the following third party libraries and would like
to thank all of them for making life easier for developers.

-- Included Jars/Libraries --
Jakarta Commons:
 This product includes software developed by the Apache Software 
 Foundation (http://www.apache.org/). Read the "requirements.txt" 
 file for details. 
 Apache Software License is included in the file "apache_license.txt"

Metouia Look And Feel :
 A free pluggable look and feel for java. License : GNU LGPL 
 http://mlf.sourceforge.net 

-- Non-Included Jars/Libraries --
JUnit :
 A framework to write repeatable tests. 


Date: 15 Feb 2014
---------------------

Copyright (C) 2003-2014 QuickServer.org
http://www.quickserver.org/
https://github.com/QuickServerLab/QuickServer-Main