"use client";

import { useEffect, useMemo, useState, useCallback } from "react";
import { useRouter } from "next/navigation";
import Button from "@/components/ui/Button";
import TicketActions from "@/components/features/TicketActions";
import { useAuth } from "@/hooks/useAuth";
import { useTickets } from "@/hooks/useTickets";
import { ROLES } from "@/constants/roles";
import { assignTicket, getTechnicians, getTechnicianAvailability } from "@/services/api";
import PriorityBadge from "@/components/ui/PriorityBadge";
import StatusBadge from "@/components/ui/StatusBadge";

const STATUS_OPTIONS   = ["", "NEW", "ACCEPTED", "IN_PROGRESS", "RESOLVED", "CLOSED"];
const PRIORITY_OPTIONS = ["", "LOW", "MEDIUM", "HIGH", "CRITICAL"];
const CATEGORY_OPTIONS = ["", "HARDWARE", "SOFTWARE", "NETWORK", "SECURITY", "OTHER"];
const OVERLOAD_THRESHOLD = 3;

const formatDate = (value) => {
    if (!value) return "N/A";
    const parsed = new Date(value);
    return Number.isNaN(parsed.getTime()) ? value : parsed.toLocaleString();
};

// ── Smart technician card ────────────────────────────────────────────────────
function TechnicianCard({ technician, activeCount, selected, onSelect }) {
    const overloaded = activeCount > OVERLOAD_THRESHOLD;
    const id = String(technician?.id || technician?.userId || technician?.technicianId || "");
    const label = technician?.fullName || technician?.username || technician?.email || `Technician ${id}`;

    return (
        <button
            type="button"
            onClick={() => onSelect(id)}
            className={`w-full text-left rounded-xl border px-4 py-3 transition-all duration-150 ${
                selected
                    ? "border-blue-500 bg-blue-50 ring-2 ring-blue-500/20"
                    : overloaded
                    ? "border-red-200 bg-red-50/40 hover:border-red-300"
                    : "border-gray-200 bg-white hover:border-blue-300 hover:bg-blue-50/30"
            }`}
        >
            <div className="flex items-center justify-between gap-2">
                <span className="text-sm font-medium text-gray-900 truncate">{label}</span>
                <span
                    className={`shrink-0 inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-xs font-semibold ${
                        overloaded
                            ? "bg-red-100 text-red-600 border border-red-200"
                            : "bg-gray-100 text-gray-600 border border-gray-200"
                    }`}
                >
                    {overloaded && <span className="h-1.5 w-1.5 rounded-full bg-red-500 animate-pulse" />}
                    {activeCount} active
                </span>
            </div>
            {overloaded && (
                <p className="mt-1 text-xs text-red-500 font-medium">
                    ⚠ High workload — consider another technician
                </p>
            )}
        </button>
    );
}

// ── Assignment panel ─────────────────────────────────────────────────────────
function AssignmentPanel({ ticketId, onAssigned, onCancel }) {
    const [technicians, setTechnicians]       = useState([]);
    const [availability, setAvailability]     = useState({});
    const [search, setSearch]                 = useState("");
    const [selectedId, setSelectedId]         = useState("");
    const [loading, setLoading]               = useState(true);
    const [assigning, setAssigning]           = useState(false);
    const [error, setError]                   = useState("");

    useEffect(() => {
        let active = true;
        (async () => {
            try {
                const [techs, avail] = await Promise.allSettled([
                    getTechnicians(),
                    getTechnicianAvailability(),
                ]);
                if (!active) return;
                if (techs.status === "fulfilled") setTechnicians(techs.value);
                if (avail.status === "fulfilled") setAvailability(avail.value);
            } catch {
                // individual errors handled below
            } finally {
                if (active) setLoading(false);
            }
        })();
        return () => { active = false; };
    }, []);

    const filtered = useMemo(() => {
        const kw = search.trim().toLowerCase();
        if (!kw) return technicians;
        return technicians.filter((t) => {
            const name = String(t?.fullName || t?.username || t?.email || "").toLowerCase();
            const id   = String(t?.id || t?.userId || t?.technicianId || "").toLowerCase();
            return name.includes(kw) || id.includes(kw);
        });
    }, [technicians, search]);

    const handleConfirm = async () => {
        if (!selectedId) { setError("Please select a technician."); return; }
        setAssigning(true);
        setError("");
        try {
            await assignTicket(ticketId, Number(selectedId));
            const tech = technicians.find((t) =>
                String(t?.id || t?.userId || t?.technicianId || "") === selectedId
            );
            onAssigned(tech?.fullName || tech?.username || tech?.email || `Technician ${selectedId}`);
        } catch (err) {
            setError(
                err?.response?.data?.message || err?.message || "Unable to assign ticket."
            );
        } finally {
            setAssigning(false);
        }
    };

    return (
        <div className="mt-5 rounded-xl border border-gray-100 bg-white p-5 shadow-sm space-y-4">
            <div className="flex items-center justify-between">
                <p className="text-sm font-bold text-gray-800">Assign Technician</p>
                <button type="button" onClick={onCancel} className="text-xs text-gray-400 hover:text-gray-600">
                    Cancel
                </button>
            </div>

            <input
                type="search"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Search by name or email…"
                className="h-10 w-full rounded-xl border border-gray-200 bg-gray-50 px-4 text-sm text-gray-800 placeholder:text-gray-400 focus:border-blue-500 focus:bg-white focus:outline-none focus:ring-4 focus:ring-blue-500/10 transition-all"
            />

            {loading ? (
                <p className="text-sm text-gray-400">Loading technicians…</p>
            ) : filtered.length === 0 ? (
                <p className="text-sm text-gray-400">No technicians found.</p>
            ) : (
                <div className="max-h-64 overflow-y-auto space-y-2 pr-1">
                    {filtered.map((tech) => {
                        const id = String(tech?.id || tech?.userId || tech?.technicianId || "");
                        const count = availability[id] ?? 0;
                        return (
                            <TechnicianCard
                                key={id}
                                technician={tech}
                                activeCount={count}
                                selected={selectedId === id}
                                onSelect={setSelectedId}
                            />
                        );
                    })}
                </div>
            )}

            {error && (
                <p className="text-sm text-red-600 bg-red-50 px-3 py-2 rounded-lg border border-red-100">
                    {error}
                </p>
            )}

            <div className="flex justify-end pt-1">
                <button
                    type="button"
                    onClick={handleConfirm}
                    disabled={assigning || loading || !selectedId}
                    className="bg-gray-900 text-white rounded-xl px-5 py-2 text-sm font-semibold hover:bg-gray-800 hover:shadow-md transition-all duration-200 disabled:opacity-50 disabled:shadow-none"
                >
                    {assigning ? "Assigning…" : "Confirm Assignment"}
                </button>
            </div>
        </div>
    );
}

// ── Page ─────────────────────────────────────────────────────────────────────
export default function AdminTicketsPage() {
    const router      = useRouter();
    const { user, loading: authLoading, isAuthenticated } = useAuth();
    
    // Leverage the new useTickets custom hook for isolated state management
    const { tickets, loading, filters, updateFilter, refresh } = useTickets();

    const [assignmentOpenTicketId,   setAssignmentOpenTicketId]   = useState(null);
    const [assignedTechnicianByTicket, setAssignedTechnicianByTicket] = useState({});
    const [assignmentFeedback,       setAssignmentFeedback]       = useState({ type: "", message: "" });
    const [selectedTicket,           setSelectedTicket]           = useState(null);

    const isAdmin = String(user?.role || "").toLowerCase() === ROLES.ADMIN;

    useEffect(() => {
        if (authLoading) return;
        if (!isAuthenticated || !isAdmin) router.replace("/login");
    }, [authLoading, isAuthenticated, isAdmin, router]);

    const handleAssigned = useCallback((ticketId, techName) => {
        setAssignedTechnicianByTicket((prev) => ({ ...prev, [ticketId]: techName }));
        setAssignmentOpenTicketId(null);
        setAssignmentFeedback({ type: "success", message: "Ticket assigned successfully." });
        refresh();
    }, [refresh]);

    if (authLoading || (!isAuthenticated && !isAdmin)) return null;

    const selectClass = "h-11 rounded-[10px] border border-[rgba(17,24,39,0.12)] bg-white px-4 text-sm text-ink-black focus:border-electric-sapphire focus:outline-none focus:ring-2 focus:ring-[rgba(99,102,241,0.15)]";

    return (
    <>
        <section className="rounded-2xl border border-[rgba(17,24,39,0.08)] bg-white p-8 shadow-sm">
            {/* Header */}
            <div className="flex flex-wrap items-start justify-between gap-4">
                <div>
                    <p className="text-xs font-semibold uppercase tracking-[0.25em] text-slate-grey">Admin workspace</p>
                    <h1 className="mt-3 text-[22px] font-semibold text-ink-black">All Tickets</h1>
                    <p className="mt-2 text-sm text-slate-grey">Track tickets, review details, and manage assignments.</p>
                </div>

                {assignmentFeedback.message && (
                    <div
                        role="alert"
                        className={`mt-4 rounded-[10px] border px-4 py-3 text-sm ${
                            assignmentFeedback.type === "success"
                                ? "border-[rgba(16,185,129,0.25)] bg-[#D1FAE5] text-[#065F46]"
                                : "border-[rgba(239,68,68,0.25)] bg-[#FEE2E2] text-[#991B1B]"
                        }`}
                    >
                        {assignmentFeedback.message}
                    </div>
                )}

                <Button type="button" variant="ghost" className="h-11 px-5" onClick={() => router.back()}>
                    ← Back to dashboard
                </Button>
            </div>

            {/* Filter bar */}
            <div className="mt-6 flex flex-wrap items-center justify-between gap-3">
                <div className="flex flex-wrap items-center gap-2">
                    <select className={selectClass} value={filters.status}
                        onChange={(e) => updateFilter('status', e.target.value)}>
                        {STATUS_OPTIONS.map((s) => (
                            <option key={s || "ALL_STATUS"} value={s}>{s || "All statuses"}</option>
                        ))}
                    </select>

                    <select className={selectClass} value={filters.priority}
                        onChange={(e) => updateFilter('priority', e.target.value)}>
                        {PRIORITY_OPTIONS.map((p) => (
                            <option key={p || "ALL_PRIORITY"} value={p}>{p || "All priorities"}</option>
                        ))}
                    </select>

                    <select className={selectClass} value={filters.category}
                        onChange={(e) => updateFilter('category', e.target.value)}>
                        {CATEGORY_OPTIONS.map((c) => (
                            <option key={c || "ALL_CATEGORY"} value={c}>{c || "All categories"}</option>
                        ))}
                    </select>
                </div>

                <Button type="button" variant="ghost" className="h-11 px-5" onClick={refresh} disabled={loading}>
                    Refresh
                </Button>
            </div>

            {/* Ticket list */}
            <div className="mt-6 space-y-4 w-full">
                {loading ? (
                    <p className="text-sm text-slate-grey">Loading tickets…</p>
                ) : tickets.length === 0 ? (
                    <div className="flex min-h-55 items-center justify-center rounded-[14px] border border-[rgba(17,24,39,0.08)] bg-bright-snow px-6 text-center text-sm text-slate-grey">
                        No tickets found.
                    </div>
                ) : (
                    tickets.map((ticket) => {
                        const status       = String(ticket.status || "").toUpperCase();
                        const assigneeName = ticket.assigneeName || ticket.technicianName || assignedTechnicianByTicket[ticket.id];
                        const clientName   = ticket.authorName || ticket.clientName || ticket.createdBy || "Client";

                        return (
                            <div
                                key={ticket.id}
                                className="bg-white border border-gray-200/80 rounded-xl p-5 flex flex-col xl:flex-row xl:items-center justify-between gap-6 hover:border-gray-300 hover:shadow-sm transition-all duration-200"
                            >
                                {/* Left: ticket info */}
                                <div className="flex-1 grid grid-cols-1 md:grid-cols-3 gap-4 items-center">
                                    {/* ID + Title */}
                                    <div className="space-y-1">
                                        <span className="text-xs font-mono text-gray-400 bg-gray-50 px-2 py-0.5 rounded border border-gray-100 inline-block">
                                            #TK-{ticket.id}
                                        </span>
                                        <h3 className="text-sm font-semibold text-gray-900 tracking-tight block truncate" title={ticket.title}>
                                            {ticket.title || "Untitled"}
                                        </h3>
                                    </div>

                                    {/* Category + Client */}
                                    <div className="flex flex-col space-y-1">
                                        <span className="text-xs text-gray-400">
                                            Category: <strong className="text-gray-700 font-medium">{ticket.category || "—"}</strong>
                                        </span>
                                        <span className="text-xs text-gray-400">
                                            Client: <strong className="text-gray-700 font-medium">{clientName}</strong>
                                        </span>
                                    </div>

                                    {/* Badges */}
                                    <div className="flex gap-2 items-center md:justify-end flex-wrap">
                                        <PriorityBadge priority={ticket.priority} />
                                        <StatusBadge status={ticket.status} />
                                    </div>
                                </div>

                                {/* Right: action zone — fixed horizontal row */}
                                <div className="flex flex-row items-center gap-3 justify-end min-w-[360px] h-10 border-l border-gray-200/60 pl-4">

                                    {/* View Details — always visible */}
                                    <button
                                        type="button"
                                        onClick={() => setSelectedTicket(ticket)}
                                        className="h-full px-4 border border-gray-200 rounded-lg text-xs font-medium text-gray-500 hover:text-gray-800 hover:bg-gray-50 hover:border-gray-300 transition-all whitespace-nowrap flex items-center justify-center"
                                    >
                                        View Details
                                    </button>

                                    {/* Dynamic action — fixed w-48 */}
                                    <div className="w-48 h-full">
                                        {status === "NEW" ? (
                                            <div className="w-full h-full flex items-center">
                                                <TicketActions ticket={ticket} onActionComplete={refresh} />
                                            </div>
                                        ) : status === "ACCEPTED" ? (
                                            assigneeName ? (
                                                <div className="w-full h-full flex items-center justify-center gap-1.5 rounded-lg border border-emerald-200 bg-emerald-50 px-3 text-xs font-semibold text-emerald-700 whitespace-nowrap">
                                                    <span className="h-1.5 w-1.5 rounded-full bg-emerald-500 shrink-0" />
                                                    {assigneeName}
                                                </div>
                                            ) : (
                                                <button
                                                    type="button"
                                                    className="w-full h-full bg-blue-600 hover:bg-blue-500 text-white text-xs font-medium px-4 rounded-lg transition-all active:scale-[0.98] shadow-sm flex items-center justify-center whitespace-nowrap"
                                                    onClick={() =>
                                                        setAssignmentOpenTicketId((prev) =>
                                                            prev === ticket.id ? null : ticket.id
                                                        )
                                                    }
                                                >
                                                    {assignmentOpenTicketId === ticket.id ? "Cancel" : "Assign Technician"}
                                                </button>
                                            )
                                        ) : (
                                            <div className="w-full h-full flex items-center justify-center text-xs text-gray-400 bg-gray-50 border border-gray-200/60 px-3 rounded-lg italic whitespace-nowrap">
                                                {assigneeName ? `Tech: ${assigneeName}` : "In progress"}
                                            </div>
                                        )}
                                    </div>
                                </div>

                            {/* Assignment panel — full width row below the ticket info */}
                            {assignmentOpenTicketId === ticket.id && (
                                <div className="w-full border-t border-gray-100 pt-4 mt-2">
                                    <AssignmentPanel
                                        ticketId={ticket.id}
                                        onAssigned={(name) => handleAssigned(ticket.id, name)}
                                        onCancel={() => setAssignmentOpenTicketId(null)}
                                    />
                                </div>
                            )}
                        </div>
                        );
                    })
                )}
            </div>
        </section>

        {/* Slide-over detail panel */}
        {selectedTicket && (
            <div className="fixed inset-0 z-50 flex justify-end bg-black/30 backdrop-blur-sm">
                <div className="absolute inset-0" onClick={() => setSelectedTicket(null)} />
                <div className="relative w-full max-w-lg bg-white border-l border-gray-200 h-full p-6 shadow-2xl flex flex-col gap-6 overflow-y-auto">

                    {/* Header */}
                    <div className="flex items-start justify-between border-b border-gray-100 pb-4">
                        <div>
                            <span className="text-[11px] font-mono text-gray-400 block">#TK-{selectedTicket.id}</span>
                            <h3 className="text-lg font-semibold text-gray-900 mt-1 leading-snug">{selectedTicket.title}</h3>
                        </div>
                        <button
                            onClick={() => setSelectedTicket(null)}
                            className="text-gray-400 hover:text-gray-700 p-1.5 rounded-lg hover:bg-gray-100 transition-all shrink-0"
                        >
                            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12" />
                            </svg>
                        </button>
                    </div>

                    {/* Meta grid */}
                    <div className="grid grid-cols-2 gap-4 bg-gray-50 border border-gray-100 rounded-xl p-4">
                        <div>
                            <span className="text-[10px] font-medium text-gray-400 uppercase tracking-wider block">Status</span>
                            <div className="mt-1"><StatusBadge status={selectedTicket.status} /></div>
                        </div>
                        <div>
                            <span className="text-[10px] font-medium text-gray-400 uppercase tracking-wider block">Priority</span>
                            <div className="mt-1"><PriorityBadge priority={selectedTicket.priority} /></div>
                        </div>
                        <div className="pt-2">
                            <span className="text-[10px] font-medium text-gray-400 uppercase tracking-wider block">Category</span>
                            <span className="text-sm font-medium text-gray-900 block mt-0.5">{selectedTicket.category || "—"}</span>
                        </div>
                        <div className="pt-2">
                            <span className="text-[10px] font-medium text-gray-400 uppercase tracking-wider block">Created By</span>
                            <span className="text-sm font-medium text-gray-900 block mt-0.5">
                                {selectedTicket.authorName || selectedTicket.clientName || selectedTicket.createdBy || "Client"}
                            </span>
                        </div>
                        <div className="pt-2">
                            <span className="text-[10px] font-medium text-gray-400 uppercase tracking-wider block">Created At</span>
                            <span className="text-sm font-medium text-gray-900 block mt-0.5">{formatDate(selectedTicket.createdAt)}</span>
                        </div>
                        <div className="pt-2">
                            <span className="text-[10px] font-medium text-gray-400 uppercase tracking-wider block">SLA Deadline</span>
                            <span className="text-sm font-medium text-red-600 block mt-0.5">{formatDate(selectedTicket.slaDeadline)}</span>
                        </div>
                    </div>

                    {/* Description */}
                    <div className="space-y-2">
                        <span className="text-[10px] font-medium text-gray-400 uppercase tracking-wider block">Incident Description</span>
                        <div className="bg-gray-50 border border-gray-100 rounded-xl p-4 text-sm text-gray-700 leading-relaxed whitespace-pre-wrap max-h-60 overflow-y-auto">
                            {selectedTicket.description || "No description provided."}
                        </div>
                    </div>

                    {/* Assigned technician */}
                    {(selectedTicket.technicianName || selectedTicket.assigneeName || assignedTechnicianByTicket[selectedTicket.id]) && (
                        <div className="bg-blue-50 border border-blue-100 rounded-xl p-4">
                            <span className="text-[10px] font-medium text-blue-500 uppercase tracking-wider block">Assigned Expert</span>
                            <span className="text-sm font-semibold text-gray-900 block mt-0.5">
                                {selectedTicket.technicianName || selectedTicket.assigneeName || assignedTechnicianByTicket[selectedTicket.id]}
                            </span>
                        </div>
                    )}

                    {/* Resolution (if resolved) */}
                    {selectedTicket.solution && (
                        <div className="bg-emerald-50 border border-emerald-100 rounded-xl p-4">
                            <span className="text-[10px] font-medium text-emerald-600 uppercase tracking-wider block">Resolution</span>
                            <p className="text-sm text-emerald-900 mt-1 leading-relaxed">{selectedTicket.solution}</p>
                        </div>
                    )}

                    {/* Footer */}
                    <div className="mt-auto border-t border-gray-100 pt-4 flex justify-end">
                        <button
                            onClick={() => setSelectedTicket(null)}
                            className="bg-gray-100 hover:bg-gray-200 text-gray-700 text-xs font-medium px-4 py-2.5 rounded-lg transition-all"
                        >
                            Close
                        </button>
                    </div>
                </div>
            </div>
        )}
    </>
    );
}
