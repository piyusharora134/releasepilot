import { NavLink, Outlet, useNavigate } from 'react-router-dom'

const links = [
  { to: '/', label: 'Dashboard', end: true },
  { to: '/projects', label: 'Projects' },
  { to: '/flags', label: 'Flags' },
  { to: '/sandbox', label: 'Sandbox' },
  { to: '/audit', label: 'Audit Log' },
  { to: '/code', label: 'SDK Code' },
]

export default function Layout() {
  const navigate = useNavigate()

  return (
    <div className="min-h-screen flex">
      <aside className="w-64 border-r border-slate-800 bg-slate-900/60 p-6 flex flex-col gap-8 shrink-0">
        <div>
          <p className="text-xs uppercase tracking-widest text-indigo-400">ReleasePilot</p>
          <h1 className="text-xl font-semibold">Control Center</h1>
        </div>
        <nav className="flex flex-col gap-2">
          {links.map(link => (
            <NavLink
              key={link.to}
              to={link.to}
              end={link.end}
              className={({ isActive }) =>
                `rounded-lg px-3 py-2 text-sm transition ${isActive ? 'bg-indigo-600 text-white' : 'text-slate-300 hover:bg-slate-800'}`
              }
            >
              {link.label}
            </NavLink>
          ))}
        </nav>
        <div className="mt-auto space-y-3 text-sm">
          <a
            href="http://localhost:8080/swagger-ui.html"
            target="_blank"
            rel="noreferrer"
            className="block text-slate-400 hover:text-white"
          >
            API Docs ↗
          </a>
          <button
            type="button"
            onClick={() => {
              localStorage.removeItem('rp_token')
              navigate('/login')
            }}
            className="text-slate-400 hover:text-white"
          >
            Sign out
          </button>
        </div>
      </aside>
      <main className="flex-1 p-8 overflow-auto">
        <Outlet />
      </main>
    </div>
  )
}
