CREATE TABLE "records" (
"id" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
"title" varchar,
"note" text,
"file" varchar,
"created_at" datetime NOT NULL,
"updated_at" datetime NOT NULL,
"deleted" boolean DEFAULT 'f',
"uuid" varchar,
"last_synced_at" datetime NOT NULL
);
