#FROM lwieske/java-8
FROM adoptopenjdk/openjdk10
COPY . /app
WORKDIR /app

RUN rm ./classes/*.class
RUN javac -Xlint:unchecked -Xlint:deprecation -source 1.7 -target 1.8 ./src/*.java -d ./classes

RUN cp -r ./misc/apps ./apps/
WORKDIR /app/classes
#RUN jar cvfe fusebase.jar FuseBase /app/classes/* /app/web/* /app/ui/* /app/scripts/*
RUN jar cvfe /app/fusebase.jar FuseBase ./* ../web/*
WORKDIR /app/
CMD ["java", "-jar", "fusebase.jar"]