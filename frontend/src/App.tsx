import { Navigate, Route, Routes } from 'react-router-dom'
import Layout from './components/Layout'
import AuditLogs from './pages/AuditLogs'
import CodeGenerator from './pages/CodeGenerator'
import Dashboard from './pages/Dashboard'
import FlagDetail from './pages/FlagDetail'
import Flags from './pages/Flags'
import Login from './pages/Login'
import Projects from './pages/Projects'
import Sandbox from './pages/Sandbox'

function isAuthenticated() {
  return !!localStorage.getItem('rp_token')
}

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  return isAuthenticated() ? <>{children}</> : <Navigate to="/login" replace />
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <Layout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Dashboard />} />
        <Route path="projects" element={<Projects />} />
        <Route path="flags" element={<Flags />} />
        <Route path="flags/:flagId" element={<FlagDetail />} />
        <Route path="sandbox" element={<Sandbox />} />
        <Route path="audit" element={<AuditLogs />} />
        <Route path="code" element={<CodeGenerator />} />
      </Route>
    </Routes>
  )
}
