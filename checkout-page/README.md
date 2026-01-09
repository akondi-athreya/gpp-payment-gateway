# Checkout Page

Standalone payment checkout page for customers.

## Usage

Open `index.html` in a browser with an order ID:

```
file:///path/to/checkout-page/index.html?order_id=order_xxxxxxxxx
```

Or serve it with a simple HTTP server:

```bash
python3 -m http.server 8080
```

Then access: `http://localhost:8080?order_id=order_xxxxxxxxx`

## Features

- Accepts UPI and Card payments
- Real-time payment status updates
- No authentication required (uses public endpoints)
- Mobile responsive design
- Secure card number formatting

## Integration

1. Create an order via API
2. Redirect customer to checkout page with order_id parameter
3. Customer completes payment
4. Poll payment status or use webhooks (Deliverable 2)

## API Endpoint

Update `API_BASE_URL` in index.html if backend is not at localhost:8000
