@echo off
setlocal

rem Compile Java sources into out/
if not exist out mkdir out
javac -d out -sourcepath src Main.java
if errorlevel 1 (
  echo.
  echo Compilation failed.
  pause
  exit /b 1
)

echo.
java -cp out Main
endlocal
