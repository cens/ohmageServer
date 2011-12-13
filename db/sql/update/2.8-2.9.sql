-- Update the R server.
UPDATE preference SET p_value = 'http://rdev1.mobilizingcs.org/R/call/Mobilize/' WHERE p_key = 'visualization_server_address';

-- Update the Mobility table by adding the UUID.
ALTER TABLE mobility ADD COLUMN uuid CHAR(36) NOT NULL;
UPDATE mobility SET uuid=UUID() WHERE uuid='';
ALTER TABLE mobility ADD CONSTRAINT UNIQUE (uuid);

-- Alter the survey response table by droping the timestamp.
ALTER TABLE survey_response DROP COLUMN msg_timestamp;

-- Alter the survey response table by adding the UUID.
ALTER TABLE survey_response ADD COLUMN uuid CHAR(36) NOT NULL;
UPDATE survey_response SET uuid=UUID() WHERE uuid='';
ALTER TABLE survey_response ADD CONSTRAINT UNIQUE (uuid);