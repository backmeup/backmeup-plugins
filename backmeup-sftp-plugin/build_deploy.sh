rm /cygdrive/c/data/backmeup-service/plugins/sftp-2.0.0-SNAPSHOT.jar
rm /cygdrive/c/data/backmeup-worker/plugins/sftp-2.0.0-SNAPSHOT.jar
rm ../autodeploy/sftp-2.0.0-SNAPSHOT.jar
mvn -DskipTests clean install
sleep 5
cp ../autodeploy/sftp-2.0.0-SNAPSHOT.jar /cygdrive/c/data/backmeup-service/plugins/
cp ../autodeploy/sftp-2.0.0-SNAPSHOT.jar /cygdrive/c/data/backmeup-worker/plugins/

#cp ./target/sftp-2.0.0-SNAPSHOT.jar /cygdrive/c/data/backmeup-service/plugins/
