// Cliente HTTP. Cambiar BASE si frontend y backend están separados.
const API_BASE = 'https://cetpro-andina.onrender.com/cetpro-andina/api';
const Api = {
    async req(method, path, body) {
        const opts = { method, headers: { 'Content-Type': 'application/json' } };
        if (body) opts.body = JSON.stringify(body);
        const r = await fetch(API_BASE + path, opts);
        const txt = await r.text();
        const data = txt ? JSON.parse(txt) : null;
        if (!r.ok) throw new Error(data?.error || 'Error ' + r.status);
        return data;
    },
    // alumnos
    alumnos:            ()      => Api.req('GET',    '/alumnos'),
    buscarAlumnos:      q       => Api.req('GET',    '/alumnos?q=' + encodeURIComponent(q)),
    crearAlumno:        a       => Api.req('POST',   '/alumnos', a),
    // módulos
    modulos:            ()      => Api.req('GET',    '/modulos'),
    unidadesDe:         id      => Api.req('GET',    '/modulos/' + id + '/unidades'),
    // certificados
    certificados:       f       => {
        const qs = new URLSearchParams();
        if (f?.tipo) qs.set('tipo', f.tipo);
        if (f?.anio) qs.set('anio', f.anio);
        if (f?.entregado !== undefined && f.entregado !== '') qs.set('entregado', f.entregado);
        return Api.req('GET', '/certificados?' + qs);
    },
    emitirModular:      r => Api.req('POST',  '/certificados/modular', r),
    emitirCapacitacion: r => Api.req('POST',  '/certificados/capacitacion', r),
    marcarImpreso:      (id,v) => Api.req('PATCH', '/certificados/' + id + '/impreso', { impreso: v }),
    marcarEntregado:    (id,v,f) => Api.req('PATCH', '/certificados/' + id + '/entregado', { entregado: v, firmaReceptor: f }),
    anular:             id => Api.req('DELETE', '/certificados/' + id),
};
