# Etapa 1: Construcción (Build)
# Usamos una imagen ligera de Node.js para instalar dependencias y compilar
FROM node:20-alpine AS build

WORKDIR /app

# Copiamos los archivos de definición de paquetes primero para aprovechar la caché de Docker
COPY package*.json ./

# Instalamos las dependencias
RUN npm install

# Copiamos el resto del código fuente
COPY . .

# Construimos la aplicación para producción (genera la carpeta /dist)
RUN npm run build

# Etapa 2: Servidor (Runtime)
# Usamos Nginx para servir los archivos estáticos generados
FROM nginx:alpine

# Limpiamos el directorio por defecto de Nginx
RUN rm -rf /usr/share/nginx/html/*

# Copiamos los archivos construidos desde la etapa anterior
COPY --from=build /app/dist /usr/share/nginx/html

# Exponemos el puerto 80 (el estándar de Nginx dentro del contenedor)
EXPOSE 80

# Comando por defecto para arrancar Nginx
CMD ["nginx", "-g", "daemon off;"]
