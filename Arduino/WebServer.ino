#include <ESP8266WiFi.h>
#include <Servo.h>
#define forwardPin 5
#define backPin 4
const char* ssid = "aquaDroneCam";// назва wi-fi
const char* password = "228228337337";// пароль wi-fi
Servo myservo;
// Create an instance of the server
// specify the port to listen on as an argument
WiFiServer server(80);

void setup() {
  Serial.begin(115200);
  delay(10);
  myservo.attach(0);
  pinMode(backPin, OUTPUT);
  pinMode(forwardPin, OUTPUT);
  digitalWrite(forwardPin, 0);
  digitalWrite(backPin, 0);
  // Connect to WiFi network
  Serial.println();
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);

  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("");
  Serial.println("WiFi connected");

  // Start the server
  server.begin();
  Serial.println("Server started");

  // Print the IP address
  Serial.println(WiFi.localIP());
}

void loop() {
  // Check if a client has connected
  WiFiClient client = server.available();
  if (!client) {
    return;
  }

  // Wait until the client sends some data
  Serial.println("new client");
  while (!client.available()) {
    delay(1);
  }

  // Read the first line of the request
  String req = client.readStringUntil('\r');
  Serial.println(req);
  client.flush();

  // Match the request
  int goForward;
  int goBack;
  if (req.indexOf("/servo/0") != -1) {
    myservo.write(90);
  }
  else if (req.indexOf("/motor/1") != -1) {
    digitalWrite(forwardPin, 1);
    digitalWrite(backPin, 0);
  }
  else if (req.indexOf("/motor/2") != -1) {
    digitalWrite(forwardPin, 0);
    digitalWrite(backPin, 1);
  }
  else if (req.indexOf("/motor/0") != -1) {
    digitalWrite(forwardPin, 0);
    digitalWrite(backPin, 0);
  }
  else if (req.indexOf("/servo/left") != -1) {
    myservo.write(70);
  }
  else if (req.indexOf("/servo/right") != -1) {
    myservo.write(110);
  }

  else {
    myservo.write(0);
    digitalWrite(forwardPin, 0);
    digitalWrite(backPin, 0);
    Serial.println("invalid request");
    client.stop();
    return;
  }

  // Set GPIO2 according to the request
  client.flush();

  // Prepare the response
  String s = "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n<!DOCTYPE HTML>\r\n<html>\r\nLEDPIN is now ";

  // Send the response to the client
  client.print(s);
  delay(1);
  Serial.println("Client disonnected");

  // The client will actually be disconnected
  // when the function returns and 'client' object is detroyed
}
