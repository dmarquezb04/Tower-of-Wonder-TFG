FROM php:8.2-fpm

WORKDIR /var/www/html

# Dependencias del sistema y extensiones PHP
RUN apt-get update && apt-get install -y \
    git \
    unzip \
    libzip-dev \
    && docker-php-ext-install pdo pdo_mysql zip \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

# Composer disponible como herramienta (las dependencias se instalan via bind mount)
COPY --from=composer:2 /usr/bin/composer /usr/bin/composer