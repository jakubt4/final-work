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
