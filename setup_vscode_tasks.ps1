# setup_vscode_tasks.ps1
# Creates VS Code tasks for:
# 1) Running FastAPI on port 9000 with safe reload rules
# 2) Running your audio test script against port 9000
# 3) Running your health check script against port 9000

param(
  [string]$ServerModule = "webhook_server:app",
  [string]$Port = "9000",
  [string]$AudioTest = "run_audio_test.py",
  [string]$HealthTest = "health_check_9000.py",
  [string]$ServerFileToWatch = "webhook_server.py"
)

$projectRoot = Get-Location
$vscodeDir = Join-Path $projectRoot ".vscode"
if (!(Test-Path $vscodeDir)) { New-Item -ItemType Directory -Path $vscodeDir | Out-Null }

$tasksPath = Join-Path $vscodeDir "tasks.json"

$tasksJson = @"
{
  "version": "2.0.0",
  "tasks": [
    {
      "label": "Run FastAPI (9000) - safe reload",
      "type": "shell",
      "command": ".\\.venv\\Scripts\\Activate.ps1; uvicorn $ServerModule --host 127.0.0.1 --port $Port --reload --reload-include `"$ServerFileToWatch`" --reload-exclude `"$AudioTest`" --reload-exclude `"$HealthTest`"",
      "problemMatcher": [],
      "presentation": {
        "reveal": "always",
        "panel": "dedicated",
        "clear": false
      }
    },
    {
      "label": "Run Audio Test (local 9000)",
      "type": "shell",
      "dependsOn": [],
      "command": ".\\.venv\\Scripts\\Activate.ps1; `$env:OMI_AUDIO_URL='http://127.0.0.1:$Port/omi/audio'; python `"$AudioTest`"",
      "problemMatcher": [],
      "presentation": {
        "reveal": "always",
        "panel": "shared",
        "clear": false
      }
    },
    {
      "label": "Run Health Check (local 9000)",
      "type": "shell",
      "command": ".\\.venv\\Scripts\\Activate.ps1; python `"$HealthTest`"",
      "problemMatcher": [],
      "presentation": {
        "reveal": "always",
        "panel": "shared",
        "clear": false
      }
    }
  ]
}
"@

Set-Content -Path $tasksPath -Value $tasksJson -Encoding UTF8

Write-Host "✅ Created $tasksPath"
Write-Host "Next: In VS Code go to Terminal -> Run Task..."
Write-Host " - Run 'Run FastAPI (9000) - safe reload' in one terminal"
Write-Host " - Then run 'Run Audio Test (local 9000)' in another terminal"
