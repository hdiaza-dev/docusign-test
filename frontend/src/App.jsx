import React, { useState } from 'react'
import axios from 'axios'

export default function App() {
  const [form, setForm] = useState({ name: '', email: '', q1: '', q2: '', agree: false })
  const [signUrl, setSignUrl] = useState(null)

  const onChange = e => {
    const { name, value, type, checked } = e.target
    setForm(prev => ({ ...prev, [name]: type === 'checkbox' ? checked : value }))
  }

  const submit = async e => {
    e.preventDefault()
    const payload = {
      signerName: form.name,
      signerEmail: form.email,
      returnUrl: window.location.origin + '/firmado',
      answers: {
        'Pregunta 1': form.q1,
        'Pregunta 2': form.q2,
        'Acepto condiciones': form.agree
      }
    }
    const res = await axios.post('http://localhost:8080/api/envelopes', payload)
    setSignUrl(res.data.signingUrl)
  }

  return (
    <div style={{ maxWidth: 720, margin: '40px auto', fontFamily: 'system-ui, sans-serif' }}>
      <h1>Firma de Cuestionario (DocuSign)</h1>
      <form onSubmit={submit}>
        <label>Nombre<br/><input name="name" value={form.name} onChange={onChange} required/></label><br/><br/>
        <label>Email<br/><input name="email" type="email" value={form.email} onChange={onChange} required/></label><br/><br/>
        <label>Pregunta 1<br/><input name="q1" value={form.q1} onChange={onChange}/></label><br/><br/>
        <label>Pregunta 2<br/><textarea name="q2" value={form.q2} onChange={onChange}/></label><br/><br/>
        <label><input type="checkbox" name="agree" checked={form.agree} onChange={onChange}/> Acepto condiciones</label><br/><br/>
        <button type="submit">Generar sobre y firmar</button>
      </form>

      {signUrl && (
        <div style={{ marginTop: 24 }}>
          <h2>Firma embebida</h2>
          <iframe title="DocuSign" src={signUrl} style={{ width: '100%', height: '80vh', border: '1px solid #ccc' }} />
        </div>
      )}
    </div>
  )
}
