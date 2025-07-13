-- Augmenter la taille des colonnes token et refresh_token dans la table sessions
ALTER TABLE sessions ALTER COLUMN token TYPE VARCHAR(1024);
ALTER TABLE sessions ALTER COLUMN refresh_token TYPE VARCHAR(1024);

-- Augmenter la taille maximale de user_agent
ALTER TABLE sessions ALTER COLUMN user_agent TYPE VARCHAR(512);
