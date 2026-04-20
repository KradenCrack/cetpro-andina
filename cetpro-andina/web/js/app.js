// ============================================================
//  App Frontend - CETPRO ANDINA
// ============================================================

const $ = sel => document.querySelector(sel);
const $$ = sel => document.querySelectorAll(sel);

// Estado global simple
const state = {
    modulos: [],
    unidadesActuales: [],
    alumnoSeleccionado: null,
};

// ==================== NAVEGACIÓN ====================
$$('.tab').forEach(btn => {
    btn.addEventListener('click', () => {
        $$('.tab').forEach(b => b.classList.remove('active'));
        $$('.view').forEach(v => v.classList.remove('active'));
        btn.classList.add('active');
        $('#view-' + btn.dataset.view).classList.add('active');
        // recargar datos al cambiar de vista
        cargarVista(btn.dataset.view);
    });
});

function cargarVista(vista) {
    if (vista === 'dashboard') cargarDashboard();
    if (vista === 'alumnos') cargarAlumnos();
    if (vista === 'emitir') prepararFormulario();
    if (vista === 'certificados') cargarCertificados();
}

// ==================== TOAST ====================
function toast(msg, tipo = 'ok') {
    const t = $('#toast');
    t.textContent = msg;
    t.className = 'toast show ' + tipo;
    setTimeout(() => t.classList.remove('show'), 3500);
}

// ==================== DASHBOARD ====================
async function cargarDashboard() {
    try {
        const [alumnos, certs] = await Promise.all([Api.alumnos(), Api.certificados()]);
        $('#stat-alumnos').textContent = alumnos.length;
        $('#stat-certs').textContent = certs.length;
        $('#stat-impresos').textContent = certs.filter(c => c.impreso).length;
        $('#stat-entregados').textContent = certs.filter(c => c.entregado).length;
        $('#stat-pendientes').textContent = certs.filter(c => !c.entregado).length;

        const ultimos = certs.slice(0, 10);
        $('#tabla-ultimos').innerHTML = renderTablaCerts(ultimos, true);
        bindCertAcciones();
    } catch (e) { toast(e.message, 'err'); }
}

// ==================== ALUMNOS ====================
async function cargarAlumnos() {
    try {
        const lista = await Api.alumnos();
        renderTablaAlumnos(lista);
    } catch (e) { toast(e.message, 'err'); }
}

function renderTablaAlumnos(lista) {
    if (!lista.length) {
        $('#tabla-alumnos').innerHTML = '<p>Sin alumnos registrados.</p>';
        return;
    }
    let html = `<table class="tabla"><thead><tr>
        <th>DNI</th><th>Nombre completo</th><th>Teléfono</th>
        <th>Total cert.</th><th>Entregados</th><th>Pendientes</th><th>Estado</th>
    </tr></thead><tbody>`;
    for (const a of lista) {
        const total = a.totalCertificados || 0;
        const ent = a.certificadosEntregados || 0;
        const pen = a.certificadosPendientes || 0;
        let badge;
        if (total === 0) badge = '<span class="badge gris">Sin certificados</span>';
        else if (pen > 0) badge = '<span class="badge warn">Con pendientes</span>';
        else badge = '<span class="badge ok">Al día</span>';
        html += `<tr>
            <td>${a.dni || ''}</td>
            <td>${a.nombreCompleto || a.apellidoPaterno || ''}</td>
            <td>${a.telefono || '-'}</td>
            <td>${total}</td>
            <td>${ent}</td>
            <td>${pen}</td>
            <td>${badge}</td>
        </tr>`;
    }
    html += '</tbody></table>';
    $('#tabla-alumnos').innerHTML = html;
}

$('#buscar-alumno').addEventListener('input', async e => {
    const q = e.target.value.trim();
    try {
        const lista = q.length >= 2 ? await Api.buscarAlumnos(q) : await Api.alumnos();
        renderTablaAlumnos(lista);
    } catch (err) { toast(err.message, 'err'); }
});

// Modal nuevo alumno
$('#btn-nuevo-alumno').addEventListener('click', () => $('#modal-alumno').classList.add('active'));
$('#cerrar-modal').addEventListener('click', () => $('#modal-alumno').classList.remove('active'));

$('#form-alumno').addEventListener('submit', async e => {
    e.preventDefault();
    try {
        const a = {
            dni: $('#a-dni').value.trim(),
            tipoDocumento: $('#a-tipo').value,
            nombres: $('#a-nombres').value.trim(),
            apellidoPaterno: $('#a-ap').value.trim(),
            apellidoMaterno: $('#a-am').value.trim(),
            fechaNacimiento: $('#a-fn').value || null,
            genero: $('#a-gen').value || null,
            telefono: $('#a-tel').value.trim(),
            email: $('#a-email').value.trim(),
            direccion: $('#a-dir').value.trim(),
        };
        await Api.crearAlumno(a);
        toast('Alumno registrado', 'ok');
        $('#form-alumno').reset();
        $('#modal-alumno').classList.remove('active');
        cargarAlumnos();
    } catch (err) { toast(err.message, 'err'); }
});

// ==================== FORMULARIO CERTIFICADO ====================
async function prepararFormulario() {
    try {
        state.modulos = await Api.modulos();
        const sel = $('#idModulo');
        sel.innerHTML = '<option value="">-- seleccione --</option>';
        for (const m of state.modulos) {
            sel.innerHTML += `<option value="${m.idModulo}">
                [${m.codigoModulo}] ${m.nombre} (${m.nombrePrograma})
            </option>`;
        }
        $('#fechaEmision').valueAsDate = new Date();
    } catch (e) { toast(e.message, 'err'); }
}

// Cambio de tipo de certificado
$$('input[name="tipo-cert"]').forEach(r => {
    r.addEventListener('change', () => {
        const tipo = r.value;
        $('#campos-capacitacion').style.display = (tipo === 'CAPACITACION') ? 'grid' : 'none';
        $('#campos-modular').style.display = (tipo === 'MODULAR') ? 'grid' : 'none';
    });
});

// Autocomplete alumno
let timerBusqueda;
$('#input-alumno').addEventListener('input', e => {
    clearTimeout(timerBusqueda);
    const q = e.target.value.trim();
    if (q.length < 2) { $('#sug-alumno').classList.remove('active'); return; }
    timerBusqueda = setTimeout(async () => {
        try {
            const lista = await Api.buscarAlumnos(q);
            const cont = $('#sug-alumno');
            if (!lista.length) { cont.classList.remove('active'); return; }
            cont.innerHTML = lista.slice(0, 8).map(a =>
                `<div class="item" data-id="${a.idAlumno}" data-nombre="${a.nombreCompleto}">
                    ${a.nombreCompleto}<small>DNI: ${a.dni}</small>
                </div>`).join('');
            cont.classList.add('active');
            cont.querySelectorAll('.item').forEach(it => {
                it.addEventListener('click', () => {
                    $('#input-alumno').value = it.dataset.nombre;
                    $('#idAlumno').value = it.dataset.id;
                    state.alumnoSeleccionado = parseInt(it.dataset.id);
                    cont.classList.remove('active');
                });
            });
        } catch (err) { toast(err.message, 'err'); }
    }, 250);
});
document.addEventListener('click', e => {
    if (!e.target.closest('.autocomplete')) $('#sug-alumno').classList.remove('active');
});

// Cargar unidades al seleccionar módulo
$('#idModulo').addEventListener('change', async e => {
    const id = e.target.value;
    if (!id) return;
    try {
        const unidades = await Api.unidadesDe(id);
        state.unidadesActuales = unidades;
        renderUnidades(unidades);
        // Autocompletar ciclo
        const mod = state.modulos.find(m => m.idModulo == id);
        if (mod && mod.cicloFormativo) $('#cicloFormativo').value = mod.cicloFormativo;
    } catch (err) { toast(err.message, 'err'); }
});

function renderUnidades(unidades) {
    const tbody = $('#tabla-unidades tbody');
    tbody.innerHTML = '';
    for (const u of unidades) {
        const tr = document.createElement('tr');
        tr.innerHTML = `<td>${u.nombre}</td>
            <td>${u.creditos}</td>
            <td>${u.horas}</td>
            <td><input type="number" min="0" max="20" class="nota-unidad" data-id="${u.idUnidad}" data-cred="${u.creditos}"></td>`;
        tbody.appendChild(tr);
    }
    // Recalcular promedio al escribir
    $$('.nota-unidad').forEach(inp => inp.addEventListener('input', actualizarPromedio));
    $('#promedio-preview').textContent = '-';
}

function actualizarPromedio() {
    let sumN = 0, sumC = 0;
    $$('.nota-unidad').forEach(inp => {
        const v = parseFloat(inp.value);
        const c = parseFloat(inp.dataset.cred);
        if (!isNaN(v) && !isNaN(c)) { sumN += v * c; sumC += c; }
    });
    $('#promedio-preview').textContent = sumC > 0 ? (sumN / sumC).toFixed(2) : '-';
}

// Submit del formulario
$('#form-cert').addEventListener('submit', async e => {
    e.preventDefault();
    const tipo = document.querySelector('input[name="tipo-cert"]:checked').value;
    const req = {
        idAlumno: parseInt($('#idAlumno').value),
        idModulo: parseInt($('#idModulo').value),
        fechaEmision: $('#fechaEmision').value,
        fechaInicioCurso: $('#fechaInicioCurso').value || null,
        fechaFinCurso: $('#fechaFinCurso').value || null,
        cicloFormativo: $('#cicloFormativo').value,
        lugarEmision: $('#lugarEmision').value,
        observaciones: $('#observaciones').value,
    };
    if (!req.idAlumno) { toast('Seleccione un alumno', 'err'); return; }
    if (!req.idModulo) { toast('Seleccione un módulo', 'err'); return; }

    try {
        let resultado;
        if (tipo === 'CAPACITACION') {
            req.notaFinal = parseInt($('#notaFinal').value);
            req.duracionHoras = parseInt($('#duracionHoras').value);
            resultado = await Api.emitirCapacitacion(req);
        } else {
            req.notasPorUnidad = [];
            $$('.nota-unidad').forEach(inp => {
                const v = parseInt(inp.value);
                if (!isNaN(v)) req.notasPorUnidad.push({
                    idUnidad: parseInt(inp.dataset.id),
                    calificacion: v,
                });
            });
            resultado = await Api.emitirModular(req);
        }
        toast('Certificado emitido: ' + resultado.codigoInstitucional, 'ok');
        $('#form-cert').reset();
        $('#tabla-unidades tbody').innerHTML = '';
        $('#promedio-preview').textContent = '-';
    } catch (err) { toast(err.message, 'err'); }
});

// ==================== LISTA CERTIFICADOS ====================
async function cargarCertificados() {
    try {
        const filtros = {
            tipo: $('#filtro-tipo').value,
            anio: $('#filtro-anio').value,
            entregado: $('#filtro-entregado').value,
        };
        const certs = await Api.certificados(filtros);
        $('#tabla-certificados').innerHTML = renderTablaCerts(certs, false);
        bindCertAcciones();
    } catch (e) { toast(e.message, 'err'); }
}

$('#btn-filtrar').addEventListener('click', cargarCertificados);

function renderTablaCerts(certs, compacto) {
    if (!certs.length) return '<p>No hay certificados.</p>';
    let html = `<table class="tabla"><thead><tr>
        <th>Código</th><th>Tipo</th><th>Alumno</th><th>Módulo</th>
        <th>Fecha</th><th>Nota/Prom.</th>
        <th>Impreso</th><th>Entregado</th>`;
    if (!compacto) html += '<th>Acciones</th>';
    html += '</tr></thead><tbody>';

    for (const c of certs) {
        const nota = c.tipoCertificado === 'CAPACITACION'
            ? (c.notaFinalTexto || c.notaFinal || '-')
            : (c.promedioFinal || '-');
        html += `<tr data-id="${c.idCertificado}">
            <td><b>${c.codigoInstitucional}</b></td>
            <td><span class="badge info">${c.tipoCertificado}</span></td>
            <td>${c.alumnoNombre || '-'}<br><small>${c.alumnoDni || ''}</small></td>
            <td>${c.moduloNombre || '-'}<br><small>${c.cicloFormativo || ''}</small></td>
            <td>${c.fechaEmision || '-'}</td>
            <td>${nota}</td>
            <td class="chk-cell">
                <input type="checkbox" class="chk-impreso" data-id="${c.idCertificado}" ${c.impreso ? 'checked' : ''}>
            </td>
            <td class="chk-cell">
                <input type="checkbox" class="chk-entregado" data-id="${c.idCertificado}" ${c.entregado ? 'checked' : ''}>
            </td>`;
        if (!compacto) {
            html += `<td class="acciones">
                <button class="btn-imprimir" data-id="${c.idCertificado}">🖨</button>
                <button class="btn-anular danger" data-id="${c.idCertificado}">Anular</button>
            </td>`;
        }
        html += '</tr>';
    }
    return html + '</tbody></table>';
}

function bindCertAcciones() {
    $$('.chk-impreso').forEach(chk => {
        chk.addEventListener('change', async () => {
            try {
                await Api.marcarImpreso(parseInt(chk.dataset.id), chk.checked);
                toast(chk.checked ? 'Marcado impreso' : 'Desmarcado', 'ok');
                cargarDashboard();
            } catch (e) { toast(e.message, 'err'); chk.checked = !chk.checked; }
        });
    });
    $$('.chk-entregado').forEach(chk => {
        chk.addEventListener('change', async () => {
            try {
                let firma = null;
                if (chk.checked) {
                    firma = prompt('Firma del receptor (nombre completo):');
                    if (!firma) { chk.checked = false; return; }
                }
                await Api.marcarEntregado(parseInt(chk.dataset.id), chk.checked, firma);
                toast(chk.checked ? 'Entregado' : 'Desmarcado', 'ok');
                cargarDashboard();
            } catch (e) { toast(e.message, 'err'); chk.checked = !chk.checked; }
        });
    });
    $$('.btn-imprimir').forEach(btn => {
        btn.addEventListener('click', () => {
            const id = btn.dataset.id;
            window.open(API_BASE + '/certificados/imprimir/' + id, '_blank');
        });
    });
    $$('.btn-anular').forEach(btn => {
        btn.addEventListener('click', async () => {
            if (!confirm('¿Anular este certificado?')) return;
            try {
                await Api.anular(parseInt(btn.dataset.id));
                toast('Anulado', 'ok');
                cargarCertificados();
            } catch (e) { toast(e.message, 'err'); }
        });
    });
}

// ==================== INICIO ====================
document.addEventListener('DOMContentLoaded', () => cargarDashboard());
