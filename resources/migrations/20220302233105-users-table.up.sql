create table if not exists users (
  `id` char(22) not null primary key,
  email varchar(100) not null unique key,
  full_name varchar(100) not null,
  password_hash varchar(100) not null,
  totp_secret varchar(100),
  is_tfa_enabled boolean not null default false
);