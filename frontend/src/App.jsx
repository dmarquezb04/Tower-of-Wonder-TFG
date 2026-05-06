import { useState, useCallback } from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import Header from './components/Header/Header'
import Footer from './components/Footer/Footer'
import LoginModal from './components/LoginModal/LoginModal'
import HomePage from './components/pages/HomePage'
import ContactPage from './components/pages/ContactPage'
import UserDashboard from './components/pages/UserDashboard'
import AdminDashboard from './components/pages/AdminDashboard'
import ShopPage from './components/pages/ShopPage'
import CharactersPage from './components/pages/CharactersPage'
import NewsListPage from './components/pages/NewsListPage'
import NewsPostPage from './components/pages/NewsPostPage'
import ReactivateAccount from './components/pages/ReactivateAccount'
import NewsletterConfirmPage from './components/pages/NewsletterConfirmPage'
import ProtectedRoute from './components/ProtectedRoute'
import { CartProvider } from './context/CartContext'
import { useTrackVisit } from './hooks/useTrackVisit'

/**
 * Componente auxiliar para ejecutar el hook de métricas dentro del Router.
 */
function TrackHandler() {
  useTrackVisit()
  return null
}

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
      <TrackHandler />
      <AuthProvider>
        <CartProvider>
          <Header onLoginClick={() => openModal('login')} />

        <main className="app-main">
          <Routes>
            <Route path="/"         element={<HomePage />} />
            <Route path="/contacto" element={<ContactPage />} />
            <Route path="/shop"     element={<ShopPage />} />
            <Route path="/personajes" element={<CharactersPage />} />
            <Route path="/noticias"   element={<NewsListPage />} />
            <Route path="/noticias/:slug" element={<NewsPostPage />} />
            
            {/* Rutas de Acciones por Email */}
            <Route path="/reactivate" element={<ReactivateAccount />} />
            <Route path="/newsletter/confirm" element={<NewsletterConfirmPage />} />
            
            {/* Rutas Privadas */}
            <Route path="/dashboard" element={
              <ProtectedRoute>
                <UserDashboard />
              </ProtectedRoute>
            } />
            <Route path="/admin" element={
              <ProtectedRoute requireAdmin={true}>
                <AdminDashboard />
              </ProtectedRoute>
            } />

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
        </CartProvider>
      </AuthProvider>
    </BrowserRouter>
  )
}

export default App
