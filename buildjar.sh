#!/bin/sh

rm ./classes/*.class
javac -Xlint:unchecked -source 1.7 -target 1.7 ./src/*.java -d ./classes

rm ./build/fastbase.jar

rm -rf ./build/jdbc_drivers/
cp -r ./jdbc_drivers ./build/

rm -rf ./build/apps/
cp -r ./apps ./build/

rm -rf ./build/ssl/
cp -r ./ssl ./build/

cd ./classes

jar cvfe ../build/fastbase.jar FastBase ./* ../web/* ../ui/*

cd ..