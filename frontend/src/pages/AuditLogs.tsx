import { useEffect, useState } from 'react'
import { api } from '../api/client'

interface AuditRow {
  entityType: string
  action: string
  detailsJson: string
  createdAt: string
}

export default function AuditLogs() {
  const [logs, setLogs] = useState<AuditRow[]>([])

  useEffect(() => {
    async function load() {
      const orgs = await api.getOrganizations()
      if (!orgs.length) return
      const data = await api.getAuditLogs(orgs[0].id)
      setLogs(data)
    }
    load().catch(console.error)
  }, [])

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-semibold">Audit Log</h2>
        <p className="text-slate-400 mt-1">Track organization, project, environment, and flag changes.</p>
      </div>
      <div className="space-y-3">
        {logs.map((log, index) => (
          <div key={index} className="rounded-xl border border-slate-800 bg-slate-900/50 p-4">
            <div className="flex items-center gap-3 text-sm">
              <span className="font-mono text-indigo-300">{log.entityType}</span>
              <span className="text-slate-500">•</span>
              <span>{log.action}</span>
              <span className="ml-auto text-slate-500">{new Date(log.createdAt).toLocaleString()}</span>
            </div>
            <p className="text-slate-400 text-sm mt-2">{log.detailsJson}</p>
          </div>
        ))}
        {logs.length === 0 && <p className="text-slate-500">No audit entries yet.</p>}
      </div>
    </div>
  )
}
