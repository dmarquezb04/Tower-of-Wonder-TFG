#!/bin/bash

# Script de inicialización de base de datos para Railway
# Este script se ejecuta en el pre-deploy step

echo "--- INICIANDO INICIALIZACIÓN DE BASE DE DATOS ---"

# Verificación de variables (sin mostrar contraseñas por seguridad)
if [ -z "$DB_HOST" ]; then
  echo "ERROR: La variable DB_HOST no está definida."
  exit 1
fi

echo "Conectando a: $DB_HOST..."

# Ejecutamos el script SQL
# Usamos comillas para las variables para manejar caracteres especiales
mariadb -h "$DB_HOST" -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" < init.sql

if [ $? -eq 0 ]; then
  echo "--- BASE DE DATOS INICIALIZADA CORRECTAMENTE ---"
else
  echo "--- ERROR AL INICIALIZAR LA BASE DE DATOS ---"
  exit 1
fi
