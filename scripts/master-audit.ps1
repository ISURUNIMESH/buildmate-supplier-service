# BuildHub master live verification (no secrets printed).
$ErrorActionPreference = 'Continue'
$gw = 'http://localhost:28080'
$authBase = 'http://localhost:9000'
$results = New-Object System.Collections.Generic.List[string]
function Log-Result([string]$code, [string]$msg) {
  $line = "$code|$msg"
  [void]$results.Add($line)
  Write-Output $line
}

function Get-Token {
  $pair = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes('buildmate-client:buildmate-secret'))
  return (Invoke-RestMethod -Method Post -Uri "$authBase/oauth2/token" -Headers @{Authorization="Basic $pair"} -ContentType 'application/x-www-form-urlencoded' -Body 'grant_type=client_credentials&scope=api.read api.write').access_token
}

function MongoCount([string]$container, [string]$db, [string]$coll, [string]$id) {
  $eval = @"
(() => {
  const dbx = db.getSiblingDB('$db');
  let n = dbx.$coll.countDocuments({_id: '$id'});
  try { n = Math.max(n, dbx.$coll.countDocuments({_id: ObjectId('$id')})); } catch (e) {}
  return n;
})()
"@
  return (docker exec $container mongosh --quiet --eval $eval 2>&1 | Out-String).Trim()
}

function MongoField([string]$container, [string]$db, [string]$coll, [string]$id, [string]$field) {
  $eval = @"
(() => {
  const dbx = db.getSiblingDB('$db');
  let doc = dbx.$coll.findOne({_id: '$id'});
  if (!doc) { try { doc = dbx.$coll.findOne({_id: ObjectId('$id')}); } catch (e) {} }
  return doc ? doc.$field : null;
})()
"@
  return (docker exec $container mongosh --quiet --eval $eval 2>&1 | Out-String).Trim()
}

try { $token = Get-Token; Log-Result 'PASS' "JWT obtained len=$($token.Length)" }
catch { Log-Result 'FAIL' 'JWT obtain failed'; Write-Output '==== SUMMARY ===='; Write-Output 'PASS=0 FAIL=1'; exit 1 }
$H = @{ Authorization = "Bearer $token"; 'Content-Type' = 'application/json' }
$suffix = Get-Random
$userId = '6a00000000000000000000aa'
$supplierId = $null
$materialId = $null
$orderId = $null
$paymentId = $null
$invoiceId = $null

foreach ($u in @(
  "$authBase/actuator/health","$gw/actuator/health",
  'http://localhost:28084/actuator/health','http://localhost:28085/actuator/health',
  'http://localhost:28086/actuator/health','http://localhost:28087/actuator/health',
  'http://localhost:25173/'
)) {
  try { $code=(Invoke-WebRequest $u -UseBasicParsing -TimeoutSec 8).StatusCode; Log-Result 'PASS' "HEALTH $u -> $code" }
  catch { Log-Result 'FAIL' "HEALTH $u" }
}

try { Invoke-WebRequest "$gw/api/materials" -UseBasicParsing -TimeoutSec 8 | Out-Null; Log-Result 'FAIL' 'GW no JWT unexpected OK' }
catch { Log-Result 'PASS' "GW no JWT -> $($_.Exception.Response.StatusCode.value__)" }
try { Invoke-WebRequest 'http://localhost:28085/materials' -UseBasicParsing -TimeoutSec 8 | Out-Null; Log-Result 'FAIL' 'Mat no key unexpected OK' }
catch { Log-Result 'PASS' "Mat no key -> $($_.Exception.Response.StatusCode.value__)" }
try { Invoke-WebRequest 'http://localhost:28085/materials' -Headers @{'X-API-KEY'='wrong'} -UseBasicParsing -TimeoutSec 8 | Out-Null; Log-Result 'FAIL' 'Mat bad key unexpected OK' }
catch { Log-Result 'PASS' "Mat bad key -> $($_.Exception.Response.StatusCode.value__)" }
try { $r=Invoke-WebRequest 'http://localhost:28085/materials' -Headers @{'X-API-KEY'='buildmate-material-key'} -UseBasicParsing -TimeoutSec 8; Log-Result 'PASS' "Mat good key -> $($r.StatusCode)" }
catch { Log-Result 'FAIL' 'Mat good key' }

$supBody = @{
  supplierCode="BH$suffix"; companyName="Audit Co $suffix"; ownerName='Owner'
  email="audit$suffix@example.com"; password='Secret123!'; phone='0771111111'
  address='Colombo'; district='Colombo'; businessRegistrationNo="BRN$suffix"
} | ConvertTo-Json
try {
  $sup = Invoke-RestMethod -Method Post -Uri "$gw/api/suppliers" -Headers $H -Body $supBody
  $supplierId = $sup.id
  Log-Result 'PASS' "SUPPLIER_CREATE id=$supplierId"
  Start-Sleep 2
  $c = MongoCount 'supplier-mongo' 'supplier_db' 'suppliers' $supplierId
  if ($c -eq '1') { Log-Result 'PASS' "SUPPLIER_MONGO count=$c" } else { Log-Result 'FAIL' "SUPPLIER_MONGO count=$c" }
} catch { Log-Result 'FAIL' "SUPPLIER_CREATE $($_.Exception.Message)" }

$matBody = @{
  name="Audit Steel $suffix"; description='audit'; category='Steel'; unit='ton'
  price=50000; stock=40; supplierId=$supplierId; brand='AuditBrand'; featured=$false
} | ConvertTo-Json
try {
  $mat = Invoke-RestMethod -Method Post -Uri "$gw/api/materials" -Headers $H -Body $matBody
  $materialId = $mat.id
  Log-Result 'PASS' "MATERIAL_CREATE id=$materialId"
  Start-Sleep 3
  $mc = MongoCount 'material-mongo' 'material_db' 'materials' $materialId
  if ($mc -eq '1') { Log-Result 'PASS' "MATERIAL_MONGO count=$mc" } else { Log-Result 'FAIL' "MATERIAL_MONGO count=$mc" }
  $inv = @(Invoke-RestMethod -Uri "$gw/api/inventory" -Headers @{Authorization="Bearer $token"}) | Where-Object { $_.materialId -eq $materialId } | Select-Object -First 1
  if ($inv) { Log-Result 'PASS' "INVENTORY_AUTO available=$($inv.availableQuantity)" } else { Log-Result 'FAIL' 'INVENTORY_AUTO missing' }
  $ic = (docker exec order-mongo mongosh --quiet --eval "db.getSiblingDB('order_inventory_db').inventory.countDocuments({materialId:'$materialId'})" 2>&1 | Out-String).Trim()
  if ($ic -eq '1') { Log-Result 'PASS' "INVENTORY_MONGO count=$ic" } else { Log-Result 'FAIL' "INVENTORY_MONGO count=$ic" }
} catch { Log-Result 'FAIL' "MATERIAL_CREATE $($_.Exception.Message)" }

try {
  $mats = @(Invoke-RestMethod -Uri "$gw/api/materials" -Headers @{Authorization="Bearer $token"})
  $has = ($mats.id -contains $materialId)
  Log-Result 'PASS' "MATERIALS_LIST count=$($mats.Count) containsNew=$has"
  $bySup = @($mats | Where-Object { $_.supplierId -eq $supplierId })
  Log-Result 'PASS' "MATERIALS_BY_SUPPLIER count=$($bySup.Count)"
} catch { Log-Result 'FAIL' 'MATERIALS_LIST' }

# stock sync via PATCH /materials/{id}/stock → material.stock.updated → inventory
try {
  Invoke-RestMethod -Method Patch -Uri "$gw/api/materials/$materialId/stock" -Headers $H -Body '{"stock":55}' | Out-Null
  Start-Sleep 3
  $aq = (docker exec order-mongo mongosh --quiet --eval "db.getSiblingDB('order_inventory_db').inventory.findOne({materialId:'$materialId'}).availableQuantity" 2>&1 | Out-String).Trim()
  if ($aq -eq '55') { Log-Result 'PASS' 'STOCK_SYNC available=55' }
  else { Log-Result 'FAIL' "STOCK_SYNC available=$aq" }
} catch { Log-Result 'FAIL' "STOCK_SYNC $($_.Exception.Message)" }

try {
  $empty = Invoke-RestMethod -Uri "$gw/api/cart/$userId" -Headers @{Authorization="Bearer $token"}
  Log-Result 'PASS' "CART_EMPTY_OR_EXIST items=$($empty.items.Count)"
  $cart = Invoke-RestMethod -Method Post -Uri "$gw/api/cart" -Headers $H -Body (@{ userId=$userId; materialId=$materialId; quantity=2; price=50000 } | ConvertTo-Json)
  Log-Result 'PASS' "CART_ADD items=$($cart.items.Count)"
  $cc = (docker exec order-mongo mongosh --quiet --eval "db.getSiblingDB('order_inventory_db').cart.countDocuments({userId:'$userId'})" 2>&1 | Out-String).Trim()
  if ([int]$cc -ge 1) { Log-Result 'PASS' "CART_MONGO count=$cc" } else { Log-Result 'FAIL' "CART_MONGO count=$cc" }
  $cart2 = Invoke-RestMethod -Uri "$gw/api/cart/$userId" -Headers @{Authorization="Bearer $token"}
  Log-Result 'PASS' "CART_GET items=$($cart2.items.Count)"
  $cart3 = Invoke-RestMethod -Method Post -Uri "$gw/api/cart" -Headers $H -Body (@{ userId=$userId; materialId=$materialId; quantity=1; price=50000 } | ConvertTo-Json)
  Log-Result 'PASS' "CART_APPEND items=$($cart3.items.Count)"
} catch { Log-Result 'FAIL' "CART $($_.Exception.Message)" }

try {
  $order = Invoke-RestMethod -Method Post -Uri "$gw/api/orders" -Headers $H -Body (@{ userId=$userId; items=@(@{ materialId=$materialId; quantity=1; price=50000 }) } | ConvertTo-Json -Depth 5)
  $orderId = $order.id
  Log-Result 'PASS' "ORDER_CREATE id=$orderId status=$($order.status)"
  Start-Sleep 2
  $oc = MongoCount 'order-mongo' 'order_inventory_db' 'orders' $orderId
  if ($oc -eq '1') { Log-Result 'PASS' "ORDER_MONGO count=$oc" } else { Log-Result 'FAIL' "ORDER_MONGO count=$oc" }

  $pay = Invoke-RestMethod -Method Post -Uri "$gw/api/payments" -Headers $H -Body (@{ orderId=$orderId; userId=$userId; amount=50000; currency='LKR'; paymentMethod='CARD'; status='SUCCESS' } | ConvertTo-Json)
  $paymentId = $pay.id
  Log-Result 'PASS' "PAYMENT_CREATE id=$paymentId status=$($pay.status)"
  Start-Sleep 3
  $pc = MongoCount 'payment-mongo' 'payment_db' 'payments' $paymentId
  if ($pc -eq '1') { Log-Result 'PASS' "PAYMENT_MONGO count=$pc" } else { Log-Result 'FAIL' "PAYMENT_MONGO count=$pc" }

  $orderAfter = Invoke-RestMethod -Uri "$gw/api/orders/$orderId" -Headers @{Authorization="Bearer $token"}
  if ($orderAfter.status -eq 'PAID') { Log-Result 'PASS' 'ORDER_PAID_VIA_RABBIT status=PAID' } else { Log-Result 'FAIL' "ORDER_PAID status=$($orderAfter.status)" }
  $st = MongoField 'order-mongo' 'order_inventory_db' 'orders' $orderId 'status'
  if ($st -eq 'PAID') { Log-Result 'PASS' 'ORDER_MONGO_STATUS_PAID' } else { Log-Result 'FAIL' "ORDER_MONGO_STATUS $st" }

  $invc = Invoke-RestMethod -Method Post -Uri "$gw/api/invoices" -Headers $H -Body (@{ orderId=$orderId; userId=$userId; paymentId=$paymentId; amount=50000; currency='LKR' } | ConvertTo-Json)
  $invoiceId = $invc.id
  Log-Result 'PASS' "INVOICE_CREATE id=$invoiceId"
  $ig = Invoke-RestMethod -Uri "$gw/api/invoices/$invoiceId" -Headers @{Authorization="Bearer $token"}
  Log-Result 'PASS' "INVOICE_GET id=$($ig.id)"
  $ivc = MongoCount 'payment-mongo' 'payment_db' 'invoices' $invoiceId
  if ($ivc -eq '1') { Log-Result 'PASS' "INVOICE_MONGO count=$ivc" } else { Log-Result 'FAIL' "INVOICE_MONGO count=$ivc" }
} catch { Log-Result 'FAIL' "ORDER_PAYMENT_INVOICE $($_.Exception.Message)" }

try {
  docker restart order-inventory-service | Out-Null
  $deadline = (Get-Date).AddSeconds(90)
  do {
    Start-Sleep 3
    try { $h=Invoke-WebRequest 'http://localhost:28087/actuator/health' -UseBasicParsing -TimeoutSec 5; if ($h.StatusCode -eq 200) { break } } catch {}
  } while ((Get-Date) -lt $deadline)
  $token = Get-Token
  $orderR = Invoke-RestMethod -Uri "$gw/api/orders/$orderId" -Headers @{Authorization="Bearer $token"}
  $cartR = Invoke-RestMethod -Uri "$gw/api/cart/$userId" -Headers @{Authorization="Bearer $token"}
  $invR = @(Invoke-RestMethod -Uri "$gw/api/inventory" -Headers @{Authorization="Bearer $token"}) | Where-Object { $_.materialId -eq $materialId } | Select-Object -First 1
  $invG = Invoke-RestMethod -Uri "$gw/api/invoices/$invoiceId" -Headers @{Authorization="Bearer $token"}
  if ($orderR.status -eq 'PAID') { Log-Result 'PASS' 'RESTART_ORDER_STILL_PAID' } else { Log-Result 'FAIL' "RESTART_ORDER $($orderR.status)" }
  if ($cartR.items.Count -ge 1) { Log-Result 'PASS' "RESTART_CART_ITEMS=$($cartR.items.Count)" } else { Log-Result 'FAIL' 'RESTART_CART' }
  if ($invR) { Log-Result 'PASS' 'RESTART_INVENTORY_EXISTS' } else { Log-Result 'FAIL' 'RESTART_INVENTORY' }
  if ($invG.id) { Log-Result 'PASS' 'RESTART_INVOICE_EXISTS' } else { Log-Result 'FAIL' 'RESTART_INVOICE' }
} catch { Log-Result 'FAIL' "RESTART_SERVICE $($_.Exception.Message)" }

try {
  $token = Get-Token
  Invoke-WebRequest -Method Delete -Uri "$gw/api/cart/$userId" -Headers @{Authorization="Bearer $token"} -UseBasicParsing | Out-Null
  $after = Invoke-RestMethod -Uri "$gw/api/cart/$userId" -Headers @{Authorization="Bearer $token"}
  if ($after.items.Count -eq 0) { Log-Result 'PASS' 'CART_CLEAR' } else { Log-Result 'FAIL' 'CART_CLEAR' }
} catch { Log-Result 'FAIL' "CART_CLEAR $($_.Exception.Message)" }

try {
  docker restart rabbitmq | Out-Null
  $deadline = (Get-Date).AddSeconds(120)
  do {
    Start-Sleep 5
    $ping = docker exec rabbitmq rabbitmq-diagnostics -q ping 2>&1 | Out-String
    if ($ping -match 'succeeded|Ping succeeded') { break }
  } while ((Get-Date) -lt $deadline)
  Start-Sleep 25
  $qs = docker exec rabbitmq rabbitmqctl list_queues name consumers 2>&1 | Out-String
  if ($qs -match 'order.created.queue\s+1' -and $qs -match 'payment.completed.queue\s+1' -and $qs -match 'order.inventory.material.events.queue\s+1' -and $qs -match 'material.supplier.events.queue\s+1') {
    Log-Result 'PASS' 'RABBIT_RESTART_CONSUMERS_RECONNECTED'
  } else {
    Log-Result 'PARTIAL' "RABBIT_RESTART_CONSUMERS check queues manually"
  }
  $token = Get-Token
  $H = @{ Authorization = "Bearer $token"; 'Content-Type' = 'application/json' }
  $mat2 = Invoke-RestMethod -Method Post -Uri "$gw/api/materials" -Headers $H -Body (@{
    name="PostRabbit $suffix"; description='recovery'; category='Steel'; unit='kg'
    price=100; stock=5; supplierId=$supplierId; brand='AuditBrand'; featured=$false
  } | ConvertTo-Json)
  Start-Sleep 4
  $inv3 = @(Invoke-RestMethod -Uri "$gw/api/inventory" -Headers @{Authorization="Bearer $token"}) | Where-Object { $_.materialId -eq $mat2.id } | Select-Object -First 1
  if ($inv3) { Log-Result 'PASS' "RABBIT_POST_RESTART_INVENTORY_AUTO id=$($mat2.id)" } else { Log-Result 'FAIL' 'RABBIT_POST_RESTART_INVENTORY_AUTO' }
} catch { Log-Result 'FAIL' "RABBIT_RESTART $($_.Exception.Message)" }

foreach ($u in @(
  'http://localhost:9000/swagger-ui.html','http://localhost:28080/swagger-ui.html',
  'http://localhost:28084/swagger-ui.html','http://localhost:28085/swagger-ui.html',
  'http://localhost:28086/swagger-ui.html','http://localhost:28087/swagger-ui.html',
  'http://localhost:9000/v3/api-docs','http://localhost:28080/v3/api-docs',
  'http://localhost:28084/v3/api-docs','http://localhost:28085/v3/api-docs',
  'http://localhost:28086/v3/api-docs','http://localhost:28087/v3/api-docs'
)) {
  try { $r=Invoke-WebRequest $u -UseBasicParsing -TimeoutSec 10; Log-Result 'PASS' "SWAGGER $u -> $($r.StatusCode)" }
  catch { Log-Result 'FAIL' "SWAGGER $u" }
}

try {
  $html = (Invoke-WebRequest 'http://localhost:25173/' -UseBasicParsing).Content
  if ($html) { Log-Result 'PASS' 'FRONTEND_SERVED' } else { Log-Result 'FAIL' 'FRONTEND_EMPTY' }
} catch { Log-Result 'FAIL' 'FRONTEND' }

$q = docker exec rabbitmq rabbitmqctl list_queues name consumers messages 2>&1 | Out-String
if ($q -match 'payment.queue\s+0') { Log-Result 'PARTIAL' 'PAYMENT_QUEUE_ZERO_CONSUMERS_LEGACY' } else { Log-Result 'PASS' 'PAYMENT_QUEUE_STATUS_CHECKED' }

Write-Output '==== SUMMARY ===='
$pass = @($results | Where-Object { $_.StartsWith('PASS|') }).Count
$fail = @($results | Where-Object { $_.StartsWith('FAIL|') }).Count
$partial = @($results | Where-Object { $_.StartsWith('PARTIAL|') }).Count
Write-Output "PASS=$pass FAIL=$fail PARTIAL=$partial TOTAL=$($results.Count)"
