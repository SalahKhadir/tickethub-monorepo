import { useState, useEffect, useCallback } from "react";
import { getTickets } from "@/services/api";

export function useTickets(initialStatuses = []) {
  const [tickets, setTickets] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);
  const [filters, setFilters] = useState({
    status: initialStatuses.join(","),
    priority: "",
    category: "",
    keyword: "",
    page: 0,
  });

  const fetchTickets = useCallback(async () => {
    setLoading(true);
    try {
      const data = await getTickets(filters);
      setTickets(data.content || data);
      setTotalPages(typeof data.totalPages === "number" ? data.totalPages : 0);
    } catch (error) {
      console.error("Failed to fetch tickets", error);
    } finally {
      setLoading(false);
    }
  }, [filters]);

  useEffect(() => {
    fetchTickets();
  }, [fetchTickets]);

  const updateFilter = useCallback((key, value) => {
    setFilters((prev) => ({
      ...prev,
      [key]: value === "ALL" ? "" : value,
      page: key === "page" ? value : 0,
    }));
  }, []);

  return {
    tickets,
    totalPages,
    loading,
    filters,
    updateFilter,
    setTickets,
    refresh: fetchTickets,
  };
}
