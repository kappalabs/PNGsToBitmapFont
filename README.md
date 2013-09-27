Bitmap font editor
==================

**Small project for editing existing bitmap font files (.fnt & .png).**<br \>
**This project is being developed under Linux (but other OSs are supposed to be supported) in NetBeans IDE 7.3.1**<br \>
**Java files are located under src/pngstofont, executable .jar file is in the main repository.**


In case you want to edit existing bitmap font:
----------------------------------------------
- Run the program, select **Edit font** from menu above and find original .png & .fnt files.
- Use the textfield below to check how the font will look like.
- Use the tools on the right side to adjust parameters of selected letter. The effect will appear after you hit the **Set values** button.
 - Any change will be saved automatically after you hit the **Set values** button.

In case you want to extract chars from original bitmap files:
-------------------------------------------------------------
- Run the program, select **Edit font** from menu above and find original .png & .fnt files.
- Select **Export letters** from the menu above, you will be probably asked for setting desired output directory.
 - The output is just cropped chars, every one in single .png file with name coressponding to their's char value.

In case you want to make a bitmap font from png files:
------------------------------------------------------
- Run the program, select **Edit font** from menu above and find original .png & .fnt files. It's needed for loading additional information from them.
- Select **Build font** from the menu above and select desired input and output directory.
 - Input directory is directory, which contains all of your letters in [number].png format.
 - Each png file must be representing one char, its name must be the name of char in charset table (e.g. ISO-8859-2, ASCII), where you want to add it.
- The output of this process (saved in desired export directory) is:
 - File with suffix .png, it's one bitmap image, which contains all of given chars.
 - Second has suffix .fnt. This file is xml language based source of information for end-point program. It contains specific location of each char, its dimensions and location in adjusted png file plus extra information about char value of this letter.

Suggestion:
-----------
If you want just to add some letter into your bitmap font, follow this steps:
- Extract chars from original bitmap files.
- Make your desired changes in it.
- Build new bitmap font from these changed letters.
- Edit this new bitmap font, so these new letters will show properly.
