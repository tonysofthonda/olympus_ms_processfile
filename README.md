
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

5. If errors ocurss during the line process, proper logs will be sent and line will be be invalidated.

6. The expected result at the end of file processing will new created or updated rows in Data Base, and service wil send a response like:

{
    "message": "File processed successsfully",
    "details": 
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

### 2. Process one File
#### Request
`POST /olympus/logevent/v1/event`

    curl -i -X POST -H 'Content-Type: application/json' -d { "status": 1,"msg":"SUCCESS", "file": "{fileName}" } http://{server-domain|ip}/olympus/monitor/v1/event
    
   

#### Response
    HTTP/1.1 200 OK
    Content-Type: application/json
    Transfer-Encoding: chunked
    Date: Mon, 15 May 2023 05:00:55 GMT
    
    {
    "message": "File processed successsfully",
    "details": 
}
    
    
    
#### Server Logs output (Create operation example):
    
 ----------------- Operation: CREATE --------------------------
Start:: Create Flow
Hibernate: select afefixedor0_.id as id1_2_, afefixedor0_.ack_id as ack_id2_2_, afefixedor0_.action_id as action_i3_2_, afefixedor0_.chrg_asct as chrg_asc4_2_, afefixedor0_.chrg_fcm as chrg_fcm5_2_, afefixedor0_.create_timestamp as create_t6_2_, afefixedor0_.due_date as due_date7_2_, afefixedor0_.envio_flag as envio_fl8_2_, afefixedor0_.extern_config_id as extern_c9_2_, afefixedor0_.model_color_id as model_c10_2_, afefixedor0_.order_number as order_n11_2_, afefixedor0_.order_type as order_t12_2_, afefixedor0_.origin_type as origin_13_2_, afefixedor0_.request_id as request14_2_, afefixedor0_.selling_code as selling15_2_, afefixedor0_.ship_fcm as ship_fc16_2_, afefixedor0_.ship_sct as ship_sc17_2_, afefixedor0_.start_day as start_d18_2_, afefixedor0_.status_ev_id as status_19_2_, afefixedor0_.update_timestamp as update_20_2_ from afedb.afe_fixed_orders_ev afefixedor0_ where afefixedor0_.id=?
Hibernate: select afemodelco0_.id as id1_4_, afemodelco0_.color_auto_id as color_au2_4_, afemodelco0_.color_id as color_id3_4_, afemodelco0_.create_timestamp as create_t4_4_, afemodelco0_.model_id as model_id5_4_ from afedb.afe_model_color afemodelco0_ where afemodelco0_.id=?
Hibernate: select afemodelen0_.id as id1_3_, afemodelen0_.afe_record_status_id as afe_reco2_3_, afemodelen0_.code as code3_3_, afemodelen0_.create_timestamp as create_t4_3_, afemodelen0_.description as descript5_3_, afemodelen0_.division_id as division6_3_, afemodelen0_.door_quantity as door_qua7_3_, afemodelen0_.engine_cilynder_quantity as engine_c8_3_, afemodelen0_.engine_type_number as engine_t9_3_, afemodelen0_.exclud as exclud10_3_, afemodelen0_.fuel_type_code as fuel_ty11_3_, afemodelen0_.leng_size as leng_si12_3_, afemodelen0_.model_type_id as model_t13_3_, afemodelen0_.model_year as model_y14_3_, afemodelen0_.packing_weight as packing15_3_, afemodelen0_.plant_id as plant_i16_3_, afemodelen0_.update_timestamp as update_17_3_, afemodelen0_.volumen_size as volumen18_3_, afemodelen0_.weight as weight19_3_, afemodelen0_.width_size as width_s20_3_ from afedb.afe_model afemodelen0_ where afemodelen0_.id=?
Hibernate: select afemodelty0_.id as id1_5_, afemodelty0_.create_timestamp as create_t2_5_, afemodelty0_.description as descript3_5_, afemodelty0_.model_type as model_ty4_5_, afemodelty0_.serie as serie5_5_, afemodelty0_.update_timestamp as update_t6_5_ from afedb.afe_model_type afemodelty0_ where afemodelty0_.id=?
Hibernate: select afeactione0_.id as id1_0_, afeactione0_.action as action2_0_, afeactione0_.create_timestamp as create_t3_0_, afeactione0_.update_timestamp as update_t4_0_ from afedb.afe_action afeactione0_ where afeactione0_.action=?
Hibernate: insert into afedb.afe_fixed_orders_ev (ack_id, action_id, chrg_asct, chrg_fcm, create_timestamp, due_date, envio_flag, extern_config_id, model_color_id, order_number, order_type, origin_type, request_id, selling_code, ship_fcm, ship_sct, start_day, status_ev_id, update_timestamp) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
Hibernate: insert into afedb.afe_orders_history (action_id, create_timestamp, fixed_order_id) values (?, ?, ?)
Calling logEvent service
LogEvent created with Status Code: 200 OK
Message: OK
Notification sent with Status Code: 200 OK
Message: Notification successfully sent
End:: Create Flow
