@echo off
REM Script to delete all S3 assets from Devy Board
REM WARNING: This will permanently delete all player headshot images!

echo.
echo WARNING: This will delete ALL player headshots from S3 and database!
echo.
set /p confirm="Are you sure you want to continue? (type 'yes' to confirm): "

if not "%confirm%"=="yes" (
    echo Aborted.
    exit /b 0
)

echo.
echo Sending DELETE request to https://devyboard.com/S3-delete...
echo.

curl -X DELETE https://devyboard.com/S3-delete -H "Content-Type: application/json"

echo.
echo.
echo Done!
pause
