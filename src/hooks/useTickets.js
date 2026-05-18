import { useState, useEffect } from 'react';
import { getTickets } from '@/services/api';

export function useTickets(initialStatuses = []) {
  const [tickets, setTickets] = useState([]);
  const [loading, setLoading] = useState(false);
  const [filters, setFilters] = useState({
    status: initialStatuses.join(','),
    priority: '',
    category: ''
  });

  const fetchTickets = async () => {
    setLoading(true);
    try {
      const data = await getTickets(filters);
      setTickets(data.content || data);
    } catch (error) {
      console.error("Failed to fetch tickets", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTickets();
  }, [filters]);

  const updateFilter = (key, value) => {
    setFilters(prev => ({ ...prev, [key]: value === 'ALL' ? '' : value }));
  };

  return { tickets, loading, filters, updateFilter, setTickets, refresh: fetchTickets };
}
