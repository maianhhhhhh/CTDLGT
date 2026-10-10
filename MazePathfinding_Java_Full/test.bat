@echo off
setlocal
cd /d "%~dp0"
if not exist "out" mkdir "out"
REM On Windows, Java sourcepath entries are separated with a semicolon.
javac -encoding UTF-8 -d "out" -sourcepath "src;tests" "tests\maze\tests\AlgorithmTests.java"
if errorlevel 1 (
  echo Java compilation failed.
  exit /b 1
)
java -ea -cp "out" maze.tests.AlgorithmTests
