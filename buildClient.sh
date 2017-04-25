#!/bin/bash
cd src/Client
javac -cp '.:commons-codec-1.7.jar' *.java
java LoginUI