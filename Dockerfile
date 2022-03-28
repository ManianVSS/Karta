FROM openjdk:12-alpine

LABEL maintainer="Manian VSS<manianvss@hotmail.com>"


COPY runtime/target/packages/Karta /karta
ENV KARTA_HOME=/karta
WORKDIR /karta/bin
ENTRYPOINT ["sh","KartaServer.sh"]