# Stage 1: Build
FROM node:20-alpine AS build

WORKDIR /app

# Copiamos los archivos de definición de paquetes primero para aprovechar la caché de Docker
COPY package*.json ./

# Instalamos las dependencias
RUN npm install

# Copiamos el resto del código fuente
COPY . .

# Build production files
RUN npm run build

# Stage 2: Runtime
FROM nginx:alpine

# Limpiamos el directorio por defecto de Nginx
RUN rm -rf /usr/share/nginx/html/*

# Copiamos los archivos construidos desde la etapa anterior
COPY --from=build /app/dist /usr/share/nginx/html

# Exponemos el puerto 80 (el estándar de Nginx dentro del contenedor)
EXPOSE 80

# Comando por defecto para arrancar Nginx
CMD ["nginx", "-g", "daemon off;"]
