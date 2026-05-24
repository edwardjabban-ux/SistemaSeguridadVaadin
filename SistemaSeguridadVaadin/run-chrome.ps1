$ErrorActionPreference = "Stop"

$projectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectDir

$javaExe = "$env:USERPROFILE\.jdks\openjdk-26.0.1\bin\java.exe"
if (-not (Test-Path $javaExe)) {
    $javaExe = "$env:USERPROFILE\.jdks\openjdk-26\bin\java.exe"
}

if (-not (Test-Path $javaExe)) {
    throw "No se encontro Java. Instala JDK 17 o configura JAVA_HOME."
}

if (-not (Test-Path "target\run.args")) {
    throw "No existe target\run.args. Abre el proyecto en IntelliJ y compila una vez, o instala Maven y ejecuta mvn spring-boot:run."
}

$mainClassFile = Join-Path $projectDir "target\classes\com\security\SistemaSeguridadApplication.class"
if (-not (Test-Path $mainClassFile)) {
    $javaSources = Get-ChildItem -Path (Join-Path $projectDir "src\main\java") -Recurse -Filter "*.java"
    $dependencyCp = (Get-Content "target\run.args")[1].Split(";") | Where-Object {
        $_ -and
        $_ -notlike "target\classes" -and
        $_ -notlike "*-sources.jar" -and
        $_ -notlike "*\org\slf4j\slf4j-api\1.7.36\*" -and
        $_ -notlike "*\org\example\AppBase\*"
    }

    New-Item -ItemType Directory -Force -Path (Join-Path $projectDir "target\classes") | Out-Null
    & ($javaExe -replace "java.exe$", "javac.exe") --release 17 -cp ($dependencyCp -join ";") -d (Join-Path $projectDir "target\classes") $javaSources.FullName
}

$runArgs = Get-Content "target\run.args"
$classesDir = (Resolve-Path "target\classes").Path
$classpath = @($classesDir) + ($runArgs[1].Split(";") | Where-Object {
    $_ -and
    $_ -ne "target\classes" -and
    $_ -notlike "*-sources.jar" -and
    $_ -notlike "*\org\slf4j\slf4j-api\1.7.36\*" -and
    $_ -notlike "*\org\example\AppBase\*"
})

$url = "http://localhost:8080"
$chrome = "${env:ProgramFiles}\Google\Chrome\Application\chrome.exe"
if (-not (Test-Path $chrome)) {
    $chrome = "${env:ProgramFiles(x86)}\Google\Chrome\Application\chrome.exe"
}

$existingServer = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
if ($existingServer) {
    Write-Host "Ya hay una aplicacion escuchando en $url. Abriendo Chrome."
    if (Test-Path $chrome) {
        Start-Process $chrome $url
    } else {
        Start-Process $url
    }
    return
}

Start-Job -ScriptBlock {
    param($chromePath, $targetUrl)
    Start-Sleep -Seconds 8
    if (Test-Path $chromePath) {
        Start-Process $chromePath $targetUrl
    } else {
        Start-Process $targetUrl
    }
} -ArgumentList $chrome, $url | Out-Null

Write-Host "Abriendo $url en Chrome. Si la app se cierra, revisa usuario/password de PostgreSQL en src\main\resources\application.properties."
& $javaExe -cp ($classpath -join ";") com.security.SistemaSeguridadApplication
