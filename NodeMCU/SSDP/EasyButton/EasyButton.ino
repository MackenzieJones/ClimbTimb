#include <ESP8266WiFi.h>
#include <WiFiClient.h>
#include <ESP8266WiFiMulti.h> 
#include <ESP8266mDNS.h>
#include <ESP8266WebServer.h>   // Include the WebServer library

ESP8266WiFiMulti wifiMulti;     // Create an instance of the ESP8266WiFiMulti class, called 'wifiMulti'

ESP8266WebServer server(80);    // Create a webserver object that listens for HTTP request on port 80

String ssid = "Pete's a Jones";
String pass = "nicebutwhole";

const int buttonPin = 5;
int lastTimePressed = 0;
bool buttonBlock = false;
int blockTime = 2000;

MDNSResponder mdns;

void setup(void){
  Serial.begin(9600);         // Start the Serial communication to send messages to the computer
  delay(10);
  Serial.println('\n');

  wifiMulti.addAP(ssid.c_str(), pass.c_str());

  Serial.println("Connecting ...");
  int i = 0;
  while (wifiMulti.run() != WL_CONNECTED) { // Wait for the Wi-Fi to connect: scan for Wi-Fi networks, and connect to the strongest of the networks above
    delay(250);
    Serial.print('.');
  }
  Serial.println('\n');
  Serial.print("Connected to ");
  Serial.println(WiFi.SSID());
  Serial.print("IP address:\t");
  Serial.println(WiFi.localIP());

  if (MDNS.begin("easy", WiFi.localIP())) {
    mdns.addService("http", "tcp", 80);
    Serial.println("mDNS responder started");
  } else {
    Serial.println("Error setting up MDNS responder!");
  }

  server.on("/", handleRoot);
  server.onNotFound(handleRoot);
  server.begin();
  Serial.println("HTTP server started");
}

void handleRoot() {
  String json = "{\"time\": " + String(millis()) + ", \"lasttimepressed\": " + String(lastTimePressed) + "}";
  server.send(200, "application/json", json);
}


void loop(void){
  server.handleClient();
  buttonLogic();
    MDNS.update();
  
}

void buttonLogic(){
  if (!buttonBlock && !digitalRead(buttonPin)){
    buttonBlock = true;
    lastTimePressed = millis();
    Serial.println("Pressed");
  }

  if (buttonBlock && millis() - lastTimePressed > blockTime) { //If button block is on, the player released the button push, and blockTime time has elapsed
    buttonBlock = false;
    Serial.println("Ready to press again");
  }
}
