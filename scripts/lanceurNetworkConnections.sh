#!/bin/bash

#sortir de data vers src
cd ../src/

#compiler networkConnections
javac networkConnections/*.java

#lancer le Conveyor
xterm -geometry 60x20+0+0  -e 'java networkConnections/NetworkConnectionsMain "Conveyor"' &

#lancer le PainterRobot1
xterm -geometry 60x20+400+0 -e 'java networkConnections/NetworkConnectionsMain "PainterRobot1"' &
 
#lancer le PainterRobot2
xterm -geometry 60x20+0+300 -e 'java networkConnections/NetworkConnectionsMain "PainterRobot2"' &

#lancer le DecisionCenter
xterm -geometry 60x20+400+300  -e 'java networkConnections/NetworkConnectionsMain "DecisionCenter"' & 

#lancer le ShadeChanger
xterm -geometry 60x20+800+0 -e 'java networkConnections/NetworkConnectionsMain "ShadeChanger"' &
