create table if not exists users (
  email varchar(100) not null primary key,
  password_hash varchar(100) not null,
  tfa_secret varchar(100),
  is_tfa_enabled boolean not null default false
);