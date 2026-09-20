UPDATE campaigns
SET required_steps = (
    SELECT jsonb_agg(step)
    FROM jsonb_array_elements(required_steps) AS step
    WHERE step <> '"ORDER"'
)
WHERE category = 'APP_PROMOTION' AND required_steps @> '["ORDER"]';
