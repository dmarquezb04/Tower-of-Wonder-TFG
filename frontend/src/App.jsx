import { useState, useEffect, useCallback } from 'react'
import Header from './components/Header/Header'
import Footer from './components/Footer/Footer'
import LoginModal from './components/LoginModal/LoginModal'
import HomePage from './components/pages/HomePage'
import ContactPage from './components/pages/ContactPage'

const { currentPage } = window.APP_DATA

// Mapa de mensajes de error por código de URL
const ERROR_MESSAGES = {
  campos_vacios: 'Por favor completa todos los campos',
  login_incorrecto: 'Email o contraseña incorrectos',
  email_invalido: 'El formato del email no es válido',
  password_invalida: 'La contraseña debe tener entre 8 y 20 caracteres sin espacios',
  password_corta: 'La contraseña debe tener al menos 8 caracteres',
  email_existe: 'Este email ya está registrado',
  error_servidor: 'Error del servidor. Inténtalo más tarde',
  registro_fallido: 'Error al registrar. Inténtalo de nuevo',
  '2fa_not_configured': 'Error de seguridad. 2FA habilitado pero no configurado.',
  cuenta_bloqueada: 'Cuenta bloqueada temporalmente.',
  error_sistema: 'Error del sistema. Inténtalo más tarde',
}

function App() {
  const [modalOpen, setModalOpen] = useState(false)
  const [modalMode, setModalMode] = useState('login') // 'login' | 'register'
  const [modalError, setModalError] = useState(null)
  const [modalSuccess, setModalSuccess] = useState(null)

  const openModal = useCallback((mode = 'login') => {
    setModalMode(mode)
    setModalOpen(true)
  }, [])

  const closeModal = useCallback(() => {
    setModalOpen(false)
    setModalError(null)
    setModalSuccess(null)
    // Limpiar parámetros de URL
    const url = new URL(window.location)
    url.searchParams.delete('error')
    url.searchParams.delete('modal')
    url.searchParams.delete('success')
    url.searchParams.delete('message')
    window.history.replaceState({}, '', url)
  }, [])

  // Leer parámetros de URL al montar (reemplaza verificarParametrosURL())
  useEffect(() => {
    const params = new URLSearchParams(window.location.search)
    const modal = params.get('modal')
    const errorCode = params.get('error')
    const successCode = params.get('success')
    const message = params.get('message')

    if (modal === 'login' || modal === 'register') {
      openModal(modal)
      if (successCode === 'registro_exitoso') {
        setModalSuccess('¡Registro exitoso! Ahora puedes iniciar sesión')
      } else if (errorCode) {
        const msg = errorCode === 'cuenta_bloqueada' && message
          ? decodeURIComponent(message)
          : ERROR_MESSAGES[errorCode] ?? 'Ha ocurrido un error'
        setModalError(msg)
      }
    }

    // Exponer función global para compatibilidad
    window.abrirModalLogin = () => openModal('login')
  }, [openModal])

  const renderPage = () => {
    switch (currentPage) {
      case 'contacto.php': return <ContactPage />
      default: return <HomePage />
    }
  }

  return (
    <>
      <Header onLoginClick={() => openModal('login')} />
      <main style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
        {renderPage()}
      </main>
      <Footer />
      <LoginModal
        isOpen={modalOpen}
        onClose={closeModal}
        initialMode={modalMode}
        errorMessage={modalError}
        successMessage={modalSuccess}
      />
    </>
  )
}

export default App
