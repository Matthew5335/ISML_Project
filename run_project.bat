@echo off
cd /d %~dp0

setlocal EnableDelayedExpansion
set CLASSPATH=target\classes

rem Loop through lib folder and add each jar to the classpath
for %%i in (lib\*.jar) do (
    set CLASSPATH=!CLASSPATH!;%%i
)

echo Running ISML Project...
java -cp "!CLASSPATH!" org.example.Main

pause
