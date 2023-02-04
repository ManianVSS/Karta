Dry run

mvn -T 4 -DskipTests=true clean install
&&
cd packager\target\packages\Karta\bin && Karta.bat -t cycle,javatag1 -runName TestRun && cd ..\\..\\..\\..\\..
&&
cd packager\target\packages\Karta\bin && KartaServer.bat
&&
cd ..\\..\\..\\..\\..
cd packager\target\packages\Karta\bin && Karta.bat -startMinion



