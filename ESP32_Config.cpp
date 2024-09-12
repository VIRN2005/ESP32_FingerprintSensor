#include <WiFi.h>
#include <ESP32Servo.h>

// Reemplazar con credenciales de WiFi
const char* ssid = "SSID";
const char* password = "PASSWORD";
WiFiServer server(80);

Servo myServo;  // Crear objeto servo
// Pines y ángulos
const int servoPin = 12;       // Cambiar al pin adecuado si es necesario
const int openAngle = 90;     // Ángulo para abrir la puerta
const int closeAngle = 0;     // Ángulo para cerrar la puerta
const int delayTime = 5000;   // Tiempo que la puerta estará abierta (en milisegundos)

void setup() {
  // Depuración del Puerto
  Serial.begin(115200);

  // Conectar el ESP32 al WiFi
  WiFi.begin(ssid, password);

  // Espera a que se conecte a la red WiFi
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("Connected to WiFi");
  Serial.print("IP Address: ");
  Serial.println(WiFi.localIP());

  // Inicializar el servidor
  server.begin();

  // Inicializar el servo
  myServo.attach(servoPin);
  myServo.write(closeAngle);  // Asegurarse de que el servo comienza en la posición de cerrado
}

void loop() {
  // Espera a que haya un cliente conectado
  WiFiClient client = server.available();

  if (client) {
    Serial.println("Client connected");

    // Lee los datos
    String request = "";
    while (client.available()) {
      char c = client.read();
      request += c;
    }

    // Procesa el mensaje recibido
    Serial.println("Received message:");
    Serial.println(request);

    // Mover el servo para abrir la puerta
    Serial.println("Opening door...");
    myServo.write(openAngle);

    // Esperar un tiempo y luego cerrar la puerta
    delay(delayTime);
    Serial.println("Closing door...");
    myServo.write(closeAngle);

    // Envía una respuesta al cliente
    client.println("HTTP/1.1 200 OK");
    client.println("Content-Type: text/plain");
    client.println("Connection: close");
    client.println();
    client.println("Door opened and closed");

    // Cierra la conexión
    client.stop();
    Serial.println("Client disconnected");
  }
}
