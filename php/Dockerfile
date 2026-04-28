FROM php:8.2-fpm

WORKDIR /var/www/html

# Herramientas del sistema necesarias para Composer y extensiones PHP
RUN apt-get update && apt-get install -y \
    git \
    unzip \
    libzip-dev \
    && docker-php-ext-install pdo pdo_mysql zip \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

# Composer
COPY --from=composer:2 /usr/bin/composer /usr/bin/composer

# Instalar dependencias PHP (PHPMailer, etc.)
COPY composer.json ./
RUN composer install --no-dev --optimize-autoloader --no-interaction