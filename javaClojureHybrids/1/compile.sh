#!/bin/sh
lein clean
lein with-profile comp1 compile && lein with-profile comp2 compile
