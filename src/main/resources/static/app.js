const state = {
    auth: JSON.parse(localStorage.getItem("workflow.auth") || "null"),
    profile: null
};

const $ = (selector) => document.querySelector(selector);
const today = () => new Date().toISOString().slice(0, 10);

function setMessage(message, isError = false) {
    const element = $("#global-message");
    element.textContent = message || "";
    element.className = isError ? "message error" : "message";
}

function setLoginError(message) {
    $("#login-error").textContent = message || "";
}

async function api(path, options = {}) {
    const headers = { ...(options.body ? { "Content-Type": "application/json" } : {}), ...(options.headers || {}) };
    if (state.auth?.token) headers.Authorization = `${state.auth.tokenType || "Bearer"} ${state.auth.token}`;
    const response = await fetch(path, { ...options, headers });
    const contentType = response.headers.get("content-type") || "";
    const body = contentType.includes("application/json") ? await response.json() : await response.text();
    if (response.status === 401) {
        logout();
        throw new Error("Your session has expired. Please sign in again.");
    }
    if (!response.ok) {
        const validation = body?.validationErrors ? ` ${Object.values(body.validationErrors).join(" ")}` : "";
        throw new Error((body?.message || "Request failed.") + validation);
    }
    return body;
}

function showRoleSections(role) {
    document.querySelectorAll(".employee-only").forEach((element) => element.hidden = false);
    document.querySelectorAll(".manager-only").forEach((element) => element.hidden = !["MANAGER", "ADMIN"].includes(role));
    document.querySelectorAll(".admin-only").forEach((element) => element.hidden = role !== "ADMIN");
}

function renderProfile(profile) {
    $("#profile").innerHTML = `
        <p><strong>${escapeHtml(profile.firstName)} ${escapeHtml(profile.lastName)}</strong></p>
        <p>${escapeHtml(profile.email)}</p>
        <p><span class="status">${escapeHtml(profile.role)}</span></p>
        <p class="muted">${escapeHtml(profile.department?.name || "No department")}</p>`;
}

function renderList(selector, items, renderer, empty = "Nothing to display.") {
    const element = $(selector);
    element.innerHTML = items.length ? items.map(renderer).join("") : `<p class="muted">${empty}</p>`;
}

function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, (character) => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#039;"
    }[character]));
}

async function loadEmployeeData() {
    const [leaves, tasks] = await Promise.all([
        api(`/api/leaves/my?employeeId=${state.profile.id}`),
        api(`/api/tasks/my?employeeId=${state.profile.id}`)
    ]);
    renderList("#leave-history", leaves, (leave) => `
        <div class="item"><strong>${escapeHtml(leave.startDate)} to ${escapeHtml(leave.endDate)}</strong>
        <p>${escapeHtml(leave.reason)}</p><span class="status">${escapeHtml(leave.status)}</span></div>`);
    renderList("#my-tasks", tasks, taskCard, "No assigned tasks.");
}

function taskCard(task) {
    const next = { ASSIGNED: "IN_PROGRESS", IN_PROGRESS: "COMPLETED" }[task.status];
    return `<div class="item"><strong>${escapeHtml(task.title)}</strong>
        <p>${escapeHtml(task.description)}</p><p><span class="status">${escapeHtml(task.status)}</span>
        ${task.assigneeName ? ` ${escapeHtml(task.assigneeName)}` : ""}</p>
        ${next ? `<button class="small" data-task-id="${task.id}" data-next-status="${next}">Move to ${next}</button>` : ""}</div>`;
}

async function loadManagerData() {
    const departmentId = state.profile.department?.id;
    if (!departmentId) {
        setMessage("Assign a department to this account to view team data.", true);
        return;
    }
    const [employees, pendingLeaves, tasks] = await Promise.all([
        api("/api/employees"),
        api("/api/leaves/pending"),
        api(`/api/tasks/team?departmentId=${departmentId}`)
    ]);
    renderList("#team-employees", employees, (employee) => `
        <div class="item"><strong>${escapeHtml(employee.firstName)} ${escapeHtml(employee.lastName)}</strong>
        <p>${escapeHtml(employee.email)} · ${escapeHtml(employee.role)}</p><small>ID: ${employee.id}</small></div>`);
    renderList("#pending-leaves", pendingLeaves, (leave) => `
        <div class="item"><strong>${escapeHtml(leave.employeeName)}</strong>
        <p>${escapeHtml(leave.startDate)} to ${escapeHtml(leave.endDate)} · ${escapeHtml(leave.reason)}</p>
        <button class="small" data-leave-id="${leave.id}" data-decision="approve">Approve</button>
        <button class="small danger" data-leave-id="${leave.id}" data-decision="reject">Reject</button></div>`,
        "No pending leave requests.");
    renderList("#team-tasks", tasks, (task) => `${taskCard(task)}
        <label>Assign employee ID</label><input id="assignee-${task.id}" type="number" min="1">
        <button class="small" data-assign-task="${task.id}">Assign</button>`);
}

async function loadDashboard() {
    $("#login-view").hidden = true;
    $("#app-view").hidden = false;
    $("#welcome-title").textContent = `Welcome, ${state.auth.username}`;
    showRoleSections(state.auth.role);
    try {
        state.profile = await api("/api/employees/me");
        renderProfile(state.profile);
        await loadEmployeeData();
        if (["MANAGER", "ADMIN"].includes(state.auth.role)) await loadManagerData();
    } catch (error) {
        setMessage(error.message, true);
    }
}

function logout() {
    localStorage.removeItem("workflow.auth");
    state.auth = null;
    state.profile = null;
    $("#app-view").hidden = true;
    $("#login-view").hidden = false;
}

$("#login-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    setLoginError("");
    const username = $("#login-username").value.trim();
    const password = $("#login-password").value;
    if (!username || !password) return setLoginError("Username and password are required.");
    const button = event.submitter;
    button.disabled = true;
    try {
        state.auth = await api("/api/auth/login", { method: "POST", body: JSON.stringify({ username, password }) });
        localStorage.setItem("workflow.auth", JSON.stringify(state.auth));
        await loadDashboard();
    } catch (error) {
        setLoginError(error.message);
    } finally {
        button.disabled = false;
    }
});

$("#logout-button").addEventListener("click", logout);

$("#leave-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const startDate = $("#leave-start").value;
    const endDate = $("#leave-end").value;
    if (!startDate || !endDate || !$("#leave-reason").value.trim()) return setMessage("Complete all leave fields.", true);
    if (startDate < today() || endDate < startDate) return setMessage("Choose a valid leave date range.", true);
    try {
        await api("/api/leaves", { method: "POST", body: JSON.stringify({
            employeeId: state.profile.id, startDate, endDate, reason: $("#leave-reason").value.trim()
        })});
        event.target.reset();
        setMessage("Leave request submitted.");
        await loadEmployeeData();
    } catch (error) { setMessage(error.message, true); }
});

$("#task-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    if (!event.target.checkValidity()) return event.target.reportValidity();
    try {
        await api("/api/tasks", { method: "POST", body: JSON.stringify({
            title: $("#task-title").value.trim(), description: $("#task-description").value.trim(),
            priority: $("#task-priority").value, dueDate: $("#task-due-date").value || null,
            creatorId: state.profile.id
        })});
        event.target.reset();
        setMessage("Task created.");
        await loadManagerData();
    } catch (error) { setMessage(error.message, true); }
});

$("#employee-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    if (!event.target.checkValidity()) return event.target.reportValidity();
    try {
        await api("/api/employees", { method: "POST", body: JSON.stringify({
            username: $("#employee-username").value.trim(), email: $("#employee-email").value.trim(),
            password: $("#employee-password").value, firstName: $("#employee-first-name").value.trim(),
            lastName: $("#employee-last-name").value.trim(), role: $("#employee-role").value
        })});
        event.target.reset();
        setMessage("Employee created.");
        await loadManagerData();
    } catch (error) { setMessage(error.message, true); }
});

document.addEventListener("click", async (event) => {
    const statusButton = event.target.closest("[data-task-id]");
    const decisionButton = event.target.closest("[data-leave-id]");
    const assignButton = event.target.closest("[data-assign-task]");
    try {
        if (statusButton) {
            await api(`/api/tasks/${statusButton.dataset.taskId}/status`, {
                method: "PUT", body: JSON.stringify({ status: statusButton.dataset.nextStatus })
            });
            setMessage("Task status updated.");
            await loadEmployeeData();
        } else if (decisionButton) {
            const comment = window.prompt("Optional review comment:", "") ?? "";
            await api(`/api/leaves/${decisionButton.dataset.leaveId}/${decisionButton.dataset.decision}`, {
                method: "PUT", body: JSON.stringify({ reviewerId: state.profile.id, reviewComment: comment })
            });
            setMessage("Leave request processed.");
            await loadManagerData();
        } else if (assignButton) {
            const assigneeId = Number($(`#assignee-${assignButton.dataset.assignTask}`).value);
            if (!assigneeId) return setMessage("Enter an employee ID.", true);
            await api(`/api/tasks/${assignButton.dataset.assignTask}/assign?assigneeId=${assigneeId}`, { method: "PUT" });
            setMessage("Task assigned.");
            await loadManagerData();
        }
    } catch (error) { setMessage(error.message, true); }
});

if (state.auth) loadDashboard();
