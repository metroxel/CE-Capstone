#include <WiFi101.h>
#include <WiFiClient.h>
#include <WiFiServer.h>
#include <WiFiSSLClient.h>
#include <WiFiUdp.h>

// http://stackoverflow.com/questions/21287087/maintaining-communication-between-arduino-and-java-program

#include <SPI.h>
//#include <WiFi.h>

int status = WL_IDLE_STATUS;
// update depending on what WIFI we are using
//char ssid[] = "iPhone (3)";
//char pass[] = "123456789";

char ssid[] = "Verizon-SM-G900V-D376";
char pass[] = "ehmq943!";


IPAddress remoteIp(172,20,10,5);

int port = 37899;

// PINS NEED TO BE UPDATED!
int speed1 = 12;
int motor1 = 13;
int speed2 =11;
int motor2 = 10;

String message = "";

long lastSent;

WiFiClient client;

void setup()
{
    // start the serial for debugging
    Serial.begin(9600);
    pinMode(9, OUTPUT);
    digitalWrite(9, LOW);

    //check if the wifi shield is present
    if(WiFi.status() == WL_NO_SHIELD){
        Serial.println("WiFi shield not present! Press reset to try again.");
        while(true); //stops the program
    }

    connectWiFi();
    printWifiStatus();
    connectClient(3);
}

void loop(){

    if(client){

      // sends data to the server every 15 seconds
      if(millis() >= (lastSent + 15000))
      {
        sendCharacter('*');
      }
        if(client.available()){

            char c = client.read();

            if(c != '\n'){
                message += c;
            }
            else{
                Serial.println("Received message: "+message);
                checkMessage();
                sendMessage(message);
                message = "";
            }
        }
    }
}

void printWifiStatus() {
    // print the SSID of the network you're attached to:
    Serial.print("SSID: ");
    Serial.println(WiFi.SSID());

    // print your WiFi shield's IP address:
    IPAddress ip = WiFi.localIP();
    Serial.print("IP Address: ");
    Serial.println(ip);
}

void connectWiFi(){

    if( status != WL_CONNECTED){
        while(status != WL_CONNECTED){

            Serial.print("Attempting connection to network...");

            status = WiFi.begin(ssid, pass);
            delay(3000);

            if(status == WL_CONNECTED){
                Serial.println(" SUCSESS");
            }
            else{
                Serial.println(" FAILED");
                delay(3000);
                connectWiFi();
            }
        }
    }   
}

void connectClient(int retries){

    //Attempt connection to server

    if(retries <= 0){
        Serial.println("FAILED");
        Serial.println("Connection to server failed.");
        while(true);
    }

    Serial.print("Attempting conenction to server... ");

    if(client.connect(remoteIp, port)){
        Serial.println("SUCSESS");
        sendMessage("Hello server!");
    }
    else if(retries > 0){
        Serial.println("FAILED");
        connectClient(retries - 1);
    }

}

void checkMessage(){

    if(message == "on"){
        digitalWrite(9, HIGH);
    }

    if(message == "off"){
        digitalWrite(9, LOW);
    }

    pinMode(motor1, OUTPUT);
    pinMode(motor2,OUTPUT);
    
    // commands for moving the robot
    
    // turn left
    if(message == "left"){
      digitalWrite(motor1, LOW);
      digitalWrite(motor2, HIGH);
      analogWrite(speed1, 0);
      analogWrite(speed2, 200);
      delay(5000);
    }

    // turn right
    if(message == "right"){
      digitalWrite(motor1, HIGH);
      digitalWrite(motor2, LOW);
      analogWrite(speed1, 200);
      analogWrite(speed2, 0);
      delay(5000);
    }
    
    // turn forward
    if(message == "up"){
      digitalWrite(motor1, HIGH);
      digitalWrite(motor2, HIGH);
      analogWrite(speed1, 100);
      analogWrite(speed2, 100);
      delay(5000);
    }

    
    // turn backward
    if(message == "down"){
      digitalWrite(motor1, LOW);
      digitalWrite(motor2, LOW);
      analogWrite(speed1, 100);
      analogWrite(speed2, 100);
      delay(5000);
    }

    // stop
    if(message == "stop"){
      digitalWrite(motor1, LOW);
      digitalWrite(motor2, LOW);
      analogWrite(speed1, 0);
      analogWrite(speed2, 0);  
    }
}

void sendMessage(String toSend){

    if(client){
        client.println(toSend+'\n');
        client.flush();
        Serial.println("Sendt message: "+toSend);
    }
    else{
        Serial.println("Could not send message; Not connected.");
    }
}

void sendCharacter(char toSend){
  if(client){
      client.println(toSend);
      lastSent = millis();
    }
    else {
      Serial.println("Could not send character!");
    }
  }
