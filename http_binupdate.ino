#include <WiFi.h>
#include <WebServer.h>
#include <ElegantOTA.h>

const char* ssid = "iotadmin";       // Your WiFi SSID
const char* password = "12345678";   // Your WiFi Password

WebServer server(80); // Create a web server on port 80

const int ledPin = 32; // GPIO pin where the LED is connected

void setup() {
  Serial.begin(115200);
  
  // Set the LED pin as output
  pinMode(ledPin, OUTPUT);

  // Connect to Wi-Fi
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Connecting to WiFi...");
  }
  
  // Display the connected IP address
  Serial.println("Connected to WiFi, IP address: ");
  Serial.println(WiFi.localIP());

  // Set up the web server route
  server.on("/", HTTP_GET, []() {
    server.send(200, "text/plain", "Hello, this is ESP32 OTA Web Server!");
  });

  // Start OTA with the web server
  ElegantOTA.begin(&server);  
  server.begin();  
  Serial.println("HTTP server started");
}

void loop() {
  server.handleClient();  // Handle client requests for the web server

  // Blink the LED
  digitalWrite(ledPin, HIGH);   // Turn the LED on
  delay(3000);                  // Wait for a second
  digitalWrite(ledPin, LOW);    // Turn the LED off
  delay(3000);                  // Wait for a second
  Serial.println("I am umar");
}
