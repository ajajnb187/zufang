-- 修改数据库字段长度以支持Base64图片
-- Base64编码的图片数据会比原始URL长很多，需要使用LONGTEXT类型

-- 1. 修改用户表的头像字段
ALTER TABLE users MODIFY COLUMN avatar_url LONGTEXT COMMENT '用户头像Base64';

-- 2. 修改房源图片表的图片URL字段
ALTER TABLE house_images MODIFY COLUMN image_url LONGTEXT COMMENT '图片Base64';

-- 3. 修改文件上传表的文件URL字段
ALTER TABLE file_uploads MODIFY COLUMN file_url LONGTEXT COMMENT '文件Base64';

-- 验证修改
SHOW COLUMNS FROM users LIKE 'avatar_url';
SHOW COLUMNS FROM house_images LIKE 'image_url';
SHOW COLUMNS FROM file_uploads LIKE 'file_url';
