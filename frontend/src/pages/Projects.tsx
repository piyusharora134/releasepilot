import { FormEvent, useEffect, useState } from 'react'
import { api } from '../api/client'
import type { Organization, Project } from '../api/client'

export default function Projects() {
  const [orgs, setOrgs] = useState<Organization[]>([])
  const [projects, setProjects] = useState<Project[]>([])
  const [selectedOrg, setSelectedOrg] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [name, setName] = useState('')
  const [key, setKey] = useState('')
  const [description, setDescription] = useState('')
  const [error, setError] = useState('')
  const [expandedProject, setExpandedProject] = useState<string | null>(null)

  async function load(orgId: string) {
    const list = await api.getProjects(orgId)
    const detailed = await Promise.all(list.map(p => api.getProject(p.id)))
    setProjects(detailed)
  }

  useEffect(() => {
    api.getOrganizations().then(data => {
      setOrgs(data)
      if (data.length) {
        setSelectedOrg(data[0].id)
        load(data[0].id).catch(console.error)
      }
    })
  }, [])

  async function handleCreate(e: FormEvent) {
    e.preventDefault()
    setError('')
    try {
      await api.createProject({ organizationId: selectedOrg, name, key, description })
      setShowForm(false)
      setName('')
      setKey('')
      setDescription('')
      await load(selectedOrg)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create project')
    }
  }

  async function handleRegenerateKey(envId: string) {
    await api.regenerateApiKey(envId)
    await load(selectedOrg)
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between gap-4 flex-wrap">
        <div>
          <h2 className="text-2xl font-semibold">Projects</h2>
          <p className="text-slate-400 mt-1">Each project gets dev, staging, and prod environments.</p>
        </div>
        <button
          type="button"
          onClick={() => setShowForm(true)}
          className="rounded-lg bg-indigo-600 hover:bg-indigo-500 px-4 py-2 text-sm font-medium"
        >
          New project
        </button>
      </div>

      {orgs.length > 1 && (
        <select
          className="rounded-lg bg-slate-950 border border-slate-700 px-3 py-2 text-sm"
          value={selectedOrg}
          onChange={e => {
            setSelectedOrg(e.target.value)
            load(e.target.value).catch(console.error)
          }}
        >
          {orgs.map(org => (
            <option key={org.id} value={org.id}>{org.name}</option>
          ))}
        </select>
      )}

      {showForm && (
        <form onSubmit={handleCreate} className="rounded-xl border border-slate-800 bg-slate-900/50 p-6 space-y-4 max-w-lg">
          <h3 className="font-medium">Create project</h3>
          <input className="w-full rounded-lg bg-slate-950 border border-slate-700 px-3 py-2" placeholder="Name" value={name} onChange={e => setName(e.target.value)} required />
          <input className="w-full rounded-lg bg-slate-950 border border-slate-700 px-3 py-2 font-mono" placeholder="Key (e.g. web-app)" value={key} onChange={e => setKey(e.target.value)} required />
          <input className="w-full rounded-lg bg-slate-950 border border-slate-700 px-3 py-2" placeholder="Description (optional)" value={description} onChange={e => setDescription(e.target.value)} />
          {error && <p className="text-red-400 text-sm">{error}</p>}
          <div className="flex gap-2">
            <button type="submit" className="rounded-lg bg-indigo-600 px-4 py-2 text-sm">Create</button>
            <button type="button" onClick={() => setShowForm(false)} className="rounded-lg border border-slate-700 px-4 py-2 text-sm">Cancel</button>
          </div>
        </form>
      )}

      <div className="space-y-4">
        {projects.map(project => (
          <div key={project.id} className="rounded-xl border border-slate-800 bg-slate-900/40 overflow-hidden">
            <button
              type="button"
              className="w-full text-left p-4 flex items-center justify-between hover:bg-slate-800/40"
              onClick={() => setExpandedProject(expandedProject === project.id ? null : project.id)}
            >
              <div>
                <p className="font-medium">{project.name}</p>
                <p className="text-sm text-slate-400 font-mono">{project.key}</p>
              </div>
              <span className="text-slate-500 text-sm">{project.environments?.length ?? 0} environments</span>
            </button>
            {expandedProject === project.id && (
              <div className="border-t border-slate-800 p-4 space-y-3">
                {project.environments?.map(env => (
                  <div key={env.id} className="flex flex-wrap items-center gap-3 text-sm">
                    <span className="font-mono text-indigo-300 w-20">{env.key}</span>
                    <span className="text-slate-400">{env.name}</span>
                    <code className="text-xs bg-slate-950 px-2 py-1 rounded flex-1 min-w-0 truncate">{env.apiKey}</code>
                    <button
                      type="button"
                      onClick={() => handleRegenerateKey(env.id)}
                      className="text-indigo-400 hover:text-indigo-300 text-xs shrink-0"
                    >
                      Regenerate key
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>
        ))}
        {projects.length === 0 && <p className="text-slate-500">No projects yet. Create one to get started.</p>}
      </div>
    </div>
  )
}
