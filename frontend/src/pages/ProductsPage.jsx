import { useState } from 'react';
import { Link } from 'react-router-dom';
import Alert from '../components/Alert';
import Button from '../components/Button';
import ProductCard from '../components/ProductCard';
import { useProducts } from '../hooks/useProducts';
import { useOrders } from '../hooks/useOrders';

export default function ProductsPage() {
  const { products, loading, error, refetch } = useProducts();
  const { createOrder } = useOrders();
  const [recentOrder, setRecentOrder] = useState(null);

  const handleBuy = async (productId) => {
    const order = await createOrder([{ productId, quantity: 1 }]);
    setRecentOrder(order);
    // Note: Stock will be deducted when order is COMPLETED, not immediately
    return order;
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

  if (products.length === 0) {
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
            d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"
          />
        </svg>
        <h3 className="mt-2 text-sm font-medium text-gray-900">No products</h3>
        <p className="mt-1 text-sm text-gray-500">
          No products are currently available.
        </p>
      </div>
    );
  }

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">GPU Products</h1>
        <p className="text-gray-600 mt-1">Browse our selection of GPUs</p>
      </div>

      {/* Recent order notification */}
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

      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
        {products.map((product) => (
          <ProductCard key={product.id} product={product} onBuy={handleBuy} />
        ))}
      </div>
    </div>
  );
}
