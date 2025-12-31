# This PowerShell script checks if adb is available and attempts to fix the PATH if not.
# Usage: Run this script in a new PowerShell window (not inside VS Code)

$androidSdkRoot = "$env:LOCALAPPDATA\Android\Sdk"
$platformTools = Join-Path $androidSdkRoot 'platform-tools'
$adbPath = Join-Path $platformTools 'adb.exe'

if (-Not (Test-Path $adbPath)) {
    Write-Host "adb.exe not found. Please install Android Studio and ensure the SDK is installed."
    exit 1
}

# Check if adb is in the PATH
$envPath = [System.Environment]::GetEnvironmentVariable('Path', 'User')
if ($envPath -notlike "*$platformTools*") {
    [System.Environment]::SetEnvironmentVariable('Path', "$envPath;$platformTools", 'User')
    Write-Host "Added $platformTools to user PATH. Please restart your terminal or log out and back in."
} else {
    Write-Host "$platformTools is already in PATH."
}

# Test adb
& $adbPath version
