DROP VIEW IF EXISTS public_store_view;
CREATE OR REPLACE VIEW public_store_view AS
SELECT
    s.id,
    s.name,
    s.slug,
    s.description,
    s.logo,
    s.is_active,
    u.id as owner_id,
    u.username as owner_name
FROM
    store s
        JOIN users u ON s.owner_id = u.id
WHERE
    s.is_active = true;