FROM tomcat:10-jdk17-openjdk-slim

# Limpiamos Tomcat
RUN rm -rf /usr/local/tomcat/webapps/*

# Buscamos cualquier archivo .war en el repositorio y lo volvemos la raíz
COPY **/*.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]
