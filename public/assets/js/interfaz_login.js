/**
 * MODAL DE LOGIN
 * Gestiona la ventana modal para login/registro
 */

// Crear el modal al cargar el DOM
document.addEventListener('DOMContentLoaded', () => {
    crearModalLogin();
    verificarParametrosURL();
    configurarEventos();
});

/**
 * Crea la estructura HTML del modal de login
 */
function crearModalLogin() {
    const modalHTML = `
        <div id="modal-overlay" class="modal-overlay">
            <div id="modal-login" class="modal-container">
                <button id="modal-close" class="modal-close">&times;</button>
                
                <div class="modal-content">
                    <h2 id="modal-title">Iniciar Sesión</h2>
                    
                    <div id="modal-error" class="modal-error" style="display: none;"></div>
                    
                    <form id="form-login" action="${BASE_URL}auth/login.php" method="POST">
                        <input type="hidden" name="referer" value="${window.location.pathname + window.location.search}">
                        
                        <div class="form-row">
                            <label for="login-email">Correo electrónico:</label>
                            <div class="form-field">
                                <input type="email" id="login-email" name="email" required>
                            </div>
                        </div>
                        
                        <div class="form-row">
                            <label for="login-password">Contraseña:</label>
                            <div class="form-field">
                                <input type="password" id="login-password" name="password" required>
                            </div>
                        </div>
                        
                        <!-- Contenedor para hCaptcha (se añadirá posteriormente) -->
                        <div id="hcaptcha-container" class="form-row form-row--center">
                            <!-- El widget de hCaptcha irá aquí -->
                        </div>
                        
                        <div class="form-row form-row--center">
                            <button type="submit" class="btn-submit">Continuar</button>
                        </div>
                    </form>
                    
                    <div class="modal-footer">
                        <p id="modal-toggle-text">
                            ¿No tienes cuenta? <a href="#" id="toggle-register">Regístrate aquí</a>
                        </p>
                    </div>
                </div>
            </div>
        </div>
    `;
    
    // Insertar el modal en el body
    document.body.insertAdjacentHTML('beforeend', modalHTML);
    
    // Inyectar estilos CSS
    inyectarEstilosModal();
}

/**
 * Inyecta los estilos CSS del modal
 */
function inyectarEstilosModal() {
    const styles = `
        <style>
            /* Overlay oscuro de fondo */
            .modal-overlay {
                display: none;
                position: fixed;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background: rgba(0, 0, 0, 0.7);
                z-index: 9999;
                justify-content: center;
                align-items: center;
                animation: fadeIn 0.3s ease;
            }
            
            .modal-overlay.active {
                display: flex;
            }
            
            @keyframes fadeIn {
                from { opacity: 0; }
                to { opacity: 1; }
            }
            
            /* Contenedor del modal */
            .modal-container {
                background: linear-gradient(180deg, rgba(255,250,243,0.98) 0%, rgba(255,244,232,0.95) 100%);
                width: 90%;
                max-width: 650px;
                border-radius: 12px;
                box-shadow: 0 20px 60px rgba(27,18,18,0.3);
                border: 1px solid rgba(156,122,61,0.15);
                position: relative;
                animation: slideDown 0.3s ease;
                max-height: 90vh;
                overflow-y: auto;
            }
            
            @keyframes slideDown {
                from {
                    transform: translateY(-50px);
                    opacity: 0;
                }
                to {
                    transform: translateY(0);
                    opacity: 1;
                }
            }
            
            /* Botón cerrar */
            .modal-close {
                position: absolute;
                top: 15px;
                right: 20px;
                background: none;
                border: none;
                font-size: 32px;
                color: #5b4632;
                cursor: pointer;
                line-height: 1;
                transition: color 0.2s ease, transform 0.2s ease;
                z-index: 10;
            }
            
            .modal-close:hover {
                color: #b88628;
                transform: rotate(90deg);
            }
            
            /* Contenido del modal */
            .modal-content {
                padding: 40px 50px;
                font-family: 'Cinzel', Georgia, serif;
            }
            
            .modal-content h2 {
                text-align: center;
                color: #5b4632;
                font-size: 32px;
                margin: 0 0 30px 0;
                font-weight: 600;
            }
            
            /* Mensajes de error */
            .modal-error {
                background: rgba(220, 53, 69, 0.1);
                border: 1px solid rgba(220, 53, 69, 0.3);
                color: #a02633;
                padding: 12px 16px;
                border-radius: 8px;
                margin-bottom: 20px;
                font-size: 14px;
                font-family: 'Inter', sans-serif;
            }
            
            /* Formulario */
            #form-login .form-row {
                display: flex;
                align-items: center;
                gap: 18px;
                margin: 16px 0;
            }
            
            #form-login .form-row--center {
                justify-content: center;
            }
            
            #form-login label {
                width: 35%;
                text-align: right;
                padding-right: 12px;
                color: #5b4632;
                font-weight: 500;
                font-size: 15px;
            }
            
            #form-login .form-field {
                width: 65%;
            }
            
            #form-login input[type="email"],
            #form-login input[type="password"] {
                width: 100%;
                background-color: #fff6f1;
                border: 1px solid rgba(150,130,110,0.2);
                border-radius: 8px;
                box-shadow: inset 0 2px 6px rgba(255,255,255,0.6);
                transition: border-color 140ms ease, box-shadow 140ms ease;
                padding: 12px 14px;
                font-size: 15px;
                font-family: 'Inter', sans-serif;
            }
            
            #form-login input[type="email"]:focus,
            #form-login input[type="password"]:focus {
                outline: none;
                border-color: #a97c44;
                box-shadow: 0 8px 20px rgba(169,124,68,0.08);
            }
            
            /* Contenedor hCaptcha */
            #hcaptcha-container {
                min-height: 80px;
                display: flex;
                justify-content: center;
                align-items: center;
                margin: 20px 0;
            }
            
            /* Botón submit */
            .btn-submit {
                background: linear-gradient(180deg, #b88628 0%, #8a5f1a 100%);
                color: #fff;
                border: 1px solid rgba(0,0,0,0.12);
                border-radius: 10px;
                cursor: pointer;
                box-shadow: 0 6px 18px rgba(138,95,26,0.18);
                transition: transform 140ms ease, box-shadow 140ms ease, filter 120ms ease;
                padding: 14px 36px;
                font-size: 17px;
                font-weight: 600;
                font-family: 'Cinzel', Georgia, serif;
                margin-top: 10px;
            }
            
            .btn-submit:hover {
                filter: brightness(1.06);
                transform: translateY(-2px);
            }
            
            .btn-submit:active {
                transform: translateY(1px);
                box-shadow: inset 0 2px 6px rgba(0,0,0,0.12);
            }
            
            /* Footer del modal */
            .modal-footer {
                text-align: center;
                margin-top: 20px;
                padding-top: 20px;
                border-top: 1px solid rgba(150,130,110,0.15);
            }
            
            .modal-footer p {
                margin: 0;
                font-family: 'Inter', sans-serif;
                font-size: 14px;
                color: #5b4632;
            }
            
            .modal-footer a {
                color: #b88628;
                text-decoration: none;
                font-weight: 600;
                transition: color 0.2s ease;
            }
            
            .modal-footer a:hover {
                color: #8a5f1a;
                text-decoration: underline;
            }
            
            /* Modal simple - más ancho */
            .modal-container-simple {
                max-width: 780px;
            }
            
            .modal-container-simple .modal-content {
                text-align: center;
            }
            
            /* Responsive */
            @media (max-width: 600px) {
                .modal-content {
                    padding: 25px 20px;
                }
                
                #form-login .form-row {
                    flex-direction: column;
                    align-items: stretch;
                    gap: 8px;
                }
                
                #form-login label {
                    width: 100%;
                    text-align: left;
                    padding-right: 0;
                }
                
                #form-login .form-field {
                    width: 100%;
                }
            }
        </style>
    `;
    
    document.head.insertAdjacentHTML('beforeend', styles);
}

/**
 * Configura los eventos del modal
 */
function configurarEventos() {
    const overlay = document.getElementById('modal-overlay');
    const closeBtn = document.getElementById('modal-close');
    const form = document.getElementById('form-login');
    const toggleLink = document.getElementById('toggle-register');
    
    // Cerrar modal al hacer clic en el botón X
    closeBtn.addEventListener('click', cerrarModal);
    
    // Cerrar modal al hacer clic fuera del contenedor
    overlay.addEventListener('click', (e) => {
        if (e.target === overlay) {
            cerrarModal();
        }
    });
    
    // Cerrar modal con la tecla ESC
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && overlay.classList.contains('active')) {
            cerrarModal();
        }
    });
    
    // Alternar entre login y registro
    toggleLink.addEventListener('click', (e) => {
        e.preventDefault();
        alternarModo();
    });
    
    // Configurar acción del formulario según modo (login/registro)
    form.addEventListener('submit', (e) => {
        // El formulario se enviará normalmente, pero aquí puedes añadir validaciones
        actualizarReferer();
    });
}

/**
 * Abre el modal de login
 */
function abrirModal() {
    const overlay = document.getElementById('modal-overlay');
    overlay.classList.add('active');
    document.body.style.overflow = 'hidden'; // Prevenir scroll del body
}

/**
 * Cierra el modal de login
 */
function cerrarModal() {
    const overlay = document.getElementById('modal-overlay');
    overlay.classList.remove('active');
    document.body.style.overflow = ''; // Restaurar scroll
    
    // Limpiar error si existe
    ocultarError();
    
    // Limpiar parámetros de la URL
    const url = new URL(window.location);
    url.searchParams.delete('error');
    url.searchParams.delete('modal');
    window.history.replaceState({}, '', url);
}

/**
 * Muestra un mensaje de error en el modal
 */
function mostrarError(mensaje, tipo = 'error') {
    const errorDiv = document.getElementById('modal-error');
    errorDiv.textContent = mensaje;
    errorDiv.style.display = 'block';
    
    // Cambiar estilo según tipo
    if (tipo === 'success') {
        errorDiv.style.background = 'rgba(40, 167, 69, 0.1)';
        errorDiv.style.borderColor = 'rgba(40, 167, 69, 0.3)';
        errorDiv.style.color = '#155724';
    } else {
        errorDiv.style.background = 'rgba(220, 53, 69, 0.1)';
        errorDiv.style.borderColor = 'rgba(220, 53, 69, 0.3)';
        errorDiv.style.color = '#a02633';
    }
}

/**
 * Oculta el mensaje de error
 */
function ocultarError() {
    const errorDiv = document.getElementById('modal-error');
    errorDiv.style.display = 'none';
}

/**
 * Verifica los parámetros de la URL para abrir el modal automáticamente
 */
function verificarParametrosURL() {
    const urlParams = new URLSearchParams(window.location.search);
    const modal = urlParams.get('modal');
    
    if (modal === 'login' || modal === 'register') {
        abrirModal();
        
        // Si es modo registro, cambiar el formulario
        if (modal === 'register') {
            const form = document.getElementById('form-login');
            const esLogin = form.action.includes('login.php');
            if (esLogin) {
                alternarModo();
            }
        }
        
        // Mostrar mensaje de error si existe
        const error = urlParams.get('error');
        const success = urlParams.get('success');
        
        if (success === 'registro_exitoso') {
            mostrarError('¡Registro exitoso! Ahora puedes iniciar sesión', 'success');
        } else if (error === 'campos_vacios') {
            mostrarError('Por favor completa todos los campos');
        } else if (error === 'login_incorrecto') {
            mostrarError('Email o contraseña incorrectos');
        } else if (error === 'email_invalido') {
            mostrarError('El formato del email no es válido');
        } else if (error === 'password_invalida') {
            mostrarError('La contraseña debe tener entre 8 y 20 caracteres sin espacios');
        } else if (error === 'password_corta') {
            mostrarError('La contraseña debe tener al menos 8 caracteres');
        } else if (error === 'email_existe') {
            mostrarError('Este email ya está registrado');
        } else if (error === 'error_servidor') {
            mostrarError('Error del servidor. Por favor intenta más tarde');
        } else if (error === 'registro_fallido') {
            mostrarError('Error al registrar. Inténtalo de nuevo');
        } else if (error === 'error_sistema') {
            mostrarError('Error del sistema. Por favor intenta más tarde');
        }
    }
}

/**
 * Actualiza el campo hidden referer antes de enviar el formulario
 */
function actualizarReferer() {
    const refererInput = document.querySelector('input[name="referer"]');
    if (refererInput) {
        // Limpiar parámetros de error de la URL actual
        const url = new URL(window.location);
        url.searchParams.delete('error');
        url.searchParams.delete('modal');
        refererInput.value = url.pathname + url.search;
    }
}

/**
 * Alterna entre modo login y registro
 */
function alternarModo() {
    const form = document.getElementById('form-login');
    const title = document.getElementById('modal-title');
    const btnSubmit = document.querySelector('.btn-submit');
    const toggleText = document.getElementById('modal-toggle-text');
    const toggleLink = document.getElementById('toggle-register');
    
    // Obtener modo actual
    const esLogin = form.action.includes('login.php');
    
    if (esLogin) {
        // Cambiar a modo REGISTRO
        form.action = BASE_URL + 'auth/register.php';
        title.textContent = 'Crear Cuenta';
        btnSubmit.textContent = 'Registrarse';
        toggleText.innerHTML = '¿Ya tienes cuenta? <a href="#" id="toggle-register">Inicia sesión aquí</a>';
    } else {
        // Cambiar a modo LOGIN
        form.action = BASE_URL + 'auth/login.php';
        title.textContent = 'Iniciar Sesión';
        btnSubmit.textContent = 'Continuar';
        toggleText.innerHTML = '¿No tienes cuenta? <a href="#" id="toggle-register">Regístrate aquí</a>';
    }
    
    // Re-configurar el evento del nuevo enlace
    document.getElementById('toggle-register').addEventListener('click', (e) => {
        e.preventDefault();
        alternarModo();
    });
    
    // Limpiar errores
    ocultarError();
}

/**
 * Función pública para abrir el modal desde otros scripts
 * Uso: abrirModalLogin()
 */
window.abrirModalLogin = abrirModal;

/**
 * MENÚ HAMBURGUESA RESPONSIVE
 * Funciones para controlar el menú móvil
 */

/**
 * Toggle del menú principal en móvil
 */
function toggleMenu() {
    const menu = document.getElementById('menudiv');
    menu.classList.toggle('active');
    
    // Cerrar el menú al hacer clic fuera de él
    if (menu.classList.contains('active')) {
        document.addEventListener('click', cerrarMenuFuera);
    } else {
        document.removeEventListener('click', cerrarMenuFuera);
    }
}

/**
 * Cierra el menú si se hace clic fuera de él
 */
function cerrarMenuFuera(event) {
    const menu = document.getElementById('menudiv');
    const toggle = document.getElementById('menu-toggle');
    
    if (!menu.contains(event.target) && !toggle.contains(event.target)) {
        menu.classList.remove('active');
        document.removeEventListener('click', cerrarMenuFuera);
    }
}

/**
 * Toggle de submenús en móvil
 */
function toggleSubmenu(event) {
    if (window.innerWidth <= 768) {
        event.preventDefault();
        const li = event.target.closest('li');
        li.classList.toggle('active');
    }
}

/**
 * Cierra el menú al hacer clic en un enlace (excepto submenús)
 */
document.addEventListener('DOMContentLoaded', () => {
    const menuLinks = document.querySelectorAll('#menu a:not([onclick*="toggleSubmenu"])');
    menuLinks.forEach(link => {
        link.addEventListener('click', () => {
            if (window.innerWidth <= 768) {
                document.getElementById('menudiv').classList.remove('active');
                document.removeEventListener('click', cerrarMenuFuera);
            }
        });
    });
});

// Funciones globales
window.toggleMenu = toggleMenu;
window.toggleSubmenu = toggleSubmenu;

/*  
Abre un modal simple con título y contenido
 */
function abrirModalSimple(contenido) {
    // Eliminar modal anterior si existe
    const existente = document.getElementById('modal-simple');
    if (existente) existente.remove();

    // Crear modal
    const modal = `
        <div id="modal-simple" class="modal-overlay">
            <div class="modal-container modal-container-simple">
                <button class="modal-close" onclick="cerrarModalSimple()">&times;</button>
                <div class="modal-content">
                    ${contenido}
                </div>
            </div>
        </div>
    `;

    document.body.insertAdjacentHTML('beforeend', modal);
    
    const overlay = document.getElementById('modal-simple');
    setTimeout(() => overlay.classList.add('active'), 10);

    // Cerrar al hacer clic fuera
    overlay.addEventListener('click', (e) => {
        if (e.target === overlay) cerrarModalSimple();
    });
}

function cerrarModalSimple() {
    const modal = document.getElementById('modal-simple');
    if (modal) {
        modal.classList.remove('active');
        setTimeout(() => modal.remove(), 300);
    }
}
