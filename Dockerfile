FROM openjdk:8-jre
COPY ./runtime/target/packages/Karta /karta
ENV KARTA_HOME /karta
WORKDIR /karta/samples
CMD ../bin/KartaServer.sh


