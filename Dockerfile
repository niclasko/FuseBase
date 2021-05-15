FROM lwieske/java-8
COPY build /app
WORKDIR /app
CMD java -jar fusebase.jar