@echo off
setlocal enabledelayedexpansion

if not exist "bin" mkdir bin
if not exist "lib" mkdir lib

set "MYSQL_JAR="
for %%F in (lib\mysql-connector-java-*.jar lib\mysql-connector-j-*.jar) do (
    set "MYSQL_JAR=%%F"
    goto :found_jar
)

:found_jar
if "%MYSQL_JAR%"=="" (
    echo [INFO] MySQL connector not found in lib/. Downloading mysql-connector-j-8.0.33.jar...
    curl -L -o "lib\mysql-connector-j-8.0.33.jar" "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar"
    if errorlevel 1 (
        echo [ERROR] Failed to download MySQL connector.
        exit /b 1
    )
    set "MYSQL_JAR=lib\mysql-connector-j-8.0.33.jar"
    echo [INFO] Download successful.
) else (
    echo [INFO] Found MySQL connector: %MYSQL_JAR%
)

set "FLATLAF_JAR=lib\flatlaf-3.2.1.jar"
if not exist "%FLATLAF_JAR%" (
    echo [INFO] FlatLaf not found in lib/. Downloading flatlaf-3.2.1.jar...
    curl -L -o "%FLATLAF_JAR%" "https://repo1.maven.org/maven2/com/formdev/flatlaf/3.2.1/flatlaf-3.2.1.jar"
    if errorlevel 1 (
        echo [ERROR] Failed to download FlatLaf.
        exit /b 1
    )
)

echo [INFO] Cleaning previous build...
if exist bin\com rd /s /q bin\com

echo [INFO] Compiling Java files...
dir /s /B src\*.java > sources.txt
javac -cp "lib/*" --add-modules java.sql -d bin @sources.txt

if errorlevel 1 (
    echo [ERROR] Compilation failed.
    exit /b 1
)

set "JAR_CMD=jar"
where jar >nul 2>nul
if errorlevel 1 (
    for /d %%d in ("C:\Program Files\Java\jdk*") do (
        if exist "%%d\bin\jar.exe" set "JAR_CMD=%%d\bin\jar.exe"
    )
)

echo [INFO] Generating server.jar...
set "MYSQL_JAR_MF=!MYSQL_JAR:\=/!"
if exist server.mf del server.mf
(echo Main-Class: com.chatroom.server.ServerExec)> server.mf
(echo Class-Path: !MYSQL_JAR_MF!)>> server.mf
"!JAR_CMD!" cfm server.jar server.mf -C bin com/chatroom
if errorlevel 1 (
    echo [ERROR] Failed to generate server.jar.
    exit /b 1
)
del server.mf

echo [INFO] Generating client.jar...
set "FLATLAF_JAR_MF=!FLATLAF_JAR:\=/!"
if exist client.mf del client.mf
(echo Main-Class: com.chatroom.client.ClientExec)> client.mf
(echo Class-Path: !FLATLAF_JAR_MF!)>> client.mf
"!JAR_CMD!" cfm client.jar client.mf -C bin com/chatroom -C res .
if errorlevel 1 (
    echo [ERROR] Failed to generate client.jar.
    exit /b 1
)
del client.mf

echo [INFO] Build completed successfully!
endlocal
