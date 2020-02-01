#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>
#include <ESP8266SSDP.h>

#ifndef STASSID
#define STASSID "Pleasure"
#define STAPSK  "bingbing"
#endif

const char* ssid = STASSID;
const char* password = STAPSK;

ESP8266WebServer HTTP(80);

const int ledPin = 13;
const int buttonPin = 5;
int lastTimePressed = 0;
bool buttonBlock = false;
int blockTime = 2000;

void setup() {
  Serial.begin(9600);
  Serial.println();
  initPins();
  initHTTP();
}

void initHTTP(){
  Serial.println("Starting WiFi...");

  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);
  if (WiFi.waitForConnectResult() == WL_CONNECTED) {

    Serial.printf("Starting HTTP...\n");
    HTTP.on("/", HTTP_GET, []() {
      String json = prepJson();
      HTTP.send(200, "application/json", json);
    });
    HTTP.on("/description.xml", HTTP_GET, []() {
      SSDP.schema(HTTP.client());
    });
    HTTP.begin();

    Serial.printf("Starting SSDP...\n");
    SSDP.setSchemaURL("description.xml");
    SSDP.setHTTPPort(80);
    SSDP.setName("Button1");
    SSDP.begin();

    Serial.printf("Ready!\n");
  } else {
    Serial.printf("WiFi Failed\n");
    while (1) {
      delay(100);
    }
  }
}

String prepJson(){
  return "{\"time\": " + String(millis()) + ", \"lasttimepressed\": " + String(lastTimePressed) + "}";
}

void initPins() {
  pinMode(buttonPin, INPUT_PULLUP);
  pinMode(ledPin, OUTPUT);
  digitalWrite(ledPin, LOW);
}

void loop() {
  HTTP.handleClient();
  delay(1);
  if (!buttonBlock && !digitalRead(buttonPin)){
    buttonBlock = true;
    lastTimePressed = millis();
    Serial.println("Pressed");
  }

  if (buttonBlock) {
    if (millis() - lastTimePressed > blockTime){
      buttonBlock = false;
      Serial.println("Ready to press again");
    }
  }
}
