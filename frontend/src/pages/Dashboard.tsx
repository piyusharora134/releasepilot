import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api/client'

export default function Dashboard() {
  const [userName, setUserName] = useState('')
  const [stats, setStats] = useState({ orgs: 0, projects: 0, flags: 0 })

  useEffect(() => {
    async function load() {
      const [me, orgs] = await Promise.all([api.getMe(), api.getOrganizations()])
      setUserName(me.name)
      let projectCount = 0
      let flagCount = 0
      for (const org of orgs) {
        const projects = await api.getProjects(org.id)
        projectCount += projects.length
        for (const project of projects) {
          const flags = await api.getFlags(project.id)
          flagCount += flags.length
        }
      }
      setStats({ orgs: orgs.length, projects: projectCount, flags: flagCount })
    }
    load().catch(console.error)
  }, [])

  const cards = [
    { label: 'Organizations', value: stats.orgs, link: '/projects' },
    { label: 'Projects', value: stats.projects, link: '/projects' },
    { label: 'Feature Flags', value: stats.flags, link: '/flags' },
  ]

  return (
    <div className="space-y-8">
      <div>
        <h2 className="text-2xl font-semibold">Welcome{userName ? `, ${userName}` : ''}</h2>
        <p className="text-slate-400 mt-1">Release control plane for safe progressive delivery.</p>
      </div>
      <div className="grid md:grid-cols-3 gap-4">
        {cards.map(card => (
          <Link
            key={card.label}
            to={card.link}
            className="rounded-xl border border-slate-800 bg-slate-900/50 p-5 hover:border-indigo-700 transition"
          >
            <p className="text-sm text-slate-400">{card.label}</p>
            <p className="text-3xl font-semibold mt-2">{card.value}</p>
          </Link>
        ))}
      </div>
      <div className="rounded-xl border border-slate-800 bg-slate-900/40 p-6">
        <h3 className="font-medium mb-3">Quick start</h3>
        <ol className="list-decimal list-inside space-y-2 text-slate-300 text-sm">
          <li>Create a <Link to="/projects" className="text-indigo-400 hover:underline">project</Link> (dev/staging/prod envs are auto-created)</li>
          <li>Add <Link to="/flags" className="text-indigo-400 hover:underline">feature flags</Link> to the project</li>
          <li>Test evaluations in the <Link to="/sandbox" className="text-indigo-400 hover:underline">sandbox</Link></li>
          <li>Integrate using <Link to="/code" className="text-indigo-400 hover:underline">SDK code snippets</Link></li>
        </ol>
      </div>
    </div>
  )
}
