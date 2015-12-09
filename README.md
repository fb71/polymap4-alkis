## ALKIS-Auskunft für [Polymap4](http://github.com/Polymap4)

Näheres zur Bedienung im [Benutzerhandbuch](https://github.com/fb71/polymap4-alkis/wiki).

### Import und Datenhaltung

Die ALKIS-Daten müssen im [NAS-Format](https://de.wikipedia.org/wiki/Normbasierte_Austauschschnittstelle) bereitstehen. Der Import der ALKIS-Daten erfolgt über den [NAS-Treiber in GDAL/OGR](http://www.gdal.org/drv_nas.html). Zur Vereinfachung des Imports kann [norGIS ALKIS Import](https://github.com/norBIT/alkisimport#norgis-alkis-import) verwendet werden. Als Datenhaltung wird typischerweise eine **PostgreSQL**/PostGIS Datenbank verwendet.

### Modellierung

Die ALKIS-Auskunft bezieht sich auf das Schema, welches vom NAS-Treiber in OGR erzeugt wird. Die Modellierung der Daten erfolgt nach der **GeoInfoDoc V6.0.1** und dem **ALKIS-Objektkatalog V6.0**. Das interne Modell wird mit dem [Model2 Framework](https://github.com/Polymap4/polymap4-model) umgesetzt, welches Bestandteil von Polymap4 ist.

Das kommentierte interne Modell kann im [Quellcode](https://github.com/fb71/polymap4-alkis/tree/master/plugins/org.polymap.alkis/src/org/polymap/alkis/model) eingesehen werden.

### Abfragen und Volltextsuche

Neben einem Suchformular ist eine Volltextsuche umgesetzt. Diese beinhaltet eine Vorschlagsfunktion und spezielle Notation für die Angabe und Abfrage von Flurstücksnummern. Der gesamte Datenbestand kann damit sehr leicht und natürlich abgefragt werden, ohne das spezielles Wissen über den internen Aufbau von ALKIS vorhanden sein muss.

Näheres zur Volltextsuche und deren Bedienung im [Benutzerhandbuch](https://github.com/fb71/polymap4-alkis/wiki).
