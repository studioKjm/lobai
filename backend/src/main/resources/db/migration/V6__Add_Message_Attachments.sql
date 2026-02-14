-- Add attachment fields to messages table

ALTER TABLE messages
ADD COLUMN attachment_url VARCHAR(500) NULL COMMENT '첨부 파일 URL',
ADD COLUMN attachment_type VARCHAR(50) NULL COMMENT '파일 MIME 타입',
ADD COLUMN attachment_name VARCHAR(255) NULL COMMENT '원본 파일명';

-- Add index for messages with attachments
CREATE INDEX idx_messages_with_attachments ON messages(attachment_url);
