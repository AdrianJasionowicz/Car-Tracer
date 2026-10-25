@echo off
rem Buduje dist\CarTracker\CarTracker.exe (z wbudowana Java - nie trzeba jej instalowac)
rem i tworzy skrot na pulpicie. Opcjonalnie dodaje aplikacje do autostartu Windows.
setlocal
cd /d "%~dp0"

if not defined JAVA_HOME set "JAVA_HOME=%USERPROFILE%\.jdks\corretto-21.0.12.1"
if not exist "%JAVA_HOME%\bin\jpackage.exe" (
    echo Nie znaleziono jpackage w "%JAVA_HOME%". Ustaw JAVA_HOME na JDK 21.
    exit /b 1
)

tasklist /FI "IMAGENAME eq CarTracker.exe" | find /I "CarTracker.exe" >nul && (
    echo CarTracker jest uruchomiony - zamknij go ^(menu ikony w zasobniku -^> Zamknij^) i sprobuj ponownie.
    exit /b 1
)

echo [1/3] Budowanie jara...
call "%~dp0mvnw.cmd" -q -B package || exit /b 1

echo [2/3] Tworzenie CarTracker.exe...
if exist target\jpackage-input rmdir /s /q target\jpackage-input
mkdir target\jpackage-input
copy /y target\carTracker-0.0.1-SNAPSHOT.jar target\jpackage-input\carTracker.jar >nul
if exist dist\CarTracker rmdir /s /q dist\CarTracker
"%JAVA_HOME%\bin\jpackage.exe" --type app-image --name CarTracker --app-version 1.0 ^
    --input target\jpackage-input --main-jar carTracker.jar ^
    --java-options "-Dfile.encoding=UTF-8" --dest dist || exit /b 1

echo [3/3] Skrot na pulpicie...
set "EXE=%~dp0dist\CarTracker\CarTracker.exe"
powershell -NoProfile -Command ^
    "$s = (New-Object -ComObject WScript.Shell).CreateShortcut([Environment]::GetFolderPath('Desktop') + '\CarTracker.lnk');" ^
    "$s.TargetPath = $env:EXE; $s.WorkingDirectory = Split-Path $env:EXE; $s.Save()"

choice /M "Uruchamiac CarTracker w tle (w zasobniku) przy starcie Windows"
if errorlevel 2 goto done
powershell -NoProfile -Command ^
    "$s = (New-Object -ComObject WScript.Shell).CreateShortcut([Environment]::GetFolderPath('Startup') + '\CarTracker.lnk');" ^
    "$s.TargetPath = $env:EXE; $s.Arguments = '--tray'; $s.WorkingDirectory = Split-Path $env:EXE; $s.Save()"
echo Dodano do autostartu.

:done
echo.
echo Gotowe: %EXE%
echo Skrot CarTracker jest na pulpicie.
endlocal
