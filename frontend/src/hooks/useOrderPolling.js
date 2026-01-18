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
