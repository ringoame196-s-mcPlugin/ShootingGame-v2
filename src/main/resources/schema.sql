CREATE TABLE IF NOT EXISTS targets (
world_name TEXT NOT NULL,
    x INTEGER NOT NULL,
    y INTEGER NOT NULL,
    z INTEGER NOT NULL,
    PRIMARY KEY (world_name, x, y, z)
);