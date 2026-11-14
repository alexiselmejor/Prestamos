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
    <title>Iniciar Sesión - ${f:h(siteName)}</title>
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
                <!-- Insignia UNCP y sistema -->
                <div class="mb-6 inline-flex items-center gap-3.5 rounded-xl border border-white/[0.18] bg-black/[0.22] px-4.5 py-2.5 backdrop-blur-sm">
                    <img src="${ctx}/assets/img/uncp-escudo.png" alt="UNCP" class="size-11 rounded-full border-[1.5px] border-gold bg-black object-contain shadow-[0_4px_10px_rgb(0_0_0/0.2)]">
                    <div>
                        <span class="mb-0.5 block text-xs font-bold uppercase tracking-[1.2px] text-white/85">UNIVERSIDAD NACIONAL DEL CENTRO DEL PERÚ</span>
                        <h1 class="mb-0 text-[1.12rem] font-bold leading-tight text-white">Préstamos de Equipos</h1>
                    </div>
                </div>

                <!-- Título y descripción principal -->
                <div class="my-auto pt-4 md:pt-12">
                    <h2 class="mb-5 text-[3.25rem] font-extrabold leading-[1.15] tracking-[-1px] text-white">Acceso seguro<br>al sistema</h2>

                    <p class="mb-9 max-w-[490px] text-base leading-[1.65] text-white/[0.88]">
                        Portal de gestión de préstamos de equipos universitarios de la Oficina de Tecnologías de la Información. Solo personal autorizado.
                    </p>

                    <ul class="m-0 mb-8 list-none p-0">
                        <li class="mb-3.5 flex items-center text-[0.96rem] font-medium text-white/95"><i class="ri-checkbox-circle-line mr-3.5 shrink-0 text-[1.35rem] text-emerald-400"></i><span>Solicitudes y aprobaciones en línea</span></li>
                        <li class="mb-3.5 flex items-center text-[0.96rem] font-medium text-white/95"><i class="ri-checkbox-circle-line mr-3.5 shrink-0 text-[1.35rem] text-emerald-400"></i><span>Control de inventario y devoluciones</span></li>
                        <li class="mb-3.5 flex items-center text-[0.96rem] font-medium text-white/95"><i class="ri-checkbox-circle-line mr-3.5 shrink-0 text-[1.35rem] text-emerald-400"></i><span>Trazabilidad completa por equipo</span></li>
                    </ul>
                </div>
            </div>

            <!-- Marca de agua de fondo UNCP -->
            <div class="pointer-events-none absolute -bottom-6 left-12 z-[1] select-none text-[9rem] font-black leading-none tracking-[2px] text-white/5">UNCP</div>

            <div class="relative z-[2] border-t border-white/15 pt-5 text-[0.83rem] text-white/70">&copy; ${anio != null ? anio : '2026'} OTI UNCP - Todos los derechos reservados</div>
        </aside>

        <!-- Columna derecha: formulario de inicio de sesión -->
        <main class="flex flex-[1_1_100%] items-center justify-center bg-white px-6 py-10 lg:flex-[1_1_48%] lg:px-10 lg:py-14">
            <div class="w-full max-w-[440px]">

                <div class="mb-6">
                    <h2 class="mb-1.5 text-[2.1rem] font-extrabold tracking-[-0.5px] text-ink">Iniciar sesión</h2>
                    <p class="mb-0 text-[0.94rem] text-muted">Ingresa con tu correo institucional y contraseña para continuar.</p>
                </div>

                <c:if test="${not empty error}">
                    <div class="alert alert-danger flex items-center mb-6 py-2 px-4 rounded-lg" role="alert">
                        <i class="ri-error-warning-fill me-2 text-xl shrink-0"></i>
                        <div class="text-[0.875em]">${f:h(error)}</div>
                    </div>
                </c:if>

                <form method="POST" action="${ctx}/login" novalidate>
                    <t:csrf/>

                    <!-- Correo institucional -->
                    <div class="mb-4">
                        <label for="email" class="form-label text-[0.86rem] font-semibold text-slate-700 mb-2">Correo institucional</label>
                        <div class="input-group-custom flex items-center">
                            <span class="input-group-text"><i class="ri-mail-line"></i></span>
                            <input type="email" class="form-control" id="email" name="email"
                                   placeholder="usuario@universidad.edu" value="${f:h(param.email)}"
                                   required autofocus autocomplete="username">
                        </div>
                    </div>

                    <!-- Contraseña -->
                    <div class="mb-6">
                        <label for="password" class="form-label text-[0.86rem] font-semibold text-slate-700 mb-2">Contraseña</label>
                        <div class="input-group-custom flex items-center">
                            <span class="input-group-text"><i class="ri-lock-line"></i></span>
                            <input type="password" class="form-control" id="password" name="password"
                                   placeholder="••••••••" required autocomplete="current-password">
                            <button type="button" class="btn-toggle-pwd" id="togglePassword" title="Mostrar u ocultar contraseña">
                                <i class="ri-eye-line" id="togglePasswordIcon"></i>
                            </button>
                        </div>
                    </div>

                    <button type="submit" class="btn btn-login-submit w-full">
                        <i class="ri-login-box-line text-xl"></i>
                        <span>Iniciar sesión</span>
                    </button>
                </form>

                <!-- Enlace de recuperación -->
                <div class="mt-8 flex items-center justify-center gap-1.5 text-center text-[0.9rem] text-muted">
                    <span>¿Problemas para acceder?</span>
                    <a href="${ctx}/recuperar-password" class="font-semibold text-primary no-underline transition-colors hover:text-[#003026] hover:underline">Contacta a la Mesa de Ayuda</a>
                </div>

            </div>
        </main>

    </div>

    <!-- Alternar visibilidad de la contraseña -->
    <!-- SweetAlert2 -->
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>

    <c:if test="${not empty error}">
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            Swal.fire({
                icon: 'error',
                title: '¡Error de acceso!',
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
            const toggleBtn = document.getElementById('togglePassword');
            const passwordInput = document.getElementById('password');
            const icon = document.getElementById('togglePasswordIcon');

            if (toggleBtn && passwordInput && icon) {
                toggleBtn.addEventListener('click', function () {
                    const isPassword = passwordInput.getAttribute('type') === 'password';
                    passwordInput.setAttribute('type', isPassword ? 'text' : 'password');
                    icon.classList.toggle('ri-eye-line', !isPassword);
                    icon.classList.toggle('ri-eye-off-line', isPassword);
                });
            }
        });
    </script>
</body>
</html>
