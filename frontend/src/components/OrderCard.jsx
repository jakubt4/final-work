import { useEffect, useRef } from 'react';
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
  const previousStatusRef = useRef(initialOrder.status);
  const { order: polledOrder, isPolling } = useOrderPolling(
    initialOrder.id,
    initialOrder.status
  );

  const order = polledOrder || initialOrder;
  const status = statusConfig[order.status] || statusConfig.PENDING;

  // Detect status changes
  useEffect(() => {
    if (order.status !== previousStatusRef.current) {
      previousStatusRef.current = order.status;
      if (onStatusChange) {
        onStatusChange(order.id, order.status);
      }
    }
  }, [order.status, order.id, onStatusChange]);

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
