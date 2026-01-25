#!/bin/bash
echo "Starting Devy BigBoard Server with LOCAL profile..."
echo ""
./gradlew :app-server:bootRun --args="--spring.profiles.active=local"
