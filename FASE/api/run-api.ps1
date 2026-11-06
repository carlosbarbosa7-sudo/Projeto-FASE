$ErrorActionPreference = 'Stop'

$javaHomeCandidates = @()
if (-not [string]::IsNullOrWhiteSpace($env:JAVA_HOME)) {
    $javaHomeCandidates += $env:JAVA_HOME
}

$jdkSearchRoots = @(
    'C:\Program Files\Eclipse Adoptium'
    (Join-Path $env:USERPROFILE '.jdk')
)
foreach ($jdkSearchRoot in $jdkSearchRoots) {
    if (Test-Path -LiteralPath $jdkSearchRoot) {
        $javaHomeCandidates += Get-ChildItem -LiteralPath $jdkSearchRoot -Directory -Filter 'jdk-25*' |
            Sort-Object Name -Descending |
            Select-Object -ExpandProperty FullName
    }
}

$javaHomeLocal = $null
foreach ($javaHomeCandidate in $javaHomeCandidates) {
    $javaExe = Join-Path $javaHomeCandidate 'bin\java.exe'
    if (Test-Path -LiteralPath $javaExe) {
        $javaVersionOutput = & $env:ComSpec /d /c "`"$javaExe`" -version 2>&1"
        if ($LASTEXITCODE -eq 0 -and ($javaVersionOutput -join "`n") -match 'version "25\.') {
            $javaHomeLocal = $javaHomeCandidate
            break
        }
    }
}
if (-not $javaHomeLocal) {
    throw 'Java 25 LTS não encontrado. Instale o JDK 25 ou configure JAVA_HOME para um JDK 25.'
}

$mavenCmd = Join-Path $PSScriptRoot '.tools\apache-maven-3.10.0\bin\mvn.cmd'
if (-not (Test-Path -LiteralPath $mavenCmd)) {
    throw 'Maven local não encontrado. Consulte o README.md da API para instalá-lo.'
}

$env:JAVA_HOME = $javaHomeLocal
$env:Path = "$javaHomeLocal\bin;$env:Path"

$adminUsername = Read-Host 'Usuário da conta administradora'
if ([string]::IsNullOrWhiteSpace($adminUsername)) {
    throw 'Informe o usuário da conta administradora.'
}
$secureAdminPassword = Read-Host 'Senha da conta administradora (não será exibida)' -AsSecureString
$adminPasswordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secureAdminPassword)
$mysqlUser = Read-Host 'Usuário MySQL (Enter para root)'
$env:DB_USERNAME = if ([string]::IsNullOrWhiteSpace($mysqlUser)) { 'root' } else { $mysqlUser.Trim() }

$securePassword = Read-Host 'Senha MySQL (não será exibida)' -AsSecureString
$passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
$previousDbPassword = $env:DB_PASSWORD
$previousAdminUsername = $env:FASE_ADMIN_USERNAME
$previousAdminPassword = $env:FASE_ADMIN_PASSWORD
$runExitCode = 1
try {
    $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer)
    $env:FASE_ADMIN_USERNAME = $adminUsername.Trim()
    $env:FASE_ADMIN_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($adminPasswordPointer)
    if ([string]::IsNullOrWhiteSpace($env:FASE_ADMIN_PASSWORD)) {
        throw 'A senha administrativa não pode ficar vazia.'
    }
    Push-Location $PSScriptRoot
    & $mavenCmd '-Dmaven.repo.local=.tools/repository' 'spring-boot:run'
    $runExitCode = $LASTEXITCODE
}
finally {
    if ((Get-Location).Path -eq $PSScriptRoot) {
        Pop-Location
    }
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($adminPasswordPointer)
    if ($null -eq $previousDbPassword) {
        Remove-Item Env:DB_PASSWORD -ErrorAction SilentlyContinue
    } else {
        $env:DB_PASSWORD = $previousDbPassword
    }
    if ($null -eq $previousAdminUsername) {
        Remove-Item Env:FASE_ADMIN_USERNAME -ErrorAction SilentlyContinue
    } else {
        $env:FASE_ADMIN_USERNAME = $previousAdminUsername
    }
    if ($null -eq $previousAdminPassword) {
        Remove-Item Env:FASE_ADMIN_PASSWORD -ErrorAction SilentlyContinue
    } else {
        $env:FASE_ADMIN_PASSWORD = $previousAdminPassword
    }
}
exit $runExitCode
