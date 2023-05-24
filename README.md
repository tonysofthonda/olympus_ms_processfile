
# Process File Microservice  

This module is a service that read & proces a **File** based on a input **Template File**. Its purpose is to read each of tlines contained in the file and perform the related operation and perform the required changes in the the **DataBase**.

This application exposes 2 single endpoints (as a RESTFul service) 
  1. One that check the health of srevice. 
  2. One that starts the process file action, with a customizable template. 


## How it works

1. The project includes a properties file  (**application.properties**), with the entries:  
   `file.sourcedir: To indicate where the file should be read`.
   A `processFileTemplate file: To indicate how lines and data should be read`.
     
2. Once called the service, the template file is validated to ensure the correct data lecture and extraction, if this is incorrect the process ends throwing an exception.  
   
3. The service performs a validation over the file that will be read, if the file can not be read, the service throws an exception.

4. If evehing is correct the service will perform an extraction of each line separated by "\n", will create tokens for each of the fileds that will be processed.

5. Each of these tokens are separately processed, and acording the instructions contained in the line, data base instructions will be performed.

5. If errors ocurss during the 

{
    "status": 1,
    "msg":"SUCCESS",
    "file": "{fileName}"
}


## Tools  

+ Java v1.8.0_202
+ Maven v3.8.6
+ Spring Boot v2.6.14
+ JUnit v5.8.2 with AssertJ v3.21.0
+ Lombok v1.18.24
+ Logback v1.2.11


## Run the app

Obtaining the application's Jar file  
`$ mvn clean install`  
  
Running the project as an executable JAR  
`$ mvn spring-boot:run`  

Running the tests  
`$ mvn test`  


## Usage

### 1. Service Health check endpoint
#### Request
`GET /olympus/monitor/v1/health`

    curl -i -X GET -H http://{server-domain|ip}/olympus/monitor/health

#### Response
    HTTP/1.1 200 OK
    Content-Type: application/json
    Transfer-Encoding: chunked
    Date: Mon, 22 May 2023 05:00:55 GMT
    
   Honda Olympus [name: ms.monitor] [version: 1.0.2] [profile: dev] 2023-05-22T05:00:55 America/Mexico_City

### 2. Manually run service once
#### Request
`POST /olympus/logevent/v1/event`

    curl -i -X POST -H 'Content-Type: application/json'  http://{server-domain|ip}/olympus/monitor/v1/event

#### Response
    HTTP/1.1 200 OK
    Content-Type: application/json
    Transfer-Encoding: chunked
    Date: Mon, 15 May 2023 05:00:55 GMT
    
    {
    "message": "Monitor check successfully",
    "details": 
    }
    
    
    
#### Server Logs output:
    
227 Entering Passive Mode (172,31,17,4,4,12).
LIST /ms.transferfile/inbound
150 Here comes the directory listing.
226 Directory send OK.
First file: -rw-------    1 1006     1006            0 May 24 17:58 empty-file1.txt
Calling logEvent service
EventVO [source=ms.monitor, status=2, msg=SUCCESS, file=]
LogEvent created with Status Code: 200 OK
Message: OK
Calling transferFile service
TransferFileVO [status=1, msg=SUCCESS, file=empty-file1.txt]
