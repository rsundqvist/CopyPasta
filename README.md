# About Copy Pasta

>***Download "CopyPasta.zip" and run "CopyPasta.jar" to get started [(link)](https://github.com/whisp91/CopyPasta/raw/master/CopyPasta.zip).***

Copy Pasta is a program developed to aid in grading assignment exercises. Common feedback ("Pasta") can be created and categorized to speed up the process. Wildcards and templates are used to reduce the risk of mistakes, and to reduce clutter. The program will automatically load/store the most recent data (feedback and Pasta) when starting/exiting.

 
## Typical workflow

The program was designed and implemented with the following workflow in mind:

### 1. Setup

JSON-files received from course owner. Import the Pasta and the feedback template.

### 2. Create feedback from templates

Enter the group numbers, "3 5, 6  9, potato" for groups {3, 5, 6, 9, potato}. Tokens separated by spaces and/or comma will be treated as a separate group.

### 3. Write feedback.  

Pasta items can be categorized using tags. Tags are automatically converted to lower case, and any leading or trailing whitespace will be removed. Commonly used phrases can be saved using the Pasta Editor (Ctrl-G), then exported (RMB -> Export) and shared.

### 4. Export the feedback

Under the "Setup" panel, click the "Export All" button (Ctrl-E) to select a target folder. All feedback items will be exported as individal .txt-files for each group in the list, and a single .json-file containing all of them (used for later import, or simply discarded).
 
## Creator

Richard Sundqvist (richard.sundqvist@live.se)

Git repo: https://github.com/whisp91/CopyPasta
