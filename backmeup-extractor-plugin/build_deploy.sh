rm /cygdrive/c/data/backmeup-service/plugins/extractor-2.0.0-SNAPSHOT.jar
rm /cygdrive/c/data/backmeup-worker/plugins/extractor-2.0.0-SNAPSHOT.jar
rm ../autodeploy/extractor-2.0.0-SNAPSHOT.jar
mvn -DskipTests clean install
sleep 5
cp ../autodeploy/extractor-2.0.0-SNAPSHOT.jar /cygdrive/c/data/backmeup-service/plugins/
cp ../autodeploy/extractor-2.0.0-SNAPSHOT.jar /cygdrive/c/data/backmeup-worker/plugins/
chown bartham: /cygdrive/c/data/backmeup-worker/plugins/*
chmod 770 /cygdrive/c/data/backmeup-worker/plugins/*

chown bartham: /cygdrive/c/data/backmeup-service/plugins/*
chmod 770 /cygdrive/c/data/backmeup-service/plugins/*

#cp ./target/sftp-2.0.0-SNAPSHOT.jar /cygdrive/c/data/backmeup-service/plugins/
