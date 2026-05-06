-- Tower of Wonder - Database Initialization Script
-- Sincronizado con entidades Spring Boot (Fase 4)

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

-- --------------------------------------------------------
-- 1. Roles del sistema
-- --------------------------------------------------------
CREATE TABLE `roles` (
  `id_rol` int(11) NOT NULL AUTO_INCREMENT,
  `nombre_rol` varchar(50) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_rol`),
  UNIQUE KEY `nombre_rol` (`nombre_rol`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `roles` (`id_rol`, `nombre_rol`, `descripcion`) VALUES
(1, 'admin', 'Administrador con acceso total'),
(2, 'moderator', 'Moderador con permisos especiales'),
(3, 'user', 'Usuario normal del sistema');

-- --------------------------------------------------------
-- 2. Usuarios
-- --------------------------------------------------------
CREATE TABLE `usuarios` (
  `id_usuario` int(11) NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `username` varchar(50) DEFAULT NULL,
  `password_hash` varchar(255) NOT NULL,
  `two_fa_enabled` tinyint(1) DEFAULT 0,
  `twofa_secret` varchar(255) DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT 1,
  `recovery_token` varchar(100) DEFAULT NULL,
  `recovery_token_expiry` datetime DEFAULT NULL,
  `fecha_creacion` datetime DEFAULT current_timestamp(),
  `ultimo_login` datetime DEFAULT NULL,
  `id_rol` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `email` (`email`),
  KEY `idx_username` (`username`),
  KEY `id_rol` (`id_rol`),
  CONSTRAINT `fk_usuario_rol` FOREIGN KEY (`id_rol`) REFERENCES `roles` (`id_rol`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 3. Categorías de Productos
-- --------------------------------------------------------
CREATE TABLE `categories` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `categories` (`id`, `name`) VALUES
(1, 'Peluche'),
(2, 'Llavero'),
(3, 'Pegatina');

-- --------------------------------------------------------
-- 4. Productos (Tienda)
-- --------------------------------------------------------
CREATE TABLE `products` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `description` text DEFAULT NULL,
  `price` decimal(10,2) NOT NULL,
  `stock` int(11) NOT NULL DEFAULT 0,
  `image_url` varchar(255) DEFAULT NULL,
  `category_id` bigint(20) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  KEY `category_id` (`category_id`),
  CONSTRAINT `fk_product_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `products` (`name`, `description`, `price`, `stock`, `category_id`, `active`, `image_url`) VALUES
('Peluche de Kyra', 'Peluche de tu protagonista favorita, Kyra', 20.99, 999, 1, 1, '/assets/img/products/peluche_kyra.png'),
('Peluche de Lycaon', 'Peluche de tu interés amoroso favorito, Lycaon', 20.99, 999, 1, 1, '/assets/img/products/peluche_lycaon.png'),
('Peluche de Albion', 'Peluche de tu compañero de aventuras favorito, Albion', 20.99, 999, 1, 0, ''),
('Peluche de Alfimbio', 'Peluche de tu extremista favorito, Alfimbio', 20.99, 999, 1, 0, ''),
('Llavero de Kyra', 'Llavero de tu protagonista favorita, Kyra', 4.99, 999, 2, 1, ''),
('Llavero de Lycaon', 'Llavero de tu interés amoroso favorito, Lycaon', 4.99, 999, 2, 1, ''),
('Llavero de Albion', 'Llavero de tu compañero de aventuras favorito, Albion', 4.99, 999, 2, 1, ''),
('Llavero de Alfimbio', 'Llavero de tu extremista favorito, Alfimbio', 4.99, 999, 2, 1  , ''),
('Pegatina de Kyra', 'Pegatina de tu protagonista favorita, Kyra', 0.99, 999, 3, 1, ''),
('Pegatina de Lycaon', 'Pegatina de tu interés amoroso favorito, Lycaon', 0.99, 999, 3, 1, ''),
('Pegatina de Albion', 'Pegatina de tu compañero de aventuras favorito, Albion', 0.99, 999, 3, 1, ''),
('Pegatina de Alfimbio', 'Pegatina de tu extremista favorito, Alfimbio', 0.99, 999, 3, 1, '');

-- --------------------------------------------------------
-- 5. Pedidos y Detalles
-- --------------------------------------------------------
CREATE TABLE `shop_orders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `id_usuario` int(11) NOT NULL,
  `total_price` decimal(10,2) NOT NULL,
  `order_date` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `id_usuario` (`id_usuario`),
  CONSTRAINT `fk_order_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `shop_order_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  `quantity` int(11) NOT NULL,
  `price_at_purchase` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `order_id` (`order_id`),
  KEY `product_id` (`product_id`),
  CONSTRAINT `fk_item_order` FOREIGN KEY (`order_id`) REFERENCES `shop_orders` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_item_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 6. Newsletter (Subscriptores)
-- --------------------------------------------------------
CREATE TABLE `subscribers` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `confirmed` tinyint(1) NOT NULL DEFAULT 0,
  `confirmation_token` varchar(100) DEFAULT NULL,
  `subscribed_at` datetime DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 7. Métricas (Page Views)
-- --------------------------------------------------------
CREATE TABLE `page_views` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `url` varchar(255) NOT NULL,
  `fecha` datetime NOT NULL,
  `ip` varchar(45) DEFAULT NULL,
  `navegador` varchar(255) DEFAULT NULL,
  `zona` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 8. Seguridad (JWT Blacklist)
-- --------------------------------------------------------
CREATE TABLE `jwt_blacklist` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `token_jti` varchar(64) NOT NULL,
  `fecha_expiracion` datetime NOT NULL,
  `fecha_revocacion` datetime DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `token_jti` (`token_jti`),
  KEY `idx_expiracion` (`fecha_expiracion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 9. Mensajes de Contacto
-- --------------------------------------------------------
CREATE TABLE `contact_messages` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `email` varchar(255) NOT NULL,
  `asunto` varchar(150) DEFAULT NULL,
  `mensaje` text NOT NULL,
  `fecha_creacion` datetime DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 10. Personajes del Juego
-- --------------------------------------------------------
CREATE TABLE `game_characters` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `slug` varchar(100) NOT NULL,
  `description` text DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `slug` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 10b. Imágenes de Personajes (lista dinámica, sin límite)
-- --------------------------------------------------------
CREATE TABLE `character_images` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `character_id` bigint(20) NOT NULL,
  `image_url` varchar(255) NOT NULL,
  `sort_order` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `character_id` (`character_id`),
  CONSTRAINT `fk_char_image` FOREIGN KEY (`character_id`) REFERENCES `game_characters` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 11. Noticias / Posts del Blog
-- --------------------------------------------------------
CREATE TABLE `news_posts` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `slug` varchar(255) NOT NULL,
  `content` text NOT NULL,
  `summary` varchar(500) DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `published_at` datetime DEFAULT current_timestamp(),
  `active` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `slug` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
