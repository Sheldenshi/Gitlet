# Gitlet Design Document
**Name**: Shelden Shi

## Classes and Data Structures

###Blobs



###Commits

#### Instance Variables
* Message - contains the message of a commit
* Time - time at which a commit was created. Assigned by the constructor
* Parent - the parent commit of a commit object.
* Blobs - An ArrayList that stores all the sh1 codes for blobs of its files.
###Commands

####Methods
* init(String[] args): args Array in format: {'init'} Creates a new Gitlet version-control system in the current directory. 
###Main
Will take the commands given to the program and redirect processes accordingly


## Algorithms

###Commands
####Methods

#####init(String[] args): 
1. Creates a Commit object that has message "initial commit", parent null, time 0.
2. Creates the .gitlet file
3. Has a single  branch: master, which points to this initial commit, and master will be the current branch.
4. All initial commits have the same UID


## Persistence


