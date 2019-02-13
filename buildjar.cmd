del classes\*.class
mkdir classes
javac -Xlint:unchecked -source 1.7 -target 1.7 src\*.java -d classes

del /S /Q build\fusebase.jar

rd /S /Q build\jdbc_drivers
xcopy /S /E /Y jdbc_drivers build\jdbc_drivers\

cd classes

jar cvfe ..\build\fusebase.jar FuseBase ./* ../web/* ../ui/*

cd ..