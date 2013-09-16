PNGsToBitmapFont
================

**Small project for making bitmap font from given png files or from bitmap font files (.fnt & .png).**<br \>
**This project is being developed under Linux (but other OSs are supposed to be supported) in NetBeans IDE 7.3.1**<br \>
**Java files are located under src/pngstofont, this project has not been built yet.**

THIS PROGRAM IS CURRENTY IN DEVELOPMENT
---------------------------------------
...so more improvements are about to be implemented soon.

In case you want to make a bitmap font from png files:
------------------------------------------------------
- Each png file must representing one char, its name must be the name of char in charset table (e.g. ISO-8859-2, ASCII), where you want to add it.
- The output of this process is:
 - File with suffix .png, it's one bitmap image, which contains all of given chars.
 - Second has suffix .fnt. This file is xml language based source of information for end-point program. It contains specific location of each char, its dimensions and location in adjusted png file plus extra information about char value of this letter.

In case you want to extract chars from original bitmap files:
-------------------------------------------------------------
- Place files font.fnt & font.png to some folder, specify this folder in PNGsToFONT class in Configuration part and run the FontInit.java class.
- The output is just cropped chars, every one in single .png file with name coressponding to their's char value.

More specific information is about to be added soon.
