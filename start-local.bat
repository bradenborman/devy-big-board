@echo off
echo Starting Devy BigBoard Server with LOCAL profile...
echo.
gradlew.bat :app-server:bootRun --args="--spring.profiles.active=local"
