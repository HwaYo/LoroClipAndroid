CREATE TABLE "record" (
"id" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
"title" varchar,
"note" text,
"file" varchar,
"local_file" varchar,
"created_at" datetime NOT NULL,
"updated_at" datetime NOT NULL,
"deleted" boolean DEFAULT 'f',
"uuid" varchar,
"synced_at" datetime,
"dirty" boolean DEFAULT 'f',
);

CREATE TABLE "bookmark" (
"id" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
"color" varchar,
"name" varchar,
"created_at" datetime NOT NULL,
"updated_at" datetime NOT NULL,
"uuid" varchar,
"synced_at" datetime,
"dirty" boolean DEFAULT 'f'
);

CREATE TABLE "bookmark_history" (
"id" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
"start" float,
"end" float,
"record" integer,
"bookmark" integer,
"uuid" varchar,
"synced_at" datetime,
"dirty" boolean DEFAULT 'f'
);

CREATE TABLE "frame_gains" (
"id" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
"frames" varchar,
"record" integer
);