#!/bin/sh

rm ./classes/*.class
javac -Xlint:unchecked -Xlint:deprecation -source 1.7 -target 1.8 ./src/*.java -d ./classes

rm ./build/fusebase.jar

rm -rf ./build/jdbc_drivers/
cp -r ./jdbc_drivers ./build/

rm -rf ./build/apps/
cp -r ./apps ./build/

rm -rf ./build/ssl/
cp -r ./ssl ./build/

cd ./classes

jar cvfe ../build/fusebase.jar FuseBase ./* ../web/* ../ui/* ../scripts/*

cd ..