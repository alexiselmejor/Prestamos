<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="f" uri="http://universidad.edu/presta/funciones" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Recuperar Contraseña - ${f:h(siteName)}</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/remixicon@4.5.0/fonts/remixicon.css">
    <link rel="stylesheet" href="${ctx}/assets/css/tailwind.css?v=20261113e">
    <!-- SweetAlert2 -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/sweetalert2@11/dist/sweetalert2.min.css">
</head>
<body class="bg-white text-slate-800">

    <div class="flex min-h-screen-ui">

        <!-- Columna izquierda: hero universitario UNCP -->
        <aside class="relative hidden flex-[1_1_52%] flex-col justify-between overflow-hidden bg-[#003d30] bg-cover bg-center px-[4.5rem] py-14 text-white lg:flex" style="background-image:url('${ctx}/assets/img/uncp-campus.jpg')">
            <div class="absolute inset-0 z-[1] bg-[linear-gradient(145deg,rgb(0_68_52/0.93)_0%,rgb(0_52_40/0.88)_50%,rgb(0_38_29/0.94)_100%)]"></div>
            <div class="relative z-[2]">
                <div class="mb-6 inline-flex items-center gap-3.5 rounded-xl border border-white/[0.18] bg-black/[0.22] px-4.5 py-2.5 backdrop-blur-sm">
                    <img src="${ctx}/assets/img/uncp-escudo.png" alt="UNCP" class="size-11 rounded-full border-[1.5px] border-gold bg-black object-contain shadow-[0_4px_10px_rgb(0_0_0/0.2)]">
                    <div>
                        <span class="mb-0.5 block text-xs font-bold uppercase tracking-[1.2px] text-white/85">UNIVERSIDAD NACIONAL DEL CENTRO DEL PERÚ</span>
                        <h1 class="mb-0 text-[1.12rem] font-bold leading-tight text-white">Préstamos de Equipos</h1>
                    </div>
                </div>

                <div class="my-auto pt-4 md:pt-12">
                    <h2 class="mb-5 text-[3.25rem] font-extrabold leading-[1.15] tracking-[-1px] text-white">Acceso seguro<br>al sistema</h2>

                    <p class="mb-9 max-w-[490px] text-base leading-[1.65] text-white/[0.88]">
                        Portal de gestión de préstamos de equipos universitarios de la Oficina de Tecnologías de la Información. Restablecimiento seguro de credenciales.
                    </p>

                    <ul class="m-0 mb-8 list-none p-0">
                        <li class="mb-3.5 flex items-center text-[0.96rem] font-medium text-white/95"><i class="ri-checkbox-circle-line mr-3.5 shrink-0 text-[1.35rem] text-emerald-400"></i><span>Verificación de identidad con correo y DNI</span></li>
                        <li class="mb-3.5 flex items-center text-[0.96rem] font-medium text-white/95"><i class="ri-checkbox-circle-line mr-3.5 shrink-0 text-[1.35rem] text-emerald-400"></i><span>Restablecimiento inmediato de contraseña</span></li>
                        <li class="mb-3.5 flex items-center text-[0.96rem] font-medium text-white/95"><i class="ri-checkbox-circle-line mr-3.5 shrink-0 text-[1.35rem] text-emerald-400"></i><span>Protección y encriptación de datos de usuario</span></li>
                    </ul>
                </div>
            </div>

            <!-- Marca de agua de fondo UNCP -->
            <div class="pointer-events-none absolute -bottom-6 left-12 z-[1] select-none text-[9rem] font-black leading-none tracking-[2px] text-white/5">UNCP</div>

            <div class="relative z-[2] border-t border-white/15 pt-5 text-[0.83rem] text-white/70">&copy; ${anio != null ? anio : '2026'} OTI UNCP - Todos los derechos reservados</div>
        </aside>

        <!-- Columna derecha: formulario de recuperación -->
        <main class="flex flex-[1_1_100%] items-center justify-center bg-white px-6 py-10 lg:flex-[1_1_48%] lg:px-10 lg:py-14">
            <div class="w-full max-w-[440px]">

                <div class="mb-5 flex size-12 items-center justify-center rounded-xl bg-emerald-100 text-2xl text-emerald-800"><i class="ri-shield-check-line"></i></div>

                <div class="mb-6">
                    <h2 class="mb-1.5 text-[2.1rem] font-extrabold tracking-[-0.5px] text-ink">Recuperar Contraseña</h2>
                    <p class="mb-0 text-[0.94rem] text-muted">
                        <c:choose>
                            <c:when test="${paso == 1}">Verifica tu identidad con tu correo y DNI para restablecer tu clave.</c:when>
                            <c:when test="${paso == 2}">Hola <strong>${f:h(nombre)}</strong>, ingresa tu nueva contraseña segura.</c:when>
                            <c:otherwise>Proceso de recuperación completado con éxito.</c:otherwise>
                        </c:choose>
                    </p>
                </div>

                <c:if test="${not empty error}">
                    <div class="alert alert-danger flex items-center mb-6 py-2 px-4 rounded-lg" role="alert">
                        <i class="ri-error-warning-fill me-2 text-xl shrink-0"></i>
                        <div class="text-[0.875em]">${f:h(error)}</div>
                    </div>
                </c:if>

                <c:choose>
                    <%-- PASO 3: ÉXITO --%>
                    <c:when test="${paso == 3}">
                        <div class="alert alert-success flex items-center mb-6 py-4 px-4 rounded-lg" role="alert">
                            <i class="ri-checkbox-circle-fill me-2 text-2xl shrink-0 text-success"></i>
                            <div class="text-[0.875em]">${f:h(exito)}</div>
                        </div>

                        <a href="${ctx}/login" class="btn btn-action-submit w-full">
                            <i class="ri-login-box-line me-1"></i>
                            <span>Iniciar sesión ahora</span>
                        </a>
                    </c:when>

                    <%-- PASO 1: VALIDAR CORREO Y DNI --%>
                    <c:when test="${paso == 1}">
                        <form method="POST" action="${ctx}/recuperar-password" novalidate>
                            <t:csrf/>
                            <input type="hidden" name="accion" value="verificar">

                            <div class="mb-4">
                                <label for="email" class="form-label text-[0.86rem] font-semibold text-slate-700 mb-2">Correo institucional *</label>
                                <div class="input-group-custom flex items-center">
                                    <span class="input-group-text"><i class="ri-mail-line"></i></span>
                                    <input type="email" class="form-control" id="email" name="email"
                                           placeholder="usuario@universidad.edu" required
                                           value="${f:h(emailVal)}" autofocus autocomplete="email">
                                </div>
                            </div>

                            <div class="mb-6">
                                <label for="dni" class="form-label text-[0.86rem] font-semibold text-slate-700 mb-2">DNI (8 dígitos) *</label>
                                <div class="input-group-custom flex items-center">
                                    <span class="input-group-text"><i class="ri-id-card-line"></i></span>
                                    <input type="text" class="form-control" id="dni" name="dni"
                                           maxlength="8" minlength="8" pattern="[0-9]{8}"
                                           placeholder="Ej. 71234567" required
                                           oninput="this.value = this.value.replace(/[^0-9]/g, '');"
                                           value="${f:h(dniVal)}" autocomplete="off">
                                </div>
                                <small class="text-muted block mt-1 text-[0.78rem]">El DNI registrado en tu cuenta.</small>
                            </div>

                            <button type="submit" class="btn btn-action-submit w-full">
                                <i class="ri-search-line me-1"></i>
                                <span>Verificar identidad</span>
                            </button>
                        </form>

                        <div class="mt-7 text-center">
                            <a href="${ctx}/login" class="inline-flex items-center gap-1.5 text-[0.9rem] font-medium text-muted no-underline transition-colors hover:text-primary"><i class="ri-arrow-left-line"></i><span>Volver a Iniciar Sesión</span></a>
                        </div>
                    </c:when>

                    <%-- PASO 2: NUEVA CONTRASEÑA --%>
                    <c:otherwise>
                        <form method="POST" action="${ctx}/recuperar-password" novalidate>
                            <t:csrf/>
                            <input type="hidden" name="accion" value="cambiar">

                            <div class="mb-4">
                                <label for="password" class="form-label text-[0.86rem] font-semibold text-slate-700 mb-2">Nueva Contraseña *</label>
                                <div class="input-group-custom flex items-center">
                                    <span class="input-group-text"><i class="ri-lock-line"></i></span>
                                    <input type="password" class="form-control" id="password" name="password"
                                           required minlength="8" placeholder="Mínimo 8 caracteres"
                                           autofocus autocomplete="new-password">
                                    <button type="button" class="btn-toggle-pwd" id="togglePassword1" title="Mostrar u ocultar contraseña">
                                        <i class="ri-eye-line" id="togglePasswordIcon1"></i>
                                    </button>
                                </div>
                            </div>

                            <div class="mb-6">
                                <label for="password_confirm" class="form-label text-[0.86rem] font-semibold text-slate-700 mb-2">Confirmar Nueva Contraseña *</label>
                                <div class="input-group-custom flex items-center">
                                    <span class="input-group-text"><i class="ri-lock-password-line"></i></span>
                                    <input type="password" class="form-control" id="password_confirm" name="password_confirm"
                                           required minlength="8" placeholder="Repite la contraseña"
                                           autocomplete="new-password">
                                    <button type="button" class="btn-toggle-pwd" id="togglePassword2" title="Mostrar u ocultar contraseña">
                                        <i class="ri-eye-line" id="togglePasswordIcon2"></i>
                                    </button>
                                </div>
                            </div>

                            <button type="submit" class="btn btn-action-submit w-full">
                                <i class="ri-checkbox-circle-line me-1"></i>
                                <span>Restablecer Contraseña</span>
                            </button>
                        </form>

                        <div class="mt-7 text-center">
                            <a href="${ctx}/login" class="inline-flex items-center gap-1.5 text-[0.9rem] font-medium text-muted no-underline transition-colors hover:text-primary"><i class="ri-arrow-left-line"></i><span>Cancelar y volver</span></a>
                        </div>
                    </c:otherwise>
                </c:choose>

            </div>
        </main>

    </div>

    <!-- Script de alternar contraseñas -->
    <!-- SweetAlert2 -->
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>

    <c:if test="${not empty error}">
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            Swal.fire({
                icon: 'error',
                title: '¡Error!',
                html: '<p class="mb-0" style="font-size:0.95rem;">${f:h(error)}</p>',
                confirmButtonText: '<i class="ri-check-line me-1"></i> Entendido',
                confirmButtonColor: '#004d3d',
                customClass: {
                    confirmButton: 'btn btn-primary swal-btn px-6 py-2 font-bold',
                    popup: 'swal-uncp-popup'
                },
                buttonsStyling: false
            });
        });
    </script>
    </c:if>

    <script>
        document.addEventListener('DOMContentLoaded', function () {
            function setupToggle(btnId, inputId, iconId) {
                const btn = document.getElementById(btnId);
                const input = document.getElementById(inputId);
                const icon = document.getElementById(iconId);
                if (btn && input && icon) {
                    btn.addEventListener('click', function () {
                        const isPassword = input.getAttribute('type') === 'password';
                        input.setAttribute('type', isPassword ? 'text' : 'password');
                        icon.classList.toggle('ri-eye-line', !isPassword);
                        icon.classList.toggle('ri-eye-off-line', isPassword);
                    });
                }
            }
            setupToggle('togglePassword1', 'password', 'togglePasswordIcon1');
            setupToggle('togglePassword2', 'password_confirm', 'togglePasswordIcon2');
        });
    </script>
</body>
</html>
