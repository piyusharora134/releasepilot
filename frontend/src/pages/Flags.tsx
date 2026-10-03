import { FormEvent, useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api/client'
import type { FeatureFlag, Project } from '../api/client'

interface FlagRow extends FeatureFlag {
  projectName: string
}

export default function Flags() {
  const [flags, setFlags] = useState<FlagRow[]>([])
  const [projects, setProjects] = useState<Project[]>([])
  const [showForm, setShowForm] = useState(false)
  const [projectId, setProjectId] = useState('')
  const [key, setKey] = useState('')
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [flagType, setFlagType] = useState<'BOOLEAN' | 'STRING' | 'NUMERIC' | 'JSON'>('BOOLEAN')
  const [defaultServeValue, setDefaultServeValue] = useState('false')
  const [error, setError] = useState('')

  const loadFlags = useCallback(async () => {
    const rows: FlagRow[] = []
    const orgList = await api.getOrganizations()
    const allProjects: Project[] = []
    for (const org of orgList) {
      const orgProjects = await api.getProjects(org.id)
      allProjects.push(...orgProjects)
      for (const project of orgProjects) {
        const projectFlags = await api.getFlags(project.id)
        projectFlags.forEach(flag => rows.push({ ...flag, projectName: project.name }))
      }
    }
    setProjects(allProjects)
    setFlags(rows)
    if (allProjects.length && !projectId) setProjectId(allProjects[0].id)
  }, [projectId])

  useEffect(() => {
    loadFlags().catch(console.error)
  }, [loadFlags])

  async function handleCreate(e: FormEvent) {
    e.preventDefault()
    setError('')
    try {
      await api.createFlag({ projectId, key, name, description, flagType, defaultServeValue })
      setShowForm(false)
      setKey('')
      setName('')
      setDescription('')
      await loadFlags()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create flag')
    }
  }

  async function handleDelete(flagId: string) {
    if (!confirm('Delete this flag?')) return
    await api.deleteFlag(flagId)
    await loadFlags()
  }

  async function toggleGlobal(flag: FlagRow) {
    await api.updateFlag(flag.id, { enabled: !flag.enabled })
    await loadFlags()
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between gap-4 flex-wrap">
        <div>
          <h2 className="text-2xl font-semibold">Feature Flags</h2>
          <p className="text-slate-400 mt-1">Create, toggle, and manage flags across projects.</p>
        </div>
        <button
          type="button"
          onClick={() => setShowForm(true)}
          disabled={!projects.length}
          className="rounded-lg bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 px-4 py-2 text-sm font-medium"
        >
          New flag
        </button>
      </div>

      {showForm && (
        <form onSubmit={handleCreate} className="rounded-xl border border-slate-800 bg-slate-900/50 p-6 space-y-4 max-w-lg">
          <h3 className="font-medium">Create feature flag</h3>
          <select className="w-full rounded-lg bg-slate-950 border border-slate-700 px-3 py-2" value={projectId} onChange={e => setProjectId(e.target.value)} required>
            {projects.map(p => (
              <option key={p.id} value={p.id}>{p.name}</option>
            ))}
          </select>
          <input className="w-full rounded-lg bg-slate-950 border border-slate-700 px-3 py-2 font-mono" placeholder="Key" value={key} onChange={e => setKey(e.target.value)} required />
          <input className="w-full rounded-lg bg-slate-950 border border-slate-700 px-3 py-2" placeholder="Name" value={name} onChange={e => setName(e.target.value)} required />
          <input className="w-full rounded-lg bg-slate-950 border border-slate-700 px-3 py-2" placeholder="Description" value={description} onChange={e => setDescription(e.target.value)} />
          <select className="w-full rounded-lg bg-slate-950 border border-slate-700 px-3 py-2" value={flagType} onChange={e => setFlagType(e.target.value as typeof flagType)}>
            <option value="BOOLEAN">Boolean</option>
            <option value="STRING">String</option>
            <option value="NUMERIC">Numeric</option>
            <option value="JSON">JSON</option>
          </select>
          <input className="w-full rounded-lg bg-slate-950 border border-slate-700 px-3 py-2 font-mono" placeholder="Default value" value={defaultServeValue} onChange={e => setDefaultServeValue(e.target.value)} required />
          {error && <p className="text-red-400 text-sm">{error}</p>}
          <div className="flex gap-2">
            <button type="submit" className="rounded-lg bg-indigo-600 px-4 py-2 text-sm">Create</button>
            <button type="button" onClick={() => setShowForm(false)} className="rounded-lg border border-slate-700 px-4 py-2 text-sm">Cancel</button>
          </div>
        </form>
      )}

      <div className="rounded-xl border border-slate-800 overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-slate-900/80 text-slate-400">
            <tr>
              <th className="text-left p-3">Key</th>
              <th className="text-left p-3">Name</th>
              <th className="text-left p-3">Project</th>
              <th className="text-left p-3">Type</th>
              <th className="text-left p-3">Status</th>
              <th className="text-right p-3">Actions</th>
            </tr>
          </thead>
          <tbody>
            {flags.map(flag => (
              <tr key={flag.id} className="border-t border-slate-800">
                <td className="p-3 font-mono text-indigo-300">
                  <Link to={`/flags/${flag.id}`} className="hover:underline">{flag.key}</Link>
                </td>
                <td className="p-3">{flag.name}</td>
                <td className="p-3">{flag.projectName}</td>
                <td className="p-3 text-slate-400">{flag.flagType}</td>
                <td className="p-3">
                  <button
                    type="button"
                    onClick={() => toggleGlobal(flag)}
                    className={`px-2 py-1 rounded-full text-xs ${flag.enabled ? 'bg-emerald-900 text-emerald-300' : 'bg-slate-800 text-slate-400'}`}
                  >
                    {flag.enabled ? 'Enabled' : 'Disabled'}
                  </button>
                </td>
                <td className="p-3 text-right space-x-2">
                  <Link to={`/flags/${flag.id}`} className="text-indigo-400 hover:underline text-xs">Edit</Link>
                  <button type="button" onClick={() => handleDelete(flag.id)} className="text-red-400 hover:underline text-xs">Delete</button>
                </td>
              </tr>
            ))}
            {flags.length === 0 && (
              <tr>
                <td colSpan={6} className="p-6 text-center text-slate-500">
                  {projects.length ? 'No flags yet. Create your first flag.' : 'Create a project first, then add flags.'}
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
