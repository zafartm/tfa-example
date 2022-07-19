create table if not exists stripe_events (
  event_id varchar(100) not null primary key,
  event_details json not null,
  time_stamp timestamp not null default current_timestamp(),
  process_timestamp timestamp null default null,
  result enum('processed', 'ignored', 'error') null default null,
  error_trace text null default null,
  event_type varchar(100) generated always as (json_unquote(json_extract(event_details,'$.type'))),
  object_type varchar(100) generated always as (event_details->>'$.data.object.object'),
  object_id varchar(100) generated always as (event_details->>'$.data.object.id'),
  customer_id varchar(100) generated always as (event_details->>'$.data.object.customer'),
  subscription_id varchar(100) generated always as (event_details->>'$.data.object.subscription')
);
