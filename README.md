# Voorbeeld TTN MQTT broker app

Deze Andriod app laat zien hoe er met de MQTT broker van The Things Network kan worden verbonden.
De app maakt gebruik van de [Paho](http://www.eclipse.org/paho/clients/android/) library om te verbinden over MQTT.

Voor de app verbinding kan maken met de MQTT broker. 
In the MainActivity zijn er drie waardes die moeten worden ingevuld om 

```
public class MainActivity extends AppCompatActivity implements MQTTService.MQTTMessageListener {

    private MQTTService mqttService;

    //TODO: Configure these properties to connect to MQTT broker.
    // You can find the values in the TTN dashboard.
    private final String region = "eu";
    private final String applicationId = "";
    private final String applicationAccessKey = "";

    ...
}
```

Nadat de app is geconfigureerd zal het bij het opstarten meteen verbinden met de TTN MQTT broker.
Wanneer een LoRa bericht wordt ontvangen door TTN zal het ook verschijnen in de lijst in de app.
Indien het bericht door een payload functie is verwerkt dan zal de payload (JSON) in de lijst verschijnen,
anders zal de 'raw' payload worden getoond.
