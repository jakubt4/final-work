import { useState, useEffect, useCallback } from 'react';
import api from '../api/axios';

export function useOrders() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchOrders = useCallback(async () => {
    setLoading(true);
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

  useEffect(() => {
    fetchOrders();
  }, [fetchOrders]);

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
  };
}

export default useOrders;
