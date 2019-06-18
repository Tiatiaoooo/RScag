@echo off
javac -d scagnostics scagnostics/*.java
javac -d scagnostics -classpath scagnostics scagnostics/*.java
