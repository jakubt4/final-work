import { useState } from 'react';
import Button from './Button';

export default function ProductCard({ product, onBuy }) {
  const [purchasing, setPurchasing] = useState(false);
  const [feedback, setFeedback] = useState(null);

  const isOutOfStock = product.stock === 0;

  const getStockColor = (stock) => {
    if (stock === 0) return 'text-red-600';
    if (stock <= 5) return 'text-yellow-600';
    return 'text-green-600';
  };

  const getStockBgColor = (stock) => {
    if (stock === 0) return 'bg-red-100';
    if (stock <= 5) return 'bg-yellow-100';
    return 'bg-green-100';
  };

  const handleBuy = async () => {
    setPurchasing(true);
    setFeedback(null);
    try {
      await onBuy(product.id);
      setFeedback({ type: 'success', message: 'Order placed!' });
      setTimeout(() => setFeedback(null), 3000);
    } catch (err) {
      setFeedback({
        type: 'error',
        message: err.response?.data?.message || 'Purchase failed',
      });
    } finally {
      setPurchasing(false);
    }
  };

  const truncateDescription = (text, maxLength = 80) => {
    if (!text || text.length <= maxLength) return text;
    return text.substring(0, maxLength) + '...';
  };

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4 hover:shadow-md transition-shadow">
      <div className="space-y-3">
        <h3 className="text-lg font-semibold text-gray-900">{product.name}</h3>

        <p className="text-sm text-gray-600 min-h-[40px]">
          {truncateDescription(product.description)}
        </p>

        <div className="flex items-center justify-between">
          <span className="text-2xl font-bold text-gray-900">
            ${product.price.toFixed(2)}
          </span>
          <span
            className={`text-sm font-medium px-2 py-1 rounded ${getStockBgColor(product.stock)} ${getStockColor(product.stock)}`}
          >
            {product.stock === 0 ? 'Out of Stock' : `${product.stock} in stock`}
          </span>
        </div>

        {feedback && (
          <div
            className={`text-sm px-3 py-2 rounded ${
              feedback.type === 'success'
                ? 'bg-green-100 text-green-700'
                : 'bg-red-100 text-red-700'
            }`}
          >
            {feedback.message}
          </div>
        )}

        <Button
          onClick={handleBuy}
          disabled={isOutOfStock}
          loading={purchasing}
          className="w-full"
        >
          {isOutOfStock ? 'Out of Stock' : 'Buy Now'}
        </Button>
      </div>
    </div>
  );
}
