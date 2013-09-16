PNGsToBitmapFont
================

**Small project for making bitmap font from given png files or from bitmap font files (.fnt & .png).**<br \>

THIS PROGRAM IS CURRENTY IN DEVELOPMENT
---------------------------------------
...so more improvements are about to be implemented soon.

In case you want to make a bitmap font from png files:
------------------------------------------------------
- Each png file must representing one char, its name must be the name of char in ASCII table, where you want to add it.
- The output of this process is:
 - File with suffix .png, it's one bitmap image, which contains all of given chars.
 - Second has suffix .fnt. This file is xml language based source of information for end-point program. It contains specific location of each char, its dimensions and location in adjusted png file plus extra information about char value of this letter. 
 - **Note:** Development about .fnt file is not done yet, char elements are made almost right, but the head of this file could make some troubles. In this case, take the head from the original .fnt file and just edit the scaleH="xxx" scaleW="xxx" values so that they will corespond to ajdusted .png size.

In case you want to extract chars from original bitmap files:
-------------------------------------------------------------
- Place files font.fnt & font.png to some folder, specify this folder in PNGsToFONT.PATH and run the FontInit.java class.
- The output is just cropped chars, every one in single .png file with name coressponding to their's char value.

More specific information is about to be added soon.
