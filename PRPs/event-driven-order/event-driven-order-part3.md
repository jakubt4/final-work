# PRP Part 3: Frontend Polling & UI Updates

<!-- EXPAND_BEFORE_IMPLEMENTING: This part requires full technical detail expansion -->

**Feature:** Event-Driven Order Lifecycle & Notifications
**Part Focus:** React frontend polling for order status updates
**Created:** 2026-01-18
**Confidence Score:** 9/10

---

## Executive Summary

This PRP covers:
1. useOrderPolling hook for automatic status updates
2. OrderCard component updates with processing indicator
3. OrdersPage integration with polling
4. Status-specific UI feedback (spinners, transitions)

**Files Covered in This Part:** 5 files

---

## 1. Files to Create/Modify

### 1.1 New Files

| # | File Path | Purpose |
|---|-----------|---------|
| 1 | `frontend/src/hooks/useOrderPolling.js` | Polling hook for individual order status |

### 1.2 Modified Files

| # | File Path | Modification |
|---|-----------|--------------|
| 1 | `frontend/src/hooks/useOrders.js` | Add refetch interval for active orders |
| 2 | `frontend/src/components/OrderCard.jsx` | Add processing indicator, polling integration |
| 3 | `frontend/src/pages/OrdersPage.jsx` | Integrate polling, show live updates |
| 4 | `frontend/src/pages/ProductsPage.jsx` | Show order status after purchase |

---

## 2. Implementation Details

### 2.1 useOrderPolling Hook

**File:** `frontend/src/hooks/useOrderPolling.js`

```javascript
import { useState, useEffect, useCallback, useRef } from 'react';
import api from '../api/axios';

const ACTIVE_STATUSES = ['PENDING', 'PROCESSING'];
const POLL_INTERVAL = 3000; // 3 seconds

/**
 * Hook for polling a single order's status until it reaches a terminal state.
 *
 * @param {number} orderId - The order ID to poll
 * @param {string} initialStatus - Initial status to determine if polling is needed
 * @returns {Object} - { order, loading, error, isPolling, stopPolling }
 */
export function useOrderPolling(orderId, initialStatus) {
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [isPolling, setIsPolling] = useState(ACTIVE_STATUSES.includes(initialStatus));
  const intervalRef = useRef(null);

  const fetchOrder = useCallback(async () => {
    if (!orderId) return;

    try {
      setLoading(true);
      const response = await api.get(`/orders/${orderId}`);
      setOrder(response.data);

      // Stop polling on terminal states
      if (!ACTIVE_STATUSES.includes(response.data.status)) {
        setIsPolling(false);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch order');
      setIsPolling(false);
    } finally {
      setLoading(false);
    }
  }, [orderId]);

  const stopPolling = useCallback(() => {
    setIsPolling(false);
  }, []);

  useEffect(() => {
    if (!isPolling || !orderId) {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
        intervalRef.current = null;
      }
      return;
    }

    // Initial fetch
    fetchOrder();

    // Set up polling interval
    intervalRef.current = setInterval(fetchOrder, POLL_INTERVAL);

    return () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
        intervalRef.current = null;
      }
    };
  }, [isPolling, orderId, fetchOrder]);

  return { order, loading, error, isPolling, stopPolling };
}

export default useOrderPolling;
```

### 2.2 Updated useOrders Hook

**File:** `frontend/src/hooks/useOrders.js`
**Action:** ADD polling capability for orders list

```javascript
import { useState, useEffect, useCallback, useRef } from 'react';
import api from '../api/axios';

const ACTIVE_STATUSES = ['PENDING', 'PROCESSING'];
const POLL_INTERVAL = 3000; // 3 seconds

export function useOrders() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const intervalRef = useRef(null);

  const hasActiveOrders = useCallback(() => {
    return orders.some((order) => ACTIVE_STATUSES.includes(order.status));
  }, [orders]);

  const fetchOrders = useCallback(async () => {
    setError(null);
    try {
      const response = await api.get('/orders');
      setOrders(response.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch orders');
    } finally {
      setLoading(false);
    }
  }, []);

  // Initial fetch
  useEffect(() => {
    fetchOrders();
  }, [fetchOrders]);

  // Polling for active orders
  useEffect(() => {
    // Clear any existing interval
    if (intervalRef.current) {
      clearInterval(intervalRef.current);
      intervalRef.current = null;
    }

    // Only poll if there are active orders
    if (hasActiveOrders()) {
      intervalRef.current = setInterval(fetchOrders, POLL_INTERVAL);
    }

    return () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
        intervalRef.current = null;
      }
    };
  }, [hasActiveOrders, fetchOrders]);

  const createOrder = async (items) => {
    const response = await api.post('/orders', { items });
    setOrders((prev) => [response.data, ...prev]);
    return response.data;
  };

  return {
    orders,
    loading,
    error,
    refetch: fetchOrders,
    createOrder,
    hasActiveOrders: hasActiveOrders(),
  };
}

export default useOrders;
```

### 2.3 Updated OrderCard Component

**File:** `frontend/src/components/OrderCard.jsx`
**Action:** ADD processing indicator and status transition animations

```javascript
import { useState, useEffect } from 'react';
import { useOrderPolling } from '../hooks/useOrderPolling';

const statusConfig = {
  PENDING: {
    bg: 'bg-yellow-100',
    text: 'text-yellow-800',
    label: 'Pending',
    showSpinner: true,
  },
  PROCESSING: {
    bg: 'bg-blue-100',
    text: 'text-blue-800',
    label: 'Processing',
    showSpinner: true,
  },
  COMPLETED: {
    bg: 'bg-green-100',
    text: 'text-green-800',
    label: 'Completed',
    showSpinner: false,
  },
  EXPIRED: {
    bg: 'bg-red-100',
    text: 'text-red-800',
    label: 'Expired',
    showSpinner: false,
  },
};

const ProcessingSpinner = () => (
  <svg
    className="animate-spin h-4 w-4 text-blue-600"
    xmlns="http://www.w3.org/2000/svg"
    fill="none"
    viewBox="0 0 24 24"
  >
    <circle
      className="opacity-25"
      cx="12"
      cy="12"
      r="10"
      stroke="currentColor"
      strokeWidth="4"
    />
    <path
      className="opacity-75"
      fill="currentColor"
      d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
    />
  </svg>
);

export default function OrderCard({ order: initialOrder, onStatusChange }) {
  const [previousStatus, setPreviousStatus] = useState(initialOrder.status);
  const { order: polledOrder, isPolling } = useOrderPolling(
    initialOrder.id,
    initialOrder.status
  );

  const order = polledOrder || initialOrder;
  const status = statusConfig[order.status] || statusConfig.PENDING;

  // Detect status changes
  useEffect(() => {
    if (order.status !== previousStatus) {
      setPreviousStatus(order.status);
      if (onStatusChange) {
        onStatusChange(order.id, order.status);
      }
    }
  }, [order.status, previousStatus, order.id, onStatusChange]);

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div
      className={`bg-white rounded-lg shadow-sm border border-gray-200 p-4 transition-all duration-300 ${
        isPolling ? 'border-blue-300 shadow-md' : ''
      }`}
    >
      <div className="flex justify-between items-start mb-4">
        <div>
          <p className="text-sm text-gray-500">Order #{order.id}</p>
          <p className="text-sm text-gray-500">{formatDate(order.createdAt)}</p>
        </div>
        <div className="flex items-center gap-2">
          {status.showSpinner && isPolling && <ProcessingSpinner />}
          <span
            className={`px-3 py-1 rounded-full text-xs font-medium ${status.bg} ${status.text}`}
          >
            {status.label}
          </span>
        </div>
      </div>

      {/* Processing message */}
      {isPolling && (
        <div className="mb-4 p-3 bg-blue-50 rounded-md">
          <p className="text-sm text-blue-700">
            {order.status === 'PENDING' && 'Your order is being prepared...'}
            {order.status === 'PROCESSING' && 'Processing payment, please wait...'}
          </p>
        </div>
      )}

      <div className="border-t border-gray-100 pt-3">
        <h4 className="text-sm font-medium text-gray-700 mb-2">Items</h4>
        <ul className="space-y-2">
          {order.items?.map((item, index) => (
            <li
              key={index}
              className="flex justify-between text-sm border-b border-gray-50 pb-2 last:border-0"
            >
              <span className="text-gray-600">
                {item.productName || `Product #${item.productId}`} x {item.quantity}
              </span>
              <span className="text-gray-900 font-medium">
                ${(item.price * item.quantity).toFixed(2)}
              </span>
            </li>
          ))}
        </ul>
      </div>

      <div className="border-t border-gray-200 mt-3 pt-3 flex justify-between">
        <span className="text-sm font-medium text-gray-700">Total</span>
        <span className="text-lg font-bold text-gray-900">
          ${order.total.toFixed(2)}
        </span>
      </div>

      {/* Status change notification */}
      {order.status === 'COMPLETED' && (
        <div className="mt-3 p-3 bg-green-50 rounded-md">
          <p className="text-sm text-green-700">
            Your order has been completed! Thank you for your purchase.
          </p>
        </div>
      )}

      {order.status === 'EXPIRED' && (
        <div className="mt-3 p-3 bg-red-50 rounded-md">
          <p className="text-sm text-red-700">
            This order has expired. Please contact support if you need assistance.
          </p>
        </div>
      )}
    </div>
  );
}
```

### 2.4 Updated OrdersPage

**File:** `frontend/src/pages/OrdersPage.jsx`
**Action:** ADD status change handling and refresh notification

```javascript
import { useState } from 'react';
import { Link } from 'react-router-dom';
import Alert from '../components/Alert';
import Button from '../components/Button';
import OrderCard from '../components/OrderCard';
import { useOrders } from '../hooks/useOrders';

export default function OrdersPage() {
  const { orders, loading, error, refetch, hasActiveOrders } = useOrders();
  const [statusChanges, setStatusChanges] = useState([]);

  const handleStatusChange = (orderId, newStatus) => {
    setStatusChanges((prev) => [
      ...prev.filter((sc) => sc.orderId !== orderId),
      { orderId, newStatus, timestamp: Date.now() },
    ]);

    // Auto-dismiss after 5 seconds
    setTimeout(() => {
      setStatusChanges((prev) => prev.filter((sc) => sc.orderId !== orderId));
    }, 5000);
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center py-12">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-md mx-auto py-8">
        <Alert type="error" message={error} />
        <div className="mt-4 text-center">
          <Button onClick={refetch}>Retry</Button>
        </div>
      </div>
    );
  }

  if (orders.length === 0) {
    return (
      <div className="text-center py-12">
        <svg
          className="mx-auto h-12 w-12 text-gray-400"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
          />
        </svg>
        <h3 className="mt-2 text-sm font-medium text-gray-900">No orders yet</h3>
        <p className="mt-1 text-sm text-gray-500">
          Start shopping to see your orders here.
        </p>
        <div className="mt-6">
          <Link
            to="/products"
            className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
          >
            Browse Products
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div>
      <div className="mb-6">
        <div className="flex justify-between items-center">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">My Orders</h1>
            <p className="text-gray-600 mt-1">View your order history</p>
          </div>
          {hasActiveOrders && (
            <div className="flex items-center gap-2 text-sm text-blue-600">
              <div className="animate-pulse w-2 h-2 bg-blue-600 rounded-full"></div>
              <span>Live updates active</span>
            </div>
          )}
        </div>
      </div>

      {/* Status change notifications */}
      {statusChanges.length > 0 && (
        <div className="mb-4 space-y-2">
          {statusChanges.map((sc) => (
            <Alert
              key={`${sc.orderId}-${sc.timestamp}`}
              type={sc.newStatus === 'COMPLETED' ? 'success' : 'info'}
              message={`Order #${sc.orderId} status updated to ${sc.newStatus}`}
            />
          ))}
        </div>
      )}

      <div className="space-y-4">
        {orders.map((order) => (
          <OrderCard
            key={order.id}
            order={order}
            onStatusChange={handleStatusChange}
          />
        ))}
      </div>
    </div>
  );
}
```

### 2.5 Updated ProductsPage (Optional Enhancement)

**File:** `frontend/src/pages/ProductsPage.jsx`
**Action:** ADD order status tracking after purchase

```javascript
// Add to existing imports
import { useState } from 'react';
import Alert from '../components/Alert';

// Inside ProductsPage component, add state for recent order
const [recentOrder, setRecentOrder] = useState(null);

// Modify handleBuy to track the order
const handleBuy = async (productId) => {
  try {
    setLoading(true);
    const order = await createOrder([{ productId, quantity: 1 }]);
    setRecentOrder(order);
    updateProductStock(productId, 1); // Optimistic update (may not happen immediately now)
  } catch (err) {
    setError(err.response?.data?.message || 'Failed to create order');
  } finally {
    setLoading(false);
  }
};

// Add recent order notification in JSX (after the header)
{recentOrder && (
  <div className="mb-6">
    <Alert
      type="info"
      message={`Order #${recentOrder.id} created! Status: ${recentOrder.status}. View your orders page for live updates.`}
    />
    <div className="mt-2 text-center">
      <Link
        to="/orders"
        className="text-blue-600 hover:text-blue-800 text-sm font-medium"
      >
        View Orders
      </Link>
    </div>
  </div>
)}
```

---

## 3. Validation Gates

### Gate 1: Lint Check
```bash
cd frontend && npm run lint
```
**Expected:** No errors

### Gate 2: Build Check
```bash
cd frontend && npm run build
```
**Expected:** Build succeeds

### Gate 3: Manual E2E Test
1. Start backend: `./mvnw spring-boot:run`
2. Start frontend: `cd frontend && npm run dev`
3. Login and create an order
4. Verify:
   - Order appears immediately with PENDING status
   - Status updates to PROCESSING within 1-2 seconds
   - Spinner shows during active states
   - Status updates to COMPLETED or stays PROCESSING (50% rate)
   - Polling stops on terminal states
   - "Live updates active" indicator shows when orders are processing

---

## 4. User Experience Flow

```
1. User clicks "Buy" on product
   └─> Order created with PENDING status
   └─> User sees success message with order ID

2. OrderCard displays PENDING
   └─> Yellow badge
   └─> Spinner icon
   └─> "Your order is being prepared..." message

3. After ~1s: Status changes to PROCESSING
   └─> Blue badge
   └─> "Processing payment, please wait..." message
   └─> Polling continues every 3s

4a. Success (50%): After ~5s status becomes COMPLETED
    └─> Green badge
    └─> "Your order has been completed!" message
    └─> Polling stops
    └─> Stock deducted from product

4b. Failure (50%): Status stays PROCESSING
    └─> After 10 minutes: Scheduler expires order
    └─> Status becomes EXPIRED
    └─> Red badge
    └─> "This order has expired." message
    └─> Polling stops
```

---

## 5. Dependencies

- **Depends on Part 1:** Backend API must return order status
- **Depends on Part 2:** Expiration creates EXPIRED status

---

## 6. Summary

**Part 3 delivers:**
- useOrderPolling hook for automatic status updates
- Enhanced OrderCard with processing indicators
- Live update indicators on OrdersPage
- Automatic polling start/stop based on order status
- User-friendly status transition messages

**Files created/modified:** 5
