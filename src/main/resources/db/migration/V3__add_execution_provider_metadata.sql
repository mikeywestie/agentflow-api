ALTER TABLE executions
    ADD COLUMN provider_name VARCHAR(80),
    ADD COLUMN model_name VARCHAR(160);
