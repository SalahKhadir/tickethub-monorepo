"use client";

import AdminOverviewPanel from "@/components/features/AdminOverviewPanel";
import { useTickets } from "@/hooks/useTickets";

export default function AdminPage() {
    const { tickets } = useTickets();
    return <AdminOverviewPanel tickets={tickets} />;
}
