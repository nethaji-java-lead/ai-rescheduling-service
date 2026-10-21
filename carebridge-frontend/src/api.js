const API_BASE_URL = (
    import.meta.env.VITE_API_BASE_URL || "http://localhost:8080"
).replace(/\/+$/, "");

/**
 * Common API request handler.
 */
async function apiRequest(path, options = {}) {
    const response = await fetch(`${API_BASE_URL}${path}`, {
        ...options,
        headers: {
            Accept: "application/json",
            ...(options.body ? { "Content-Type": "application/json" } : {}),
            ...options.headers,
        },
    });

    // Read the response body only once.
    const responseText = await response.text();

    let data = null;

    if (responseText) {
        try {
            data = JSON.parse(responseText);
        } catch {
            data = responseText;
        }
    }

    if (!response.ok) {
        const message =
            data && typeof data === "object"
                ? data.message || data.error || JSON.stringify(data)
                : data || response.statusText;

        throw new Error(`HTTP ${response.status}: ${message}`);
    }

    return data;
}

function encodeId(id) {
    if (id === undefined || id === null || id === "") {
        throw new Error("A valid ID is required.");
    }

    return encodeURIComponent(id);
}

// --------------------------------------------------
// Providers
// --------------------------------------------------

export function getProviders() {
    return apiRequest("/providers");
}

export const fetchProviders = getProviders;

export function updateProviderStatus(providerId, status) {
    return apiRequest(`/providers/${encodeId(providerId)}/status`, {
        method: "PATCH",
        body: JSON.stringify({ status }),
    });
}

export const changeProviderStatus = updateProviderStatus;

// --------------------------------------------------
// Appointments
// --------------------------------------------------

export function getAppointments(status) {
    const query = status
        ? `?status=${encodeURIComponent(status)}`
        : "";

    return apiRequest(`/appointments${query}`);
}

export const fetchAppointments = getAppointments;

export function createAppointment(appointment) {
    return apiRequest("/appointments", {
        method: "POST",
        body: JSON.stringify(appointment),
    });
}

export function suggestReschedule(appointmentId) {
    return apiRequest(
        `/appointments/${encodeId(appointmentId)}/suggest`,
        {
            method: "POST",
        }
    );
}

export const generateSuggestions = suggestReschedule;
export const generateSuggestion = suggestReschedule;

// --------------------------------------------------
// Slots
// --------------------------------------------------

export function getSlots() {
    return apiRequest("/slots");
}

export const fetchSlots = getSlots;

export function updateSlotStatus(slotId, status) {
    return apiRequest(`/slots/${encodeId(slotId)}/status`, {
        method: "PATCH",
        body: JSON.stringify({ status }),
    });
}

// --------------------------------------------------
// Rescheduling suggestions
// --------------------------------------------------

export function getSuggestions() {
    return apiRequest("/suggestions");
}

export const fetchSuggestions = getSuggestions;

/**
 * Review a suggestion.
 * The backend error confirms that it expects "decision",
 * rather than "status", in the request body.
 */
export function updateSuggestionStatus(suggestionId, decision) {
    const normalizedDecision = String(decision || "").toUpperCase();

    if (!["ACCEPTED", "REJECTED"].includes(normalizedDecision)) {
        throw new Error(
            'Decision must be either "ACCEPTED" or "REJECTED".'
        );
    }

    return apiRequest(`/suggestions/${encodeId(suggestionId)}`, {
        method: "PATCH",
        body: JSON.stringify({
            decision: normalizedDecision,
        }),
    });
}

export const reviewSuggestion = updateSuggestionStatus;

// --------------------------------------------------
// Backend connectivity
// --------------------------------------------------

export async function checkBackendHealth() {
    try {
        await getProviders();
        return true;
    } catch (error) {
        console.error("Backend connection failed:", error);
        return false;
    }
}

// --------------------------------------------------
// Default Export Object
// --------------------------------------------------

export const api = {
    getProviders,
    fetchProviders,
    updateProviderStatus,
    changeProviderStatus,
    getAppointments,
    fetchAppointments,
    createAppointment,
    suggestReschedule,
    generateSuggestions,
    generateSuggestion,
    getSlots,
    fetchSlots,
    updateSlotStatus,
    getSuggestions,
    fetchSuggestions,
    updateSuggestionStatus,
    reviewSuggestion,
    checkBackendHealth,
};

export default api;