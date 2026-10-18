# Guia d'estudi de l'examen — ProTube

> Document viu per preparar l'explicació individual del projecte. S'actualitza
> després de cada canvi de codi. No substitueix el codi: resumeix què fa
> l'aplicació, quines peces es modifiquen i quines decisions s'han de saber justificar.

**Última actualització:** 17 d'octubre de 2026

## 1. Què és ProTube

ProTube és una aplicació web per veure vídeos i, segons la funcionalitat
implementada, registrar-se, iniciar sessió, pujar contingut, editar vídeos, cercar,
comentar i consultar analítiques. El projecte està dividit en un frontend, un backend
i una eina auxiliar que prepara vídeos d'exemple.

L'examen pot demanar explicar com s'ha implementat una funcionalitat, quines capes
afecta, com es prova i quines decisions d'arquitectura o de producte s'han pres.

## 2. Parts del projecte i responsabilitat

### Frontend

El frontend és una SPA feta amb React, TypeScript i Vite. Mostra la interfície,
gestiona els estats de càrrega i error, recull les accions de l'usuari i consulta el
backend. Les variables d'entorn permeten canviar el domini de l'API i dels fitxers
multimèdia sense modificar la interfície.

### Backend

El backend és una API REST amb Spring Boot. Rep peticions HTTP, coordina la lògica de
l'aplicació i obté les dades mitjançant ports i adaptadors. També pot servir fitxers
estàtics i recursos multimèdia del magatzem de vídeos.

El recorregut d'una petició és:

1. El navegador inicia una petició a l'API.
2. Un controlador REST identifica la ruta i la tradueix a una operació d'aplicació.
3. Un servei coordina l'operació i aplica les regles de negoci.
4. Un port de sortida abstrau d'on provenen les dades.
5. Un adaptador de persistència o infraestructura llegeix o desa la informació.
6. La resposta torna al frontend, que actualitza la pantalla.

### Video grabber

L'eina Python descarrega vídeos, crea una miniatura, retalla el fitxer i genera
metadades. Cada vídeo queda representat per tres fitxers amb el mateix identificador:

- `.mp4`: vídeo reproduïble.
- `.webp`: miniatura.
- `.json`: títol, descripció, etiquetes, durada, estadístiques i comentaris.

El backend i l'eina comparteixen el directori d'emmagatzematge. La ruta es configura
amb `ENV_PROTUBE_STORE_DIR` i no s'ha de fixar al codi.

## 3. Tasca del Sprint 1: Browse Video List

La primera tasca implementada és **Browse Video List**. L'objectiu és que la pantalla
principal mostri els vídeos reals preparats pel `videoGrabber`, amb una miniatura i
informació suficient perquè l'usuari pugui descobrir contingut.

El backend ja exposava `GET /api/videos`, però abans només retornava noms de prova en
memòria. Ara el catàleg llegeix el directori configurat, busca els fitxers `.json`,
extreu el títol, la descripció i la durada, i construeix les adreces dels fitxers
`.webp` i `.mp4`. Només publica un vídeo si té les metadades i els dos fitxers
multimèdia necessaris.

La resposta del catàleg conté, per cada vídeo:

- identificador;
- títol i descripció;
- durada;
- URL de la miniatura;
- URL del vídeo.

Les entrades amb JSON invàlid, títol absent o fitxers multimèdia incomplets es descarten
i es registra un avís. Això evita mostrar targetes que després donarien un error 404.
Si el directori no existeix, el catàleg retorna una llista buida i deixa constància del
problema als logs.

Al frontend, `useAllVideos` demana la llista i `VideoGrid` la presenta en targetes
responsives. Cada targeta mostra la miniatura, el títol, la descripció i la durada si
està disponible. La interfície conserva els estats de càrrega, èxit i error.

## 4. Com s'afegeix una funcionalitat nova

Per implementar una història com pujar un vídeo, cercar o comentar:

1. Definir el comportament i els criteris d'acceptació.
2. Decidir les dades, entitats i relacions necessàries.
3. Expressar les regles de negoci al domini.
4. Coordinar l'operació al servei d'aplicació.
5. Afegir l'adaptador de base de dades, fitxers o servei extern.
6. Exposar una API amb validació i errors coherents.
7. Afegir la pantalla i els estats de càrrega/error al frontend.
8. Cobrir la regla, el contracte HTTP i el recorregut visible amb tests.
9. Actualitzar aquesta guia amb què s'ha fet i què cal entendre.

## 5. Punts que cal saber explicar

### Persistència

Les dades en memòria o H2 serveixen per al desenvolupament inicial, però el MVP
requereix una base de dades persistent. Cal saber explicar com les dades sobreviuen a
un reinici, quines entitats es relacionen i com una migració manté l'estructura.

### Autenticació i autorització

No n'hi ha prou amb amagar un botó al frontend. El backend ha d'identificar l'usuari i
comprovar permisos en cada operació protegida. Per exemple, només el propietari d'un
vídeo hauria de poder editar-ne les metadades.

### Errors

Cal diferenciar una entrada invàlida, un recurs inexistent, una operació no autoritzada
i un error intern. El frontend ha de mostrar un estat comprensible i no un èxit fals.

### Tests i CI

Els tests unitaris comproven regles aïllades; els tests de controlador comproven el
contracte HTTP; els tests del frontend comproven la interfície; i els tests E2E
comproven els recorreguts principals des del navegador. La CI executa build, tests,
lint i cobertura.

### Docker i configuració

La instal·lació ha de poder arrencar en un entorn net amb Docker Compose. Les
credencials, rutes i dominis arriben per variables d'entorn i no es guarden al
repositori.

## 6. Preguntes de repàs

- Què passa des que l'usuari obre la pantalla principal fins que veu les targetes?
- Quina responsabilitat té cada capa del backend?
- Per què el catàleg no ha de llegir fitxers directament des del controlador?
- Què passa si falta el `.json`, el `.mp4` o el `.webp`?
- Com canviaries l'adaptador de fitxers per PostgreSQL sense refer el frontend?
- Quines entitats caldrien per a usuaris, vídeos, comentaris, respostes i likes?
- Com impediries que un usuari editi el vídeo d'una altra persona?
- Quins estats ha de mostrar una pantalla mentre una petició carrega o falla?
- Quins tests afegiries abans de donar per acabada una funcionalitat?

## 7. Registre de canvis per estudiar

| Data | Canvi funcional | Què cal entendre |
|---|---|---|
| 17/10/2026 | Es crea la guia i s'estableix que s'ha de mantenir després de cada canvi de codi. | L'examen avalua la comprensió del funcionament i de les decisions, no la memorització de línies. |
| 17/10/2026 | S'implementa el catàleg real de vídeos de l'US-04. | El backend llegeix els triples del magatzem, valida que siguin utilitzables i el frontend els mostra en targetes. |
| 17/10/2026 | Es corregeix la configuració de Jest del frontend. | Jest 30 utilitza una configuració CommonJS amb `babel-jest`, aprofitant Babel per transformar TypeScript i React i evitant la incompatibilitat amb `ts-jest` 29. |
| 17/10/2026 | Es corregeix el build de la imatge Docker del backend. | La construcció de producció no executa tests ni el control JaCoCo, perquè la cobertura es valida a la CI; així Docker pot crear el jar i la CI continua protegint la qualitat. |
| 18/10/2026 | S'implementa el detall i la reproducció de vídeos de l'US-05. | El backend consulta un vídeo per identificador a través dels ports i retorna 404 si no existeix; el frontend obre una vista amb reproductor HTML5, poster, metadades i estats d'error. |
| 18/10/2026 | S'afegeixen proves automàtiques dels estats de reproducció i s'estableix el llindar de cobertura al 90%. | Cada canvi de codi ha d'executar proves automàtiques; els tests cobreixen càrrega, èxit, error, 404, identificadors insegurs i metadades invàlides. |
| 18/10/2026 | La cobertura passa a mesurar globalment el codi d'aplicació frontend i backend. | Jest inclou tots els fitxers de l'aplicació i JaCoCo exigeix com a mínim un 90% global d'instruccions, línies, branques i mètodes. |
| 18/10/2026 | Es documenta el prompt aprovat per mantenir la cobertura global del 90%. | El prompt `prompts/10-enforce-global-coverage.md` obliga a executar totes les suites, mesurar el codi d'aplicació complet i fer fallar el build si qualsevol mètrica baixa del llindar. |

## 8. Temes pendents

- Registre, autenticació, sessions i protecció de rutes.
- Persistència real d'usuaris, vídeos, comentaris i analítiques.
- Pujada i edició de vídeos amb control del propietari.
- Cerca i paginació.
- Comentaris, respostes i likes.
- Analítiques del contingut propi.
- Tests E2E dels recorreguts principals.
- Docker Compose i configuració de producció.
