PATH=/bin:/usr/bin:/usr/local/git/bin
mvn -DaltDeploymentRepository=repo::default::file:../kfsWfl-mvn/release clean deploy 
cd ../kfsWfl-mvn
git pull
git add *
git commit -a -m 'release'
git push origin master
