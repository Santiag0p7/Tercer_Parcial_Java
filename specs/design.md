# Guía de diseño UI/UX — CakeTrack

> Versión 2.0. Reemplaza la guía anterior basada en Redbrick Coffee.
> Referencia de estructura: londonterrariums.com (secciones, ritmo y jerarquía). Los colores, textos y contenidos son propios de CakeTrack.
> Implementación: `src/main/resources/static/css/caketrack.css`.
> Esta guía es obligatoria para toda vista Thymeleaf nueva o modificada (ver `AGENTS.md`).

---

## 1. Concepto

CakeTrack es la puerta de entrada de una pastelería artesanal. La landing presenta la pastelería, muestra el catálogo y permite ingresar al sistema. Una vez dentro, cada rol ve un panel con sus herramientas.

El estilo es cálido y cercano: fondo crema, acentos fresa y caramelo, tarjetas blancas con esquinas redondeadas y títulos en serif. Se prioriza que el usuario entienda rápido qué puede hacer.

## 2. Estructura de la landing

De arriba abajo:

1. **Anuncio superior:** banda fresa a todo el ancho con un mensaje corto.
2. **Barra de navegación fija:** logo, enlaces a las secciones y botón "Ingresar".
3. **Hero:** titular, texto de apoyo, dos acciones (ver catálogo e ingresar) y una imagen grande.
4. **Historia:** texto y imagen en dos columnas, alternando el lado.
5. **Catálogo:** tarjetas con los productos disponibles, tomados de la base de datos.
6. **Equipo:** cinco tarjetas, una por rol, con lo que hace cada uno.
7. **Acceso:** formulario de ingreso con los mensajes de error y de éxito.
8. **Pie de página:** marca, nota de privacidad (Ley 1581 de 2012).

Cada sección tiene una etiqueta pequeña en mayúsculas, un titular y un párrafo corto.

## 3. Tokens de color

| Nombre | Valor | Token | Uso |
|---|---|---|---|
| Crema | `#fffaf4` | `--color-crema` | Fondo de página |
| Blanco | `#ffffff` | `--color-blanco` | Tarjetas, barra de navegación, formularios |
| Vainilla | `#f6eee4` | `--color-vainilla` | Secciones alternas y superficies suaves |
| Fresa | `#b5345a` | `--color-berry` | Acción principal, enlaces, titulares |
| Fresa oscuro | `#8f2444` | `--color-berry-oscuro` | Estado hover de la acción principal |
| Cacao | `#3b2a22` | `--color-cacao` | Texto de cuerpo y pie de página |
| Pistacho | `#5f7f45` | `--color-pistacho` | Estado positivo (activo, disponible) |
| Caramelo | `#c98b3d` | `--color-caramelo` | Etiquetas de sección |

Contraste principal: fresa sobre blanco 5.8:1, cacao sobre crema más de 12:1. Ambos cumplen WCAG AA para texto normal.

## 4. Tipografía

| Familia | Rol | Pesos |
|---|---|---|
| Playfair Display | Titulares (hero, secciones, títulos de página) | 400 |
| Inter | Cuerpo, navegación, botones, formularios, etiquetas | 400, 500, 600 |

Escala:

- Titular de hero: `clamp(2.4rem, 5vw, 4rem)`, interlineado 1.08.
- Titular de sección: `clamp(1.8rem, 3vw, 2.5rem)`.
- Etiqueta: 0.8rem, mayúsculas, espaciado 0.14em, caramelo, peso 600.
- Texto destacado: 1.1rem, cacao.
- Cuerpo: 16px, interlineado 1.6.

## 5. Radios, espaciado y sombras

- **Tarjetas e imágenes:** radio 24px.
- **Botones y campos:** radio 50px (forma de píldora).
- **Enlaces de ayuda:** radio 6px.
- **Sombra suave:** `0 10px 30px rgba(59, 42, 34, 0.08)`. Se usa solo en tarjetas, nunca en texto.
- **Secciones:** 72px de padding vertical en escritorio y 48px en móvil.
- **Tarjetas:** 28px de padding en escritorio.

## 6. Componentes

- **Anuncio superior:** fondo fresa, texto blanco, 0.85rem.
- **Barra de navegación:** fondo blanco con sombra leve, marca en titular serif fresa.
- **Botón principal (`btn-berry`):** relleno fresa, texto blanco, forma de píldora. Es la única acción rellena de cada pantalla.
- **Botón secundario (`btn-outline-berry`):** contorno fresa, relleno al pasar el cursor.
- **Botones de estado:** contorno pistacho para acciones positivas, contorno fresa para acciones de baja.
- **Tarjeta de producto:** imagen (o bloque ilustrado), insignia de tipo, nombre, descripción corta y precio en fresa.
- **Tarjeta de rol:** icono fresa, nombre del rol y una frase de qué hace.
- **Tarjeta de acceso:** blanca, con mensajes de error o éxito arriba del formulario.
- **Enlace con flecha:** fresa, sin subrayado, con una flecha al final.
- **Insignias:** forma de píldora. "Reponer" en fresa, "Normal" en pistacho.
- **Pie de página:** fondo cacao, texto crema.

## 7. Layout y responsividad

- Grilla de Bootstrap 5, con diseño móvil primero.
- En escritorio las secciones de texto e imagen van en dos columnas de la misma anchura. En móvil se apilan.
- Las tarjetas de producto pasan de 3 columnas (escritorio) a 2 (tableta) y 1 (móvil).
- Los títulos y bloques de imagen se reducen en pantallas menores de 992px.

## 8. Imágenes

La landing y el panel incorporan bloques ilustrados con iconos de Bootstrap Icons mientras no haya fotografías. Para usar fotos reales:

1. Guardar los archivos en `src/main/resources/static/img/`.
2. Reemplazar el contenido del bloque (`div.bloque-imagen` o `div.tarjeta-producto-imagen`) por una etiqueta `img` con `th:src="@{/img/nombre.jpg}"`.

Las fotos deben ser cálidas, tomadas desde arriba o a la altura de la mesa, con productos reales de la pastelería. Evitar fotos de catálogo sobre fondo blanco.

## 9. Reglas de uso

### Hacer
- Usar fresa para acciones principales, enlaces y titulares. Un solo botón relleno por pantalla.
- Mantener el texto de cuerpo en cacao, nunca en fresa.
- Agrupar el contenido en tarjetas blancas sobre el fondo crema.
- Mostrar en la landing solo productos con disponibilidad real.
- Explicar en cada tarjeta de rol lo que hace ese rol, con el alcance de la versión actual.

### No hacer
- No mostrar en la landing funciones que aún no existen (pedidos personalizados, panel de cocina, domicilios), como si ya estuvieran disponibles.
- No usar más de dos colores de acento en una misma pantalla.
- No poner texto blanco sobre el fondo caramelo ni sobre el crema.
- No ocultar el formulario de ingreso detrás de otra pantalla. La landing es la única entrada al sistema.
- No cambiar atributos `th:*`, `sec:authorize` ni `name` de formularios al ajustar estilos.

## 10. Mapa de archivos

| Archivo | Responsabilidad |
|---|---|
| `static/css/caketrack.css` | Tokens en `:root` y componentes de Bootstrap ajustados |
| `templates/fragments/layout.html` | Encabezado, anuncio, barras de navegación, pie y fuentes |
| `templates/landing.html` | Landing pública con acceso al sistema |
| `templates/inicio.html` | Panel de trabajo según el rol |
| `templates/catalogo.html`, `templates/admin/*.html` | Vistas internas del sistema |
