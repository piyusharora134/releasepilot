import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { api } from '../api/client'
import type { FeatureFlag, FlagEnvironment } from '../api/client'

export default function FlagDetail() {
  const { flagId } = useParams<{ flagId: string }>()
  const [flag, setFlag] = useState<FeatureFlag | null>(null)


  // Modal states
  const [targetingModalEnv, setTargetingModalEnv] = useState<FlagEnvironment | null>(null)
  const [rolloutModalEnv, setRolloutModalEnv] = useState<FlagEnvironment | null>(null)

  // Targeting Rule Form
  const [targetAttr, setTargetAttr] = useState('userId')
  const [targetOperator, setTargetOperator] = useState<'EQUALS' | 'IN' | 'CONTAINS' | 'GREATER_THAN' | 'LESS_THAN'>('EQUALS')
  const [targetValuesRaw, setTargetValuesRaw] = useState('')
  const [targetServeValue, setTargetServeValue] = useState('')
  const [targetPriority, setTargetPriority] = useState(1)

  // Rollout Rule Form
  const [rolloutAttr, setRolloutAttr] = useState('userId')
  const [rolloutPercentage, setRolloutPercentage] = useState(50)
  const [rolloutServeValueA, setRolloutServeValueA] = useState('true')
  const [rolloutServeValueB, setRolloutServeValueB] = useState('false')

  const [loading, setLoading] = useState(false)
  const [errorMsg, setErrorMsg] = useState('')

  async function load() {
    if (!flagId) return
    const data = await api.getFlag(flagId)
    setFlag(data)

  }

  useEffect(() => {
    load().catch(console.error)
  }, [flagId])

  async function toggleEnv(flagEnvId: string, enabled: boolean) {
    await api.updateFlagEnvironment(flagEnvId, { enabled: !enabled })
    await load()
  }

  async function handleAddTargetingRule(e: React.FormEvent) {
    e.preventDefault()
    if (!targetingModalEnv) return
    setLoading(true)
    setErrorMsg('')
    try {
      const values = targetValuesRaw.split(',').map(v => v.trim()).filter(Boolean)
      if (!values.length) {
        throw new Error('At least one value is required')
      }
      await api.addTargetingRule(targetingModalEnv.id, {
        attribute: targetAttr,
        operator: targetOperator,
        values,
        serveValue: targetServeValue || flag?.defaultServeValue || 'true',
        priority: targetPriority,
      })
      setTargetingModalEnv(null)
      setTargetValuesRaw('')
      await load()
    } catch (err: any) {
      setErrorMsg(err.message || 'Failed to add targeting rule')
    } finally {
      setLoading(false)
    }
  }

  async function handleDeleteTargetingRule(ruleId: string) {
    await api.deleteTargetingRule(ruleId)
    await load()
  }

  async function handleSetRolloutRule(e: React.FormEvent) {
    e.preventDefault()
    if (!rolloutModalEnv) return
    setLoading(true)
    setErrorMsg('')
    try {
      await api.setRolloutRule(rolloutModalEnv.id, {
        attribute: rolloutAttr,
        percentage: rolloutPercentage,
        serveValueA: rolloutServeValueA,
        serveValueB: rolloutServeValueB,
      })
      setRolloutModalEnv(null)
      await load()
    } catch (err: any) {
      setErrorMsg(err.message || 'Failed to configure rollout rule')
    } finally {
      setLoading(false)
    }
  }

  if (!flag) {
    return <p className="text-slate-400">Loading flag…</p>
  }

  return (
    <div className="space-y-6 max-w-4xl">
      <div>
        <Link to="/flags" className="text-sm text-indigo-400 hover:underline">← Back to flags</Link>
        <h2 className="text-2xl font-semibold mt-2 font-mono">{flag.key}</h2>
        <p className="text-slate-400">{flag.name}</p>
      </div>

      <div className="rounded-xl border border-slate-800 bg-slate-900/40 p-5 space-y-2 text-sm">
        <p><span className="text-slate-500">Type:</span> <span className="font-mono text-indigo-300">{flag.flagType}</span></p>
        <p><span className="text-slate-500">Default Serve Value:</span> <code className="font-mono bg-slate-800 px-2 py-0.5 rounded text-emerald-400">{flag.defaultServeValue}</code></p>
        <p><span className="text-slate-500">Global Status:</span> {flag.enabled ? <span className="text-emerald-400 font-medium">Enabled</span> : <span className="text-slate-400">Disabled</span>}</p>
        {flag.description && <p><span className="text-slate-500">Description:</span> {flag.description}</p>}
      </div>



      <div>
        <h3 className="font-medium text-lg mb-3">Environments & Rule Engine</h3>
        <div className="space-y-4">
          {flag.environments.map(env => (
            <div key={env.id} className="rounded-xl border border-slate-800 bg-slate-900/40 p-5 space-y-4">
              <div className="flex items-center justify-between">
                <div>
                  <span className="font-mono text-lg font-medium text-indigo-300">{env.environmentKey}</span>
                  <span className="text-xs text-slate-500 ml-2">({env.environmentName})</span>
                </div>
                <button
                  type="button"
                  onClick={() => toggleEnv(env.id, env.enabled)}
                  className={`px-4 py-1.5 rounded-full text-xs font-semibold transition ${
                    env.enabled ? 'bg-emerald-900/80 text-emerald-300 hover:bg-emerald-800' : 'bg-slate-800 text-slate-400 hover:bg-slate-700'
                  }`}
                >
                  {env.enabled ? 'Enabled' : 'Disabled'}
                </button>
              </div>

              {/* Targeting Rules */}
              <div className="pt-2 border-t border-slate-800/80">
                <div className="flex items-center justify-between mb-2">
                  <h4 className="text-xs uppercase tracking-wider font-semibold text-slate-400">Targeting Rules</h4>
                  <button
                    type="button"
                    onClick={() => {
                      setTargetingModalEnv(env)
                      setTargetServeValue(flag.defaultServeValue)
                      setErrorMsg('')
                    }}
                    className="text-xs text-indigo-400 hover:text-indigo-300 font-medium"
                  >
                    + Add Rule
                  </button>
                </div>
                {env.targetingRules && env.targetingRules.length > 0 ? (
                  <div className="space-y-2">
                    {env.targetingRules.map(rule => (
                      <div key={rule.id} className="flex items-center justify-between text-xs bg-slate-950/50 p-2.5 rounded-lg border border-slate-800">
                        <div className="space-x-2 font-mono">
                          <span className="text-slate-500">P{rule.priority ?? 1}:</span>
                          <span className="text-amber-300">{rule.attribute}</span>
                          <span className="text-slate-400">{rule.operator}</span>
                          <span className="text-slate-200">[{rule.values.join(', ')}]</span>
                          <span className="text-slate-400">➔</span>
                          <span className="text-emerald-400">{rule.serveValue}</span>
                        </div>
                        <button
                          type="button"
                          onClick={() => handleDeleteTargetingRule(rule.id)}
                          className="text-red-400 hover:text-red-300 ml-2"
                        >
                          Remove
                        </button>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-xs text-slate-500 italic">No targeting rules configured for this environment.</p>
                )}
              </div>

              {/* Rollout Rule */}
              <div className="pt-2 border-t border-slate-800/80">
                <div className="flex items-center justify-between mb-2">
                  <h4 className="text-xs uppercase tracking-wider font-semibold text-slate-400">Percentage Rollout</h4>
                  <button
                    type="button"
                    onClick={() => {
                      setRolloutModalEnv(env)
                      if (env.rolloutRule) {
                        setRolloutPercentage(env.rolloutRule.percentage)
                        setRolloutServeValueA(env.rolloutRule.serveValueA)
                        setRolloutServeValueB(env.rolloutRule.serveValueB)
                        setRolloutAttr(env.rolloutRule.attribute || 'userId')
                      } else {
                        setRolloutPercentage(50)
                        setRolloutServeValueA('true')
                        setRolloutServeValueB('false')
                        setRolloutAttr('userId')
                      }
                      setErrorMsg('')
                    }}
                    className="text-xs text-indigo-400 hover:text-indigo-300 font-medium"
                  >
                    {env.rolloutRule ? 'Edit Rollout' : '+ Configure Rollout'}
                  </button>
                </div>
                {env.rolloutRule ? (
                  <div className="bg-slate-950/50 p-3 rounded-lg border border-slate-800 text-xs space-y-2">
                    <div className="flex justify-between font-mono">
                      <span>Attribute: <strong className="text-amber-300">{env.rolloutRule.attribute || 'userId'}</strong></span>
                      <span>Rollout: <strong className="text-indigo-400">{env.rolloutRule.percentage}%</strong></span>
                    </div>
                    {/* Visual Bar */}
                    <div className="w-full h-2.5 bg-slate-800 rounded-full overflow-hidden flex">
                      <div style={{ width: `${env.rolloutRule.percentage}%` }} className="bg-indigo-500 h-full"></div>
                      <div style={{ width: `${100 - env.rolloutRule.percentage}%` }} className="bg-slate-700 h-full"></div>
                    </div>
                    <div className="flex justify-between text-[11px] text-slate-400 font-mono">
                      <span>{env.rolloutRule.percentage}% ➔ <code className="text-emerald-400">{env.rolloutRule.serveValueA}</code></span>
                      <span>{100 - env.rolloutRule.percentage}% ➔ <code className="text-slate-300">{env.rolloutRule.serveValueB}</code></span>
                    </div>
                  </div>
                ) : (
                  <p className="text-xs text-slate-500 italic">No percentage rollout configured (serving static value or rules).</p>
                )}
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Add Targeting Rule Modal */}
      {targetingModalEnv && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 w-full max-w-md space-y-4">
            <h3 className="text-lg font-semibold">Add Targeting Rule ({targetingModalEnv.environmentKey})</h3>
            {errorMsg && <p className="text-xs text-red-400 bg-red-950/40 p-2 rounded border border-red-900">{errorMsg}</p>}
            <form onSubmit={handleAddTargetingRule} className="space-y-3 text-sm">
              <div>
                <label className="block text-xs text-slate-400 mb-1">User Attribute</label>
                <input
                  type="text"
                  required
                  value={targetAttr}
                  onChange={e => setTargetAttr(e.target.value)}
                  placeholder="e.g. userId, email, country"
                  className="w-full bg-slate-950 border border-slate-800 rounded px-3 py-2 text-slate-200 focus:outline-none focus:border-indigo-500"
                />
              </div>
              <div>
                <label className="block text-xs text-slate-400 mb-1">Operator</label>
                <select
                  value={targetOperator}
                  onChange={e => setTargetOperator(e.target.value as any)}
                  className="w-full bg-slate-950 border border-slate-800 rounded px-3 py-2 text-slate-200 focus:outline-none focus:border-indigo-500"
                >
                  <option value="EQUALS">EQUALS</option>
                  <option value="IN">IN (comma-separated)</option>
                  <option value="CONTAINS">CONTAINS</option>
                  <option value="GREATER_THAN">GREATER_THAN</option>
                  <option value="LESS_THAN">LESS_THAN</option>
                </select>
              </div>
              <div>
                <label className="block text-xs text-slate-400 mb-1">Target Values (comma-separated)</label>
                <input
                  type="text"
                  required
                  value={targetValuesRaw}
                  onChange={e => setTargetValuesRaw(e.target.value)}
                  placeholder="e.g. user-1, user-2, US, CA"
                  className="w-full bg-slate-950 border border-slate-800 rounded px-3 py-2 text-slate-200 focus:outline-none focus:border-indigo-500"
                />
              </div>
              <div>
                <label className="block text-xs text-slate-400 mb-1">Serve Value</label>
                <input
                  type="text"
                  required
                  value={targetServeValue}
                  onChange={e => setTargetServeValue(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded px-3 py-2 text-slate-200 focus:outline-none focus:border-indigo-500"
                />
              </div>
              <div>
                <label className="block text-xs text-slate-400 mb-1">Rule Priority</label>
                <input
                  type="number"
                  min="1"
                  value={targetPriority}
                  onChange={e => setTargetPriority(Number(e.target.value))}
                  className="w-full bg-slate-950 border border-slate-800 rounded px-3 py-2 text-slate-200 focus:outline-none focus:border-indigo-500"
                />
              </div>
              <div className="flex justify-end space-x-2 pt-2">
                <button
                  type="button"
                  onClick={() => setTargetingModalEnv(null)}
                  className="px-4 py-2 rounded bg-slate-800 hover:bg-slate-700 text-xs"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  className="px-4 py-2 rounded bg-indigo-600 hover:bg-indigo-500 text-xs font-semibold text-white"
                >
                  {loading ? 'Saving…' : 'Add Rule'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Configure Rollout Rule Modal */}
      {rolloutModalEnv && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 w-full max-w-md space-y-4">
            <h3 className="text-lg font-semibold">Configure Percentage Rollout ({rolloutModalEnv.environmentKey})</h3>
            {errorMsg && <p className="text-xs text-red-400 bg-red-950/40 p-2 rounded border border-red-900">{errorMsg}</p>}
            <form onSubmit={handleSetRolloutRule} className="space-y-3 text-sm">
              <div>
                <label className="block text-xs text-slate-400 mb-1">Hashing Attribute</label>
                <input
                  type="text"
                  required
                  value={rolloutAttr}
                  onChange={e => setRolloutAttr(e.target.value)}
                  placeholder="e.g. userId"
                  className="w-full bg-slate-950 border border-slate-800 rounded px-3 py-2 text-slate-200 focus:outline-none focus:border-indigo-500"
                />
              </div>
              <div>
                <div className="flex justify-between text-xs text-slate-400 mb-1">
                  <span>Rollout Percentage</span>
                  <span className="font-mono text-indigo-400 font-bold">{rolloutPercentage}%</span>
                </div>
                <input
                  type="range"
                  min="0"
                  max="100"
                  value={rolloutPercentage}
                  onChange={e => setRolloutPercentage(Number(e.target.value))}
                  className="w-full accent-indigo-500 cursor-pointer"
                />
              </div>
              <div>
                <label className="block text-xs text-slate-400 mb-1">Serve Value A ({rolloutPercentage}% of users)</label>
                <input
                  type="text"
                  required
                  value={rolloutServeValueA}
                  onChange={e => setRolloutServeValueA(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded px-3 py-2 text-slate-200 focus:outline-none focus:border-indigo-500"
                />
              </div>
              <div>
                <label className="block text-xs text-slate-400 mb-1">Serve Value B ({100 - rolloutPercentage}% of users)</label>
                <input
                  type="text"
                  required
                  value={rolloutServeValueB}
                  onChange={e => setRolloutServeValueB(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded px-3 py-2 text-slate-200 focus:outline-none focus:border-indigo-500"
                />
              </div>
              <div className="flex justify-end space-x-2 pt-2">
                <button
                  type="button"
                  onClick={() => setRolloutModalEnv(null)}
                  className="px-4 py-2 rounded bg-slate-800 hover:bg-slate-700 text-xs"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  className="px-4 py-2 rounded bg-indigo-600 hover:bg-indigo-500 text-xs font-semibold text-white"
                >
                  {loading ? 'Saving…' : 'Save Rollout'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
