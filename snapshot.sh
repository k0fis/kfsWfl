mvn -DaltDeploymentRepository=snapshot-repo::default::file:../kfsWfl-mvn/snapshots/ clean deploy
PATH=/usr/local/git/bin
cd ../kfsWfl-mvn
git add *
git commit -m 'snapshot'
git push origin master
