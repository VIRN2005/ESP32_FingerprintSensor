#include <WiFi.h>

// Reemplazar con credenciales de WiFi
const char* ssid = "SSID";          //CAMBIAR ESTOOOO
const char* password = "PASSWORD";  //Y TAMBIÉN ESTO OTRO
const int port = 80;

WiFiServer server(port);

void setup() {
  // Depurción del Puerto
  Serial.begin(115200);

  // Conecta el ESP32 al WiFi
  WiFi.begin(ssid, password);

  // Espera a que se conecte a la red WiFi
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("Connected to WiFi");
  Serial.print("IP Address: ");
  Serial.println(WiFi.localIP());

  server.begin();
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

    // Envía una respuesta al cliente
    client.println("HTTP/1.1 200 OK");
    client.println("Content-Type: text/plain");
    client.println("Connection: close");
    client.println();
    client.println("Message received");

    // Cierra la conexión
    client.stop();
    Serial.println("Client disconnected");
  }
}
