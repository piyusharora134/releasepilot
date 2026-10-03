import { FormEvent, useEffect, useState } from 'react'
import { api, loadDefaultContext } from '../api/client'

export default function Sandbox() {
  const [apiKey, setApiKey] = useState('')
  const [flagKey, setFlagKey] = useState('')
  const [contextJson, setContextJson] = useState('{"userId":"user-123"}')
  const [result, setResult] = useState('')

  useEffect(() => {
    loadDefaultContext().then(ctx => {
      if (ctx?.project) {
        const dev = ctx.project.environments.find(e => e.key === 'dev')
        if (dev) setApiKey(dev.apiKey)
      }
    }).catch(console.error)
  }, [])

  async function handleEvaluate(e: FormEvent) {
    e.preventDefault()
    try {
      const context = JSON.parse(contextJson)
      const data = await api.evaluate(apiKey, flagKey, context)
      setResult(JSON.stringify(data, null, 2))
    } catch (err) {
      setResult(err instanceof Error ? err.message : 'Evaluation failed')
    }
  }

  return (
    <div className="space-y-6 max-w-3xl">
      <div>
        <h2 className="text-2xl font-semibold">Evaluation Sandbox</h2>
        <p className="text-slate-400 mt-1">Test SDK flag evaluation with live API keys.</p>
      </div>
      <form onSubmit={handleEvaluate} className="space-y-4 rounded-xl border border-slate-800 bg-slate-900/50 p-6">
        <input
          className="w-full rounded-lg bg-slate-950 border border-slate-700 px-3 py-2 font-mono text-sm"
          placeholder="SDK API Key"
          value={apiKey}
          onChange={e => setApiKey(e.target.value)}
          required
        />
        <input
          className="w-full rounded-lg bg-slate-950 border border-slate-700 px-3 py-2 font-mono text-sm"
          placeholder="Flag key"
          value={flagKey}
          onChange={e => setFlagKey(e.target.value)}
          required
        />
        <textarea
          className="w-full rounded-lg bg-slate-950 border border-slate-700 px-3 py-2 font-mono text-sm h-32"
          value={contextJson}
          onChange={e => setContextJson(e.target.value)}
        />
        <button type="submit" className="rounded-lg bg-indigo-600 hover:bg-indigo-500 px-4 py-2 font-medium">
          Evaluate
        </button>
      </form>
      {result && (
        <pre className="rounded-xl border border-slate-800 bg-slate-950 p-4 text-sm overflow-auto">{result}</pre>
      )}
    </div>
  )
}
