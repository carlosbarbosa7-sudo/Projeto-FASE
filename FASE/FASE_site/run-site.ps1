$ErrorActionPreference = 'Stop'
$pythonCommand = Get-Command python -ErrorAction SilentlyContinue
if (-not $pythonCommand) {
    throw 'Python não foi encontrado no PATH. Instale Python 3 ou abra o site por outro servidor local na porta 5500.'
}

Write-Host 'Site local: http://127.0.0.1:5500'
Write-Host 'Mantenha este terminal aberto enquanto usa o site. Pressione Ctrl+C para parar.'
& $pythonCommand.Source -m http.server 5500 --bind 127.0.0.1 --directory $PSScriptRoot
