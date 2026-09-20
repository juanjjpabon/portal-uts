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
# - Vamos en el tercer ajuste. Los dos anteriores fallaron distinto y eso
#   dice algo: con 60% (y perfil "dev" + pools grandes) Render mataba el
#   contenedor entero ("Ran out of memory"); ya con "prod" y pools chicos,
#   con 50% crasheo a los ~8 min (exit status 3); con 45% crasheo MAS rapido,
#   durante el arranque, y esta vez el log SI lo dice explicito: "Terminating
#   due to java.lang.OutOfMemoryError: Java heap space". No es memoria del
#   contenedor en general - es el heap mismo el que no alcanza. Bajarlo mas
#   iba en la direccion equivocada. Ahora subimos el heap y en cambio le
#   bajamos el tope a metaspace y code cache (que hasta ahora tenian mas
#   margen del que parecen necesitar) para compensar sin volver a las 512MB
#   totales del principio.
# - MaxRAMPercentage=58: mas espacio de heap (~297MB) que en los dos intentos
#   anteriores, para que la sesion de arranque (que es cuando esta cayendo)
#   tenga margen real.
# - MaxMetaspaceSize baja de 128m a 100m: sigue siendo suficiente para las
#   proxies/reflexion de Spring e Hibernate, y libera heap.
# - ReservedCodeCacheSize baja de 64m a 48m: con TieredStopAtLevel=1 (solo
#   compilador C1) el codigo compilado ocupa bastante menos que eso.
# - TieredStopAtLevel=1 y Xss256k: se mantienen del intento anterior, son
#   ganancia neta pase lo que pase con el heap.
# - UseSerialGC: recolector de una sola hebra, apropiado con poca CPU (los
#   recolectores paralelos reservan hilos que aqui no hay para usar bien).
# - ExitOnOutOfMemoryError: si igual se llega a quedar sin memoria, el JVM
#   termina de una vez para que Render reinicie el contenedor limpio, en vez
#   de quedar colgado en un estado a medias.
ENV JAVA_OPTS="-XX:+UseSerialGC -XX:MaxRAMPercentage=58.0 -XX:MaxMetaspaceSize=100m -XX:TieredStopAtLevel=1 -XX:ReservedCodeCacheSize=48m -Xss256k -XX:+ExitOnOutOfMemoryError"
CMD ["sh", "-c", "java $JAVA_OPTS -jar target/*.jar"]
