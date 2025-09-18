import React, { useState, useEffect } from 'react'
import axios from 'axios'

export default function App() {
  const [form, setForm] = useState({ name: '', email: '' })
  const [pdfFile, setPdfFile] = useState(null)
  const [signUrl, setSignUrl] = useState(null)
  const [showModal, setShowModal] = useState(false)

  const onChange = e => {
    const { name, value, type, checked } = e.target
    setForm(prev => ({ ...prev, [name]: type === 'checkbox' ? checked : value }))
  }

  useEffect(() => {
    const handleMessage = (event) => {
      if (event.data === 'signing_complete') {
        setShowModal(false)
        setSignUrl(null)
        alert('Documento firmado exitosamente')
      }
    }
    window.addEventListener('message', handleMessage)
    return () => window.removeEventListener('message', handleMessage)
  }, [])

  const submit = async e => {
    e.preventDefault()
    if (!pdfFile) {
      alert('Por favor selecciona un archivo PDF')
      return
    }
    
    const formData = new FormData()
    formData.append('signerName', form.name)
    formData.append('signerEmail', form.email)
    formData.append('returnUrl', window.location.origin + '/firmado')
    formData.append('pdfFile', pdfFile)
    
    const res = await axios.post(`${import.meta.env.VITE_API_BASE_URL}/api/envelopes`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    setSignUrl(res.data.signingUrl)
    setShowModal(true)
  }

  return (
    <div style={{ maxWidth: 720, margin: '40px auto', fontFamily: 'system-ui, sans-serif' }}>
      <h1>Firma de Documento PDF (DocuSign)</h1>
      <form onSubmit={submit}>
        <label>Nombre<br/><input name="name" value={form.name} onChange={onChange} required/></label><br/><br/>
        <label>Email<br/><input name="email" type="email" value={form.email} onChange={onChange} required/></label><br/><br/>
        <label>Archivo PDF<br/><input type="file" accept=".pdf" onChange={e => setPdfFile(e.target.files[0])} required/></label><br/><br/>
        <button type="submit">Subir PDF y firmar</button>
      </form>

      {showModal && signUrl && (
        <div style={{
          position: 'fixed', top: 0, left: 0, width: '100%', height: '100%',
          backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000
        }}>
          <div style={{
            backgroundColor: 'white', width: '90%', height: '90%', borderRadius: '8px',
            position: 'relative', display: 'flex', flexDirection: 'column'
          }}>
            <div style={{ padding: '16px', borderBottom: '1px solid #ccc', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h2 style={{ margin: 0 }}>Firma del Documento</h2>
              <button onClick={() => setShowModal(false)} style={{ background: 'none', border: 'none', fontSize: '24px', cursor: 'pointer' }}>×</button>
            </div>
            <iframe 
              title="DocuSign" 
              src={signUrl} 
              style={{ flex: 1, border: 'none', width: '100%' }}
              onLoad={() => {
                // Listen for DocuSign completion
                const checkCompletion = setInterval(() => {
                  try {
                    const iframe = document.querySelector('iframe[title="DocuSign"]')
                    if (iframe && iframe.contentWindow.location.href.includes('/firmado')) {
                      clearInterval(checkCompletion)
                      setShowModal(false)
                      setSignUrl(null)
                      alert('Documento firmado exitosamente')
                    }
                  } catch (e) {
                    // Cross-origin, ignore
                  }
                }, 1000)
                setTimeout(() => clearInterval(checkCompletion), 300000) // Stop after 5 min
              }}
            />
          </div>
        </div>
      )}
    </div>
  )
}
