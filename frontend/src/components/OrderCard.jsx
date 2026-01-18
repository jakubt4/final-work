const statusConfig = {
  PENDING: {
    bg: 'bg-yellow-100',
    text: 'text-yellow-800',
    label: 'Pending',
  },
  PROCESSING: {
    bg: 'bg-blue-100',
    text: 'text-blue-800',
    label: 'Processing',
  },
  COMPLETED: {
    bg: 'bg-green-100',
    text: 'text-green-800',
    label: 'Completed',
  },
  EXPIRED: {
    bg: 'bg-red-100',
    text: 'text-red-800',
    label: 'Expired',
  },
};

export default function OrderCard({ order }) {
  const status = statusConfig[order.status] || statusConfig.PENDING;

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
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4">
      <div className="flex justify-between items-start mb-4">
        <div>
          <p className="text-sm text-gray-500">Order #{order.id}</p>
          <p className="text-sm text-gray-500">{formatDate(order.createdAt)}</p>
        </div>
        <span
          className={`px-3 py-1 rounded-full text-xs font-medium ${status.bg} ${status.text}`}
        >
          {status.label}
        </span>
      </div>

      <div className="border-t border-gray-100 pt-3">
        <h4 className="text-sm font-medium text-gray-700 mb-2">Items</h4>
        <ul className="space-y-2">
          {order.items?.map((item, index) => (
            <li
              key={index}
              className="flex justify-between text-sm border-b border-gray-50 pb-2 last:border-0"
            >
              <span className="text-gray-600">
                {item.productName || `Product #${item.productId}`} x{' '}
                {item.quantity}
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
    </div>
  );
}
