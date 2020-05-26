# Jr.-Coding-Challenge
This code is written by John Assaf, Jr via Apache NetBeans.

The purpose of this repository is to convert comma-seperated .CSV files into .db files. NOTE: NON-COMMA BASED .CSV FILES MAY NOT WORK, SUCH FORMATS ARE UNTESTED.

This project uses the openCSV library and its dependencies as well as the SQLite jdbc library.


# HOW TO RUN THIS APP:

To operate this program, simply click the "find file" button and select the .csv file you wish to convert. Or, if you already know the address, you may type it into the top text box.

After that, simply type in the name you want your new .db file to have in the lower text box and click "convert". The program will automatically sort the entered code into good and bad databases. The biggest bottleneck of this process by far is entry into the .db file, so the progress bar displays how much of the .db file has been written.

When the progress bar hits 100% and if there are no errors, you may close the program and use your newly created files.



# NOTES:

-This program was written under the assumption that the .csv file is comma-seperated. It is possible for a .csv file to not follow this standard, and such files will not work with this converter.

-The program initially will not show any indication that it is processing. Pressing "convert" while the program is already converting may cause unintended behavior. To see if the program is converting, simply look for the incomplete database files in your system.

-The code is fully commented and the repository contains all of the necessary libraries to compile.

-My initial approach was to create a Swing-based program to make converting from .csv to .db as easy and fast as possible. As it turns out, .db entry is very slow and multithreading the entry process is not possible so I had to instead focus on making progress as visible as possible.

- Readability and simplicity were key to this project: I have no idea what technical proficiency the customer may have, so I must assume the minimum.

- A previous version that processed bad entries into a .db file (I misread the document initially) had a bug that caused the bad file to get stuck at 51% written. This bug no longer appears, but further testing is required to find the source of the bug.
