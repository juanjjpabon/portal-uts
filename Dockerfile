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
# - 50% de heap deja margen para metaspace, hilos, buffers directos y el
#   propio overhead del JVM dentro de los 512MB totales (con 60% Render
#   igual reporto "Ran out of memory (used over 512MB)" mas de una vez).
# - MaxMetaspaceSize evita que el metaspace (fuera del heap: no lo cubre
#   MaxRAMPercentage) crezca sin limite con las proxies/reflexion de Spring
#   e Hibernate y empuje al contenedor por encima de los 512MB.
# - UseSerialGC: recolector de una sola hebra, apropiado con poca CPU (los
#   recolectores paralelos reservan hilos que aqui no hay para usar bien).
# - ExitOnOutOfMemoryError: si igual se llega a quedar sin memoria, el JVM
#   termina de una vez para que Render reinicie el contenedor limpio, en vez
#   de quedar colgado en un estado a medias.
ENV JAVA_OPTS="-XX:+UseSerialGC -XX:MaxRAMPercentage=50.0 -XX:MaxMetaspaceSize=128m -XX:+ExitOnOutOfMemoryError"
CMD ["sh", "-c", "java $JAVA_OPTS -jar target/*.jar"]
