# Reinstall and Run Script

Write-Host "Uninstalling existing app..."
adb uninstall com.omiagent.assistant

Write-Host "Cleaning project..."
./gradlew clean

Write-Host "Building and Installing Debug APK..."
./gradlew installDebug

if ($LASTEXITCODE -eq 0) {
    Write-Host "Launching App..."
    adb shell am start -n com.omiagent.assistant/.LoginActivity
    Write-Host "Done!"
} else {
    Write-Host "Build failed. Fix errors and try again."
}
