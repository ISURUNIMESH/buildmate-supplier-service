# 15 — User Manual

> UI routes and behaviors as implemented in `buildmate-client`.

---

## Login

1. Open `http://localhost:25173/login` (or Compose `full` same host port)
2. Choose Google / continue to Auth Server
3. After Google consent, you return to `/oauth/callback`
4. On success you land in the authenticated app shell

If login fails, you may be redirected to `/login`.

---

## Dashboard

Path: `/`

Overview landing page inside `MainLayout` (header + sidebar). Use the sidebar to reach domain modules.

---

## Suppliers

Path: `/suppliers`

Typical actions available in the UI (backed by Supplier APIs):

- View supplier list  
- Register / create  
- Edit (update mutable fields — not password via update DTO)  
- Change status  
- Adjust rating  
- View top-rated  
- Manage documents where UI exposes document flows  

---

## Materials

Path: `/materials`

- Browse catalog  
- Create / update / delete materials  
- Adjust stock and price where controls exist  
- Work with brands and categories (via material API module)

---

## Orders

Path: `/orders`

- Create and list orders  
- Inspect order details / status  
- Filter patterns supported by API (user / status) as exposed in UI  

Creating an order publishes an asynchronous `OrderCreated` event for Payment to observe.

---

## Cart

Path: `/cart`

- Add materials to a user cart  
- View cart by user  
- Clear cart  

> Quantity update and checkout workflows are **minimal** in API/UI compared to full e-commerce carts.

---

## Inventory

Path: `/inventory`

- View inventory records  
- Create inventory entries  
- Reserve / release stock (API-backed)  
- Review inventory history where shown  

---

## Payments

Path: `/payments`

- Create payments linked to orders  
- View history / pending / by status  
- Update status, refund, or retry as the UI exposes  

Completing a payment can publish `PaymentCompleted`, after which the related order may show as **PAID**.

---

## Invoices

Path: `/invoices`

- Create invoices  
- Retrieve invoice by id  

---

## Reports

Path: `/reports`

- Revenue report  
- Monthly report  
- Top customers  

Data comes from Payment service report endpoints.

---

## Theme

Use the header theme control to switch light/dark. Preference stores as `buildmate_theme`.

---

## Logout

Use logout in the header/shell. This clears stored auth keys (`buildmate_access_token`, related user cache) and returns you to the login experience / protected-route redirect.
