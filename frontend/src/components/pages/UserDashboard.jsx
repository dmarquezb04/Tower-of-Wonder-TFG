import { useState, useEffect } from 'react'
import { QRCodeSVG } from 'qrcode.react'
import { getProfile, setup2FA, enable2FA, disable2FA, deleteAccount, updateProfile } from '../../api/authApi'
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
  const [securityError, setSecurityError] = useState(null)
  const [securitySuccess, setSecuritySuccess] = useState(null)
  const [copied, setCopied] = useState(false)
  
  // Edit Profile state
  const [isEditing, setIsEditing] = useState(false)
  const [showPasswordChange, setShowPasswordChange] = useState(false)
  const [formData, setFormData] = useState({
    username: '',
    currentPassword: '',
    newPassword: ''
  })
  const [isSaving, setIsSaving] = useState(false)
  const [personalInfoError, setPersonalInfoError] = useState(null)
  const [personalInfoSuccess, setPersonalInfoSuccess] = useState(null)
  
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
      setFormData(prev => ({
        ...prev,
        username: data.username || ''
      }))
    } catch (err) {
      setError('No se pudo cargar el perfil. ' + err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (token) {
      loadProfile()
    }
  }, [token]) // Re-ejecutar si el token cambia o se carga

  const handleStartSetup2FA = async () => {
    try {
      setSecurityError(null)
      const data = await setup2FA(token)
      setSetupData(data)
    } catch (err) {
      setSecurityError(err.message)
    }
  }

  const handleEnable2FA = async (e) => {
    e.preventDefault()
    try {
      setSecurityError(null)
      await enable2FA(token, setupData.secret, verificationCode)
      setSecuritySuccess('2FA activado correctamente.')
      setSetupData(null)
      setVerificationCode('')
      loadProfile()
    } catch (err) {
      setSecurityError(err.message)
    }
  }

  const handleDisable2FA = async (e) => {
    e.preventDefault()
    try {
      setSecurityError(null)
      await disable2FA(token, verificationCode)
      setSecuritySuccess('2FA desactivado correctamente.')
      setVerificationCode('')
      loadProfile()
    } catch (err) {
      setSecurityError(err.message)
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
      setSecurityError(null)
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
      setSecurityError(err.message)
    }
  }

  const handleCopy = () => {
    if (!setupData?.secret) return
    navigator.clipboard.writeText(setupData.secret)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  const handleEditToggle = () => {
    if (isEditing) {
      // Cancel edit
      setFormData(prev => ({
        ...prev,
        username: profile.username || '',
        currentPassword: '',
        newPassword: ''
      }))
      setShowPasswordChange(false)
    }
    setIsEditing(!isEditing)
  }

  const handleSave = async () => {
    try {
      setIsSaving(true)
      setPersonalInfoError(null)
      setPersonalInfoSuccess(null)
      
      const updateData = {
        username: formData.username
      }
      
      if (showPasswordChange) {
        if (!formData.currentPassword || !formData.newPassword) {
          throw new Error('Debes completar los campos de contraseña')
        }
        updateData.currentPassword = formData.currentPassword
        updateData.newPassword = formData.newPassword
      }
      
      await updateProfile(token, updateData)
      
      setPersonalInfoSuccess('Perfil actualizado correctamente.')
      setIsEditing(false)
      setShowPasswordChange(false)
      setFormData(prev => ({ ...prev, currentPassword: '', newPassword: '' }))
      loadProfile()
    } catch (err) {
      const msg = err.response?.data?.error || err.message
      setPersonalInfoError(msg)
    } finally {
      setIsSaving(false)
    }
  }

  const hasChanges = () => {
    if (formData.username !== (profile.username || '')) return true
    if (showPasswordChange && (formData.currentPassword || formData.newPassword)) return true
    return false
  }

  if (loading) return <div className={styles.container}>Cargando perfil...</div>
  if (error) return <div className={styles.container}><p className={styles.error}>{error}</p></div>
  if (!profile) return <div className={styles.container}>No se ha podido cargar la información del usuario. Por favor, inicia sesión de nuevo.</div>

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>Panel de Usuario</h1>
      
      <div className={styles.card}>
        <div className={styles.cardHeader}>
          <h2>Información personal</h2>
        </div>
        
        {personalInfoSuccess && <p className={styles.success}>{personalInfoSuccess}</p>}
        {personalInfoError && <p className={styles.error}>{personalInfoError}</p>}

        <div className={styles.personalInfoContent}>
          <div className={styles.fieldGroup}>
            <label className={styles.label}>Nombre de usuario</label>
            {isEditing ? (
              <input
                type="text"
                className={styles.input}
                value={formData.username}
                onChange={(e) => setFormData({ ...formData, username: e.target.value })}
              />
            ) : (
              <p className={styles.valueText}>{profile.username || 'Sin definir'}</p>
            )}
          </div>

          <div className={styles.fieldGroup}>
            <label className={styles.label}>Correo electrónico</label>
            <p className={styles.valueText}>{profile.email}</p>
          </div>

          <div className={styles.fieldGroup}>
            <label className={styles.label}>Contraseña</label>
            {isEditing ? (
              <div className={styles.passwordEditSection}>
                {!showPasswordChange ? (
                  <button 
                    className={styles.btnSecondary}
                    onClick={() => setShowPasswordChange(true)}
                  >
                    Cambiar contraseña
                  </button>
                ) : (
                  <div className={styles.passwordInputs}>
                    <input
                      type="password"
                      placeholder="Contraseña actual"
                      className={styles.input}
                      value={formData.currentPassword}
                      onChange={(e) => setFormData({ ...formData, currentPassword: e.target.value })}
                    />
                    <input
                      type="password"
                      placeholder="Nueva contraseña"
                      className={styles.input}
                      value={formData.newPassword}
                      onChange={(e) => setFormData({ ...formData, newPassword: e.target.value })}
                    />
                    <button 
                      className={styles.btnLink}
                      onClick={() => {
                        setShowPasswordChange(false)
                        setFormData({ ...formData, currentPassword: '', newPassword: '' })
                      }}
                    >
                      Cancelar cambio
                    </button>
                  </div>
                )}
              </div>
            ) : (
              <p className={styles.valueText}>••••••••••••</p>
            )}
          </div>

          <div className={styles.personalInfoActions}>
            {!isEditing ? (
              <button className={styles.btnSecondary} onClick={handleEditToggle}>
                Editar datos personales
              </button>
            ) : (
              <div className={styles.editButtons}>
                <button 
                  className={styles.btnAction} 
                  onClick={handleSave}
                  disabled={isSaving || !hasChanges()}
                >
                  {isSaving ? 'Guardando...' : 'Guardar cambios'}
                </button>
                <button className={styles.btnCancel} onClick={handleEditToggle} disabled={isSaving}>
                  Cancelar
                </button>
              </div>
            )}
          </div>
        </div>
      </div>


      <div className={styles.card}>
        <h2>Seguridad (2FA)</h2>
        
        {securitySuccess && <p className={styles.success}>{securitySuccess}</p>}
        {securityError && <p className={styles.error}>{securityError}</p>}

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
