# Usa una imagen base de OpenJDK 21
FROM maven

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el archivo JAR generado por tu aplicación
COPY . .

# Expone el puerto en el que tu aplicación Spring Boot estará escuchando
EXPOSE 8080

# Comando para ejecutar la aplicación
RUN mvn clean install

CMD mvn spring-boot:run
