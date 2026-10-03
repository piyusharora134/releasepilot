import { useEffect, useState } from 'react'
import { api, loadDefaultContext } from '../api/client'

export default function CodeGenerator() {
  const [apiKey, setApiKey] = useState('YOUR_SDK_API_KEY')
  const [flagKey, setFlagKey] = useState('my-feature-flag')

  useEffect(() => {
    loadDefaultContext().then(ctx => {
      if (ctx?.project) {
        const dev = ctx.project.environments.find(e => e.key === 'dev')
        if (dev) setApiKey(dev.apiKey)
      }
    }).catch(console.error)
  }, [])

  const snippets = {
    curl: `curl -X POST http://localhost:8080/api/v1/eval/evaluate \\
  -H "Content-Type: application/json" \\
  -H "X-API-Key: ${apiKey}" \\
  -d '{"flagKey":"${flagKey}","context":{"userId":"user-123"}}'`,

    javascript: `const response = await fetch('/api/v1/eval/evaluate', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'X-API-Key': '${apiKey}',
  },
  body: JSON.stringify({
    flagKey: '${flagKey}',
    context: { userId: 'user-123' },
  }),
});
const { data } = await response.json();
console.log(data.value);`,

    java: `HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("http://localhost:8080/api/v1/eval/evaluate"))
    .header("Content-Type", "application/json")
    .header("X-API-Key", "${apiKey}")
    .POST(HttpRequest.BodyPublishers.ofString(
        "{\\"flagKey\\":\\"${flagKey}\\",\\"context\\":{\\"userId\\":\\"user-123\\"}}"))
    .build();`,
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-semibold">SDK Code Generator</h2>
        <p className="text-slate-400 mt-1">Copy integration snippets for your application.</p>
      </div>
      <input
        className="w-full max-w-lg rounded-lg bg-slate-950 border border-slate-700 px-3 py-2 font-mono text-sm"
        value={flagKey}
        onChange={e => setFlagKey(e.target.value)}
        placeholder="Flag key"
      />
      {Object.entries(snippets).map(([lang, code]) => (
        <div key={lang} className="space-y-2">
          <h3 className="text-sm uppercase tracking-widest text-indigo-400">{lang}</h3>
          <pre className="rounded-xl border border-slate-800 bg-slate-950 p-4 text-sm overflow-auto">{code}</pre>
        </div>
      ))}
    </div>
  )
}
