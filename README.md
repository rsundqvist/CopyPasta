# Program has been deprecated
CopyPasta is no longer being developed. May not run properly in Java 9 and up, as the program has dependencies which break in a Java 9 environment.

# About CopyPasta

>Download "CopyPasta.zip" and run "CopyPasta.jar" to get started  [(link)](https://github.com/whisp91/CopyPasta/raw/master/CopyPasta.zip).

Copy Pasta is a program developed to aid in grading exercises. Common content ("Pasta") can be created and categorized to speed up the process. Wildcards and templates are used to reduce the risk of mistakes. The program will automatically load/store the most recent data (content and Pasta) when starting/exiting.

 
## Typical workflow

The program was designed and implemented with the following workflow in mind:

### 1. Setup

JSON-files received from course owner. Import the Pasta and the content template.

### 2. Create content from templates

On the "Setup" tab ***(Ctrl+2)***, enter the group numbers, "3 5, 6  9, potato" for groups {3, 5, 6, 9, potato}. Tokens separated by spaces and/or comma will be treated as a separate group.

Alternatively, groups can be created from a folder structure ***(Ctrl+I)***. In this case, a root folder containing a single folder per group is assumed. You may choose file types, such as .java-files, to include by default.

### 3. Write feedback 

Commonly used phrases can be saved using the Pasta Editor ***(Ctrl-G)***, then exported ***(RMB -> Export)*** and shared. Pasta items can be categorized using tags. Tags are automatically converted to lower case, and any leading or trailing whitespace will be removed.

From the File view, content may be added directly ***(Ctrl+F)*** with a reference to the file and caret position. Alternatively, a Pasta item may be selected from the list and inserted directly ***(Ctrl+Q)*** in an appropriate location.  If there is a %FILE:\<filename\>% tag present, the program will automatically insert the pasta there. If not, it will be appended at the end or above the %FOOTER% tag.

### 4. Export feedback

Under the "Setup" panel, click the "Export All" button ***(Ctrl-E)*** to select a target folder. All content items will be exported as individal .txt-files for each group in the list, and a single .json-file containing all of them (used for later import, or simply discarded).

Some content may contain a %MANUAL% tag, indicating that some part of the content (typically Pasta) should be changed manually by the teacher. The program will warn the user when trying to export content with this tag present.
 
## Creator

Richard Sundqvist (richard.sundqvist@live.se)

Git repo: https://github.com/whisp91/CopyPasta
