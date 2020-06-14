#!/bin/bash

#sortir de data vers src
cd ../src/

#compiler networkConnections
javac networkConnections/*.java

#compiler tank
javac tank/*.java 

#compiler machine
javac machine/*.java

#compiler partToPaint
javac partToPaint/*.java

#compiler exchangeMonitor
javac exchangeMonitor/*.java

#compiler painterRobot
javac painterRobot/*.java

#compiler conveyor
javac conveyor/*.java

#compiler shadeChanger
javac shadeChanger/*.java

#compiler decisionCenter
javac decisionCenter/*.java

#lancer le Conveyor
xterm -hold -geometry 48x16+300+0  -e 'java conveyor/ConveyorMain' &

#lancer le PainterRobot1
xterm -hold -geometry 95x30+0+250 -e 'java painterRobot/PainterRobotMain "PainterRobot1"' &

 
#lancer le PainterRobot2
xterm -hold -geometry 95x30+600+250 -e 'java painterRobot/PainterRobotMain "PainterRobot2"' &

#lancer le DecisionCenter
xterm -hold -geometry 48x16+0+0  -e 'java decisionCenter/DecisionCenterMain' & 

#lancer le ShadeChanger
xterm -hold  -geometry 48x16+600+0 -e 'java shadeChanger/ShadeChangerMain' &

#lancer le ExchangeMonitor
xterm -hold -geometry 48x16+900+0 -e 'java exchangeMonitor/ExchangeMonitorMain' &
