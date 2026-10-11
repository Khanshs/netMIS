param(
    [string]$JavaHome = $env:JAVA_HOME,
    [switch]$Checks,
    [switch]$ExceptionDemo,
    [switch]$DiscountDemo,
    [switch]$DiscountChecks,
    [switch]$AccountChecks,
    [switch]$EarlyEndDemo,
    [switch]$EarlyEndChecks,
    [switch]$SessionEndChecks
)
$ErrorActionPreference = 'Stop'
$compilerPath = $null
if ($JavaHome -and (Test-Path (Join-Path $JavaHome 'bin\javac.exe'))) {
    $compilerPath = Join-Path $JavaHome 'bin\javac.exe'
} else {
    $compilerCommand = Get-Command javac -ErrorAction SilentlyContinue
    if ($compilerCommand) { $compilerPath = $compilerCommand.Source }
}
if (-not $compilerPath) {
    $candidate = Get-ChildItem 'C:\Program Files\JetBrains\IntelliJ IDEA*\jbr\bin\javac.exe' -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($candidate) { $compilerPath = $candidate.FullName }
}
if (-not $compilerPath) { throw 'JDK not found. Pass -JavaHome with your JDK directory.' }
$runnerPath = Join-Path (Split-Path $compilerPath -Parent) 'java.exe'
$outputPath = Join-Path $PSScriptRoot 'out'
New-Item -ItemType Directory -Force -Path $outputPath | Out-Null
$sources = @(Get-ChildItem (Join-Path $PSScriptRoot 'src') -Recurse -Filter '*.java' | ForEach-Object { $_.FullName })
& $compilerPath -source 8 -target 8 -encoding UTF-8 -Xlint:-options -d $outputPath @sources
if ($LASTEXITCODE -ne 0) { throw 'Compilation failed.' }
$mainClass = 'netcafe.Main'
if ($Checks) { $mainClass = 'netcafe.Checks.NetCafeChecks' }
if ($ExceptionDemo) { $mainClass = 'netcafe.Demo.ExceptionDemo' }
if ($DiscountDemo) { $mainClass = 'netcafe.Demo.DiscountDemo' }
if ($DiscountChecks) { $mainClass = 'netcafe.Checks.DiscountChecks' }
if ($AccountChecks) { $mainClass = 'netcafe.Checks.AccountChecks' }
if ($EarlyEndDemo) { $mainClass = 'netcafe.Demo.EarlyEndDemo' }
if ($EarlyEndChecks) { $mainClass = 'netcafe.Checks.EarlyEndChecks' }
if ($SessionEndChecks) { $mainClass = 'netcafe.Checks.SessionEndChecks' }
& $runnerPath -cp $outputPath $mainClass
if ($LASTEXITCODE -ne 0) { throw 'Program failed.' }
