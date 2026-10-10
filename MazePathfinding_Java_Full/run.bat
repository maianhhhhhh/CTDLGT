@echo off
setlocal
cd /d "%~dp0"
if not exist "out" mkdir "out"
REM Compile from sourcepath directly; avoids Windows backslash escaping in javac @argfiles.
javac -encoding UTF-8 -d "out" -sourcepath "src" "src\maze\Main.java"
if errorlevel 1 (
  echo Java compilation failed.
  exit /b 1
)
java -cp "out" maze.Main
