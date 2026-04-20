# Usamos una imagen de Tomcat con Java 17 (común en NetBeans actual)
FROM tomcat:10-jdk17-openjdk-slim

# Borramos las apps por defecto de Tomcat
RUN rm -rf /usr/local/tomcat/webapps/*

# Copiamos tu archivo WAR al servidor
# Nota: Cambia 'tu-archivo.war' por el nombre real de tu archivo war si lo conoces
COPY dist/*.war /usr/local/tomcat/webapps/ROOT.war

# Exponemos el puerto 8080
EXPOSE 8080

CMD ["catalina.sh", "run"]
