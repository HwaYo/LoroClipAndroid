CREATE TABLE "records"
    ("id" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
     "title" varchar,
     "note" text,
     "bookmark" text,
     "file" varchar,
     "created_at" datetime NOT NULL,
     "updated_at" datetime NOT NULL,
     "deleted" boolean DEFAULT 'f',
     "uuid" varchar);

CREATE TABLE "bookmarks"
    ("id" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
     "color" varchar,
     "name" varchar,
     "created_at" datetime NOT NULL,
     "updated_at" datetime NOT NULL,
     "uuid" varchar);