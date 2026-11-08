/* ============================================================
   Frontend del analizador de tickets.
   Habla con la API de Spring Boot: POST /analizar y GET /historial.
   ============================================================ */

// Si la pagina se sirve desde Spring Boot (puerto 8080) usamos rutas relativas.
// Si se abre desde otro lugar (por ejemplo Live Server en 5500) apuntamos al backend.
const API = location.port === "8081" ? "" : "http://localhost:8081";

const texto = document.getElementById("texto");
const btnAnalizar = document.getElementById("btnAnalizar");
const cargando = document.getElementById("cargando");
const resultado = document.getElementById("resultado");
const historial = document.getElementById("historial");

/* ---------- Utilidades ---------- */

// Evita inyectar HTML: el texto del ticket y la respuesta del LLM no son confiables.
function escapar(valor = "") {
    return String(valor)
        .replace(/&/g, "&amp;").replace(/</g, "&lt;")
        .replace(/>/g, "&gt;").replace(/"/g, "&quot;");
}

function formatearTiempo(horas) {
    if (horas < 24) return `${horas} horas`;
    const dias = (horas / 24).toFixed(1).replace(".0", "");
    return `${horas} horas (≈ ${dias} día${dias === "1" ? "" : "s"})`;
}

function badgePrioridad(prioridad) {
    const clase = ["alta", "media", "baja"].includes(prioridad) ? prioridad : "";
    return `<span class="badge ${clase}">${escapar(prioridad)}</span>`;
}

/* ---------- Analizar ---------- */

async function analizar() {
    const ticket = texto.value.trim();

    if (ticket.length < 10) {
        alert("Escribe el ticket con al menos 10 caracteres.");
        return;
    }

    cargando.classList.remove("oculto");
    resultado.innerHTML = "";
    btnAnalizar.disabled = true;

    try {
        const respuesta = await fetch(`${API}/analizar`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ texto: ticket })
        });

        const datos = await respuesta.json().catch(() => ({}));
        if (!respuesta.ok) throw new Error(datos.error || "Error del servidor.");

        mostrarResultado(datos);
        cargarHistorial();
    } catch (error) {
        console.error(error);
        const mensaje = error instanceof TypeError
            ? "No se pudo conectar con el servidor. Verifica que Spring Boot esté en ejecución."
            : error.message;
        resultado.innerHTML = `<div class="error">❌ ${escapar(mensaje)}</div>`;
    } finally {
        cargando.classList.add("oculto");
        btnAnalizar.disabled = false;
    }
}

/* ---------- Render del resultado ---------- */

function renderAnalisis(a) {
    const confianza = Math.round((a.confianza ?? 0) * 100);
    return `
        <div class="tarjeta-analisis">
            <p class="etiqueta">✦ Predicción basada en casos históricos similares</p>
            <div class="datos">
                <div class="dato"><small>Categoría</small><strong>${escapar(a.categoria)}</strong></div>
                <div class="dato"><small>Prioridad</small>${badgePrioridad(a.prioridad)}</div>
                <div class="dato"><small>Área sugerida</small><strong>${escapar(a.area_sugerida)}</strong></div>
                <div class="dato"><small>Tiempo estimado</small><strong>${escapar(formatearTiempo(a.tiempo_estimado_horas))}</strong></div>
                <div class="dato">
                    <small>Confianza</small><strong>${confianza}%</strong>
                    <div class="barra"><div class="progreso" style="width:${confianza}%"></div></div>
                </div>
            </div>
            <p class="justificacion">${escapar(a.justificacion)}</p>
        </div>`;
}

function renderSimilar(caso, idsCitados) {
    const citado = idsCitados.includes(caso.id);
    return `
        <div class="similar ${citado ? "citado" : ""}">
            <div class="cabecera">
                <span class="texto">${escapar(caso.texto)}${citado ? '<span class="cita">usado por el modelo</span>' : ""}</span>
                <span class="porcentaje">${caso.similitud_porcentaje}%</span>
            </div>
            <div class="meta">
                caso:${caso.id} · ${escapar(caso.categoria)} · ${escapar(caso.area)} ·
                resuelto en ${caso.horas_resolucion} h
            </div>
            ${badgePrioridad(caso.prioridad)}
        </div>`;
}

function mostrarResultado(datos) {
    const similares = datos.similares ?? [];
    const idsCitados = datos.analisis?.casos_usados ?? [];

    const principal = datos.analisis
        ? renderAnalisis(datos.analisis)
        : `<div class="aviso">⚠️ ${escapar(datos.mensaje || "No se pudo generar una predicción.")}</div>`;

    const bloqueSimilares = similares.length
        ? `<div class="subtitulo">
               <h2>Tickets similares que respaldan la predicción</h2>
               <span>${similares.length} coincidencias</span>
           </div>
           ${similares.map(c => renderSimilar(c, idsCitados)).join("")}`
        : "";

    resultado.innerHTML = principal + bloqueSimilares;
}

/* ---------- Historial ---------- */

async function cargarHistorial() {
    try {
        const respuesta = await fetch(`${API}/historial`);
        if (!respuesta.ok) throw new Error();
        const filas = await respuesta.json();

        historial.innerHTML = filas.length
            ? filas.map(f => `
                <div class="fila-historial">
                    <div class="meta">
                        ${new Date(f.creadoEn).toLocaleString("es")} ·
                        ${escapar(f.categoria)} · ${escapar(f.areaSugerida)} ·
                        ${escapar(f.tiempoEstimadoHoras)} h
                    </div>
                    ${badgePrioridad(f.prioridad)}
                    <div>${escapar(f.textoTicket)}</div>
                </div>`).join("")
            : '<p class="vacio">Todavía no has analizado ningún ticket.</p>';
    } catch {
        historial.innerHTML = '<p class="vacio">No se pudo cargar el historial.</p>';
    }
}

/* ---------- Eventos ---------- */

btnAnalizar.addEventListener("click", analizar);

// Ctrl + Enter dentro del textarea también analiza
texto.addEventListener("keydown", e => {
    if (e.key === "Enter" && (e.ctrlKey || e.metaKey)) analizar();
});

document.querySelectorAll(".ejemplo").forEach(boton => {
    boton.addEventListener("click", () => {
        texto.value = boton.dataset.texto;
        analizar();
    });
});

cargarHistorial();
