import { useState, useEffect } from 'react'
import { QRCodeSVG } from 'qrcode.react'
import { getProfile, setup2FA, enable2FA, disable2FA, deleteAccount } from '../../api/authApi'
import { useAuth } from '../../context/AuthContext'
import DialogModal from '../DialogModal/DialogModal'
import styles from './UserDashboard.module.css'

export default function UserDashboard() {
  const { token, logout } = useAuth()
  
  const [profile, setProfile] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  
  // 2FA state
  const [setupData, setSetupData] = useState(null)
  const [verificationCode, setVerificationCode] = useState('')
  const [actionError, setActionError] = useState(null)
  const [actionSuccess, setActionSuccess] = useState(null)
  const [copied, setCopied] = useState(false)
  
  // Custom Dialog Modal state
  const [dialog, setDialog] = useState({
    isOpen: false,
    title: '',
    message: '',
    isAlert: false,
    isDanger: false,
    onConfirm: () => {},
    onCancel: () => {}
  })

  const loadProfile = async () => {
    try {
      setLoading(true)
      const data = await getProfile(token)
      setProfile(data)
    } catch (err) {
      setError('No se pudo cargar el perfil. ' + err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadProfile()
    // eslint-disable-next-deps
  }, [])

  const handleStartSetup2FA = async () => {
    try {
      setActionError(null)
      const data = await setup2FA(token)
      setSetupData(data)
    } catch (err) {
      setActionError(err.message)
    }
  }

  const handleEnable2FA = async (e) => {
    e.preventDefault()
    try {
      setActionError(null)
      await enable2FA(token, setupData.secret, verificationCode)
      setActionSuccess('2FA activado correctamente.')
      setSetupData(null)
      setVerificationCode('')
      loadProfile()
    } catch (err) {
      setActionError(err.message)
    }
  }

  const handleDisable2FA = async (e) => {
    e.preventDefault()
    try {
      setActionError(null)
      await disable2FA(token, verificationCode)
      setActionSuccess('2FA desactivado correctamente.')
      setVerificationCode('')
      loadProfile()
    } catch (err) {
      setActionError(err.message)
    }
  }

  const handleDeactivateAccount = () => {
    setDialog({
      isOpen: true,
      title: 'Borrar Cuenta',
      message: '¿Estás seguro de que deseas borrar tu cuenta? Tu cuenta quedará desactivada y tendrás 7 días para recuperarla antes de que sea permanente.',
      isAlert: false,
      isDanger: true,
      onConfirm: executeDeactivateAccount,
      onCancel: () => setDialog(prev => ({ ...prev, isOpen: false }))
    })
  }

  const executeDeactivateAccount = async () => {
    setDialog(prev => ({ ...prev, isOpen: false }))
    try {
      setActionError(null)
      await deleteAccount(token)
      setDialog({
        isOpen: true,
        title: 'Cuenta Desactivada',
        message: 'Cuenta desactivada correctamente. Se ha cerrado la sesión.',
        isAlert: true,
        isDanger: false,
        onConfirm: () => {
          setDialog(prev => ({ ...prev, isOpen: false }))
          logout()
        },
        onCancel: () => {}
      })
    } catch (err) {
      setActionError(err.message)
    }
  }

  const handleCopy = () => {
    if (!setupData?.secret) return
    navigator.clipboard.writeText(setupData.secret)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  if (loading) return <div className={styles.container}>Cargando perfil...</div>
  if (error) return <div className={styles.container}>{error}</div>
  if (!profile) return null

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>Panel de Usuario</h1>
      
      <div className={styles.card}>
        <h2>Mis Datos</h2>
        <p><strong>Usuario:</strong> {profile.username || 'Sin definir'}</p>
        <p><strong>Email:</strong> {profile.email}</p>
        <p><strong>Miembro desde:</strong> {new Date(profile.fechaCreacion).toLocaleDateString()}</p>
        <p><strong>Roles:</strong> {profile.roles.join(', ')}</p>
      </div>

      <div className={styles.card}>
        <h2>Seguridad (2FA)</h2>
        
        {actionSuccess && <p className={styles.success}>{actionSuccess}</p>}
        {actionError && <p className={styles.error}>{actionError}</p>}

        {profile.twoFaEnabled ? (
          <div className={styles.securitySection}>
            <p className={styles.statusOn}>✅ Autenticación en dos pasos activada</p>
            <p>Para desactivarla, introduce un código de tu aplicación de autenticación:</p>
            <form onSubmit={handleDisable2FA} className={styles.form}>
              <input
                type="text"
                placeholder="Código de 6 dígitos"
                value={verificationCode}
                onChange={(e) => setVerificationCode(e.target.value)}
                maxLength="6"
                required
                className={styles.input}
              />
              <button type="submit" className={styles.btnDanger}>Desactivar 2FA</button>
            </form>
          </div>
        ) : (
          <div className={styles.securitySection}>
            <p className={styles.statusOff}>❌ Autenticación en dos pasos desactivada</p>
            
            {!setupData ? (
              <button onClick={handleStartSetup2FA} className={styles.btnAction}>
                Configurar 2FA
              </button>
            ) : (
              <div className={styles.setupBox}>
                <p>1. Escanea este código QR con Google Authenticator o Authy:</p>
                <div className={styles.qrWrapper}>
                  <QRCodeSVG value={setupData.qrCodeUri} size={200} />
                </div>
                
                <div className={styles.secretBox}>
                  <p>O introduce este código manualmente:</p>
                  <strong className={styles.secretValue}>{setupData.secret}</strong>
                  <button 
                    type="button" 
                    className={styles.btnCopy}
                    onClick={handleCopy}
                  >
                    {copied ? '✅ ¡Copiado!' : '📋 Copiar código'}
                  </button>
                </div>
                
                <p>2. Introduce el código de 6 dígitos que te da la aplicación:</p>
                <form onSubmit={handleEnable2FA} className={styles.form}>
                  <input
                    type="text"
                    placeholder="Código numérico"
                    value={verificationCode}
                    onChange={(e) => setVerificationCode(e.target.value)}
                    maxLength="6"
                    required
                    className={styles.input}
                  />
                  <button type="submit" className={styles.btnAction}>Verificar y Activar</button>
                  <button type="button" onClick={() => setSetupData(null)} className={styles.btnCancel}>Cancelar</button>
                </form>
              </div>
            )}
          </div>
        )}
      </div>

      <div className={styles.actions}>
        <button onClick={handleDeactivateAccount} className={styles.btnDanger} style={{ marginRight: 'auto' }}>
          Borrar Cuenta
        </button>
        <button onClick={logout} className={styles.btnAction}>Cerrar Sesión</button>
      </div>

      <DialogModal {...dialog} />
    </div>
  )
}
