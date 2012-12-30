PATH=/bin:/usr/bin:/usr/local/git/bin
mvn -DaltDeploymentRepository=snapshot-repo::default::file:../kfsWfl-mvn/snapshots/ clean deploy
cd ../kfsWfl-mvn
git add *
git commit -m 'snapshot'
git push origin master
