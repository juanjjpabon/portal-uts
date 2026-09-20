# Misma version/distribucion de Java que en local (Temurin 21) en ambas etapas,
# para no repetir problemas de version como los que ya tuvimos con JAVA_HOME.

FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

COPY src/ src/
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar target/

EXPOSE 8080

# Ajustado para el plan gratis de Render: 512MB de RAM, limite duro (mata el
# contenedor sin aviso si se pasa), y muy poca CPU.
# - MaxRAMPercentage en vez de -Xmx fijo: el JVM ve el limite del contenedor
#   (cgroup) y calcula el heap sobre eso, no sobre la RAM del host.
# - 45% de heap: con 60% Render reporto "Ran out of memory"; ya con "prod"
#   activo (perfil corregido) y 50%, igual crasheo una vez mas (exit status 3,
#   ExitOnOutOfMemoryError) unos 8 minutos despues de un arranque limpio. Lo de
#   dev/thymeleaf ya estaba resuelto: lo que faltaba era margen para lo que
#   vive FUERA del heap (ver los dos puntos siguientes), asi que se le quita
#   mas espacio al heap para dárselo a eso.
# - TieredStopAtLevel=1: usa solo el compilador JIT C1, nunca C2. C2 reserva
#   memoria para codigo compilado (~240MB por defecto con tiered completo) y
#   quema CPU compilando en caliente; con 0.1 CPU y trafico bajo esa
#   optimizacion no se alcanza a aprovechar, solo cuesta memoria y arranque
#   mas lento (posiblemente relacionado con el crash: mucha compilacion JIT
#   de golpe justo cuando alguien empieza a navegar el portal).
# - ReservedCodeCacheSize=64m: tope explicito al cache de codigo compilado,
#   coherente con lo anterior.
# - Xss256k: pila mas chica por hilo (default ~1MB). Entre Tomcat, Hikari y
#   los hilos propios de la JVM son ~20-30 hilos; a 1MB c/u eso solo ya son
#   20-30MB que esta app no necesita.
# - MaxMetaspaceSize evita que el metaspace (fuera del heap: no lo cubre
#   MaxRAMPercentage) crezca sin limite con las proxies/reflexion de Spring
#   e Hibernate y empuje al contenedor por encima de los 512MB.
# - UseSerialGC: recolector de una sola hebra, apropiado con poca CPU (los
#   recolectores paralelos reservan hilos que aqui no hay para usar bien).
# - ExitOnOutOfMemoryError: si igual se llega a quedar sin memoria, el JVM
#   termina de una vez para que Render reinicie el contenedor limpio, en vez
#   de quedar colgado en un estado a medias.
ENV JAVA_OPTS="-XX:+UseSerialGC -XX:MaxRAMPercentage=45.0 -XX:MaxMetaspaceSize=128m -XX:TieredStopAtLevel=1 -XX:ReservedCodeCacheSize=64m -Xss256k -XX:+ExitOnOutOfMemoryError"
CMD ["sh", "-c", "java $JAVA_OPTS -jar target/*.jar"]
