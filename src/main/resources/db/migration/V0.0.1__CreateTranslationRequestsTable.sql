CREATE TABLE translation_requests (
    id SERIAL PRIMARY KEY,
    ip_address VARCHAR(255) NOT NULL,
    source_text TEXT NOT NULL,
    translated_text TEXT NOT NULL,
    request_time TIMESTAMP NOT NULL
);