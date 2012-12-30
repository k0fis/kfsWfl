PATH=/bin:/usr/bin:/usr/local/git/bin
mvn -DaltDeploymentRepository=repo::default::file:../kfsWfl-mvn/release clean deploy 
cd ../kfsWfl-mvn
git add *
git commit -m 'release'
git push origin master