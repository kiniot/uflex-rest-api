Get-Content .env | Where-Object { $_ -match '^\s*[^#].*=' } | ForEach-Object {
    $name, $value = $_ -split '=', 2
    $cleanValue = $value.Trim()
    $cleanValue = $cleanValue.Trim('"', "'")
    [System.Environment]::SetEnvironmentVariable($name.Trim(), $cleanValue, 'Process')
}

.\mvnw spring-boot:run