-- Ajouter la colonne subdomain à la table store
ALTER TABLE store ADD COLUMN IF NOT EXISTS subdomain VARCHAR(255);

-- Ajouter une contrainte d'unicité
CREATE UNIQUE INDEX IF NOT EXISTS idx_store_subdomain ON store (subdomain);
