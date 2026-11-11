# Guia d'estudi de l'examen — ProTube

> Document viu per preparar l'explicació individual del projecte. S'actualitza
> després de cada canvi de codi. No substitueix el codi: resumeix què fa
> l'aplicació, quines peces es modifiquen i quines decisions s'han de saber justificar.

**Última actualització:** 9 de novembre de 2026

### Subscripcions d'usuaris

Les subscripcions permeten que un usuari autenticat segueixi altres perfils. El
backend utilitza el port d'entrada `ManageSubscriptionUseCase`, el servei
`SubscriptionService`, el port de sortida `SubscriptionPort` i l'adaptador JDBC
`JdbcSubscriptionAdapter`; la taula `subscriptions` manté una fila única per
parella i impedeix subscriure's a un mateix.

Els endpoints autenticats permeten consultar l'estat, subscriure's, cancel·lar la
subscripció i llistar els perfils seguits. El frontend mostra el botó
`Subscribe`/`Unsubscribe` als perfils públics i afegeix la ruta `#subscriptions`
per consultar la llista, amb enllaços de tornada als perfils.

A la vista de detall d'un vídeo també es carrega el perfil de l'autor a partir de
`ownerId`. Sota el reproductor es mostra el seu avatar i nom com a enllaç al
perfil, juntament amb el control de subscripció; l'estat i els canvis reutilitzen
els mateixos endpoints autenticats.

El catàleg PostgreSQL incorpora també el `username` públic de l'autor en el resum
del vídeo. Això permet mostrar el nom real sota el reproductor encara que la resta
del perfil públic no estigui disponible.
La consulta agrupada inclou aquest camp en `GROUP BY`, perquè PostgreSQL exigeix
agrupar totes les columnes seleccionades que no són agregades.

### Mode fosc persistent

El mode fosc és una preferència pròpia de l'usuari, no només un estat visual local.
El backend reutilitza `user_preferences.dark_mode` mitjançant el port d'entrada
`ManageUserPreferencesUseCase`, el servei `UserPreferencesService`, el port de sortida
`UserPreferencesPersistencePort` i el repositori JDBC. Els endpoints autenticats
`GET/PUT /api/preferences/me` permeten llegir i canviar el valor.

El frontend carrega la preferència quan existeix una sessió, aplica les classes
`light-mode` o `dark-mode` al contenidor principal i mostra un botó accessible per
activar o desactivar el tema. El valor torna a l'estat anterior si la persistència
falla; els usuaris anònims continuen amb el tema clar per defecte.

### Favorite Videos

La funcionalitat de vídeos favorits permet que cada usuari autenticat desi o elimini
un vídeo i consulti la seva llista personal. El backend segueix l'arquitectura
hexagonal: `ManageVideoFavoriteUseCase` és el port d'entrada, `VideoFavoriteService`
valida la sessió i l'existència del vídeo, i `VideoFavoritePort` amaga l'adaptador
JDBC que desa una fila única per parella usuari-vídeo. La migració normalitza
`favorites.video_id` a text per mantenir compatibilitat amb els identificadors UUID
de producció i els identificadors numèrics del catàleg de fitxers.

El frontend consulta l'estat del favorit a la vista de detall, mostra un control
per afegir-lo o eliminar-lo i ofereix la ruta autenticada `#favorites`, que recupera
els vídeos guardats. Les proves cobreixen les regles del servei, la persistència,
els controladors i els estats de càrrega, error i canvi del control React.

### Qualitat del codi del perfil i format del frontend

El frontend manté els fitxers TypeScript/TSX amb el format definit per Prettier,
incloent els tipus d'estat de `App` i les proves de navegació i perfil. A més,
`ProfilePage` declara totes les dependències del seu `useEffect`, mentre que
`App` estabilitza el callback que sincronitza el nom i l'avatar de la sessió.
Això evita avisos de linter i reexecucions inesperades de l'efecte, sense canviar
el comportament funcional. Les proves i la compilació del frontend continuen
verificant aquest flux.

### Alineació del control de perfil públic

El checkbox `Public profile` del formulari de perfil té una regla d'estil pròpia.
Els estils de mida completa dels camps de text ja no s'apliquen als checkbox, que
ara mantenen una mida compacta i s'alineen amb el seu text. Això evita que el
checkbox ocupi tota la fila i desplaci visualment l'etiqueta.

### Importació segura del `video-store`

L'importador de producció només incorpora metadades del video-grabber. Les
metadades generades per una pujada d'usuari contenen `ownerId` i s'ignoren durant
l'arrencada de Docker, perquè aquests vídeos ja tenen el seu registre propi a
PostgreSQL. Aquesta separació evita que cada reinici creï una segona còpia del
mateix vídeo amb origen `VIDEO_GRABBER`.

### Cobertura dels adaptadors JDBC i de l'importador

Els adaptadors JDBC de vídeos, publicació, visualitzacions, comentaris i analítiques
ara tenen proves de les rutes d'èxit, errors d'identificador, autorització i
persistència. L'importador del `video-store` també es prova amb fitxers temporals per
verificar que ignora metadades incompletes o invàlides i importa correctament tags,
categories i miniatures WebP o PNG. Aquestes proves utilitzen mocks de les dependències
JDBC i no introdueixen H2 com a substitut de PostgreSQL; el context de l'aplicació i
la integració continuen funcionant amb PostgreSQL. L'objectiu és mantenir el llindar
global de JaCoCo per sobre del 90%.

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

### Actualització del nom mostrat

Quan un usuari desa el seu perfil, el frontend actualitza també l'estat global de
la sessió amb el `displayName` retornat pel backend. La capçalera mostra aquest
nom com a salutació (`Hi, ...`), mentre que el `username` es conserva per a
l'identificador públic amb el format `@username`. Això evita que la interfície
mostri el nom tècnic del compte quan l'usuari ha configurat un nom diferent.
Si el perfil té un avatar, també es mostra una versió petita del mateix avatar al
costat de la salutació; si no n'hi ha cap, es mostra la inicial del nom.
En carregar el perfil propi, el frontend sincronitza el nom i l'avatar amb
l'estat global de la sessió, de manera que la capçalera també reflecteix els
valors ja desats abans de tornar al catàleg.

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
responsives. En pantalles grans es mostren tres targetes per fila per aprofitar millor
l'espai i fer les miniatures més llegibles; en pantalles mitjanes es redueix a dues i
en mòbil a una. Cada targeta mostra la miniatura, el títol amb una mida compacta i la durada
amb una mida encara més petita si està disponible;
la descripció queda reservada a `VideoDetail`, on l'usuari pot llegir-la després
d'obrir el vídeo. La interfície conserva els estats de càrrega, èxit i error.

La segona iteració de l'US-04 afegeix paginació al contracte `GET /api/videos`:
`page` identifica la pàgina començant per zero i `size` limita la mida entre 1 i 100.
La resposta inclou el contingut i la informació `totalElements`, `totalPages` i
`hasNext`. El frontend carrega els primers 12 vídeos i ofereix "Load more videos"
quan hi ha més contingut, acumulant les pàgines sense substituir les targetes ja
visibles. Això evita carregar tot el directori de vídeos en una sola petició i
compleix el criteri d'acceptació per a catàlegs més grans.

### US-06: pujada de vídeos

La pujada es protegeix amb l'atribut de sessió `authenticatedUserId`; si no hi ha una
sessió vàlida, el backend respon 401 i el frontend mostra el login en lloc del formulari.
El formulari demana títol, descripció, un fitxer MP4 de fins a 100 MB i una miniatura
JPEG, PNG, WebP o GIF. El port d'entrada
`UploadVideoUseCase` rep un model de fitxer independent de Spring i el servei valida els
camps abans d'escriure; mai utilitza el nom original del fitxer com a ruta.

El backend crea un identificador UUID i escriu el vídeo, la miniatura proporcionada i el
JSON de metadades en fitxers temporals dins del directori configurat. Només mou els tres
fitxers a les seves rutes públiques quan totes les operacions han acabat; si hi ha un
error, elimina temporals i fitxers parcials. L'adaptador PostgreSQL desa també la clau i
el MIME real de la miniatura a `video_assets`, mentre que el catàleg continua acceptant
les miniatures `.webp` del `videoGrabber` i les extensions de les pujades.

### Disseny responsive del catàleg i del reproductor

La interfície de vídeos utilitza ara un contenidor fluid amb una graella que adapta el
nombre de targetes segons l'amplada disponible. Les targetes tenen miniatures amb
proporció 16:9, títols retallats de manera segura i estats hover sense alterar la
navegació. La barra d'accions del catàleg passa a una disposició vertical en pantalles
petites, evitant que els enllaços i el botó de tancar sessió surtin de la pantalla.
El reproductor del detall ocupa l'amplada disponible, conserva la proporció del vídeo
i evita el desbordament horitzontal en mòbil. El canvi és només de presentació i no
modifica els contractes ni la lògica de càrrega.

### Refinament visual dels controls

El botó de càrrega de més vídeos té ara un estil propi, amb contrast, vores
arrodonides, efectes hover i focus visible. També s'han uniformitzat els estats
interactius de la navegació i els camps dels formularis, inclòs el selector de fitxers.
Aquest refinament manté la jerarquia visual en escriptori i conserva controls amplis i
fàcils de prémer en dispositius mòbils.

### US-07: edició de metadades dels vídeos

El detall del vídeo mostra un editor només per al propietari autenticat. El frontend
envia títol, descripció, tags i categoria al backend, però l'autorització real es fa
al servidor comparant l'usuari de la sessió amb l'`ownerId` guardat en els metadades.
L'adaptador de fitxers actualitza el JSON mitjançant un fitxer temporal i un moviment
atòmic, conservant el vídeo, la miniatura i la durada. Els vídeos antics sense
propietari continuen sent visibles però no es poden editar; els usuaris no autenticats
o que no són propietaris reben una resposta d'accés denegat.

Després de desar, el detall demana una recàrrega del primer lot del catàleg abans de
tornar a la llista. Això evita que el catàleg mostri el títol antic que encara quedava
en l'estat de React carregat abans de l'edició.

### Arquitectura hexagonal de l'Epic 4

Les US-06 i US-07 tenen ara ports d'entrada explícits: `UploadVideoUseCase` i
`UpdateVideoMetadataUseCase`. El controlador només tradueix multipart, sessió, errors i
respostes HTTP; no coneix els serveis concrets. Els serveis d'aplicació contenen les
validacions i la normalització, i depenen només dels ports de sortida.

La publicació de fitxers està darrere de `VideoPublicationPort`, implementat per
`FileVideoPublicationAdapter`. Aquest adaptador encapsula `Multipart` ja convertit,
Jackson, la miniatura PNG, els fitxers temporals i els moviments atòmics. La persistència
de metadades d'edició continua darrere de `VideoCatalogPort`. Així es conserva el
comportament actual però es pot substituir el sistema d'emmagatzematge sense modificar
les regles de negoci.

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

El login de l'US-02 reutilitza la sessió HTTP: el backend normalitza l'email, busca
l'usuari i verifica el hash PBKDF2 sense retornar mai la contrasenya. Només després
d'una verificació correcta crea la sessió amb `authenticatedUserId`; les credencials
incorrectes responen amb un missatge genèric i no creen cap sessió autenticada. El
logout invalida explícitament la sessió existent. El frontend conserva el cookie de
sessió amb `withCredentials`, mostra el formulari de login i ofereix el tancament de
sessió des del catàleg.

El perfil de l'US-03 es desa a `user_profiles`, relacionat amb `users` per `user_id`.
El backend exposa el perfil propi només amb sessió, permet actualitzar els camps
editables i exposa altres perfils únicament quan són públics. El frontend reutilitza
el mateix component per a la vista editable pròpia i la vista pública de lectura;
l'avatar és una referència URL en aquesta primera iteració, mentre que la pujada del
fitxer queda per a una futura funcionalitat d'emmagatzematge.

### Errors

Cal diferenciar una entrada invàlida, un recurs inexistent, una operació no autoritzada
i un error intern. El frontend ha de mostrar un estat comprensible i no un èxit fals.

### Arquitectura hexagonal aplicada a autenticació i perfils

Login, logout, registre i perfil segueixen ara una separació explícita de ports i
adaptadors. Els controladors HTTP, situats als adaptadors d'entrada, depenen dels
ports d'entrada (`LoginUseCase`, `LogoutUseCase`, `RegistrationUseCase` i
`ProfileUseCase`) i del `SessionPort`, no de les classes concretes ni de
`HttpSession`. Els serveis d'aplicació implementen aquests casos d'ús i només
coneixen ports de sortida.

La persistència queda darrere de `UserPersistencePort`,
`UserAuthenticationPort` i `UserProfilePersistencePort`; els repositoris JPA/JDBC
són els adaptadors que implementen aquests contractes. `HttpSessionAdapter` encapsula
la sessió Servlet i implementa `SessionPort`. D'aquesta manera es pot substituir la
persistència o la tecnologia de sessió sense modificar la lògica dels casos d'ús.

### US-11: analítiques dels vídeos propis

La pantalla d'analítiques només és accessible per a una sessió autenticada i consulta
`GET /api/analytics/me`. El controlador utilitza `SessionPort` per obtenir l'usuari
actual i depèn del port d'entrada `ViewOwnVideoAnalyticsUseCase`; per tant, no coneix
ni el sistema de fitxers ni cap repositori concret.

`VideoAnalyticsService` coordina la consulta mitjançant el port de sortida
`VideoAnalyticsPort`. L'adaptador de fitxers llegeix els JSON dels vídeos del
magatzem compartit, filtra per `ownerId` i exposa les mètriques de visualitzacions,
comentaris i likes. Els comptadors absents es tracten com zero perquè els vídeos
antics i els importats pel grabber continuïn funcionant. Si l'usuari no té vídeos,
l'API retorna una llista buida i React mostra un estat buit; els errors de càrrega es
mostren explícitament.

Quan el reproductor comença una reproducció, el frontend envia una petició al
cas d'ús de registre de visualització una sola vegada per obertura del vídeo.
`RegisterVideoViewUseCase` i `VideoViewService` formen el port d'entrada i la
regla d'aplicació, mentre que `VideoViewPort` i `FileVideoViewAdapter` amaguen
l'actualització atòmica del comptador `views` al magatzem de fitxers. Això fa que
les visualitzacions siguin persistents i que les analítiques les reflecteixin en
consultes posteriors.

El frontend afegeix la ruta `#analytics`, un hook `useAnalytics` i targetes per vídeo.
La publicació de nous vídeos inicialitza els comptadors a zero dins del metadata, de
manera que l'estructura queda preparada per connectar-la a esdeveniments reals de
visualitzacions, comentaris i likes sense trencar els ports.
Quan s'edita la metadata d'un vídeo, l'adaptador conserva també el bloc d'analítiques;
això evita que una edició del títol o la descripció faci desaparèixer les mètriques
existents.

### US-08: cerca de vídeos

La cerca reutilitza el mateix endpoint paginat `GET /api/videos`, amb el paràmetre
opcional `query`, i funciona tant per a usuaris autenticats com anònims. El controlador
continua depenent del port d'entrada `ListVideosUseCase`; el servei valida la paginació
i el port de sortida `VideoCatalogPort` encapsula la consulta del catàleg. Com que el
catàleg actual és basat en fitxers, `FileVideoCatalogAdapter` llegeix la metadata
publicable i filtra abans de paginar.

La puntuació prioritza una coincidència exacta del títol, després una coincidència
parcial del títol, la descripció i finalment les etiquetes. La consulta ignora espais
inicials i finals i no distingeix majúscules de minúscules. Les coincidències amb la
mateixa puntuació es desempatent pel títol i l'identificador, de manera que la
resposta és determinista. Una consulta buida conserva l'ordre i el comportament del
catàleg normal.

El frontend afegeix un formulari de cerca accessible al capçal del catàleg. Enter i el
botó Search reinicien la pàgina a zero, Clear torna al catàleg complet i Load more
manté la consulta activa. El hook ignora respostes antigues quan hi ha una cerca més
recent, i mostra estats de càrrega, error i «No videos found.» sense tractar l'absència
de resultats com un error. Les targetes, la navegació, la reproducció i la ruta
`#analytics` continuen reutilitzant els components existents.

Els tests cobreixen coincidències exactes, parcials, de descripció i etiquetes,
majusculació, ordre determinista, paginació, consulta buida i estats de la interfície.

### US-09: comentaris de vídeos

Els comentaris es modelen com una funcionalitat independent amb `Comment`, el port
d'entrada `CommentUseCase` i el port de sortida `CommentPersistencePort`. El servei
valida que el vídeo existeixi al catàleg basat en fitxers, rebutja contingut buit o
superior a 2.000 caràcters i aplica la paginació després de recuperar els comentaris
ordenats de manera estable, del més nou al més antic.

L'adaptador JDBC crea la taula de comentaris i la seva relació amb `users`, i retorna
també el nom públic de l'autor. El controlador exposa `GET
/api/videos/{videoId}/comments` per a tothom i `POST` per a usuaris autenticats.
L'autor s'obté sempre de `SessionPort`, mai d'un identificador enviat pel frontend;
això evita la suplantació i manté la lògica d'autenticació fora del domini.

La vista de detall incorpora una secció responsive amb estat de càrrega, error i
llista buida, avís d'inici de sessió per a visitants anònims, validació client,
desactivació durant l'enviament i un botó Load more. El text es renderitza com a text
React, no com HTML, i el comentari creat s'insereix immediatament sense duplicats.
Els tests cobreixen persistència H2, autenticació, validacions, paginació, errors i
integració amb el detall del vídeo; la cerca US-08 i les analítiques US-11 no canvien.

### Format de les dates dels comentaris

El backend serialitza el `LocalDateTime` de Java com una matriu numèrica amb any, mes,
dia, hora, minuts, segons i nanosegons. El frontend no pot passar aquesta matriu
directament a `new Date` com si fos una cadena ISO, perquè produiria una data
invàlida. `CommentsSection` accepta tant aquesta representació com cadenes ISO,
converteix les matrius amb validació dels components i mostra `Date unavailable` si
la dada és nul·la o incorrecta, evitant ensenyar `Invalid Date`.

Els tests cobreixen les dates persistides retornades per l'API, les dates de
comentaris acabats de crear i els valors absents o malformats. La correcció és només
de presentació i no modifica la cerca US-08 ni les analítiques US-11.

### US-10: respostes i likes dels comentaris

US-10 amplia la mateixa funcionalitat hexagonal de comentaris sense posar la lògica
de negoci al controlador. `CommentService` valida que una resposta tingui un
comentari pare del mateix vídeo, manté l'autoria derivada de la sessió i delega la
persistència a `CommentPersistencePort`. `JdbcCommentRepository` desa la relació
pare-fill i utilitza `comment_likes` amb una clau composta d'usuari i comentari,
de manera que un usuari no pot crear likes duplicats; el canvi de like retorna el
nou comptador i l'estat de l'usuari actual.

La lectura de comentaris continua sent pública i compatible amb comentaris arrel,
però, quan hi ha sessió, també inclou l'estat `likedByCurrentUser`. El controlador
només coordina les rutes HTTP i `SessionPort` proporciona la identitat. El frontend
renderitza les respostes de forma anidada, permet contestar i actualitza el like
després de la resposta del servidor. Els tests cobreixen la validació de pares,
la persistència i alternança de likes, l'autenticació de les rutes i la interacció
de respostes i likes; US-09 i US-11 es mantenen separats.

Cada comentari nou, incloses les respostes, incrementa de manera atòmica el
comptador `analytics.comments` del metadata del vídeo. El cas d'ús de comentaris
depèn del port de sortida `VideoCommentPort`, i `FileVideoCommentAdapter` amaga
l'actualització del JSON i el moviment temporal. Això permet que la pantalla
d'analítiques de l'owner reflecteixi també la participació real. En la interfície,
les respostes tenen una vora i un marge lateral per identificar visualment que
pertanyen al comentari original.

### Rating de vídeos

El rating es tracta com un cas d'ús propi dins del mòdul de vídeo. El port d'entrada
`RateVideoUseCase` i `VideoRatingService` validen l'existència del vídeo, exigeixen
una sessió per escriure i accepten només valors de l'1 al 5. El port de sortida
`VideoRatingPort` amaga la persistència JDBC; `JdbcVideoRatingAdapter` desa una fila
per combinació vídeo-usuari i actualitza la valoració existent amb `ON CONFLICT`.
La migració `V6__video_ratings.sql` crea la taula i les restriccions sense modificar
migracions Flyway ja aplicades.

`VideoRatingController` exposa una lectura pública de la mitjana, el nombre total i
la valoració de l'usuari actual, i una operació autenticada per crear o modificar la
valoració. El detall del vídeo utilitza `useVideoRating`, mostra la mitjana i permet
seleccionar una puntuació només si l'usuari està autenticat. Els tests cobreixen
validació del servei, identitat derivada de la sessió, errors HTTP i la interacció
del control de rating al frontend. El resultat `SMALLINT` de PostgreSQL es converteix
com un `Number` abans de construir el model, perquè els drivers JDBC poden retornar
`Short` en lloc d'`Integer`. La interfície presenta estrelles, destaca la puntuació
seleccionada, mostra la mitjana i el nombre de valoracions, i manté els botons
desactivats mentre es desa la valoració.
La persistència utilitza el mateix identificador textual que el catàleg, perquè el
catàleg de desenvolupament basat en fitxers pot utilitzar identificadors numèrics,
mentre que el catàleg PostgreSQL utilitza UUID. El frontend diferencia errors de
sessió, vídeo inexistent i errors del servidor per fer visible la causa.

### Internacionalització del frontend

La interfície React utilitza `i18next` i `react-i18next` amb anglès com a idioma
principal i castellà com a alternativa. Les traduccions es concentren a
`frontend/src/i18n.ts`, i els components consumeixen les claus mitjançant
`useTranslation` en lloc de mantenir textos visibles duplicats. El selector
d'idioma és accessible, actualitza el context de traducció i desa la preferència
a `localStorage`, de manera que es conserva entre sessions.

La configuració s'importa abans de muntar React i també s'inicialitza en els tests
de Jest. Això permet provar el text anglès per defecte i evitar que els components
renderitzats directament depenguin de `main.tsx`. Les proves existents mantenen el
comportament funcional i el build comprova els tipus i la compilació de les dues
llengües. Els textos del perfil, autenticació, catàleg, vídeos, comentaris,
preferits, subscripcions, analítica i càrrega de vídeos utilitzen les traduccions;
els missatges d'error desconeguts del backend es conserven perquè continuïn sent
accionables.

### Reparació de migracions Flyway

Les migracions Flyway aplicades són immutables: modificar `V2` provoca un conflicte
de checksum en bases PostgreSQL persistents. La migració `V2` es manté amb el seu
contingut original i les funcionalitats posteriors s'apliquen mitjançant versions
noves. `V10__normalize_favorites_video_ids_safely.sql` comprova que la taula
`favorites` existeix abans de modificar-la, de manera que una base antiga sense
aquesta taula també pot completar totes les migracions i arrencar correctament.

### Compatibilitat amb l'esquema PostgreSQL existent

La base de dades de Docker podia contenir una versió anterior de la taula `comments`
amb la columna `text`, mentre que el model actual utilitza `content`. Com que
`CREATE TABLE IF NOT EXISTS` no modifica una taula existent, les consultes JDBC
fallaven amb un error de columna inexistent. L'inicialitzador comprova les columnes
reals a `information_schema` i renombra `text` a `content` amb `ALTER TABLE`, sense
recrear la taula ni perdre identificadors, autors, vídeos o dates.

La migració és idempotent: una segona arrencada no torna a renombrar la columna i
afegeix només les restriccions i l'índex que falten. Si detecta simultàniament `text`
i `content`, atura l'arrencada de l'inicialitzador amb un error explícit per evitar
decidir quina dada conservar. Els tests cobreixen l'esquema heretat, la preservació
de files, les arrencades repetides, l'esquema actual, el conflicte i les operacions
JDBC posteriors.

### Compatibilitat dels identificadors de vídeo dels comentaris

El catàleg de desenvolupament basat en fitxers pot utilitzar identificadors
numèrics, mentre que el catàleg PostgreSQL utilitza UUID. Per mantenir els dos
escenaris, `comments.video_id` es desa com `VARCHAR(255)` i
`JdbcCommentRepository` sempre envia l'identificador textual a JDBC.

Les bases persistents creades amb l'esquema antic podien conservar
`comments.video_id` com `UUID`. La migració `V8__normalize_comment_video_ids.sql`
converteix aquesta columna a text sense perdre les dades existents. Això evita
l'error PostgreSQL `uuid = character varying` quan la pantalla consulta els
comentaris d'un vídeo de `video-store`, i conserva també els identificadors UUID
del catàleg PostgreSQL. Com que `video_analytics_summary` compta les files de
`comments`, el comentari persistit continua incrementant el comptador d'analítica.

### Selecció de l'adaptador de comptadors de comentaris

El perfil `dev` utilitza el catàleg de fitxers i, per tant, els identificadors
dels vídeos poden ser numèrics. L'adaptador `JdbcVideoCommentAdapter` només és
vàlid per al catàleg PostgreSQL, on els identificadors són UUID; si s'injectava
com a adaptador primari durant el desenvolupament, rebutjava un ID com `5` i
retornava un `404` després de desar el comentari. Ara
`FileVideoCommentAdapter` està actiu en `dev` i `JdbcVideoCommentAdapter` en
`prod`, mantenint coherent el comptador d'analítica amb el catàleg seleccionat.

### Configuració compartida del magatzem de vídeos

La ruta del magatzem ja no depèn del directori personal de cap membre de l'equip.
En desenvolupament, si no es defineix `ENV_PROTUBE_STORE_DIR`, el backend resol
`../video-store/` respecte del directori `backend/`. La configuració compartida
d'IntelliJ utilitza `$PROJECT_DIR$/video-store/`, i Docker munta el mateix directori
del repositori per defecte. El directori continua ignorat per Git perquè conté fitxers
multimèdia generats; cada persona només ha de generar o copiar els vídeos una vegada.
Els tests d'aplicació mockegen ports, mentre que els tests dels adaptadors comproven
la integració amb HTTP, JDBC i JPA.

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

- Com comprova la suite E2E el recorregut complet sense importar classes del frontend
  ni del backend?
- Quines capes travessa una prova Playwright des del navegador fins a l'adaptador
  d'emmagatzematge?
- Per què la suite utilitza un MP4 sintètic en memòria i no un fitxer multimèdia
  versionat?
- Què passa des que l'usuari obre la pantalla principal fins que veu les targetes?
- Quina responsabilitat té cada capa del backend?
- Per què el catàleg no ha de llegir fitxers directament des del controlador?
- Què passa si falta el `.json`, el `.mp4` o el `.webp`?
- Com canviaries l'adaptador de fitxers per PostgreSQL sense refer el frontend?
- Quines entitats caldrien per a usuaris, vídeos, comentaris, respostes i likes?
- Com impediries que un usuari editi el vídeo d'una altra persona?
- Quins estats ha de mostrar una pantalla mentre una petició carrega o falla?
- Quins tests afegiries abans de donar per acabada una funcionalitat?

### CI del backend i PostgreSQL

Els tests d'arrencada del backend utilitzen la mateixa tecnologia de persistència que
l'aplicació: PostgreSQL amb Flyway. La pipeline de GitHub Actions declara PostgreSQL
com a servei, espera que el healthcheck el marqui com a disponible i passa les
variables de connexió al procés Maven. Això evita que el context de Spring falli per
un `Connection refused` a `localhost:5432` i no introdueix una base de dades
alternativa només per als tests.

El perfil Maven `coverage` existeix explícitament perquè la comanda de CI
`mvn clean verify -Pcoverage` sigui reproducible. JaCoCo continua aplicando els
llindars definits al `pom.xml`; el perfil només fa vàlida la comanda del pipeline.

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
| 18/10/2026 | S'implementa el registre de l'US-01 amb validació, detecció de duplicats i autenticació automàtica. | El backend valida la petició, normalitza l'email, desa la contrasenya amb PBKDF2 i guarda l'identificador d'usuari a la sessió HTTP; el frontend valida abans d'enviar, mostra errors de camp i confirma l'estat autenticat després d'un 201. |
| 18/10/2026 | Es millora la pantalla de registre amb una interfície visual més clara i professional. | El formulari separa la capçalera de marca, les etiquetes, els camps, els missatges d'error i l'acció principal; també incorpora focus visible, placeholders, indicadors d'entrada invàlida i adaptació a pantalles petites sense canviar el contracte de l'API. |
| 18/10/2026 | Es centra el títol del catàleg de vídeos. | La barra superior del catàleg manté l'acció de registre a la dreta i presenta el títol centrat independentment de l'amplada del text. |
| 18/10/2026 | Es corregeix l'arrencada del registre en PostgreSQL. | El perfil de producció inicialitza de manera idempotent la taula `users` abans de consultar duplicats; així l'API no depèn que el script SQL general s'hagi executat prèviament. |
| 19/10/2026 | S'implementa el login de l'US-02 amb logout i sessió HTTP. | El servei verifica hashes PBKDF2, usa errors genèrics per a credencials incorrectes i només crea la sessió després d'autenticar; la interfície React permet entrar i sortir. |
| 19/10/2026 | S'implementa el perfil de l'US-03 amb vista pròpia editable i perfil públic. | `ProfileController`, el servei i l'adaptador JDBC separen HTTP, negoci i persistència; la visibilitat pública evita mostrar controls d'edició a altres usuaris. |
| 19/10/2026 | Es corregeix la compatibilitat entre l'entitat `User`, l'esquema H2 i la consulta de perfils. | El camp `status` forma part del model i l'inicialitzador afegeix la columna si una base de desenvolupament ja existia; així `GET /api/profiles/me` pot filtrar usuaris actius sense fallar. |
| 19/10/2026 | Es centralitza la navegació hash del frontend i es fan visibles els errors de guardat del perfil. | React manté el hash actual en estat propi, de manera que canviar entre catàleg i perfil actualitza la vista sense recarregar; els errors HTTP del backend ja no es perden en un missatge genèric. |
| 19/10/2026 | Es configura CORS per permetre el guardat del perfil des del frontend local. | El navegador fa un preflight `OPTIONS` abans del `PUT`; declarar explícitament mètodes, cabeceres i credencials evita que Axios mostri un error de xarxa encara que el backend estigui encès. |
| 19/10/2026 | Es redissenya la capçalera per mostrar el logo petit a la cantonada superior esquerra. | El logo es posiciona de manera absoluta dins de la capçalera i el contingut conserva l'alineació central sense que la marca ocupe l'espai principal. |
| 19/10/2026 | Es col·loquen les accions de sessió a la mateixa línia superior que el logo. | El nom de l'usuari i el logout es posicionen dins de la capçalera visual del catàleg, mantenint el títol centrat i les targetes separades. |
| 09/11/2026 | S'implementa l'autenticació Google OAuth2/OIDC. | Spring Security gestiona el flux de codi d'autorització al servidor, crea l'usuari i el perfil a partir del `sub` de Google sense enllaçar comptes locals només per email, i reutilitza la sessió HTTP existent. `/api/auth/me` permet restaurar la sessió al frontend i els botons React mai exposen tokens. |
| 09/11/2026 | S'ajusta el retorn OAuth de Docker i es cobreixen els adaptadors amb tests focalitzats. | Compose redirigeix a `http://localhost` mentre el desenvolupament Vite pot sobreescriure `ENV_PROTUBE_FRONTEND_URL`; els tests verifiquen el provisionament, els conflictes d'email, els redirects i la restauració de sessió. |
| 19/10/2026 | S'ajusta l'escala visual de les accions de sessió. | L'enllaç al perfil manté més protagonisme i el botó de logout utilitza una mida i un espaiat més compactes per equilibrar la capçalera. |
| 19/10/2026 | S'ajusta la jerarquia tipogràfica del formulari de registre. | Les etiquetes dels camps tenen més presència i els placeholders i el text d'ajuda són més petits, de manera que la informació principal queda clarament diferenciada de l'exemple. |
| 19/10/2026 | Es completa la cobertura mínima del 90% al frontend i backend. | Els tests cobreixen branques de navegació, errors de formularis, permisos de perfil, validacions, persistència H2 i credencials; Jest i JaCoCo fan fallar el build si alguna mètrica global baixa del 90%. |
| 19/10/2026 | Es resolen les rutes multimèdia relatives en desenvolupament. | El frontend transforma les adreces `/media/...` retornades per l'API en URLs del domini multimèdia configurat, de manera que funciona tant amb Vite a `localhost:5173` com amb el frontend servit pel backend o Docker. |
| 19/10/2026 | Es completa la paginació de l'US-04. | El servei calcula la pàgina sol·licitada i el frontend afegeix 12 vídeos més quan l'usuari ho demana; els tests cobreixen la resposta paginada, els límits i l'acumulació de resultats. |
| 19/10/2026 | S'implementa la pujada de vídeos de l'US-06. | La sessió protegeix l'endpoint, el servei valida MP4 i mida, publica vídeo, miniatura i metadades de manera atòmica i el frontend mostra errors i estat de pujada. |
| 20/10/2026 | Es completa la migració de login, logout, registre i perfils a arquitectura hexagonal. | Els controladors depenen de ports d'entrada, els serveis de ports de sortida i els adaptadors encapsulen persistència i sessió; la suite backend manté 90% o més de cobertura global. |
| 20/10/2026 | Es completa l'arquitectura hexagonal de les US-06 i US-07 de l'Epic 4. | Els casos d'ús d'upload i edició s'exposen com a ports d'entrada; el servei d'upload ja no depèn de `MultipartFile`, Jackson ni del sistema de fitxers, i els adaptadors conserven la publicació i l'edició atòmiques. |
| 21/10/2026 | Es corregeix el límit de pujada del frontend Docker. | Nginx aplicava el seu límit multipart per defecte abans que la petició arribés al backend; `client_max_body_size 100M` el fa coherent amb la validació del frontend i els límits de Spring Boot. |
| 21/10/2026 | S'ordena el catàleg perquè els vídeos nous apareguin al principi. | L'adaptador de catàleg ordena els fitxers MP4 per data de modificació descendent abans d'aplicar la paginació, de manera que una pujada recent és la primera targeta. |
| 29/10/2026 | S'implementa la US-11 amb arquitectura hexagonal. | El backend exposa les analítiques dels vídeos propis mitjançant ports i l'adaptador del magatzem, i el frontend mostra les mètriques i l'estat buit. |
| 29/10/2026 | S'implementa la US-09 de comentaris amb ports hexagonals i persistència JDBC. | La sessió determina l'autor, el vídeo es valida contra el catàleg actual, i la interfície permet publicar i paginar comentaris sense afectar la cerca ni les analítiques. |
| 29/10/2026 | Es migra de manera segura l'esquema heretat de comentaris a PostgreSQL. | `text` es renombra a `content` sense perdre files; l'inicialitzador és idempotent i rebutja conflictes entre les dues columnes. |
| 29/10/2026 | Es corregeix la visualització de les dates dels comentaris. | El frontend interpreta la matriu `LocalDateTime` del backend i gestiona dates ISO, absents o invàlides sense mostrar `Invalid Date`. |
| 01/11/2026 | S'afegeixen proves E2E amb Playwright per als recorreguts principals de l'MVP. | Les proves obren el frontend real servit per Docker, registren un usuari, pugen un vídeo, el cerquen, obren el detall i publiquen un comentari. La prova valida el sistema complet a través d'HTTP, mantenint la separació entre ports, serveis i adaptadors hexagonals; el fitxer MP4 de prova es crea en memòria per no versionar mitjans generats. |
| 01/11/2026 | Es corregeix la sessió del frontend després del registre i es reforça la navegació E2E a la pantalla de pujada. | El compte creat queda disponible tant com a usuari registrat com com a usuari autenticat; les rutes de pujada i analítiques comparteixen el mateix estat derivat i la prova Playwright comprova que realment s'ha renderitzat el formulari abans d'emplenar-lo. |
| 01/11/2026 | Es fa exacte el selector del títol en la prova E2E del detall de vídeo. | Playwright podia trobar alhora el títol de la targeta i el títol del detall perquè els selectors de rol permeten coincidències parcials per defecte; exigir `exact: true` evita l'ambigüitat i valida la pantalla correcta. |
| 01/11/2026 | Es corregeix la persistència local de Docker per a PostgreSQL. | Compose munta PostgreSQL a `postgres-data/`, separat de `video-store/`; així els registres de la base de dades i els fitxers multimèdia sobreviuen a `docker compose down` seguit de `up --build`, sempre que no s'esborrin les carpetes ni s'utilitzi `down -v`. |
| 01/11/2026 | Es migren les dades de vídeo i analítica a PostgreSQL amb Flyway i adaptadors JDBC hexagonals. | PostgreSQL passa a ser la font de veritat de la identitat del vídeo, els metadades, les referències als fitxers, les visualitzacions i els comptadors. Els fitxers físics continuen al `video-store`, però els serveis només coneixen els ports de sortida i no depenen de JDBC ni del sistema de fitxers. |
| 01/11/2026 | S'afegeix una prova d'integració de persistència després de reiniciar el backend. | La prova publica un vídeo, reinicia només el contenidor del backend i comprova que el catàleg el troba després; així es verifica que les dades de domini sobreviuen perquè estan a PostgreSQL i no a memòria o JSON. |
| 01/11/2026 | Es corregeix la persistència dels comentaris amb identificadors UUID. | El repositori JDBC adapta els identificadors de vídeo al tipus PostgreSQL abans de consultar o inserir; així desapareix l'HTTP 500, els comentaris es guarden i la vista d'analítiques els compta des de la base de dades. |
| 02/11/2026 | Es configura el catàleg segons l'entorn d'execució. | En desenvolupament, `FileVideoCatalogAdapter` llegeix els vídeos i miniatures del `video-store`; en producció, `JdbcVideoCatalogAdapter` consulta PostgreSQL. Això evita que un adaptador JDBC buit amagui el catàleg local de fitxers. |
| 02/11/2026 | Es limita el nom visible del perfil a 50 caràcters. | El frontend limita l'entrada i el backend valida el mateix límit abans de persistir-lo, evitant noms excessivament llargs que trenquin el disseny. |
| 02/11/2026 | Es fa compatible la migració Flyway de vídeos amb bases PostgreSQL persistents antigues. | `V2__video_persistence.sql` crea la taula `comments` si encara no existeix abans de construir `video_analytics_summary`. Això permet que una instal·lació nova i una base conservada per Docker arrenquin sense esborrar `postgres-data`; les migracions idempotents mantenen les dades existents. |
| 02/11/2026 | Es corregeix el catàleg buit de Docker en instal·lacions noves. | Compose utilitza el perfil `prod` i `VideoStoreDatabaseImporter` importa de manera idempotent la metadata i les referències dels assets des de `video-store` a PostgreSQL. `JdbcVideoCatalogAdapter` consulta la base de dades, mentre els fitxers `.mp4` i les miniatures continuen al magatzem muntat. |
| 04/11/2026 | Es corregeix el test d'arrencada del backend a la CI sense substituir PostgreSQL. | GitHub Actions inicia un servei PostgreSQL saludable abans de Maven i li passa les credencials de la base de dades; el perfil `coverage` també queda definit al `pom.xml` perquè la verificació amb JaCoCo no produeixi un avís de perfil inexistent. |

| 09/11/2026 | Es completa la cobertura de branques del flux OAuth per superar el 90% global. | Els tests cobreixen identitats Google nul·les o buides, noms i correus alternatius durant el provisioning, conflictes de compte i errors de redirecció. Això protegeix els camins excepcionals sense canviar el comportament de producció. |
| 09/11/2026 | Es recupera la taula `auth_accounts` en bases PostgreSQL persistents creades amb un esquema anterior. | La migració Flyway `V4__restore_auth_accounts.sql` crea de manera idempotent la relació d'identitats externes, les restriccions i l'índex que necessita `JdbcGoogleAccountAdapter`; així el callback OAuth no falla amb un error SQL després de l'autorització de Google. |
| 09/11/2026 | Es força el flush de l'usuari abans d'inserir el compte Google amb JDBC. | `JdbcGoogleAccountAdapter` usa `saveAndFlush()` perquè la fila `users` existeixi abans de validar la clau forana d'`auth_accounts`; el test focalitzat verifica l'ordre i evita que una excepció de PostgreSQL deixi la transacció actual avortada. |

| 09/11/2026 | Es corregeix la creació de sessió després del login amb Google. | El callback OAuth utilitza directament la petició HTTP que proporciona Spring Security per crear la sessió, perquè aquest flux passa pel filtre de seguretat abans del context de DispatcherServlet. |
| 09/11/2026 | Es corregeix la inicial del perfil a la capçalera del catàleg. | El fallback de l'avatar ara s'avalua com una expressió React i mostra la primera lletra del nom, username o correu, en lloc d'imprimir el codi literal. |
| 09/11/2026 | Es milloren els botons d'autenticació amb Google. | Els formularis de login i registre comparteixen un botó amb icona, contrast blanc, mida consistent i estats hover per diferenciar clarament l'accés OAuth de l'acció principal. |
| 09/11/2026 | Es corregeix un conflicte de checksum de Flyway sense perdre dades. | Les migracions ja aplicades no s'han de modificar: `V2__video_persistence.sql` recupera el contingut original i la normalització de `comments` passa a `V5__normalize_comments_schema.sql`. Això permet que Flyway validi les bases existents i apliqui el canvi com una migració nova, sense `repair` ni esborrar `postgres-data`. |
| 09/11/2026 | Es manté Google OAuth configurat en el perfil `dev` sense bloquejar l'arrencada. | Quan les variables d'entorn estan buides, Spring utilitza credencials fictícies no sensibles; el registre i el login locals funcionen, però Google OAuth només queda operatiu quan es proporcionen les claus reals de presentació. |

## 8. Temes pendents

- Protecció de rutes i autorització per recurs.
- Persistència relacional dels vídeos pujats segons NFR-01.
- Respostes i likes dels comentaris.
- Analítiques del contingut propi.
- Tests E2E dels recorreguts principals (implementats amb Playwright; cal executar-los en CI i en una instal·lació Docker local).
- Docker Compose i configuració de producció.
