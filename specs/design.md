# Guía de diseño UI/UX — CakeTrack

> Fuente: `DESIGN.md` (Redbrick Coffee, *Style Reference*), tomado como guía oficial de CakeTrack.
> Implementación: `src/main/resources/static/css/caketrack.css`.
> Esta guía es obligatoria para toda vista Thymeleaf nueva o modificada (ver `AGENTS.md`).

---

## 1. Concepto

Estilo editorial impreso en tinta roja sobre papel blanco cálido. El rojo `#e82c2a` es el único acento cromático: aparece en logo, navegación, etiquetas de sección, bordes y acciones. El fondo es blanco, las tarjetas son gris suave y no hay sombras.

Los títulos usan una serif fina (peso 300, nunca negrita) y el texto de interfaz una sans humanista con espaciado positivo.

## 2. Tokens de color

| Nombre | Valor | Token | Uso |
|---|---|---|---|
| Brick Red | `#e82c2a` | `--color-brick-red` | Acento: bordes de acciones, enlaces, etiquetas, títulos display |
| Press Black | `#212529` | `--color-press-black` | Texto de párrafos y cuerpo |
| Cream White | `#ffffff` | `--color-cream-white` | Lienzo de página, fondo de navegación, base de tarjetas |
| Soft Grey | `#f2f2f2` | `--color-soft-grey` | Superficie de tarjetas y paneles elevados |
| Carbon | `#121212` | `--color-carbon` | Pie de página y paneles inversos |

No se usa un segundo color de marca. Estados como "disponible" o "activo" se expresan con Carbon, y las alertas con Brick Red.

## 3. Tokens tipográficos

| Familia | Rol | Pesos | Tamaños | Tracking |
|---|---|---|---|---|
| Surt (sustituto: **Inter**) | Cuerpo, navegación, botones, etiquetas de tarjeta, toda la UI funcional | 400 | 15, 20, 30, 35 px | 0.013em (15 px), 0.017em (20 px), 0.03em (30 px), 0.04em (35 px) |
| Editorial Old (sustituto: **Playfair Display**) | Solo titulares display | 300 | 35 px en la app (100–150 px reservados para portadas) | -0.005em |
| GTStandard-M (sustituto: **JetBrains Mono**) | Microetiquetas en mayúsculas, anuncios, insignias | 400 | 10 px | 0.06em |

Interlineado: cuerpo 1.5, subtítulos 1.2, display 0.99–1.2.

## 4. Espaciado, radios y layout

- **Escala de espaciado:** 5, 8, 10, 12, 15, 20, 30, 50, 117, 234 px.
- **Radios:**
  - Tarjetas e imágenes: 25 px
  - Botones y campos de texto: 50 px
  - Enlaces: 3.75 px
- **Layout:**
  - Separación entre secciones: 50 px
  - Padding de tarjeta: 30 px
  - Separación de elementos: 10 px
- **Densidad:** compacta.
- **Responsivo:** móvil primero con la grilla de Bootstrap 5. En pantallas menores de 576 px los títulos bajan a 30 px.

## 5. Superficies y elevación

| Nivel | Nombre | Valor | Uso |
|---|---|---|---|
| 0 | Lienzo blanco | `#ffffff` | Fondo de todas las secciones |
| 1 | Tarjeta gris | `#f2f2f2` | Tarjetas y bloques de contenido |
| 2 | Banda roja | `#e82c2a` | Anuncio superior y bandas de color |
| 3 | Panel carbón | `#121212` | Pie de página y paneles inversos |

**Sin sombras.** La elevación se logra con el radio de 25 px y el cambio de superficie de blanco a gris.

## 6. Componentes

- **Anuncio superior:** banda roja a todo el ancho, texto blanco en mayúsculas de 10 px con tracking 0.06em. Alto aproximado de 30 px.
- **Marca:** "CakeTrack" en Surt 400, 20 px, tracking 0.017em, color rojo.
- **Enlace de navegación:** Surt 400, 15 px, rojo, sin subrayado ni fondo. Al pasar el cursor solo cambia la opacidad.
- **Etiqueta de sección:** 10 px en mayúsculas, rojo, tracking 0.06em, con filete de 1px debajo.
- **Enlace con flecha:** 10–15 px, rojo, alineado a la derecha, con radio de 3.75 px.
- **Botón de acción:** solo contorno de 1px rojo, radio de 50 px, padding 10px × 20px, sin relleno.
- **Campo de texto:** contorno rojo de 1px, radio de 50 px, padding 10px × 20px.
- **Tarjeta:** fondo `#f2f2f2`, radio de 25 px, padding de 30 px, sin sombra.
- **Tabla:** encabezados en microetiqueta roja con filete inferior; filas separadas por líneas finas.
- **Insignia:** 10 px en mayúsculas, radio de 50 px. Alerta en rojo sólido, estado normal en carbón.
- **Pie de página:** panel carbón con texto blanco en microetiqueta.

## 7. Reglas de uso

### Hacer
- Usar Brick Red en cada texto interactivo, etiqueta de sección y acción de contorno.
- Títulos display con peso 300 y tamaño adecuado; nunca negrita.
- Aplicar el tracking indicado a cada tamaño de Surt.
- Radio de 25 px en tarjetas, 50 px en botones y campos, 3.75 px en enlaces.
- Mantener las tarjetas de producto sobre `#f2f2f2` con 30 px de padding y sin sombra.

### No hacer
- No introducir botones con fondo sólido. La única superficie de acción es el contorno rojo.
- No poner en negrita los titulares. El peso 300 es parte de la identidad.
- No añadir un segundo color de marca.
- No usar sombras en tarjetas ni imágenes.
- No usar texto blanco sobre `#f2f2f2`.
- No usar fotografía de producto sobre fondo blanco tipo catálogo de comercio electrónico.

## 8. Adaptación en CakeTrack

Decisiones tomadas al aplicar la guía a la aplicación, que no aparecen en el original:

| Tema | Decisión | Motivo |
|---|---|---|
| Tipografías | Se usan Inter, Playfair Display y JetBrains Mono de Google Fonts | Las fuentes originales son comerciales; la guía lista estos sustitutos |
| Tamaños de display | Títulos de página a 35 px (no 100–150 px) | 100–150 px son para portadas; la app es una herramienta de gestión |
| Botón primario | Contorno rojo, igual que cualquier otra acción | La guía prohíbe botones rellenos |
| Botón activo de filtro | Contorno carbón con subrayado | Distingue el filtro activo sin relleno |
| Estados de disponibilidad | Carbón para "Disponible/Activo", rojo para alertas | La paleta no incluye verde ni ámbar |
| Filetes de tabla | Carbón al 12 % de opacidad | Derivado de Carbon para líneas finas, no es un color nuevo |
| Pie de página | Panel carbón | Guía: pie en panel oscuro o claro |

### Contraste

El rojo `#e82c2a` sobre blanco tiene una relación de contraste de **4.35:1**. Cumple el mínimo de 3:1 para texto grande, pero queda debajo del 4.5:1 requerido para texto pequeño. Por eso el texto de cuerpo se mantiene en `#212529` y el rojo se reserva para etiquetas, enlaces y acciones. Si se necesita cumplimiento estricto de accesibilidad, revisar este token en un ADR.

## 9. Mapa de archivos

| Archivo | Responsabilidad |
|---|---|
| `src/main/resources/static/css/caketrack.css` | Tokens en `:root`, sobrescritura de componentes Bootstrap |
| `src/main/resources/templates/fragments/layout.html` | Fuentes, Bootstrap Icons, hoja de estilos, anuncio, barra de navegación y pie |
| `src/main/resources/templates/*.html` | Vistas que incluyen el anuncio y el layout |
