FROM openjdk:17
EXPOSE 1996
ADD target/report-details.jar report-details.jar
ENTRYPOINT ["java","-jar","report-details.jar"]