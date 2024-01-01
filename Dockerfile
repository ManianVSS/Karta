FROM manianvss/java-chrome-driver:1.0

LABEL maintainer="Manian VSS<manianvss@hotmail.com>"

COPY runtime/target/packages/Karta /karta
ENV KARTA_HOME=/karta
WORKDIR /karta/samples

EXPOSE 18080

ENTRYPOINT ["sh","../bin/KartaServer.sh"]
