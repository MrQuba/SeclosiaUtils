# SeclosiaUtils
Utility mod for Seclosia Minecraft server. This mod is just server side
# Content
## Command shortcuts
### What are command shortcuts?
Command shortcuts allows for executing commands in shorter (or more readable) way
### How to execute command shortcuts?
```
/seclosia <command shortcut>
```
### How to add command shortcuts?
```
/seclosia add <command shortcut (without spaces)> <command you want to create shortcut for, do not use '/' sign, can include spaces>
```
### Examples
```
/secloia add helloworld say Hello World!
/seclosia helloworld
```
## Locations
### What are locations?
Locations allow for saving coordinates and accessing them via command
### How to get coordinates of locations?
```
/seclosia location <location name>
```
### How to add locations?
```
/seclosia add location <location name (without spaces)> <x coordinate of location> <y coordinate of location> <z coordinate of location>
```
### Examples
```
/secloia add location home 0 63 0
/seclosia location home
```
## Macros
### What are macros
Macros work like command shortcuts, but allow for executing multiple commands
### How to execute macros?
```
/seclosia macro <macro name>
```
### How to add macros?
```
/seclosia add macro <macro name (without spaces)> <commands in macro, can contain spaces, divide commands by using commas>
```
### Examples
```
/secloia add macro helloworld say Hello,say World!
/seclosia macro helloworld
```
## TPS & MSPT
This is disabled by default, to enable it use [#6](https://github.com/MrQuba/SeclosiaUtils/issues/6) :
```
/seclosia enable mspt
```
and
```
/seclosia enable tps
```
To get value you use commands:
```
/seclosia get mspt
```
and
```
/seclosia get tps
```
## Notes
You do not need to restart server to use those commands, but they won't appear in suggestions before you do so. \

