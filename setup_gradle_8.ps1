$gradleVersion = "8.5"
$gradleUrl = "https://services.gradle.org/distributions/gradle-$gradleVersion-bin.zip"
$gradleZip = "gradle-$gradleVersion-bin.zip"
$gradleDest = "gradle_temp"

Write-Host "Downloading Gradle $gradleVersion..."
Invoke-WebRequest -Uri $gradleUrl -OutFile $gradleZip

Write-Host "Extracting..."
Expand-Archive -Path $gradleZip -DestinationPath $gradleDest -Force

$gradleBin = "$PWD\$gradleDest\gradle-$gradleVersion\bin\gradle.bat"

Write-Host "Generating wrapper using Gradle $gradleVersion..."
& $gradleBin wrapper --gradle-version 8.5

Write-Host "Done."
