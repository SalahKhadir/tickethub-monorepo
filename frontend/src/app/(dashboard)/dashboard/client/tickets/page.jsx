"use client";

import ClientTicketsPanel from "@/components/features/ClientTicketsPanel";
import { useTickets } from "@/hooks/useTickets";

export default function ClientTicketsPage() {
  const { tickets, totalPages, loading, filters, updateFilter, refresh } =
    useTickets();

  return (
    <ClientTicketsPanel
      tickets={tickets}
      totalPages={totalPages}
      loading={loading}
      filters={filters}
      updateFilter={updateFilter}
      refresh={refresh}
    />
  );
}
