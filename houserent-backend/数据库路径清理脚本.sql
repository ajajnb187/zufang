-- ====================================================================
-- 图片路径数据清理脚本
-- 用途：清理数据库中错误格式的图片路径
-- 执行时机：在重启服务器之前执行，确保数据格式正确
-- ====================================================================

-- 1. 清理用户头像URL（去掉 api/img/ 前缀）
UPDATE users 
SET avatar_url = CASE
    -- 如果以 api/img/ 开头，去掉这个前缀
    WHEN avatar_url LIKE 'api/img/%' THEN SUBSTRING(avatar_url, 9)
    -- 如果以 /api/img/ 开头，去掉这个前缀
    WHEN avatar_url LIKE '/api/img/%' THEN SUBSTRING(avatar_url, 10)
    -- 如果是完整的HTTP URL，提取相对路径
    WHEN avatar_url LIKE 'http://%' THEN 
        SUBSTRING_INDEX(SUBSTRING_INDEX(avatar_url, '/', -2), '?', 1)
    -- 如果是完整的HTTPS URL，提取相对路径
    WHEN avatar_url LIKE 'https://%' THEN 
        SUBSTRING_INDEX(SUBSTRING_INDEX(avatar_url, '/', -2), '?', 1)
    -- 其他情况保持不变
    ELSE avatar_url
END
WHERE avatar_url IS NOT NULL 
  AND (avatar_url LIKE 'api/img/%' 
       OR avatar_url LIKE '/api/img/%'
       OR avatar_url LIKE 'http://%' 
       OR avatar_url LIKE 'https://%');

-- 2. 清理文件上传记录的URL（去掉 api/img/ 前缀）
UPDATE file_uploads 
SET file_url = CASE
    -- 如果以 api/img/ 开头，去掉这个前缀
    WHEN file_url LIKE 'api/img/%' THEN SUBSTRING(file_url, 9)
    -- 如果以 /api/img/ 开头，去掉这个前缀
    WHEN file_url LIKE '/api/img/%' THEN SUBSTRING(file_url, 10)
    -- 如果是完整的HTTP URL，提取相对路径
    WHEN file_url LIKE 'http://%' THEN 
        SUBSTRING_INDEX(SUBSTRING_INDEX(file_url, '/', -2), '?', 1)
    -- 如果是完整的HTTPS URL，提取相对路径
    WHEN file_url LIKE 'https://%' THEN 
        SUBSTRING_INDEX(SUBSTRING_INDEX(file_url, '/', -2), '?', 1)
    -- 其他情况保持不变
    ELSE file_url
END
WHERE file_url IS NOT NULL 
  AND (file_url LIKE 'api/img/%' 
       OR file_url LIKE '/api/img/%'
       OR file_url LIKE 'http://%' 
       OR file_url LIKE 'https://%');

-- 3. 清理房源图片URL（去掉 api/img/ 前缀）
UPDATE house_images 
SET image_url = CASE
    -- 如果以 api/img/ 开头，去掉这个前缀
    WHEN image_url LIKE 'api/img/%' THEN SUBSTRING(image_url, 9)
    -- 如果以 /api/img/ 开头，去掉这个前缀
    WHEN image_url LIKE '/api/img/%' THEN SUBSTRING(image_url, 10)
    -- 如果是完整的HTTP URL，提取相对路径
    WHEN image_url LIKE 'http://%' THEN 
        SUBSTRING_INDEX(SUBSTRING_INDEX(image_url, '/', -2), '?', 1)
    -- 如果是完整的HTTPS URL，提取相对路径
    WHEN image_url LIKE 'https://%' THEN 
        SUBSTRING_INDEX(SUBSTRING_INDEX(image_url, '/', -2), '?', 1)
    -- 其他情况保持不变
    ELSE image_url
END
WHERE image_url IS NOT NULL 
  AND (image_url LIKE 'api/img/%' 
       OR image_url LIKE '/api/img/%'
       OR image_url LIKE 'http://%' 
       OR image_url LIKE 'https://%');

-- ====================================================================
-- 验证清理结果
-- ====================================================================

-- 检查用户头像URL格式
SELECT 
    '用户头像' AS 数据类型,
    COUNT(*) AS 总数,
    SUM(CASE WHEN avatar_url LIKE 'http%' THEN 1 ELSE 0 END) AS 含HTTP的数量,
    SUM(CASE WHEN avatar_url LIKE '%api/img/%' THEN 1 ELSE 0 END) AS 含api_img的数量,
    SUM(CASE WHEN avatar_url LIKE 'avatars/%' OR avatar_url LIKE 'houserent/%' THEN 1 ELSE 0 END) AS 正确格式数量
FROM users 
WHERE avatar_url IS NOT NULL;

-- 检查文件上传记录URL格式
SELECT 
    '文件上传' AS 数据类型,
    COUNT(*) AS 总数,
    SUM(CASE WHEN file_url LIKE 'http%' THEN 1 ELSE 0 END) AS 含HTTP的数量,
    SUM(CASE WHEN file_url LIKE '%api/img/%' THEN 1 ELSE 0 END) AS 含api_img的数量,
    SUM(CASE WHEN file_url LIKE 'avatars/%' OR file_url LIKE 'houserent/%' THEN 1 ELSE 0 END) AS 正确格式数量
FROM file_uploads 
WHERE file_url IS NOT NULL;

-- 检查房源图片URL格式
SELECT 
    '房源图片' AS 数据类型,
    COUNT(*) AS 总数,
    SUM(CASE WHEN image_url LIKE 'http%' THEN 1 ELSE 0 END) AS 含HTTP的数量,
    SUM(CASE WHEN image_url LIKE '%api/img/%' THEN 1 ELSE 0 END) AS 含api_img的数量,
    SUM(CASE WHEN image_url LIKE 'houses/%' OR image_url LIKE 'houserent/%' THEN 1 ELSE 0 END) AS 正确格式数量
FROM house_images 
WHERE image_url IS NOT NULL;

-- ====================================================================
-- 说明
-- ====================================================================
-- 正确的路径格式示例：
-- - avatars/xxx.jpg
-- - houses/xxx.jpg
-- - houserent/xxx.jpg
--
-- 错误的格式示例（这些都会被清理）：
-- - api/img/avatars/xxx.jpg
-- - /api/img/avatars/xxx.jpg
-- - http://192.168.31.106:9000/avatars/xxx.jpg
-- - http://192.168.31.106:8888/api/img/avatars/xxx.jpg
-- ====================================================================
