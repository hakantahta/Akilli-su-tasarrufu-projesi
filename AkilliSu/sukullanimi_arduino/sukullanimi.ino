#include <Arduino.h>
#include <ESP8266WiFi.h>
#include <Firebase_ESP_Client.h>
#include <addons/TokenHelper.h>
#include <addons/RTDBHelper.h>

/* Wifi Bağlantı Bilgileri */
#define WIFI_SSID "Hakan Tahta" // Wifi Adı
#define WIFI_PASSWORD "hkn123456"        // Wifi Şifresi

/* Firebase Api Key */
#define API_KEY "AIzaSyAZoZ7lN2AJw5bjWgGf7UA6VCYM6WT96SI"

/* RealTime Database url */
#define DATABASE_URL "https://suproje-4844a-default-rtdb.firebaseio.com/"

/* Oluşturulan Kullanıcı E-mail Ve Şifresi */
#define USER_EMAIL "tahtahakan54@gmail.com"
#define USER_PASSWORD "123456"

// Define Firebase Data object
FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;

unsigned long sendDataPrevMillis = 0;

const int YFS201pin = D1; // Su akış sensörünün bağlı olduğu pin

// Veritabanına kaydedecek olduğumuz değişken tanımları
float suAkis;           // Su akış sensöründen gelen bilgi (litre cinsinden)
float suKullanimLitre = 0; // Su kullanımını litre cinsinden tutacak sayaç

void setup()
{
  Serial.begin(115200);

  // Wifi bağlantısını burada yapıyoruz ve cihaz bağlanasıya kadar bekliyoruz
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting to Wi-Fi");
  unsigned long ms = millis();
  while (WiFi.status() != WL_CONNECTED)
  {
    Serial.print(".");
    delay(300);
  }
  Serial.println();
  Serial.print("Connected with IP: ");
  Serial.println(WiFi.localIP());
  Serial.println();

  Serial.printf("Firebase Client v%s\n\n", FIREBASE_CLIENT_VERSION);

  /* Assign the api key (required) */
  config.api_key = API_KEY;

  /* Assign the user sign-in credentials */
  auth.user.email = USER_EMAIL;
  auth.user.password = USER_PASSWORD;

  /* Assign the RTDB URL (required) */
  config.database_url = DATABASE_URL;

  /* Assign the callback function for the long-running token generation task */
  config.token_status_callback = tokenStatusCallback; // see addons/TokenHelper.h

  fbdo.setBSSLBufferSize(4096, 1024);

  fbdo.setResponseSize(2048);

  Firebase.begin(&config, &auth);

  Firebase.setDoubleDigits(5);

  config.timeout.serverResponse = 10 * 1000;

  pinMode(YFS201pin, INPUT);
}

void loop()
{
  // Su akış sensöründen gelen değeri oku
  suAkis = pulseIn(YFS201pin, HIGH); // Bu değeri doğrudan su kullanımı olarak kabul edebilirsiniz, ancak sensörün spesifikasyonlarına göre düzeltebilirsiniz.

  // Su kullanımını litre cinsinden güncelle
  suKullanimLitre += suAkis/100000;

  // Firebase'e sadece su akış sensöründen veri alındığında gönder
  if (suAkis > 0)
  {
    if (Firebase.ready() && (millis() - sendDataPrevMillis > 5000 || sendDataPrevMillis == 0))
    {
      sendDataPrevMillis = millis();

      // "su_kullanimi" düğümüne su kullanımını litre cinsinden gönder
      Firebase.RTDB.setFloat(&fbdo, F("/su_kullanimi"), suKullanimLitre);
      Serial.println("Su Kullanımı değeri Firebase'e gönderildi: " + String(suKullanimLitre) + " litre");

      Serial.println();
    }
  }

  delay(1000); // Gereksiz yüklenmeyi önlemek için kısa bir gecikme ekleyebilirsiniz
}
