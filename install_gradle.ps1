$gradleVersion = "9.2.1"
$gradleUrl = "https://services.gradle.org/distributions/gradle-$gradleVersion-bin.zip"
$gradleZip = "$env:TEMP\gradle-$gradleVersion-bin.zip"
$gradleDir = "C:\Gradle\gradle-$gradleVersion"

# Download Gradle
Invoke-WebRequest -Uri $gradleUrl -OutFile $gradleZip

# Create directory and extract
Expand-Archive -Path $gradleZip -DestinationPath "C:\Gradle" -Force

# Set PATH for current session
$env:Path = "C:\Gradle\gradle-$gradleVersion\bin;" + $env:Path

# Optionally, add to user PATH permanently
[Environment]::SetEnvironmentVariable("Path", "C:\Gradle\gradle-$gradleVersion\bin;" + [Environment]::GetEnvironmentVariable("Path", [EnvironmentVariableTarget]::User), [EnvironmentVariableTarget]::User)

Write-Host "Gradle $gradleVersion installed. Restart your terminal and run 'gradle -v' to verify."
