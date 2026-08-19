@echo off

echo Compiling...
javac -cp ".;ojdbc8.jar" --module-path "C:\libetc\javafx-sdk-21.0.10\lib" --add-modules javafx.controls,javafx.fxml -d out *.java

if %errorlevel% neq 0 (
    echo.
    echo Compilation failed.
    pause
    exit /b
)

echo.
echo Running Bug Tracker...
java -cp "out;ojdbc8.jar" --module-path "C:\libetc\javafx-sdk-21.0.10\lib" --add-modules javafx.controls,javafx.fxml Main

pause