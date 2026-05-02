import { useState, useCallback } from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import Header from './components/Header/Header'
import Footer from './components/Footer/Footer'
import LoginModal from './components/LoginModal/LoginModal'
import HomePage from './components/pages/HomePage'
import ContactPage from './components/pages/ContactPage'

/**
 * App — Componente raíz de la aplicación.
 *
 * Estructura:
 *   <BrowserRouter>          — React Router v6
 *     <AuthProvider>         — Contexto de autenticación global
 *       <Header>             — Logo + botón login/logout + menú toggle
 *       <main>               — Contenido de la página según ruta
 *         <Routes>/<Route>   — /, /contacto
 *       </main>
 *       <Footer>
 *       <LoginModal>         — Modal global de login/registro/2FA
 *     </AuthProvider>
 *   </BrowserRouter>
 *
 * El modal se gestiona en App porque puede abrirse desde el Header
 * o desde cualquier punto de la aplicación.
 */
function App() {
  const [modalOpen, setModalOpen]   = useState(false)
  const [modalMode, setModalMode]   = useState('login') // 'login' | 'register'

  const openModal = useCallback((mode = 'login') => {
    setModalMode(mode)
    setModalOpen(true)
  }, [])

  const closeModal = useCallback(() => {
    setModalOpen(false)
  }, [])

  return (
    <BrowserRouter>
      <AuthProvider>
        <Header onLoginClick={() => openModal('login')} />

        <main className="app-main">
          <Routes>
            <Route path="/"         element={<HomePage />} />
            <Route path="/contacto" element={<ContactPage />} />
            {/* Cualquier ruta desconocida redirige al inicio */}
            <Route path="*"         element={<Navigate to="/" replace />} />
          </Routes>
        </main>

        <Footer />

        <LoginModal
          isOpen={modalOpen}
          onClose={closeModal}
          initialMode={modalMode}
        />
      </AuthProvider>
    </BrowserRouter>
  )
}

export default App
