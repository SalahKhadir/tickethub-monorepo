"use client";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { ROUTES } from "@/constants/routes";
import { createTicket } from "@/services/api";
const PRIORITY_OPTIONS = [
    { value: "LOW", label: "Low" },
    { value: "MEDIUM", label: "Medium" },
    { value: "HIGH", label: "High" },
    { value: "CRITICAL", label: "Critical" },
];
const CATEGORY_OPTIONS = [
    { value: "NETWORK", label: "Network" },
    { value: "HARDWARE", label: "Hardware" },
    { value: "SOFTWARE", label: "Software" },
    { value: "ACCESS", label: "Access" },
];
export default function TicketForm() {
    const router = useRouter();
    const [formState, setFormState] = useState({
        title: "",
        description: "",
        priority: PRIORITY_OPTIONS[1].value,
        category: CATEGORY_OPTIONS[0].value,
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const handleChange = (event) => {
        const { name, value } = event.target;
        setFormState((prev) => ({ ...prev, [name]: value }));
    };
    const handleSubmit = async (event) => {
        event.preventDefault();
        setError("");
        setSuccess("");
        setLoading(true);
        try {
            await createTicket(formState);
            setSuccess("Ticket created successfully. Redirecting to your tickets...");
            setFormState({
                title: "",
                description: "",
                priority: PRIORITY_OPTIONS[1].value,
                category: CATEGORY_OPTIONS[0].value,
            });
            setTimeout(() => {
                router.push(ROUTES.CLIENT_TICKETS);
            }, 1000);
        } catch (err) {
            const status = err?.response?.status;
            const backendDetails =
                err?.response?.data?.message ||
                err?.response?.data?.error ||
                err?.response?.data?.details ||
                "";
            setError(
                backendDetails
                    ? `Unable to create ticket (HTTP ${status || "?"}): ${backendDetails}`
                    : err?.message || "Unable to create ticket. Please try again."
            );
        } finally {
            setLoading(false);
        }
    };
    const inputClasses = "w-full bg-white border border-gray-200 rounded-lg px-3 py-2.5 text-sm text-gray-900 placeholder:text-gray-400 transition-colors focus:outline-none focus:border-blue-600 focus:ring-1 focus:ring-blue-600 hover:border-gray-300 shadow-sm";
    return (
        <section className="bg-white border border-[rgba(17,24,39,0.08)] rounded-2xl shadow-sm p-8 max-w-2xl mx-auto">
            <div className="mb-8 border-b border-gray-100 pb-5">
                <h2 className="text-[22px] font-semibold text-gray-900 tracking-tight">Create New Ticket</h2>
                <p className="text-sm text-gray-500 mt-1.5">Report an incident or request technical assistance.</p>
            </div>
            <form onSubmit={handleSubmit} className="space-y-6">
                <div className="flex flex-col gap-1.5">
                    <label htmlFor="title" className="text-sm font-medium text-gray-700">
                        Title
                    </label>
                    <input
                        id="title"
                        name="title"
                        value={formState.title}
                        onChange={handleChange}
                        placeholder="Short summary of your issue"
                        required
                        className={inputClasses}
                    />
                </div>
                <div className="flex flex-col gap-1.5">
                    <label htmlFor="description" className="text-sm font-medium text-gray-700">
                        Description
                    </label>
                    <textarea
                        id="description"
                        name="description"
                        value={formState.description}
                        onChange={handleChange}
                        placeholder="Describe the issue in detail"
                        className={`${inputClasses} min-h-[120px] resize-y`}
                        required
                    />
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                    <div className="flex flex-col gap-1.5">
                        <label htmlFor="category" className="text-sm font-medium text-gray-700">
                            Category
                        </label>
                        <select
                            id="category"
                            name="category"
                            value={formState.category}
                            onChange={handleChange}
                            className={inputClasses}
                            required
                        >
                            {CATEGORY_OPTIONS.map((option) => (
                                <option key={option.value} value={option.value}>
                                    {option.label}
                                </option>
                            ))}
                        </select>
                    </div>
                    <div className="flex flex-col gap-1.5">
                        <label htmlFor="priority" className="text-sm font-medium text-gray-700">
                            Priority
                        </label>
                        <select
                            id="priority"
                            name="priority"
                            value={formState.priority}
                            onChange={handleChange}
                            className={inputClasses}
                            required
                        >
                            {PRIORITY_OPTIONS.map((option) => (
                                <option key={option.value} value={option.value}>
                                    {option.label}
                                </option>
                            ))}
                        </select>
                    </div>
                </div>
                {error ? (
                    <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                        {error}
                    </div>
                ) : null}
                {success ? (
                    <div className="rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">
                        {success}
                    </div>
                ) : null}
                <div className="flex justify-end gap-3 mt-8 pt-4">
                    <button
                        type="button"
                        onClick={() => router.back()}
                        className="text-gray-500 hover:text-gray-800 font-medium text-sm px-4 py-2 bg-transparent transition-colors"
                        disabled={loading}
                    >
                        Cancel
                    </button>
                    <button
                        type="submit"
                        className="bg-blue-600 hover:bg-blue-700 text-white font-medium text-sm px-6 py-2.5 rounded-lg transition-all shadow-sm active:scale-[0.98] disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
                        disabled={loading}
                    >
                        {loading ? (
                            <>
                                <div className="w-4 h-4 rounded-full border-2 border-white/20 border-t-white animate-spin"></div>
                                Submitting...
                            </>
                        ) : "Submit Ticket"}
                    </button>
                </div>
            </form>
        </section>
    );
}
