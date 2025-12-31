$BASE="https://web-production-0ddab3.up.railway.app"

# 5A) Turn Open House ON for a test uid
Invoke-WebRequest "$BASE/mode/openhouse/start?uid=test" -Method POST -UseBasicParsing

# 5B) Send fake audio
$bytes = [byte[]](0..255)
Invoke-WebRequest "$BASE/omi/audio?uid=test&sample_rate=16000" -Method POST -ContentType "application/octet-stream" -Body $bytes -UseBasicParsing

# 5C) Turn Open House OFF and send again
Invoke-WebRequest "$BASE/mode/openhouse/stop?uid=test" -Method POST -UseBasicParsing
Invoke-WebRequest "$BASE/omi/audio?uid=test&sample_rate=16000" -Method POST -ContentType "application/octet-stream" -Body $bytes -UseBasicParsing
