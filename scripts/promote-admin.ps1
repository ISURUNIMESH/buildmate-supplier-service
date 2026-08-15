<#
.SYNOPSIS
  Promote a BuildMate auth user to ROLE_ADMIN in MongoDB.

.DESCRIPTION
  Updates buildmate_auth_db.users by email so JWT logins include ADMIN.
  After running, the user must sign out and sign in again to refresh the JWT.

.PARAMETER Email
  User email (Google account email used at login).

.PARAMETER MongoUri
  Mongo connection string (default: local auth-mongo).

.EXAMPLE
  .\scripts\promote-admin.ps1 -Email "you@example.com"
#>
param(
  [Parameter(Mandatory = $true)]
  [string]$Email,

  [string]$MongoUri = "mongodb://localhost:27017"
)

$ErrorActionPreference = "Stop"
$normalized = $Email.Trim().ToLowerInvariant()

if (-not (Get-Command mongosh -ErrorAction SilentlyContinue)) {
  Write-Error "mongosh not found on PATH. Install MongoDB Shell, then retry."
}

# Escape for embedding inside a single-quoted JS string
$escaped = $normalized.Replace('\', '\\').Replace("'", "\'")

$js = @"
const email = '$escaped';
const result = db.getSiblingDB('buildmate_auth_db').users.updateOne(
  { email: email },
  { `$set: { roles: ['ROLE_ADMIN', 'ROLE_USER'], updatedAt: new Date() } }
);
printjson(result);
if (result.matchedCount === 0) {
  quit(2);
}
"@

Write-Host "Promoting '$normalized' to ROLE_ADMIN on $MongoUri ..."
& mongosh $MongoUri --quiet --eval $js
$code = $LASTEXITCODE
if ($code -eq 2) {
  Write-Error "No user found with email '$normalized'. Sign in once via Google first, then re-run."
}
if ($code -ne 0) {
  Write-Error "mongosh failed with exit code $code"
}

Write-Host ""
Write-Host "Done. Sign out of BuildMate and sign in again so the JWT picks up ROLE_ADMIN."
Write-Host "Admin users can then open searchable User ID dropdowns (GET /api/auth/users)."
