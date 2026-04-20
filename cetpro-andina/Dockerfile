FROM tomcat:10-jdk17-openjdk-slim

# Borramos todo lo que trae Tomcat por defecto
RUN rm -rf /usr/local/tomcat/webapps/*

# Copiamos tu archivo WAR y lo renombramos a ROOT.war
# Esto obliga a que tu página sea la principal del link
COPY cetpro-andina/dist/*.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]
