#!/bin/bash

export APP_SECRET=lemonpi
rm accounts.db
lein do clean, repl
