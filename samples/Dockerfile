FROM manianvss/karta:0.1.1

LABEL maintainer="Manian VSS<manianvss@hotmail.com>"


COPY src/main/resources /karta/bin
COPY target/samples-0.1.1.jar /karta/bin

WORKDIR /karta/bin
ENTRYPOINT ["sh","KartaServer.sh","-t","cycle"]