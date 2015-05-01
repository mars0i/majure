#!/bin/sh
lein clean
lein with-profile precomp compile && lein with-profile midcomp compile
