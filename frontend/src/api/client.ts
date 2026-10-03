const API_BASE = (import.meta.env.VITE_API_BASE_URL ?? '') + '/api/v1';

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface AuthData {
  accessToken: string;
  tokenType?: string;
  user?: { id: string; email: string; name: string };
}

export interface Organization {
  id: string;
  name: string;
  slug: string;
}

export interface Project {
  id: string;
  organizationId: string;
  name: string;
  key: string;
  description?: string;
  environments: Environment[];
}

export interface Environment {
  id: string;
  projectId: string;
  name: string;
  key: string;
  apiKey: string;
}

export interface FeatureFlag {
  id: string;
  projectId: string;
  key: string;
  name: string;
  description?: string;
  flagType: 'BOOLEAN' | 'STRING' | 'NUMERIC' | 'JSON';
  defaultServeValue: string;
  enabled: boolean;
  environments: FlagEnvironment[];
}

export interface TargetingRule {
  id: string;
  flagEnvironmentId: string;
  priority?: number;
  attribute: string;
  operator: 'EQUALS' | 'IN' | 'CONTAINS' | 'GREATER_THAN' | 'LESS_THAN';
  values: string[];
  serveValue: string;
}

export interface RolloutRule {
  id: string;
  flagEnvironmentId: string;
  attribute?: string;
  percentage: number;
  serveValueA: string;
  serveValueB: string;
}

export interface FlagEnvironment {
  id: string;
  flagId: string;
  environmentId: string;
  environmentName: string;
  environmentKey: string;
  enabled: boolean;
  serveValue?: string;
  targetingRules?: TargetingRule[];
  rolloutRule?: RolloutRule;
}

function authHeaders(): HeadersInit {
  const token = localStorage.getItem('rp_token');
  return token
    ? { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' }
    : { 'Content-Type': 'application/json' };
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: { ...authHeaders(), ...options.headers },
  });
  const body = await response.json();
  if (!response.ok || body.success === false) {
    throw new Error(body.message || 'Request failed');
  }
  return body.data as T;
}

export const api = {
  register: async (payload: { name: string; email: string; password: string }) => {
    const data = await request<AuthData>('/auth/register', { method: 'POST', body: JSON.stringify(payload) });
    return data;
  },

  login: async (payload: { email: string; password: string }) => {
    const data = await request<AuthData>('/auth/login', { method: 'POST', body: JSON.stringify(payload) });
    return data;
  },

  getMe: () => request<{ id: string; email: string; name: string }>('/auth/me'),

  getOrganizations: () => request<Organization[]>('/organizations'),

  createOrganization: (payload: { name: string; slug: string }) =>
    request<Organization>('/organizations', { method: 'POST', body: JSON.stringify(payload) }),

  getProjects: (organizationId: string) =>
    request<Project[]>(`/projects?organizationId=${organizationId}`),

  createProject: (payload: { organizationId: string; name: string; key: string; description?: string }) =>
    request<Project>('/projects', { method: 'POST', body: JSON.stringify(payload) }),

  getProject: (projectId: string) => request<Project>(`/projects/${projectId}`),

  createEnvironment: (projectId: string, payload: { name: string; key: string }) =>
    request<Environment>(`/projects/${projectId}/environments`, {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  regenerateApiKey: (environmentId: string) =>
    request<Environment>(`/projects/environments/${environmentId}/regenerate-key`, { method: 'POST' }),

  getFlags: (projectId: string) => request<FeatureFlag[]>(`/flags?projectId=${projectId}`),

  getFlag: (flagId: string) => request<FeatureFlag>(`/flags/${flagId}`),

  createFlag: (payload: {
    projectId: string;
    key: string;
    name: string;
    description?: string;
    flagType: string;
    defaultServeValue: string;
  }) => request<FeatureFlag>('/flags', { method: 'POST', body: JSON.stringify(payload) }),

  updateFlag: (flagId: string, payload: { name?: string; description?: string; enabled?: boolean; defaultServeValue?: string }) =>
    request<FeatureFlag>(`/flags/${flagId}`, { method: 'PATCH', body: JSON.stringify(payload) }),

  deleteFlag: (flagId: string) =>
    request<void>(`/flags/${flagId}`, { method: 'DELETE' }),

  updateFlagEnvironment: (flagEnvironmentId: string, payload: { enabled?: boolean; serveValue?: string }) =>
    request<FlagEnvironment>(`/flags/environments/${flagEnvironmentId}`, {
      method: 'PATCH',
      body: JSON.stringify(payload),
    }),

  addTargetingRule: (
    flagEnvironmentId: string,
    payload: { priority?: number; attribute: string; operator: string; values: string[]; serveValue: string }
  ) =>
    request<TargetingRule>(`/flags/environments/${flagEnvironmentId}/targeting-rules`, {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  deleteTargetingRule: (ruleId: string) =>
    request<void>(`/flags/targeting-rules/${ruleId}`, { method: 'DELETE' }),

  setRolloutRule: (
    flagEnvironmentId: string,
    payload: { attribute?: string; percentage: number; serveValueA: string; serveValueB: string }
  ) =>
    request<RolloutRule>(`/flags/environments/${flagEnvironmentId}/rollout-rules`, {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  evaluate: async (apiKey: string, flagKey: string, context: Record<string, unknown>) => {
    const response = await fetch(`${API_BASE}/eval/evaluate`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'X-API-Key': apiKey },
      body: JSON.stringify({ flagKey, context }),
    });
    const body = await response.json();
    if (!response.ok) throw new Error(body.message || 'Evaluation failed');
    return body.data;
  },

  getAuditLogs: (organizationId: string) =>
    request<Array<{ entityType: string; action: string; detailsJson: string; createdAt: string }>>(
      `/audit-logs?organizationId=${organizationId}`,
    ),
};

/** Load first org → first project for convenience helpers */
export async function loadDefaultContext() {
  const orgs = await api.getOrganizations();
  if (!orgs.length) return null;
  const projects = await api.getProjects(orgs[0].id);
  if (!projects.length) return { org: orgs[0], project: null as Project | null };
  const project = await api.getProject(projects[0].id);
  return { org: orgs[0], project };
}
