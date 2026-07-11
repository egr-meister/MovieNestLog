@rem Gradle startup script for Windows (standard wrapper)
@if "%DEBUG%"=="" @echo off
set DIRNAME=%~dp0
set APP_HOME=%DIRNAME%
set CLASSPATH=%APP_HOME%gradle\wrapper\gradle-wrapper.jar
if defined JAVA_HOME (set JAVACMD="%JAVA_HOME%\bin\java.exe") else (set JAVACMD=java.exe)
%JAVACMD% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
