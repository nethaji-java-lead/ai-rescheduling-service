
import { useCallback, useEffect, useMemo, useState } from "react";
import { api } from "./api";
import "./index.css";

const demoAppointments = [
  {
    id: "APT-001",
    patientName: "John Martinez",
    appointmentType: "ROUTINE",
    providerId: "PRV-001",
    providerName: "Dr. Sarah Chen",
    slotId: "SLT-001",
    status: "RESCHEDULE_PENDING",
  },
  {
    id: "APT-002",
    patientName: "Emily Watson",
    appointmentType: "FOLLOW_UP",
    providerId: "PRV-001",
    providerName: "Dr. Sarah Chen",
    slotId: "SLT-002",
    status: "RESCHEDULE_PENDING",
  },
  {
    id: "APT-003",
    patientName: "Aisha Patel",
    appointmentType: "URGENT",
    providerId: "PRV-003",
    providerName: "Dr. Priya Sharma",
    slotId: "SLT-003",
    status: "CONFIRMED",
  },
  {
    id: "APT-004",
    patientName: "Robert Kim",
    appointmentType: "ROUTINE",
    providerId: "PRV-005",
    providerName: "Dr. Ananya Iyer",
    slotId: "SLT-006",
    status: "CONFIRMED",
  },
];

const demoProviders = [
  { id: "PRV-001", name: "Dr. Sarah Chen", specialty: "GENERAL", status: "BUSY" },
  { id: "PRV-002", name: "Dr. James Okonkwo", specialty: "SPECIALIST", status: "AVAILABLE" },
  { id: "PRV-003", name: "Dr. Priya Sharma", specialty: "PEDIATRIC", status: "BUSY" },
  { id: "PRV-004", name: "Dr. Rahul Verma", specialty: "GENERAL", status: "AVAILABLE" },
  { id: "PRV-005", name: "Dr. Ananya Iyer", specialty: "SPECIALIST", status: "BUSY" },
];

const demoSuggestions = [
  {
    id: "SUG-001",
    appointmentId: "APT-001",
    patientName: "John Martinez",
    recommendedSlotId: "SLT-005",
    confidence: 0.94,
    reasoning: "A nearby available slot with a general practitioner can reduce disruption for the patient.",
    status: "PENDING",
  },
  {
    id: "SUG-002",
    appointmentId: "APT-002",
    patientName: "Emily Watson",
    recommendedSlotId: "SLT-004",
    confidence: 0.88,
    reasoning: "An alternative provider is available. Front-desk approval is required before confirming the change.",
    status: "PENDING",
  },
];

const demoSlots = [
  { id: "SLT-004", providerId: "PRV-002", startTime: "Available alternative" },
  { id: "SLT-005", providerId: "PRV-004", startTime: "Available alternative" },
];

const getArray = (data) => {
  if (Array.isArray(data)) return data;
  if (Array.isArray(data?.content)) return data.content;
  if (Array.isArray(data?.data)) return data.data;
  return [];
};

const pretty = (value = "") =>
    String(value).replaceAll("_", " ").toLowerCase()
        .replace(/\b\w/g, (letter) => letter.toUpperCase());

const statusClass = (value = "") =>
    String(value).toLowerCase().replaceAll("_", "-");

function StatCard({ label, value, note, icon }) {
  return (
      <div className="stat-card">
        <div className="stat-top">
          <span className="stat-label">{label}</span>
          <span className="stat-icon">{icon}</span>
        </div>
        <div className="stat-value">{value}</div>
        <div className="stat-note">{note}</div>
      </div>
  );
}

function StatusBadge({ status }) {
  return (
      <span className={`status-badge ${statusClass(status)}`}>
      <span className="status-dot" />
        {pretty(status)}
    </span>
  );
}

export default function App() {
  const [activeTab, setActiveTab] = useState("Overview");
  const [appointments, setAppointments] = useState(demoAppointments);
  const [providers, setProviders] = useState(demoProviders);
  const [suggestions, setSuggestions] = useState(demoSuggestions);
  const [slots] = useState(demoSlots);
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const [connected, setConnected] = useState(false);
  const [notice, setNotice] = useState("");
  const [busyId, setBusyId] = useState("");

  const refresh = useCallback(async () => {
    setLoading(true);
    try {
      const [appointmentData, suggestionData] = await Promise.all([
        api.getAppointments(),
        api.getSuggestions(),
      ]);

      setAppointments(getArray(appointmentData));
      setSuggestions(getArray(suggestionData));
      setConnected(true);

      // Provider details may not have a list endpoint in the current backend.
      // Keep the known roster until that endpoint is available.
      setNotice("");
    } catch {
      setConnected(false);
      setNotice("Backend unavailable. Showing demo data; changes will not be saved.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    refresh();
  }, [refresh]);

  const pendingSuggestions = useMemo(
      () => suggestions.filter((item) => item.status === "PENDING"),
      [suggestions]
  );

  const filteredAppointments = useMemo(() => {
    const query = search.toLowerCase().trim();
    return appointments.filter((item) =>
        [
          item.id,
          item.patientName,
          item.patient?.name,
          item.providerName,
          item.provider?.name,
          item.appointmentType,
          item.status,
        ].some((value) => String(value || "").toLowerCase().includes(query))
    );
  }, [appointments, search]);

  const updateProvider = async (provider, status) => {
    setBusyId(provider.id);
    try {
      if (connected) {
        await api.updateProviderStatus(provider.id, status);
        await refresh();
        setNotice(`${provider.name} status updated to ${pretty(status)}.`);
      } else {
        setProviders((current) =>
            current.map((item) =>
                item.id === provider.id ? { ...item, status } : item
            )
        );
        setNotice("Demo only: provider status changed locally, not on the backend.");
      }
    } catch (error) {
      setNotice(`Could not update provider: ${error.message}`);
    } finally {
      setBusyId("");
    }
  };

  const reviewSuggestion = async (suggestion, status) => {
    setBusyId(suggestion.id);
    try {
      if (connected) {
        await api.updateSuggestionStatus(suggestion.id, status);
        await refresh();
        setNotice(`Suggestion ${pretty(status).toLowerCase()}.`);
      } else {
        setSuggestions((current) =>
            current.map((item) =>
                item.id === suggestion.id ? { ...item, status } : item
            )
        );
        setNotice(`Demo only: suggestion ${pretty(status).toLowerCase()} locally.`);
      }
    } catch (error) {
      setNotice(`Could not review suggestion: ${error.message}`);
    } finally {
      setBusyId("");
    }
  };

  const generateSuggestion = async (appointment) => {
    setBusyId(appointment.id);
    try {
      if (!connected) {
        setNotice("Start the backend to generate a real scheduling suggestion.");
        return;
      }
      await api.generateSuggestion(appointment.id);
      await refresh();
      setActiveTab("Rescheduling");
      setNotice("Suggestion request submitted. Refresh if the backend processes it asynchronously.");
    } catch (error) {
      setNotice(`Could not request a suggestion: ${error.message}`);
    } finally {
      setBusyId("");
    }
  };

  const navItems = [
    ["Overview", "◫"],
    ["Appointments", "▦"],
    ["Rescheduling", "↗"],
    ["Care Team", "♧"],
  ];

  const confirmedCount = appointments.filter((a) => a.status === "CONFIRMED").length;
  const rescheduledCount = appointments.filter((a) => a.status === "RESCHEDULED").length;

  return (
      <div className="app-shell">
        <aside className="sidebar">
          <div className="brand">
            <div className="brand-mark">✚</div>
            <div>
              <div className="brand-name">CareBridge</div>
              <div className="brand-subtitle">SMART CARE OPERATIONS</div>
            </div>
          </div>

          <div className="nav-label">WORKSPACE</div>
          <nav className="nav-list">
            {navItems.map(([name, icon]) => (
                <button
                    key={name}
                    className={`nav-item ${activeTab === name ? "active" : ""}`}
                    onClick={() => setActiveTab(name)}
                >
                  <span className="nav-icon">{icon}</span>
                  <span>{name}</span>
                  {name === "Rescheduling" && pendingSuggestions.length > 0 && (
                      <span className="nav-count">{pendingSuggestions.length}</span>
                  )}
                </button>
            ))}
          </nav>

          <div className="sidebar-bottom">
            <div className="sidebar-help">
              <div className="help-icon">?</div>
              <div>
                <strong>Need help?</strong>
                <p>Care operations workspace</p>
              </div>
            </div>
            <div className="user-profile">
              <div className="avatar">FD</div>
              <div className="user-info">
                <strong>Front Desk</strong>
                <span>Care coordinator</span>
              </div>
              <span className="user-menu">•••</span>
            </div>
          </div>
        </aside>

        <main className="main-panel">
          <header className="topbar">
            <div className="breadcrumb">Care Operations <span>/</span> {activeTab}</div>
            <div className="topbar-right">
            <span className={`connection ${connected ? "online" : "offline"}`}>
              <span className="connection-dot" />
              {connected ? "Backend connected" : "Demo mode"}
            </span>
              <button className="icon-button" title="Refresh data" onClick={refresh}>↻</button>
              <div className="top-avatar">FD</div>
            </div>
          </header>

          <section className="page-content">
            <div className="page-heading">
              <div>
                <div className="eyebrow">CAREBRIDGE / OPERATIONS</div>
                <h1>{activeTab === "Care Team" ? "Care team" : activeTab}</h1>
                <p>
                  {activeTab === "Overview" && "A clear view of today's appointments and scheduling activity."}
                  {activeTab === "Appointments" && "Manage appointments and request alternative scheduling options."}
                  {activeTab === "Rescheduling" && "Review AI-assisted recommendations before making changes."}
                  {activeTab === "Care Team" && "Monitor provider availability and respond to schedule changes."}
                </p>
              </div>
              <div className="heading-actions">
                <button className="button button-light" onClick={refresh}>↻ Refresh</button>
                <button className="button button-primary" onClick={() => setActiveTab("Appointments")}>
                  + View appointments
                </button>
              </div>
            </div>

            {notice && (
                <div className={`notice ${connected ? "" : "notice-warning"}`}>
                  <span>{notice}</span>
                  <button onClick={() => setNotice("")}>×</button>
                </div>
            )}

            {activeTab === "Overview" && (
                <>
                  <div className="stat-grid">
                    <StatCard label="Total appointments" value={appointments.length} note="Across current schedule" icon="▦" />
                    <StatCard label="Confirmed" value={confirmedCount} note="Ready for patient visits" icon="✓" />
                    <StatCard label="Needs attention" value={pendingSuggestions.length} note="Suggestions awaiting review" icon="◷" />
                    <StatCard label="Rescheduled" value={rescheduledCount} note="Successfully rescheduled" icon="↗" />
                  </div>

                  <div className="dashboard-grid">
                    <section className="panel appointments-panel">
                      <div className="panel-heading">
                        <div><h2>Recent appointments</h2><p>Current appointment overview</p></div>
                        <button className="text-button" onClick={() => setActiveTab("Appointments")}>View all →</button>
                      </div>
                      <AppointmentTable
                          appointments={appointments.slice(0, 4)}
                          onSuggest={generateSuggestion}
                          busyId={busyId}
                      />
                    </section>

                    <section className="panel">
                      <div className="panel-heading">
                        <div><h2>Needs your review</h2><p>AI recommendations awaiting approval</p></div>
                        <span className="count-pill">{pendingSuggestions.length}</span>
                      </div>
                      <div className="suggestion-list">
                        {pendingSuggestions.slice(0, 3).map((item) => (
                            <SuggestionCard
                                key={item.id}
                                suggestion={item}
                                onReview={reviewSuggestion}
                                busyId={busyId}
                            />
                        ))}
                        {pendingSuggestions.length === 0 && <EmptyState text="No suggestions need review." />}
                      </div>
                      <button className="button button-light full-button" onClick={() => setActiveTab("Rescheduling")}>
                        Open rescheduling queue →
                      </button>
                    </section>
                  </div>

                  <section className="panel team-panel">
                    <div className="panel-heading">
                      <div><h2>Care team availability</h2><p>Provider status overview</p></div>
                      <button className="text-button" onClick={() => setActiveTab("Care Team")}>Manage team →</button>
                    </div>
                    <ProviderGrid providers={providers} onUpdate={updateProvider} busyId={busyId} />
                  </section>
                </>
            )}

            {activeTab === "Appointments" && (
                <section className="panel">
                  <div className="panel-heading">
                    <div><h2>All appointments</h2><p>Search by patient, provider, ID, or status</p></div>
                    <span className="count-pill">{filteredAppointments.length} records</span>
                  </div>
                  <input
                      className="search-input"
                      placeholder="Search appointments..."
                      value={search}
                      onChange={(event) => setSearch(event.target.value)}
                  />
                  <AppointmentTable
                      appointments={filteredAppointments}
                      onSuggest={generateSuggestion}
                      busyId={busyId}
                      showAction
                  />
                </section>
            )}

            {activeTab === "Rescheduling" && (
                <section className="panel">
                  <div className="panel-heading">
                    <div><h2>Suggestion review queue</h2><p>Human approval is required before a recommendation is applied.</p></div>
                    <span className="count-pill">{pendingSuggestions.length} pending</span>
                  </div>
                  <div className="suggestion-grid">
                    {pendingSuggestions.map((item) => (
                        <SuggestionCard
                            key={item.id}
                            suggestion={item}
                            onReview={reviewSuggestion}
                            busyId={busyId}
                            detailed
                        />
                    ))}
                  </div>
                  {pendingSuggestions.length === 0 && <EmptyState text="All caught up. There are no pending suggestions." />}
                </section>
            )}

            {activeTab === "Care Team" && (
                <section className="panel">
                  <div className="panel-heading">
                    <div><h2>Provider directory</h2><p>Update availability to trigger the rescheduling workflow.</p></div>
                    <span className="count-pill">{providers.length} providers</span>
                  </div>
                  <ProviderGrid providers={providers} onUpdate={updateProvider} busyId={busyId} detailed />
                </section>
            )}

            {loading && <div className="loading-line">Checking backend connection…</div>}
            <footer className="footer">CareBridge <span>•</span> Appointment scheduling and rescheduling</footer>
          </section>
        </main>
      </div>
  );
}

function AppointmentTable({ appointments, onSuggest, busyId, showAction = false }) {
  if (!appointments.length) return <EmptyState text="No appointments found." />;

  return (
      <div className="table-wrap">
        <table>
          <thead>
          <tr>
            <th>Patient</th>
            <th>Provider</th>
            <th>Type</th>
            <th>Status</th>
            {showAction && <th>Action</th>}
          </tr>
          </thead>
          <tbody>
          {appointments.map((item) => {
            const patientName = item.patientName || item.patient?.name || "Unknown patient";
            const providerName = item.providerName || item.provider?.name || item.providerId || "Unassigned";
            return (
                <tr key={item.id}>
                  <td>
                    <div className="patient-cell">
                      <div className="patient-avatar">{patientName.split(" ").map((s) => s[0]).slice(0, 2).join("")}</div>
                      <div><strong>{patientName}</strong><small>{item.id}</small></div>
                    </div>
                  </td>
                  <td>{providerName}</td>
                  <td><span className="type-text">{pretty(item.appointmentType || item.type)}</span></td>
                  <td><StatusBadge status={item.status || "UNKNOWN"} /></td>
                  {showAction && (
                      <td>
                        <button
                            className="small-action"
                            disabled={busyId === item.id}
                            onClick={() => onSuggest(item)}
                        >
                          {busyId === item.id ? "Working…" : "Suggest"}
                        </button>
                      </td>
                  )}
                </tr>
            );
          })}
          </tbody>
        </table>
      </div>
  );
}

function SuggestionCard({ suggestion, onReview, busyId, detailed = false }) {
  const confidence = Number(suggestion.confidence ?? 0);
  const percent = Math.round(confidence <= 1 ? confidence * 100 : confidence);
  const patient = suggestion.patientName || suggestion.appointment?.patientName || suggestion.appointmentId;
  const slot = suggestion.recommendedSlotId || suggestion.slotId || "Alternative slot";

  return (
      <div className={`suggestion-card ${detailed ? "suggestion-detailed" : ""}`}>
        <div className="suggestion-top">
          <span className="ai-label"><span>✦</span> AI SUGGESTION</span>
          <span className="confidence">{percent}% match</span>
        </div>
        <h3>{patient}</h3>
        <p className="suggestion-id">Appointment {suggestion.appointmentId || suggestion.appointment?.id || "—"}</p>
        <div className="recommendation">
          <div className="recommendation-icon">↗</div>
          <div><small>RECOMMENDED SLOT</small><strong>{slot}</strong></div>
        </div>
        <p className="reasoning">
          {suggestion.reasoning || suggestion.patientFriendlyReasoning || "The scheduling engine recommended this alternative based on current availability."}
        </p>
        {detailed && (
            <div className="suggestion-meta">
              <span>Suggestion ID</span><strong>{suggestion.id}</strong>
              <span>Confidence</span><strong>{percent}%</strong>
            </div>
        )}
        <div className="suggestion-actions">
          <button
              className="button button-primary"
              disabled={busyId === suggestion.id}
              onClick={() => onReview(suggestion, "ACCEPTED")}
          >
            {busyId === suggestion.id ? "Saving…" : "✓ Accept"}
          </button>
          <button
              className="button button-light"
              disabled={busyId === suggestion.id}
              onClick={() => onReview(suggestion, "REJECTED")}
          >
            Reject
          </button>
        </div>
      </div>
  );
}

function ProviderGrid({ providers, onUpdate, busyId, detailed = false }) {
  return (
      <div className={`provider-grid ${detailed ? "provider-grid-detailed" : ""}`}>
        {providers.map((provider) => (
            <div className="provider-card" key={provider.id}>
              <div className="provider-top">
                <div className="provider-avatar">{provider.name.split(" ").slice(-1)[0][0]}</div>
                <StatusBadge status={provider.status} />
              </div>
              <h3>{provider.name}</h3>
              <p>{pretty(provider.specialty)}</p>
              <div className="provider-id">{provider.id}</div>
              <label className="provider-label" htmlFor={`status-${provider.id}`}>Update availability</label>
              <select
                  id={`status-${provider.id}`}
                  value={provider.status}
                  disabled={busyId === provider.id}
                  onChange={(event) => onUpdate(provider, event.target.value)}
              >
                <option value="AVAILABLE">Available</option>
                <option value="BUSY">Busy</option>
                <option value="OFFLINE">Offline</option>
              </select>
            </div>
        ))}
      </div>
  );
}

function EmptyState({ text }) {
  return <div className="empty-state"><span>✓</span><p>{text}</p></div>;
}
