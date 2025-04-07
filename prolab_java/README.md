# İzmit Ulaşım Sistemi

Bu proje, İzmit'teki toplu taşıma sistemini simüle eden bir Java uygulamasıdır. Kullanıcılar, farklı ulaşım seçeneklerini kullanarak rota planlaması yapabilir ve en uygun güzergahı bulabilirler.

## Özellikler

- Otobüs, tramvay ve taksi seçenekleri
- Farklı yolcu tipleri (genel, öğrenci, öğretmen, yaşlı)
- Çeşitli ödeme yöntemleri (nakit, kredi kartı, Kent kartı)
- Harita üzerinde rota görselleştirme
- Durak ve koordinat bazlı rota planlama
- Maliyet, mesafe ve süre hesaplamaları

## Gereksinimler

- Java 11 veya üzeri
- Maven 3.6 veya üzeri
- JavaFX 16
- MapJFX 3.0.0

## Kurulum

1. Projeyi klonlayın:
```bash
git clone https://github.com/yourusername/izmit-transportation.git
cd izmit-transportation
```

2. Maven bağımlılıklarını yükleyin:
```bash
mvn clean install
```

3. Uygulamayı çalıştırın:
```bash
mvn javafx:run
```

## Proje Yapısı

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── izmit/
│   │           └── transportation/
│   │               ├── TransportationApp.java
│   │               ├── TransportationSystem.java
│   │               ├── gui/
│   │               │   └── TransportationGUI.java
│   │               └── models/
│   │                   ├── Location.java
│   │                   ├── Stop.java
│   │                   ├── Vehicle.java
│   │                   ├── Bus.java
│   │                   ├── Tram.java
│   │                   ├── Taxi.java
│   │                   ├── Passenger.java
│   │                   ├── PaymentMethod.java
│   │                   ├── RouteSegment.java
│   │                   └── RouteOption.java
│   └── resources/
│       ├── fxml/
│       │   └── transportation_gui.fxml
│       └── Duraklar.json
└── test/
    └── java/
        └── com/
            └── izmit/
                └── transportation/
                    └── tests/
```

## Kullanım

1. Uygulamayı başlatın
2. Başlangıç noktasını seçin (koordinat veya durak)
3. Bitiş noktasını seçin (koordinat veya durak)
4. Yolcu tipini seçin
5. Ödeme yöntemini seçin
6. "Rota Hesapla" butonuna tıklayın
7. Harita üzerinde rotayı görüntüleyin ve detayları inceleyin

## Test

Testleri çalıştırmak için:
```bash
mvn test
```

## Lisans

Bu proje MIT lisansı altında lisanslanmıştır. Detaylar için [LICENSE](LICENSE) dosyasına bakın.

## Katkıda Bulunma

1. Bu depoyu fork edin
2. Yeni bir branch oluşturun (`git checkout -b feature/amazing-feature`)
3. Değişikliklerinizi commit edin (`git commit -m 'Add some amazing feature'`)
4. Branch'inizi push edin (`git push origin feature/amazing-feature`)
5. Bir Pull Request oluşturun 