ALTER TABLE messages ADD COLUMN receiver_id BIGINT;

-- Add foreign key constraint for receiver_id
ALTER TABLE messages
    ADD CONSTRAINT FK_messages_receiver FOREIGN KEY (receiver_id) REFERENCES users (id) ON DELETE SET NULL;
