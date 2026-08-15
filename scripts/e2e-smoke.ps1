# BuildHub E2E smoke (local gateway). Uses client-credentials token only.
$ErrorActionPreference = 'Continue'
$gw = 'http://localhost:28080'
$authBase = 'http://localhost:9000'
$pair = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes('buildmate-client:buildmate-secret'))
$token = (Invoke-RestMethod -Method Post -Uri "$authBase/oauth2/token" -Headers @{Authorization="Basic $pair"} -ContentType 'application/x-www-form-urlencoded' -Body 'grant_type=client_credentials&scope=api.read api.write').access_token
$auth = @{ Authorization = "Bearer $token" }
$suffix = Get-Random

$supJson = @{
  supplierCode = "BH$suffix"
  companyName = "BuildHub Co $suffix"
  ownerName = "Test Owner"
  email = "owner$suffix@example.com"
  password = "Secret123!"
  phone = "0771234567"
  address = "42 Main St"
  district = "Colombo"
  businessRegistrationNo = "BRN$suffix"
} | ConvertTo-Json

try {
  $sup = Invoke-RestMethod -Method Post -Uri "$gw/api/suppliers" -Headers $auth -ContentType 'application/json' -Body $supJson
  Write-Output "PASS_SUPPLIER id=$($sup.id)"
  $supplierId = $sup.id
} catch {
  Write-Output "FAIL_SUPPLIER $($_.Exception.Message)"
  if ($_.ErrorDetails) { Write-Output $_.ErrorDetails.Message }
  exit 1
}

$matJson = @{
  name = "Steel Rod $suffix"
  description = "High tensile steel rod for BuildHub E2E"
  category = "Steel"
  unit = "ton"
  price = 98000
  stock = 75
  supplierId = $supplierId
  brand = "LankaSteel"
  featured = $false
} | ConvertTo-Json

try {
  $mat = Invoke-RestMethod -Method Post -Uri "$gw/api/materials" -Headers $auth -ContentType 'application/json' -Body $matJson
  Write-Output "PASS_MATERIAL id=$($mat.id)"
  $materialId = $mat.id
} catch {
  Write-Output "FAIL_MATERIAL $($_.Exception.Message)"
  if ($_.ErrorDetails) { Write-Output $_.ErrorDetails.Message }
  exit 1
}

Start-Sleep -Seconds 4
$mats = @(Invoke-RestMethod -Uri "$gw/api/materials" -Headers $auth)
Write-Output "PASS_MATERIALS_COUNT $($mats.Count) containsNew=$($mats.id -contains $materialId)"

$inv = @(Invoke-RestMethod -Uri "$gw/api/inventory" -Headers $auth)
$invRow = $inv | Where-Object { $_.materialId -eq $materialId }
Write-Output "INVENTORY_AUTO=$([bool]$invRow) inventoryCount=$($inv.Count)"
if (-not $invRow) {
  $createdInv = Invoke-RestMethod -Method Post -Uri "$gw/api/inventory" -Headers $auth -ContentType 'application/json' -Body (@{ materialId=$materialId; availableQuantity=75; reservedQuantity=0; minimumStock=5 } | ConvertTo-Json)
  Write-Output "PASS_INVENTORY_MANUAL id=$($createdInv.id)"
}

$userId = '6a0000000000000000000001'
$orderBody = @{ userId = $userId; items = @(@{ materialId = $materialId; quantity = 2; price = 98000 }) } | ConvertTo-Json -Depth 5
try {
  $order = Invoke-RestMethod -Method Post -Uri "$gw/api/orders" -Headers $auth -ContentType 'application/json' -Body $orderBody
  Write-Output "PASS_ORDER id=$($order.id) status=$($order.status)"
  $orderId = $order.id
} catch {
  Write-Output "FAIL_ORDER $($_.Exception.Message)"
  if ($_.ErrorDetails) { Write-Output $_.ErrorDetails.Message }
  exit 1
}

Start-Sleep -Seconds 2
$payBody = @{ orderId = $orderId; userId = $userId; amount = 196000; currency = 'LKR'; paymentMethod = 'CARD'; status = 'SUCCESS' } | ConvertTo-Json
try {
  $pay = Invoke-RestMethod -Method Post -Uri "$gw/api/payments" -Headers $auth -ContentType 'application/json' -Body $payBody
  Write-Output "PASS_PAYMENT id=$($pay.id) status=$($pay.status)"
  $paymentId = $pay.id
} catch {
  Write-Output "FAIL_PAYMENT $($_.Exception.Message)"
  if ($_.ErrorDetails) { Write-Output $_.ErrorDetails.Message }
  $paymentId = $null
}

if ($paymentId -and $pay.status -notin @('SUCCESS','COMPLETED','PAID')) {
  try {
    $pay2 = Invoke-RestMethod -Method Patch -Uri "$gw/api/payments/$paymentId/status?status=SUCCESS" -Headers $auth
    Write-Output "PASS_PAYMENT_STATUS status=$($pay2.status)"
  } catch {
    Write-Output "FAIL_PAYMENT_STATUS $($_.Exception.Message)"
  }
}

Start-Sleep -Seconds 3
$ord2 = Invoke-RestMethod -Uri "$gw/api/orders/$orderId" -Headers $auth
Write-Output "ORDER_AFTER_PAYMENT status=$($ord2.status)"

try {
  $invoice = Invoke-RestMethod -Method Post -Uri "$gw/api/invoices" -Headers $auth -ContentType 'application/json' -Body (@{ paymentId=$paymentId; orderId=$orderId; userId=$userId; amount=196000 } | ConvertTo-Json)
  Write-Output "PASS_INVOICE id=$($invoice.id)"
  $got = Invoke-RestMethod -Uri "$gw/api/invoices/$($invoice.id)" -Headers $auth
  Write-Output "PASS_INVOICE_GET id=$($got.id)"
} catch {
  Write-Output "FAIL_INVOICE $($_.Exception.Message)"
  if ($_.ErrorDetails) { Write-Output $_.ErrorDetails.Message }
}

Write-Output 'E2E_DONE'
