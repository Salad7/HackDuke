/*

Code for ISD1820 Voice Recording Module
by eLab Peers (C) 2014.

Visit us at:
http://www.elabpeers.com

All rights reserved.

Wiring:
1.  VCC to 5V on Arduino board
2.  GND to GND on Arduino board
3.  REC to Pin 11 on Arduino board
4.  P-E to Pin 13 on Arduino board

*/
#include <SoftwareSerial.h>
int Rec = 11;
int Play = 13;
SoftwareSerial BTserial(10, 11); // RX | TX
int sensorPin = A0;
int sensorValue = 0;

void setup()
{ 
  pinMode(Rec, OUTPUT);
  pinMode(Play, OUTPUT);
  BTserial.begin(9600);
}

void loop()
{
  sensorValue = analogRead(sensorPin);
  digitalWrite(Rec, HIGH);
  delay(10000);
  digitalWrite(Rec, LOW);
  delay(5000);
  digitalWrite(Play, HIGH);
  delay(100);
  digitalWrite(Play, LOW);
  delay(10000);
  BTserial.print("1234");
  BTserial.print(",");
  BTserial.print("1234.0");
  BTserial.print(",");
  BTserial.print("1234 hPa");
  BTserial.print(",");
  BTserial.print("500 ml/s");
  BTserial.print(",");
  BTserial.print(sensorValue);
  BTserial.print(";");
  //message to the receiving device
  delay(20);
}  



