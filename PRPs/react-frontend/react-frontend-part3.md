# PRP Part 3: Product & Order Modules (To Be Expanded)

<!-- DEFERRED: Expand this outline into full implementation details when ready to execute -->

**Feature:** Product Display & Order Management
**Scope:** ~6 files

---

## 1. Module Overview

This part implements the core e-commerce functionality:
- Product listing page with GPU grid
- Buy functionality (add to order)
- Order creation and listing
- Order status display

---

## 2. Files to Implement

| # | File Path | Purpose |
|---|-----------|---------|
| 1 | `frontend/src/pages/ProductsPage.jsx` | Product grid display |
| 2 | `frontend/src/pages/OrdersPage.jsx` | User's orders list |
| 3 | `frontend/src/components/ProductCard.jsx` | Individual product display |
| 4 | `frontend/src/components/OrderCard.jsx` | Individual order display |
| 5 | `frontend/src/hooks/useProducts.js` | Product data fetching hook |
| 6 | `frontend/src/hooks/useOrders.js` | Order data fetching hook |

---

## 3. High-Level Requirements

### 3.1 Products Page (`ProductsPage.jsx`)
- Fetch products from `GET /api/products`
- Display grid of product cards (3-4 columns responsive)
- Loading state while fetching
- Error state if fetch fails
- Empty state if no products

### 3.2 Product Card (`ProductCard.jsx`)
- Display: Name, Description (truncated), Price, Stock
- "Buy Now" button
- Disabled state when out of stock (stock === 0)
- Loading state on purchase
- Success/error feedback

### 3.3 Orders Page (`OrdersPage.jsx`)
- Fetch user's orders from `GET /api/orders`
- Display list of order cards
- Loading state while fetching
- Empty state: "No orders yet"
- Link back to products

### 3.4 Order Card (`OrderCard.jsx`)
- Display: Order ID, Date, Status, Total
- List of items (product name, quantity, price)
- Status badge with colors:
  - PENDING: Yellow
  - PROCESSING: Blue
  - COMPLETED: Green
  - EXPIRED: Red

### 3.5 Custom Hooks
- **useProducts:** Fetch, loading, error, refetch
- **useOrders:** Fetch, loading, error, createOrder, refetch

---

## 4. API Interactions

```javascript
// Get Products
GET /api/products
Response: [{ id, name, description, price, stock, createdAt, updatedAt }]

// Create Order
POST /api/orders
Body: { items: [{ productId: number, quantity: number }] }
Response: { id, userId, total, status, items: [...], createdAt, updatedAt }

// Get User Orders
GET /api/orders
Response: [{ id, userId, total, status, items: [...], createdAt, updatedAt }]
```

---

## 5. User Flow

### Buy Product Flow
```
[User on Products page]
  → [Click "Buy Now" on product]
  → [Show loading state on button]
  → [POST /api/orders with productId, quantity: 1]
  → [Success: Show success toast, update stock display]
  → [Error: Show error toast]
```

### View Orders Flow
```
[User clicks "My Orders" in navbar]
  → [Navigate to /orders]
  → [Fetch GET /api/orders]
  → [Display order cards]
```

---

## 6. Styling Guidelines

### Product Grid
```
Desktop (lg): 4 columns
Tablet (md): 3 columns
Mobile (sm): 2 columns
XS: 1 column
```

### Product Card
- White background, rounded corners
- Shadow on hover
- Price in bold, larger font
- Stock indicator (green if > 5, yellow if 1-5, red if 0)

### Order Card
- White background, rounded corners
- Status badge in top-right
- Items list with subtle dividers
- Total in bold at bottom

---

## 7. Validation Gates

```bash
# Manual testing steps:
1. npm run dev (with backend running)
2. Login with valid credentials
3. Navigate to /products
4. Verify products load in grid
5. Click "Buy Now" on a product
6. Navigate to /orders
7. Verify order appears in list
8. Check stock decremented on product
```

---

## 8. Dependencies on Part 1 & 2

- `api/axios.js` for API calls
- `AuthContext` for user authentication
- `Layout` and `Navbar` for page structure
- `Button`, `Alert` components for UI consistency

---

## 9. Error Handling

| Scenario | UI Response |
|----------|-------------|
| Products fetch fails | Show error alert with retry button |
| Orders fetch fails | Show error alert with retry button |
| Order creation fails | Show error toast, keep button enabled |
| Out of stock | Disable "Buy Now" button, show "Out of Stock" |
| Insufficient stock | Show error: "Only X items available" |

---

## 10. State Management

### Products Page State
```javascript
{
  products: [],      // Product list
  loading: boolean,  // Fetching state
  error: string,     // Error message
  purchasing: number // Product ID being purchased (for button loading)
}
```

### Orders Page State
```javascript
{
  orders: [],        // Order list
  loading: boolean,  // Fetching state
  error: string      // Error message
}
```

---

## 11. Optional Enhancements (If Time Permits)

- Toast notifications for success/error
- Quantity selector on product card
- Order detail modal/page
- Product search/filter
- Pagination for products/orders
