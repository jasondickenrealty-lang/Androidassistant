# This PowerShell script sets up adb for Windows and adds it to the PATH
# Usage: Run this script as Administrator

# Set Android SDK path (update this if your SDK is in a different location)
$androidSdkRoot = "$env:LOCALAPPDATA\Android\Sdk"
$adbPath = Join-Path $androidSdkRoot 'platform-tools\adb.exe'

if (-Not (Test-Path $adbPath)) {
    Write-Host "adb not found. Please install Android Studio and ensure the SDK is installed."
    exit 1
}

# Add platform-tools to the user PATH if not already present
$platformTools = Join-Path $androidSdkRoot 'platform-tools'
$currentPath = [Environment]::GetEnvironmentVariable('Path', 'User')
if ($currentPath -notlike "*$platformTools*") {
    [Environment]::SetEnvironmentVariable('Path', "$currentPath;$platformTools", 'User')
    Write-Host "Added $platformTools to user PATH. Please restart your terminal."
} else {
    Write-Host "$platformTools is already in PATH."
}

# Test adb
& $adbPath version
