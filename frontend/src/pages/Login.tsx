import { FormEvent, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api/client'

export default function Login() {
  const navigate = useNavigate()
  const [mode, setMode] = useState<'login' | 'register'>('login')
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError('')
    try {
      const result =
        mode === 'login'
          ? await api.login({ email, password })
          : await api.register({ name, email, password })
      localStorage.setItem('rp_token', result.accessToken)
      navigate('/')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Authentication failed')
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center p-6 bg-slate-950">
      <form onSubmit={handleSubmit} className="w-full max-w-md rounded-2xl border border-slate-800 bg-slate-900/70 p-8 space-y-4">
        <div>
          <p className="text-indigo-400 text-sm uppercase tracking-widest">ReleasePilot</p>
          <h1 className="text-2xl font-semibold mt-1">{mode === 'login' ? 'Sign in' : 'Create account'}</h1>
          <p className="text-slate-400 text-sm mt-2">Feature flags & progressive rollouts</p>
        </div>
        {mode === 'register' && (
          <input
            className="w-full rounded-lg bg-slate-950 border border-slate-700 px-3 py-2"
            placeholder="Name"
            value={name}
            onChange={e => setName(e.target.value)}
            required
          />
        )}
        <input
          className="w-full rounded-lg bg-slate-950 border border-slate-700 px-3 py-2"
          placeholder="Email"
          type="email"
          value={email}
          onChange={e => setEmail(e.target.value)}
          required
        />
        <input
          className="w-full rounded-lg bg-slate-950 border border-slate-700 px-3 py-2"
          placeholder="Password"
          type="password"
          value={password}
          onChange={e => setPassword(e.target.value)}
          required
          minLength={6}
        />
        {error && <p className="text-red-400 text-sm">{error}</p>}
        <button type="submit" className="w-full rounded-lg bg-indigo-600 hover:bg-indigo-500 py-2 font-medium">
          {mode === 'login' ? 'Sign in' : 'Register'}
        </button>
        <button
          type="button"
          className="w-full text-sm text-slate-400 hover:text-white"
          onClick={() => setMode(mode === 'login' ? 'register' : 'login')}
        >
          {mode === 'login' ? 'Need an account? Register' : 'Already have an account? Sign in'}
        </button>
      </form>
    </div>
  )
}
