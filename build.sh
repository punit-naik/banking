#!/bin/bash

rm accounts.db
lein do clean, ring uberjar
