#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>
#include <ESP8266SSDP.h>

#ifndef STASSID
#define STASSID "imperial moist towelettes"
#define STAPSK  "markjones"
#endif

const char* ssid = STASSID;
const char* password = STAPSK;

ESP8266WebServer HTTP(80);

int lastTimePressed;

void setup() {
  Serial.begin(9600);
  Serial.println();
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

void loop() {
  HTTP.handleClient();
  delay(1);
}
