#include <WiFi.h>
#include <WebServer.h>    // Include WebServer instead of AsyncWebServer
#include <ElegantOTA.h>   // Use ElegantOTA for OTA updates

const char* ssid = "iotadmin";       // Replace with your WiFi SSID
const char* password = "12345678"; // Replace with your WiFi password

WebServer server(80); // Use WebServer instead of AsyncWebServer

void setup() {
  Serial.begin(115200);

  // Connect to Wi-Fi
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Connecting to WiFi...");
  }
  Serial.println("Connected to WiFi, IP address: ");
  Serial.println(WiFi.localIP());

  // Set up a basic route for the web server
  server.on("/", HTTP_GET, []() {
    server.send(200, "text/plain", "Hello, this is ESP32 OTA Web Server!");
  });

  // Start ElegantOTA with WebServer
  ElegantOTA.begin(&server);  
  server.begin();  // Start the web server
  Serial.println("HTTP server started");
}

void loop() {
  server.handleClient();  
}
