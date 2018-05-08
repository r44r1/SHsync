### ABOUT
SHsync is a software which allows to sync music files from PC with DoCoMo Sharp featured phone running MOAP (S) operating system. These phones are designed to sync music library with Windows Media Player, but this feature frequently doesn't work then phone is used outside from DoCoMo network (eg., using it outside from Japan with HyperSim installed). Probably, app may work with other MOAP (S) phones but I can't guarantee it. If definitely won't work with MOAP (L) phones (NEC & Panasonic).

The original idea comes from Docomo M-Sync software written by weaknespace, but it doesn't work with modern operating systems (Windows 8 and 10), so I decided to make my own app instead.

The most recent version (if only I'll continue to develop it) can be found at: https://github.com/r44r1/SHsync

**PROS:**
* Should run almost everywhere: it's written in Java with API 1.6, so you just need to have JVM 1.6 or higher installed to run app. JVM 1.6 is available for Windows starting from 2000, modern Linux distributions and OS X newer than Yosemite. I also avoid to use OS-specific API except for GUI, but GUI libs for major of them are provided with application. However, keep in mind that I write & debug app under modern Windows so some parts could act different on different OS (I face this couple of times when using some standard functions) and this may lead to errors appear.
* Support automatic music re-encoding: MOAP phones can only play WMA files with bitrate less than 256k, but people who store entire music collection in low rate WMA are as rare as Amur leopards. You can just add songs in your favourite  format (most modern formats are supported) to program queue and app will care about anything else itself.  
**IMPORTANT**:  
For activate this feature, you should download FFmpeg encoder by yourself (I can't include it to distribution due to license issues) and put it together with program or provide path to encoder via "File" menu. I recommend to use static build on Windows (can be downloaded at https://ffmpeg.zeranoe.com/builds/ - just extract  "ffmpeg.exe" from zip and place it into program directory) and native builds from your distro repository on Linux.  

**CONS:**
* Doesn't support in-place tag edit: I do not think it is necessary in 2018, when tags can be edited directly from
  file explorer program

If you have any questions, you can contact me by e-mail (shown in "About" window) or by GitHub (@r44r1).

## HOW TO USE
Change your phone PC connection type to microSD mode or just eject memory card and plug it into PC directly.  

Point program to SD card root by clicking on "File" -> "Open Phone".
Add music files to work queue. You can do this by "File" -> "Add File(s)" menu or by simply drag-n-drop from file explorer.  

Unsupported files will be excluded from queue automatically, so no need to care about this. Also, songs could be excluded from  queue if needed - by double click on specified row or by selecting and pressing delete key.  
Specify path to FFmpeg encoder if needed by opening "File" -> "FFmpeg path" submenu. This isn't necessary if encoder binary is placed into program directory or you don't want to use encoding feature.

Click on "Sync" button to start synchronization process. Keep in mind that with encoder feature enabled it could take much time to complete, depending on your queue length and CPU speed.  

If app is started from command line, additional debug info regarding queue processing is printed to stdout. Use this feature if you need to catch & investigate processing errors. 

## BUILD
In order to build app, install Apache Ant >=1.8 and run this command from project root folder:
			
			ant -f build.ant.xml
			
Note that for current build script "bitness" of OS and JVM should match to get right native library selected. I'll redo this when will finish with code itself.

For Mac OS X, you should manually uncomment appropriate line under 'swtselect' section in ant file. Ant do not provide simple method for distinguish OS X from other *nixes. I do not own mac to test tricky workarounds for that.

## LICENSES

Product itself is licensed under Apache License 2.0

This product includes the following external software:

1) sqlite-jdbc library by xerial

   Licensed under Apache License 2.0.

2) jAudioTagger library by jJabz

   Licensed under LGPL v 3 license.

   Source code can be obtained at https://bitbucket.org/ijabz/jaudiotagger/src/f53ffcf512182d7e1a4e14261e7a7510687497e3/?at=v2.2.4

3) SWT libraries by Eclipse foundation

   Licensed under Eclipse Public License 1.0

Text of all licenses noticed above can be found in LICENSE file coming with app and project.