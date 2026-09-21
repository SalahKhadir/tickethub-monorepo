"use client";

"use client";

import AdminOverviewPanel from "@/components/features/AdminOverviewPanel";
import { useTickets } from "@/hooks/useTickets";

export default function AdminDashboardPage() {
  const { tickets } = useTickets();
  return <AdminOverviewPanel tickets={tickets} />;
}
