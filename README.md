# Tropiezo (Fabric 1.21.1)

Mod de Minecraft donde, corriendo, te tropezás y te caés al piso:

- **Subir un desnivel chico**: tierra arada, caminos hechos con pala, camas, alfombras, etc.
- **Flores**: pasar corriendo por flores (probabilidad configurable).
- **Caerte de un bloque**: aterrizar corriendo después de bajar un bloque o más.

Escaleras y losas **no** te hacen tropezar (se puede activar en la config).

Al tropezar: te empuja hacia adelante, quedás acostado boca abajo unos 2 segundos sin poder moverte, y después te levantás. En multijugador todos ven la caída.

> Etapa 1: la caída es una animación de "cara al piso". Todavía no usa ragdoll físico.

## Cómo conseguir el .jar sin instalar nada (GitHub Actions)

1. Creá una cuenta en https://github.com (gratis) si no tenés.
2. Creá un repositorio nuevo (botón **New**). Ponele cualquier nombre, puede ser privado.
3. Descomprimí este zip. En el repositorio nuevo tocá **uploading an existing file** y arrastrá **todo el contenido** de la carpeta (incluida la carpeta `.github`, `src`, `build.gradle`, etc.). Tocá **Commit changes**.
4. Andá a la pestaña **Actions**. Va a aparecer "Compilar mod" corriendo (tarda unos 3-5 minutos la primera vez).
5. Cuando termine con tilde verde, entrá a esa ejecución y abajo, en **Artifacts**, descargá `tropiezo-jar`. Adentro está el `.jar`.
6. Si sale una cruz roja, abrí la ejecución, copiá el error y pasámelo.

## Cómo usarlo en el juego

Necesitás Minecraft 1.21.1 con **Fabric Loader** y **Fabric API** (el .jar de Tropiezo va en la carpeta `mods`, junto con el de Fabric API). Para multijugador, el mod tiene que estar en el servidor y en todos los clientes.

## Comandos

- `/tropiezo`: te tropezás vos (para probar).
- `/tropiezo reload`: recarga la config (requiere permiso 2).

## Configuración

`config/tropiezo.json` se crea sola la primera vez que abrís el juego con el mod. Ahí podés cambiar la duración en el piso, el empujón, las probabilidades de cada disparador, si hace falta correr (sprint), etc.
