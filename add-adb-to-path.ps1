# This script adds the Android SDK platform-tools to the user PATH permanently
$platformTools = "C:\Users\jason\AppData\Local\Android\Sdk\platform-tools"
$currentPath = [Environment]::GetEnvironmentVariable("Path", "User")
if ($currentPath -notlike "*${platformTools}*") {
    [Environment]::SetEnvironmentVariable("Path", "$currentPath;${platformTools}", "User")
    Write-Output "Platform-tools added to user PATH. Please restart your terminal or log out and back in for changes to take effect."
} else {
    Write-Output "Platform-tools already in user PATH."
}
