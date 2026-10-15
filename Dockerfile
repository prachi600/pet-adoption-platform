FROM maven:3.9-eclipse-temurin-17
WORKDIR /app
COPY . .
RUN mvn -q compile && mvn -q dependency:build-classpath -Dmdep.outputFile=cp.txt -DincludeScope=compile
CMD ["sh", "-c", "java -cp target/classes:$(cat cp.txt) com.petadoption.Main"]